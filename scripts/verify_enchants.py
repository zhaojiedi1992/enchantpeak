#!/usr/bin/env python3
"""Run the enchantment verification against one supported Minecraft target."""

import argparse
import json
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
    family = matrix["targets"][args.minecraft_version]["mc_family"]
    if family not in ("mc2111", "mc26"):
        print(f"{args.minecraft_version}: 附魔物品集与新体系规格不同，跳过 datapack 深度校验，以编译器为校验")
        return 0

    configured_data = os.environ.get("MC_DATA_DIR")
    if configured_data:
        return run_verifier(Path(configured_data).resolve(), family)

    jar = minecraft_jar(args.minecraft_version)
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
