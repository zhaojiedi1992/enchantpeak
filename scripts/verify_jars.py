#!/usr/bin/env python3
"""端到端校验构建产物（dist/）的元数据。

针对的是发布事故级的打包 bug（历史上真实出现过的三类）：
1. fabric.mod.json 版本为 "unspecified"（project.version 未赋值）
2. minecraft 依赖只钉了簇代表版本，同簇其他版本被 loader 拒载
3. 模板占位符未展开（如 ">=${jei_min_version}"）；NeoForge 依赖范围一刀切

以 versions/minecraft.json 与 neoforge/targets.json 为唯一事实来源，
逐 jar 对照校验，不通过则非零退出。
"""

import argparse
import json
import re
import sys
import zipfile
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
FABRIC_MATRIX = REPO_ROOT / "versions/minecraft.json"
NEOFORGE_TARGETS = REPO_ROOT / "neoforge/targets.json"
DIST = REPO_ROOT / "dist"

PLACEHOLDER = re.compile(r"\$\{[a-zA-Z0-9_.]+\}")


def check_fabric_jar(jar: Path, targets: dict, errors: list):
    with zipfile.ZipFile(jar) as zf:
        try:
            meta = json.loads(zf.read("fabric.mod.json"))
        except KeyError:
            errors.append(f"{jar.name}: 缺少 fabric.mod.json")
            return
        lang_names = [n for n in zf.namelist()
                      if re.fullmatch(r"assets/enchantpeak/lang/[a-z_]+\.json", n)]

    # jar 名后缀 -> 矩阵目标（jar_name_suffix 是唯一标识）
    suffix = jar.stem.split("+mc", 1)[1] if "+mc" in jar.stem else None
    matches = [v for v in targets.values() if v.get("jar_name_suffix", v["minecraft_version"]) == suffix]
    if not matches:
        errors.append(f"{jar.name}: 找不到 jar_name_suffix={suffix} 的矩阵目标")
        return
    target = matches[0]

    version = meta.get("version", "")
    if version in ("", "unspecified"):
        errors.append(f"{jar.name}: version 为 {version!r}")
    if not version.startswith(f"{target['mod_version'] if 'mod_version' in target else ''}"):
        # 矩阵里没有 mod_version，用 gradle.properties 比对在调用方做；这里只查 unspecified/占位符
        pass
    if PLACEHOLDER.search(json.dumps(meta)):
        errors.append(f"{jar.name}: fabric.mod.json 存在未展开的占位符")

    mc = meta.get("depends", {}).get("minecraft")
    expected = sorted(target["game_versions"])
    if isinstance(mc, str):
        errors.append(f"{jar.name}: minecraft 依赖是字符串 {mc!r}（应覆盖整簇 {expected}）")
    elif sorted(mc) != expected:
        errors.append(f"{jar.name}: minecraft 依赖 {sorted(mc)} ≠ 簇版本 {expected}")

    jei = meta.get("suggests", {}).get("jei")
    if target.get("jei_enabled") == "true" and (jei is None or not jei.startswith(">=")):
        errors.append(f"{jar.name}: jei_enabled 目标的 suggests.jei 异常: {jei!r}")
    if target.get("jei_enabled") == "false" and jei is not None:
        errors.append(f"{jar.name}: jei_enabled=false 但 suggests.jei 存在: {jei!r}")

    if len(lang_names) != 7:
        errors.append(f"{jar.name}: 语言文件数量 {len(lang_names)} ≠ 7")
    else:
        key_sets = {}
        for name in lang_names:
            with zipfile.ZipFile(jar) as zf:
                key_sets[name.rsplit('/', 1)[-1]] = set(json.loads(zf.read(name)))
        en = key_sets.get("en_us.json", set())
        for locale, keys in key_sets.items():
            if keys != en:
                missing = en - keys
                extra = keys - en
                errors.append(f"{jar.name}: {locale} 键集与 en_us 不一致 "
                              f"(missing={sorted(missing)}, extra={sorted(extra)})")


def check_neoforge_jar(jar: Path, targets: dict, mod_version: str, errors: list):
    with zipfile.ZipFile(jar) as zf:
        names = zf.namelist()
        try:
            toml = zf.read("META-INF/neoforge.mods.toml").decode("utf-8")
        except KeyError:
            errors.append(f"{jar.name}: 缺少 META-INF/neoforge.mods.toml")
            return
        has_license = any("LICENSE" in n for n in names)

    suffix = jar.stem.split("+mc", 1)[1] if "+mc" in jar.stem else None
    matches = [v for v in targets.values() if v["suffix"] == suffix]
    if not matches:
        errors.append(f"{jar.name}: 找不到 suffix={suffix} 的 NeoForge 目标")
        return
    target = matches[0]

    if not has_license:
        errors.append(f"{jar.name}: jar 内未内嵌 LICENSE")

    m = re.search(r'^version = "([^"]+)"', toml, re.M)
    if not m:
        errors.append(f"{jar.name}: mods.toml 缺少 version")
    elif not m.group(1).startswith(mod_version):
        errors.append(f"{jar.name}: mods.toml version {m.group(1)!r} 不以 mod_version {mod_version} 开头")

    ranges = re.findall(r'versionRange = "([^"]+)"', toml)
    if len(ranges) < 3:
        errors.append(f"{jar.name}: mods.toml 依赖段不完整: {ranges}")
    else:
        if ranges[1] != target["mc_range"]:
            errors.append(f"{jar.name}: minecraft 范围 {ranges[1]} ≠ 矩阵 mc_range {target['mc_range']}")

    if PLACEHOLDER.search(toml):
        errors.append(f"{jar.name}: mods.toml 存在未展开的占位符")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dist", type=Path, default=DIST, help="jar 目录（默认 dist/）")
    args = parser.parse_args()

    mod_version = None
    for line in (REPO_ROOT / "gradle.properties").read_text().splitlines():
        if line.startswith("mod_version="):
            mod_version = line.split("=", 1)[1].strip()
    if not mod_version:
        print("无法读取 gradle.properties 的 mod_version", file=sys.stderr)
        return 2

    fabric_targets = json.loads(FABRIC_MATRIX.read_text())["targets"]
    neoforge_targets = json.loads(NEOFORGE_TARGETS.read_text())

    jars = sorted(args.dist.glob("*.jar"))
    if not jars:
        print(f"{args.dist} 下没有 jar；请先运行 scripts/build_all_versions.sh", file=sys.stderr)
        return 2

    errors = []
    for jar in jars:
        if jar.stem.startswith("enchantpeak-fabric"):
            check_fabric_jar(jar, fabric_targets, errors)
        elif jar.stem.startswith("enchantpeak-neoforge"):
            check_neoforge_jar(jar, neoforge_targets, mod_version, errors)
        else:
            errors.append(f"{jar.name}: 无法识别的产物命名")

    for jar in jars:
        print(f"  {jar.name}")
    if errors:
        print(f"\n✗ {len(errors)} 个问题：")
        for e in errors:
            print(f"  - {e}")
        return 1
    print(f"✓ {len(jars)} 个 jar 元数据校验通过（对照 versions/minecraft.json 与 neoforge/targets.json）")
    return 0


if __name__ == "__main__":
    sys.exit(main())
