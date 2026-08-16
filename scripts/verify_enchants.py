#!/usr/bin/env python3
"""Run the enchantment verification against one supported Minecraft target."""

import argparse
import json
import re
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import zipfile


REPO_ROOT = Path(__file__).resolve().parents[1]
VERSION_MATRIX = REPO_ROOT / "versions/minecraft.json"
DEEP_VERIFIER = Path(__file__).with_name("verify_enchants_deep.py")


def supported_versions():
    with VERSION_MATRIX.open(encoding="utf-8") as handle:
        matrix = json.load(handle)
    return matrix["default"], tuple(matrix["targets"])


def minecraft_jar(version):
    gradle_home = Path(os.environ.get("GRADLE_USER_HOME", Path.home() / ".gradle"))
    jar = gradle_home / "caches" / "fabric-loom" / version / "minecraft-client.jar"
    if not jar.is_file():
        raise RuntimeError(
            f"未找到 {jar}；请先运行 ./gradlew build，或通过 MC_DATA_DIR 指定已解压的数据目录"
        )
    return jar


def run_verifier(data_dir, family):
    env = os.environ.copy()
    env["MC_DATA_DIR"] = str(data_dir)
    return subprocess.run([sys.executable, str(DEEP_VERIFIER), "--source", family], env=env).returncode


def _mc1206_pc(s, o):
    """匹配 '(' at o 的 ')' 位置。"""
    d, i = 1, o + 1
    while i < len(s) and d > 0:
        if s[i] == '(': d += 1
        elif s[i] == ')': d -= 1
        i += 1
    return i - 1


def _mc1206_enclosing_method(source, pos):
    """返回 pos 所在的 private void buildXxx 方法体（精确括号平衡）。"""
    best = None
    for m in re.finditer(r'private void build[A-Z][a-zA-Z]+\s*\([^)]*\)\s*\{', source):
        if m.start() <= pos:
            best = m
        else:
            break
    if best is None:
        return None
    open_at = best.end() - 1
    close = _mc1206_pc(source, open_at)
    return source[best.start():close + 1]


