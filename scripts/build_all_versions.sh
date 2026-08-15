#!/usr/bin/env bash
# 构建所有支持的目标（Fabric 13 + NeoForge 8），产物统一放 dist/，并做元数据校验。
# Fabric 与 NeoForge 是两个完全独立的 Gradle 项目，默认并行跑（约省一半时间）；
# 可用 --fabric-only / --neoforge-only 单跑一条流水线。
set -euo pipefail

cd "$(dirname "$0")/.."

FABRIC_ONLY=0
NEOFORGE_ONLY=0
for arg in "$@"; do
    case "$arg" in
        --fabric-only) FABRIC_ONLY=1 ;;
        --neoforge-only) NEOFORGE_ONLY=1 ;;
        *) echo "用法: $0 [--fabric-only|--neoforge-only]" >&2; exit 2 ;;
    esac
done

mapfile -t versions < <(python3 -c \
    'import json; print(*json.load(open("versions/minecraft.json"))["targets"], sep="\n")')
mapfile -t neoforge_versions < <(python3 -c \
    'import json; print(*json.load(open("neoforge/targets.json")), sep="\n")')

rm -rf dist
mkdir -p dist

# ===== NeoForge 子流水线：独立 Gradle 构建（Gradle 8.14 daemon 需 Java 21）=====
NF_JAVA_HOME=""
build_neoforge() {
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
        echo "未找到 Java 21（NeoForge Gradle 8.14 daemon 要求）" >&2
        return 1
    fi
    export JAVA_HOME="${NF_JAVA_HOME}"

    cd neoforge
    for neoforge_target in "${neoforge_versions[@]}"; do
        echo "==> Building NeoForge ${neoforge_target}"
        ./gradlew clean build --daemon -Ptarget_mc="${neoforge_target}"

        local jar_path
        jar_path=$(find build/libs -maxdepth 1 -name '*.jar' ! -name '*-sources.jar' -print -quit)
        if [ -z "${jar_path}" ]; then
            echo "未找到 NeoForge ${neoforge_target} 的构建产物" >&2
            return 1
        fi
        cp "${jar_path}" ../dist/
    done
    cd ..
    echo "NeoForge 全部目标构建通过：${neoforge_versions[*]}"
}

nf_pid=""
if [ "$NEOFORGE_ONLY" -eq 1 ]; then
    build_neoforge
    python3 scripts/verify_jars.py
    JAVA_HOME="${NF_JAVA_HOME}" neoforge/gradlew --stop >/dev/null 2>&1 || true
    exit 0
fi
if [ "$FABRIC_ONLY" -eq 0 ]; then
    nf_log="$(mktemp /tmp/enchantpeak-neoforge.XXXXXX.log)"
    build_neoforge >"${nf_log}" 2>&1 &
    nf_pid=$!
fi

# ===== Fabric 主流水线（前台）：gradle 的 build 生命周期已包含 :test =====
for minecraft_version in "${versions[@]}"; do
    echo "==> Building Minecraft ${minecraft_version} (fabric)"
    ./gradlew clean build --daemon -Ptarget_mc="${minecraft_version}"
    python3 scripts/verify_enchants.py --minecraft-version "${minecraft_version}"

    jar_path=$(find build/libs -maxdepth 1 -name '*.jar' ! -name '*-sources.jar' -print -quit)
    if [ -z "${jar_path}" ]; then
        echo "未找到 Minecraft ${minecraft_version} 的构建产物" >&2
        exit 1
    fi
    cp "${jar_path}" dist/
done
echo "Fabric 全部目标构建并校验通过：${versions[*]}"

# ===== 等待 NeoForge 子流水线，失败时回放日志 =====
if [ -n "${nf_pid}" ]; then
    echo "（等待 NeoForge 并行构建完成，日志：${nf_log}）"
    if ! wait "${nf_pid}"; then
        echo "✗ NeoForge 构建失败，最后 40 行日志：" >&2
        tail -40 "${nf_log}" >&2
        rm -f "${nf_log}"
        exit 1
    fi
    rm -f "${nf_log}"
fi

# ===== 端到端校验：逐 jar 对照版本矩阵检查元数据 =====
python3 scripts/verify_jars.py

# 释放两条流水线留下的常驻 Gradle daemon（各占 2GB 堆上限）
./gradlew --stop >/dev/null 2>&1 || true
if [ -n "${NF_JAVA_HOME:-}" ]; then
    JAVA_HOME="${NF_JAVA_HOME}" neoforge/gradlew --stop >/dev/null 2>&1 || true
fi

echo "全部完成：Fabric ${#versions[@]} 个 + NeoForge ${#neoforge_versions[@]} 个目标，产物在 dist/"
