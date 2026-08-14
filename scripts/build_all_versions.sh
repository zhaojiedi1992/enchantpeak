#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

mapfile -t versions < <(python3 -c \
    'import json; print(*json.load(open("versions/minecraft.json"))["targets"], sep="\n")')

rm -rf dist
mkdir -p dist

for minecraft_version in "${versions[@]}"; do
    echo "==> Building Minecraft ${minecraft_version}"
    ./gradlew clean build --no-daemon -Ptarget_mc="${minecraft_version}"
    python3 scripts/verify_enchants.py --minecraft-version "${minecraft_version}"

    jar_path=$(find build/libs -maxdepth 1 -name '*.jar' ! -name '*-sources.jar' -print -quit)
    if [ -z "${jar_path}" ]; then
        echo "未找到 Minecraft ${minecraft_version} 的构建产物" >&2
        exit 1
    fi
    cp "${jar_path}" dist/
done

echo "全部目标构建并校验通过：${versions[*]}"
