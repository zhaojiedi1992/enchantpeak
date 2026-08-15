#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

mapfile -t versions < <(python3 -c \
    'import json; print(*json.load(open("versions/minecraft.json"))["targets"], sep="\n")')
mapfile -t neoforge_versions < <(python3 -c \
    'import json; print(*json.load(open("neoforge/targets.json")), sep="\n")')

rm -rf dist
mkdir -p dist

# ===== Fabric：根项目构建 =====
for minecraft_version in "${versions[@]}"; do
    echo "==> Building Minecraft ${minecraft_version} (fabric)"
    ./gradlew clean build test --no-daemon -Ptarget_mc="${minecraft_version}"
    python3 scripts/verify_enchants.py --minecraft-version "${minecraft_version}"

    jar_path=$(find build/libs -maxdepth 1 -name '*.jar' ! -name '*-sources.jar' -print -quit)
    if [ -z "${jar_path}" ]; then
        echo "未找到 Minecraft ${minecraft_version} 的构建产物" >&2
        exit 1
    fi
    cp "${jar_path}" dist/
done

echo "Fabric 全部目标构建并校验通过：${versions[*]}"

# ===== NeoForge：独立 Gradle 构建（Gradle 8.14 需 Java 21 daemon）=====
# 找 Java 21：优先 JAVA_HOME，其次常见安装路径（26.x 编译由 toolchain 调度 Java 25）
NF_JAVA_HOME="${JAVA_HOME:-}"
if [ -z "${NF_JAVA_HOME}" ] || ! "${NF_JAVA_HOME}/bin/java" -version 2>&1 | grep -q 'version "21'; then
    for candidate in /usr/lib/jvm/java-21-openjdk-amd64 /usr/lib/jvm/java-21-openjdk /usr/lib/jvm/temurin-21-jdk-amd64; do
        if [ -x "${candidate}/bin/java" ] && "${candidate}/bin/java" -version 2>&1 | grep -q 'version "21'; then
            NF_JAVA_HOME="${candidate}"
            break
        fi
    done
fi
if [ -z "${NF_JAVA_HOME}" ]; then
    echo "未找到 Java 21（NeoForge Gradle 8.14 daemon 要求），跳过 NeoForge 构建" >&2
    exit 1
fi
export JAVA_HOME="${NF_JAVA_HOME}"

cd neoforge
for neoforge_target in "${neoforge_versions[@]}"; do
    echo "==> Building NeoForge ${neoforge_target}"
    ./gradlew clean build --no-daemon -Ptarget_mc="${neoforge_target}"

    jar_path=$(find build/libs -maxdepth 1 -name '*.jar' ! -name '*-sources.jar' -print -quit)
    if [ -z "${jar_path}" ]; then
        echo "未找到 NeoForge ${neoforge_target} 的构建产物" >&2
        exit 1
    fi
    cp "${jar_path}" ../dist/
done
cd ..

echo "NeoForge 全部目标构建通过：${neoforge_versions[*]}"

# ===== 端到端校验：逐 jar 对照版本矩阵检查元数据 =====
python3 scripts/verify_jars.py

echo "全部完成：Fabric ${#versions[@]} 个 + NeoForge ${#neoforge_versions[@]} 个目标，产物在 dist/"
