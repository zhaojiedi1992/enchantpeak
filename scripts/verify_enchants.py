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


def run_verifier(data_dir):
    env = os.environ.copy()
    env["MC_DATA_DIR"] = str(data_dir)
    return subprocess.run([sys.executable, str(DEEP_VERIFIER)], env=env).returncode


def main():
    default_version, versions = supported_versions()
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--minecraft-version",
        default=os.environ.get("MINECRAFT_VERSION", default_version),
        choices=versions,
    )
    args = parser.parse_args()

    # Enchantments only became data-driven (bundled datapack) in 1.21.
    # Older versions hardcode enchantments in code, where the compiler is
    # the verifier: referencing a nonexistent Enchantments constant fails
    # the build. Skip datapack verification for pre-1.21 targets.
    pre_121 = not (args.minecraft_version.startswith("1.21") or args.minecraft_version.startswith("26."))
    if pre_121:
        print(f"{args.minecraft_version}: 附魔为代码内建（1.21 前非数据驱动），跳过 datapack 校验，以编译器为校验")
        return 0

    configured_data = os.environ.get("MC_DATA_DIR")
    if configured_data:
        return run_verifier(Path(configured_data).resolve())

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
        return run_verifier(data_dir)


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, RuntimeError, zipfile.BadZipFile) as error:
        print(f"校验环境错误：{error}", file=sys.stderr)
        sys.exit(1)
