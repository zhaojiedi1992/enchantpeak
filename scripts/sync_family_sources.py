#!/usr/bin/env python3
"""Fabric ↔ NeoForge/Forge 家族源码同步工具（Fabric 侧为唯一事实来源）。

各加载器侧共享的家族目录（<side>/targets.json 的 mc_family 指向 src/<family>/ 的
同名目录）里，绝大多数文件应当逐字节一致（JEI 插件代码 loader 无关）。
REI 插件是 Fabric 独有；EnchantmentData/EnchantStacks/JEI 插件各侧共用。
Forge 分两个 Gradle 构建：forge/（FG6，MC 1.20.4-）与 forge7/（FG7，MC 1.20.5+），
各自 targets.json 定义自己的簇，同步逻辑相同。

⚠️ 已知局限：EXTRA_ONLY 只能表达"仅一侧存在的文件"（加载器侧独有的白名单）。
它不能表达"两边都有但内容应当不同"的文件--这种文件 --check 会误报不一致，
同步模式会直接用 Fabric 侧覆盖加载器侧。如果将来出现这种分歧，需要把该文件
整个移出共享范围（如 EnchantPeakMod 的做法：按族拆成多份），而不是试图加白名单。

用法：
  python3 scripts/sync_family_sources.py            # 同步（以 Fabric 侧为源）
  python3 scripts/sync_family_sources.py --check    # 只检查，不一致时非零退出（CI 用）

同步规则：对每个共享族，Fabric 侧文件覆盖加载器侧同名文件；加载器侧多出的文件
（不在 EXTRA_ONLY 里）报告为漂移，Fabric 侧没有对应物的文件同样报告供人工确认。
"""

import argparse
import filecmp
import json
import shutil
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
FABRIC_SRC = REPO_ROOT / "src"

# Fabric 侧独有（REI 插件是 fabric 入口方式、EMI 尚未实现），不参与同步
FABRIC_ONLY_DIRS = ("rei", "emi")


def entry_classes(family: str) -> set:
    """加载器侧独有的 @Mod/入口类（Fabric 侧入口在 src/main，是 ClientModInitializer）"""
    return {f"{family}/com/zhaojiedi1992/enchantpeak/EnchantPeakMod.java"}


# 各加载器侧：源码根、目标矩阵、已知的合法单侧分歧
SIDES = [
    {
        "name": "neoforge",
        "src": REPO_ROOT / "neoforge/src",
        "targets": json.loads((REPO_ROOT / "neoforge/targets.json").read_text()),
        # mc1214 的 JEI：NeoForge 有 1.21.2-1.21.4 适配，JEI 官方未出过这些版本的
        # fabric 构建（maven 无 jei-1.21.4-fabric-api），Fabric 侧无 JEI 代码
        "extra_only": {"mc1214/com/zhaojiedi1992/enchantpeak/jei/JeiEnchantCategory.java",
                       "mc1214/com/zhaojiedi1992/enchantpeak/jei/JeiEnchantPlugin.java"},
    },
    {
        "name": "forge",
        "src": REPO_ROOT / "forge/src",
        "targets": json.loads((REPO_ROOT / "forge/targets.json").read_text()),
        "extra_only": set(),
    },
    {
        "name": "forge7",
        "src": REPO_ROOT / "forge7/src",
        "targets": json.loads((REPO_ROOT / "forge7/targets.json").read_text()),
        "extra_only": set(),
    },
]


def side_families(side):
    fams = []
    for target in side["targets"].values():
        fam = target["mc_family"]
        if fam not in fams and (FABRIC_SRC / fam).is_dir() and (side["src"] / fam).is_dir():
            fams.append(fam)
    return sorted(fams)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true",
                        help="只报告差异，不写文件；有差异时退出码 1")
    args = parser.parse_args()

    differences = []
    synced = 0
    total_families = 0
    for side in SIDES:
        for fam in side_families(side):
            total_families += 1
            allowed_extra = side["extra_only"] | entry_classes(fam)
            fam_root = FABRIC_SRC / fam / "java"
            side_root = side["src"] / fam / "java"
            for src_file in sorted(fam_root.rglob("*.java")):
                if any(d in src_file.parts for d in FABRIC_ONLY_DIRS):
                    continue
                rel = src_file.relative_to(fam_root)
                dst_file = side_root / rel
                if not dst_file.exists():
                    differences.append(f"[{side['name']}] 仅 Fabric 侧存在（未同步）：{fam}/{rel}")
                    continue
                if not filecmp.cmp(src_file, dst_file, shallow=False):
                    if args.check:
                        differences.append(f"[{side['name']}] 内容不一致：{fam}/{rel}")
                    else:
                        shutil.copyfile(src_file, dst_file)
                        synced += 1
            # 反向检查：加载器侧不该有 Fabric 侧没有的家族文件（入口类等已知分歧除外）
            for side_file in sorted(side_root.rglob("*.java")):
                rel = side_file.relative_to(side_root)
                if not (fam_root / rel).exists() and f"{fam}/{rel}" not in allowed_extra:
                    differences.append(f"[{side['name']}] 仅 {side['name']} 侧存在（人工确认）：{fam}/{rel}")

    if differences:
        print("✗ 家族源码存在漂移：")
        for d in differences:
            print(f"  - {d}")
        if args.check:
            print("\n运行 python3 scripts/sync_family_sources.py 同步（Fabric 侧为源）")
            return 1
    mode = "同步完成" if not args.check else "一致"
    print(f"✓ {len(SIDES)} 侧共 {total_families} 个共享家族源码{mode}（本次复制 {synced} 个文件）")
    return 0


if __name__ == "__main__":
    sys.exit(main())
