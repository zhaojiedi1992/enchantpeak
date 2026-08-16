#!/usr/bin/env python3
"""Fabric ↔ NeoForge 家族源码同步工具。

两边共享的家族目录（neoforge/targets.json 的 mc_family 指向 src/<family>/ 的
同名目录）里，绝大多数文件应当逐字节一致（JEI 插件代码 loader 无关）。
REI 插件是 Fabric 独有；EnchantmentData/EnchantStacks/JEI 插件两边共用。

⚠️ 已知局限：KNOWN_DIVERGENCES 只能表达"仅一侧存在的文件"（NeoForge 独有
路径的白名单）。它不能表达"两边都有但内容应当不同"的文件——这种文件
--check 会误报不一致，同步模式会直接用 Fabric 侧覆盖 NeoForge 侧。如果
将来出现这种分歧，需要把该文件整个移出共享范围（如 EnchantPeakMod 的
做法：按族拆成多份），而不是试图加白名单。

用法：
  python3 scripts/sync_family_sources.py            # 同步（以 Fabric 侧为源）
  python3 scripts/sync_family_sources.py --check    # 只检查，不一致时非零退出（CI 用）

同步规则：对每个共享族，Fabric 侧文件覆盖 NeoForge 侧同名文件；NeoForge 侧
多出的文件（不在 KNOWN_DIVERGENCES 里）报告为漂移，Fabric 侧没有对应物的
文件同样报告供人工确认。
"""

import argparse
import filecmp
import json
import shutil
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
FABRIC_SRC = REPO_ROOT / "src"
NEOFORGE_SRC = REPO_ROOT / "neoforge/src"

# Fabric 侧独有（REI 插件、fabric loader 入口），不参与同步
FABRIC_ONLY_DIR = "rei"

# 已知的合法双侧分歧（loader 生态差异，不是漂移）：
# - mc1214 的 JEI：NeoForge 有 1.21.2-1.21.4 适配，JEI 官方未出过这些版本的
#   fabric 构建（maven 无 jei-1.21.4-fabric-api），Fabric 侧无 JEI 代码
# - 各 NeoForge 族的 EnchantPeakMod 入口：Fabric 侧入口在 src/main
#   （ClientModInitializer），NeoForge 侧需要族内 @Mod(dist=CLIENT)（1.20.4
#   的老 FML 不支持 dist，该族是无 dist 的副本）
KNOWN_DIVERGENCES = {
    "mc1214/com/zhaojiedi1992/enchantpeak/jei/JeiEnchantCategory.java",
    "mc1214/com/zhaojiedi1992/enchantpeak/jei/JeiEnchantPlugin.java",
    "mc1204/com/zhaojiedi1992/enchantpeak/EnchantPeakMod.java",
    "mc1206/com/zhaojiedi1992/enchantpeak/EnchantPeakMod.java",
    "mc121/com/zhaojiedi1992/enchantpeak/EnchantPeakMod.java",
    "mc1214/com/zhaojiedi1992/enchantpeak/EnchantPeakMod.java",
    "mc1216/com/zhaojiedi1992/enchantpeak/EnchantPeakMod.java",
    "mc2111/com/zhaojiedi1992/enchantpeak/EnchantPeakMod.java",
    "mc26/com/zhaojiedi1992/enchantpeak/EnchantPeakMod.java",
}

NEOFORGE_TARGETS = json.loads((REPO_ROOT / "neoforge/targets.json").read_text())


def shared_families():
    fams = []
    for target in NEOFORGE_TARGETS.values():
        fam = target["mc_family"]
        if fam not in fams and (FABRIC_SRC / fam).is_dir() and (NEOFORGE_SRC / fam).is_dir():
            fams.append(fam)
    return sorted(fams)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true",
                        help="只报告差异，不写文件；有差异时退出码 1")
    args = parser.parse_args()

    differences = []
    synced = 0
    for fam in shared_families():
        fam_root = FABRIC_SRC / fam / "java"
        nf_root = NEOFORGE_SRC / fam / "java"
        for src_file in sorted(fam_root.rglob("*.java")):
            if FABRIC_ONLY_DIR in src_file.parts:
                continue
            rel = src_file.relative_to(fam_root)
            dst_file = nf_root / rel
            if not dst_file.exists():
                differences.append(f"仅 Fabric 侧存在（未同步）：{fam}/{rel}")
                continue
            if not filecmp.cmp(src_file, dst_file, shallow=False):
                if args.check:
                    differences.append(f"内容不一致：{fam}/{rel}")
                else:
                    shutil.copyfile(src_file, dst_file)
                    synced += 1
        # 反向检查：NeoForge 侧不该有 Fabric 侧没有的家族文件（已知分歧除外）
        for nf_file in sorted(nf_root.rglob("*.java")):
            if FABRIC_ONLY_DIR in nf_file.parts:
                continue
            rel = nf_file.relative_to(nf_root)
            if not (fam_root / rel).exists() and f"{fam}/{rel}" not in KNOWN_DIVERGENCES:
                differences.append(f"仅 NeoForge 侧存在（人工确认）：{fam}/{rel}")

    if differences:
        print("✗ 家族源码存在漂移：")
        for d in differences:
            print(f"  - {d}")
        if args.check:
            print("\n运行 python3 scripts/sync_family_sources.py 同步（Fabric 侧为源）")
            return 1
    print(f"✓ {len(shared_families())} 个共享家族源码同步完成（本次复制 {synced} 个文件）"
          if not args.check else f"✓ {len(shared_families())} 个共享家族源码一致")
    return 0


if __name__ == "__main__":
    sys.exit(main())
