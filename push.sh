#!/usr/bin/env bash
# EnchantPeak 一键发布脚本
# 用法: ./push.sh
# 行为: 自动 patch 自增版本号 (1.0.2 -> 1.0.3)，更新 CHANGELOG.md，提交 + 打 tag + push
# push tag 后 GitHub Actions 会自动构建并发布到 Modrinth / CurseForge / GitHub Releases
set -euo pipefail

cd "$(dirname "$0")"

# 发布必须从完全干净的 main 分支开始，避免漏提交或混入无关暂存内容。
if [ "$(git branch --show-current)" != "main" ]; then
    echo "✗ 只能从 main 分支发布" >&2
    exit 1
fi

if [ -n "$(git status --porcelain)" ]; then
    echo "✗ 工作区或暂存区存在未提交改动，请先提交或清理后再发布：" >&2
    git status --short >&2
    exit 1
fi

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

if git rev-parse -q --verify "refs/tags/v${NEW_VERSION}" >/dev/null; then
    echo "✗ tag v${NEW_VERSION} 已存在" >&2
    exit 1
fi

echo "=========================================="
echo "  EnchantPeak 发布"
echo "  ${CURRENT_VERSION}  →  ${NEW_VERSION}"
echo "=========================================="

# ---- 发布前验证 ----
echo "正在构建并校验所有受支持的 Minecraft 版本..."
scripts/build_all_versions.sh
echo "✓ 所有 Minecraft 目标构建和校验通过"

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
# 只提交版本相关文件；发布前的干净工作区检查保证不会夹带其他改动。
git add -- gradle.properties CHANGELOG.md
git commit --only -m "update to ${NEW_VERSION}" -- gradle.properties CHANGELOG.md >/dev/null 2>&1 || {
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