def main():
    default_version, versions = supported_versions()
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--minecraft-version",
        default=os.environ.get("MINECRAFT_VERSION", default_version),
        choices=versions,
    )
    args = parser.parse_args()

    # Deep 校验规格（verify_enchants_deep.py 的 ITEMS）覆盖的是"新附魔体系"：
    # 含 spear/lunge/copper 工具（1.21.11 起与 26.x 完全一致）。
    # 更早的目标（<=1.21.1）物品集不同，以编译器为校验（引用不存在的常量即编译失败）。
    with VERSION_MATRIX.open(encoding="utf-8") as handle:
        matrix = json.load(handle)
    target = matrix["targets"][args.minecraft_version]
    family = target["mc_family"]
    build_version = target["minecraft_version"]
    if family in ("mc1206",):
        # 1.20.6：jar 内无附魔定义 JSON（1.20.5 数据驱动化的过渡版本，定义不在分发包里），
        # JVM 测试也无法接线（canEnchant 恒 false）。防回归：对照 specs/enchantment_data_mc1206.json
        # 静态快照（人工审计过的基线），检测 Java 数据是否被意外改动。
        import json as _json
        from pathlib import Path as _Path
        import re as _re
        snapshot = _json.loads(
            (_Path(__file__).resolve().parents[1] / "specs" / "enchantment_data_mc1206.json").read_text())
        java_source = (REPO_ROOT / f"src/{family}/java/com/zhaojiedi1992/enchantpeak/data/EnchantmentData.java").read_text(encoding="utf-8")
        snapshot_errors = []

        def _pc(s, o):
            d, i = 1, o + 1
            while i < len(s) and d > 0:
                if s[i] == '(': d += 1
                elif s[i] == ')': d -= 1
                i += 1
            return i - 1

        # 提取当前 Java 数据
        current = {}
        pos = 0
        while True:
            idx = java_source.find('records.add(new ItemEnchantRecord(', pos)
            if idx == -1: break
            open_at = idx + len('records.add(') - 1
            close = _pc(java_source, open_at)
            call = java_source[open_at + 1:close]
            # 精确限定所在 build 方法体（括号平衡），避免 3000 字符窗口误配
            enclosing = _mc1206_enclosing_method(java_source, idx) or ""
            im = _re.match(r'\s*new\s+ItemEnchantRecord\s*\(\s*Items\.([A-Z0-9_]+)\s*,', call)
            item_ids = []
            if im:
                item_ids = [im.group(1).lower()]
            else:
                vm = _re.match(r'\s*new\s+ItemEnchantRecord\s*\(\s*([a-zA-Z0-9_]+)\s*,', call)
                if vm:
                    lv = vm.group(1)
                    fm = _re.search(r'for\s*\(\s*Item\s+' + lv + r'\s*:\s*(\w+)\s*\)', enclosing)
                    if fm:
                        lvm = _re.search(
                            r'List\s*<\s*Item\s*>\s+' + fm.group(1) + r'\s*=\s*List\.of\(', enclosing)
                        if lvm:
                            lvo = lvm.end() - 1
                            lvc = _pc(enclosing, lvo)
                            item_ids = [x.lower() for x in _re.findall(
                                r'Items\.([A-Z0-9_]+)', enclosing[lvo + 1:lvc])]
            if not item_ids:
                pos = close + 1; continue
            lst = _re.search(r',\s*List\.of\(', call)
            if not lst:
                pos = close + 1; continue
            lo = lst.end() - 1
            lc = _pc(call, lo)
            inner = call[lo + 1:lc]
            groups = []
            for gm in _re.finditer(
                r'new\s+EnchantGroup\s*\(\s*"([a-z0-9_]+)"\s*,\s*List\.of\(', inner):
                gname = gm.group(1)
                go = gm.end() - 1
                gc = _pc(inner, go)
                enchants = [[m.group(1).lower(), int(m.group(2))]
                            for m in _re.finditer(
                                r'e\(\s*Enchantments\.([A-Z0-9_]+)\s*,\s*(\d+)\s*\)',
                                inner[go + 1:gc])]
                groups.append({'name': gname, 'enchantments': enchants})
            if groups:
                for item in item_ids:
                    current[item] = groups
            pos = close + 1

        for item, spec_groups in snapshot.items():
            cur_groups = current.get(item, [])
            if cur_groups != spec_groups:
                snapshot_errors.append(
                    f"{item}: 与静态快照不一致（expected {len(spec_groups)} 组, got {len(cur_groups)} 组）")
        if snapshot_errors:
            print(f"{args.minecraft_version}: mc1206 静态快照校验失败：")
            for e in snapshot_errors[:20]:
                print(f"  ✗ {e}")
            return 1
        print(f"{args.minecraft_version}: 1.20.6 无独立数据源，静态快照校验通过（{len(snapshot)} 个物品）")
        return 0
    if family not in ("mc2111", "mc26") and not family.startswith("mc121"):
        print(f"{args.minecraft_version}: 附魔物品集与新体系规格不同，跳过 datapack 深度校验（1.18.2-1.20.4 由 JVM 深度测试覆盖）")
        return 0

    configured_data = os.environ.get("MC_DATA_DIR")
    if configured_data:
        return run_verifier(Path(configured_data).resolve(), family)

    jar = minecraft_jar(build_version)
    with tempfile.TemporaryDirectory(prefix="enchantpeak-mc-data-") as temp_dir:
        data_dir = Path(temp_dir)
        with zipfile.ZipFile(jar) as archive:
            members = [
                name for name in archive.namelist()
                if name.startswith("data/minecraft/enchantment/")
                or name.startswith("data/minecraft/tags/")
            ]
            archive.extractall(data_dir, members)
        return run_verifier(data_dir, family)


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, RuntimeError, zipfile.BadZipFile) as error:
        print(f"校验环境错误：{error}", file=sys.stderr)
        sys.exit(1)
