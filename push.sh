#!/usr/bin/env bash
# EnchantPeak 一键发布脚本
# 用法: ./push.sh
# 行为: 自动 patch 自增版本号 (1.0.2 -> 1.0.3)，更新 CHANGELOG.md，提交 + 打 tag + push
# push tag 后 GitHub Actions 会自动构建并发布到 Modrinth / CurseForge / GitHub Releases
set -euo pipefail

cd "$(dirname "$0")"

# ---- 读取当前版本号 ----
CURRENT_VERSION=$(grep '^mod_version=' gradle.properties | cut -d'=' -f2 | tr -d ' \r\n')
if [ -z "$CURRENT_VERSION" ]; then
    echo "✗ 无法从 gradle.properties 读取 mod_version" >&2
    exit 1
fi

# ---- 自增 patch 版本 ----
MAJOR=$(echo "$CURRENT_VERSION" | cut -d. -f1)
MINOR=$(echo "$CURRENT_VERSION" | cut -d. -f2)
PATCH=$(echo "$CURRENT_VERSION" | cut -d. -f3)
PATCH=$((PATCH + 1))
NEW_VERSION="${MAJOR}.${MINOR}.${PATCH}"

echo "=========================================="
echo "  EnchantPeak 发布"
echo "  ${CURRENT_VERSION}  →  ${NEW_VERSION}"
echo "=========================================="

# ---- 确认未提交变更（如果有自定义改动，先一起带上）----
if [ -n "$(git status --porcelain)" ]; then
    echo "⚠ 检测到工作区有未提交的改动："
    git status --short
    echo ""
fi

# ---- 更新 gradle.properties ----
sed -i "s/^mod_version=.*/mod_version=${NEW_VERSION}/" gradle.properties
echo "✓ gradle.properties: ${NEW_VERSION}"

# ---- 更新 CHANGELOG.md ----
TODAY=$(date +%Y-%m-%d)
if [ -f CHANGELOG.md ] && head -1 CHANGELOG.md | grep -q '^# Changelog'; then
    # 在标题行后插入新版本节
    TEMP=$(mktemp)
    {
        echo "# Changelog"
        echo ""
        echo "## ${NEW_VERSION}"
        echo ""
        echo "Released on ${TODAY}."
        echo ""
        # 保留旧内容（去掉原来的第一行标题）
        tail -n +2 CHANGELOG.md
    } > "$TEMP"
    mv "$TEMP" CHANGELOG.md
    echo "✓ CHANGELOG.md: 新增 ${NEW_VERSION} 节"
else
    echo "# Changelog" > CHANGELOG.md
    echo "" >> CHANGELOG.md
    echo "## ${NEW_VERSION}" >> CHANGELOG.md
    echo "" >> CHANGELOG.md
    echo "Released on ${TODAY}." >> CHANGELOG.md
    echo "" >> CHANGELOG.md
    echo "✓ CHANGELOG.md: 初始化"
fi

# ---- 提交 + 打 tag + push ----
git add gradle.properties CHANGELOG.md
# 同时把工作区其他已存在的改动也带上（避免遗漏）
git add -A
git commit -m "update to ${NEW_VERSION}" >/dev/null 2>&1 || {
    echo "✗ git commit 失败" >&2
    exit 1
}
echo "✓ git commit: update to ${NEW_VERSION}"

git tag "v${NEW_VERSION}"
echo "✓ git tag: v${NEW_VERSION}"

git push origin main >/dev/null 2>&1 || git push >/dev/null 2>&1
git push origin "v${NEW_VERSION}" >/dev/null 2>&1
echo "✓ git push: main + v${NEW_VERSION}"

echo ""
echo "=========================================="
echo "  ✅ 发布完成: v${NEW_VERSION}"
echo "=========================================="
echo ""
echo "CI 正在自动构建，稍后 1-2 分钟会发布到："
echo "  • GitHub:  https://github.com/zhaojiedi1992/enchantpeak/releases/tag/v${NEW_VERSION}"
echo "  • Modrinth:  https://modrinth.com/mod/enchantpeak"
echo "  • CurseForge: https://www.curseforge.com/minecraft/mc-mods/enchantpeak/files"
echo ""
echo "查看 CI 进度: gh run watch"
