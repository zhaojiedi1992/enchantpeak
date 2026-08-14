#!/bin/bash
# 构建所有支持的 Minecraft 版本
set -e

VERSIONS=("1.18.2" "1.19.2" "1.19.4" "1.20.1" "1.21.1" "1.21.11" "26.1" "26.1.2" "26.2")
OUTPUT_DIR="build/distributions"

echo "=== EnchantPeak 多版本构建脚本 ==="
echo "支持版本: ${VERSIONS[*]}"
echo ""

mkdir -p "$OUTPUT_DIR"

for version in "${VERSIONS[@]}"; do
    echo ">>> 开始构建 MC $version..."
    ./gradlew clean build -Ptarget_mc="$version" --no-daemon

    # 复制 jar 到统一目录
    if [ -f "build/libs/enchantpeak-"*".jar" ]; then
        cp build/libs/enchantpeak-*.jar "$OUTPUT_DIR/"
        echo "✓ MC $version 构建完成"
    else
        echo "✗ MC $version 构建失败"
        exit 1
    fi
    echo ""
done

echo "=== 所有版本构建完成 ==="
ls -lh "$OUTPUT_DIR"
