# EnchantPeak — 最强附魔方案速查

> **中文版** · **[English](README.md)**

Minecraft 模组，在 JEI/REI 中覆盖全部原版可附魔物品：有正向附魔的展示全部极大兼容顶配方案，仅支持诅咒的明确标识为空方案。REI 还会把每个方案注册为可搜索的独立条目。

> 💡 **推荐组合：Fabric + REI** — 独立可搜索方案条目体验最佳。同时也为所有可适配的版本簇提供 NeoForge 构建（JEI 集成）。

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.18.2~26.2-brightgreen)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-blue)](https://fabricmc.net/)
[![NeoForge](https://img.shields.io/badge/Loader-NeoForge-orange)](https://neoforged.net/)

---

## ✨ 功能特性

- 🔍 **方案搜索（REI）** — 可按当前游戏语言的物品名或附魔名搜索独立方案条目
- ⭐ **独立条目（REI）** — 每个附魔方案作为可搜索的独立条目，名称带流派后缀（如 `钻石镐-时运流`）
- 📖 **信息面板** — 在任意受支持物品上按 `R` 键，在 Information 标签查看所有方案
- 🗂️ **按用途分流** — 有冲突附魔的物品（如时运 vs 精准采集）分为多个独立流派
- ⚔️ **全面覆盖** — 92 种原版可附魔物品全部覆盖：83 种物品共 288 个极大兼容方案，另含 9 种纯诅咒物品
- 🌐 **多语言** — 界面支持英文、简体中文、日文、韩文、德文、法文、西班牙文

---

## 🎬 视频介绍

[▶ 前往 B 站观看 EnchantPeak 介绍视频](https://www.bilibili.com/video/BV1ChgW6vELA/)

---

## 📸 游戏截图

![流派方案总览](https://raw.githubusercontent.com/zhaojiedi1992/enchantpeak/main/.github/screenshots/rei-overview.png)

*每个物品的所有附魔流派以独立的附魔物品槽位展示。*

![搜索](https://raw.githubusercontent.com/zhaojiedi1992/enchantpeak/main/.github/screenshots/rei-search.png)

*REI 会索引每个方案的本地化物品名和原生附魔提示。*

![悬停详情](https://raw.githubusercontent.com/zhaojiedi1992/enchantpeak/main/.github/screenshots/rei-detail.png)

*鼠标悬停任一槽位，原生渲染完整的附魔列表，无重复文字。*

---

## 📦 下载与安装

每个 JAR 覆盖一个 API 兼容簇（文件名中的 `mc` 范围即所含版本，如 `mc1.19.2-1.19.4`）。选择包含你游戏版本的那一行即可。

**Fabric（推荐 — 含 REI 独立搜索条目）：**

| JAR `mc` 范围 | 覆盖版本 |
|---|---|
| `mc1.18.2` | 1.18.2 |
| `mc1.19.2-1.19.4` | 1.19.2 / 1.19.3 / 1.19.4 |
| `mc1.20.1-1.20.2` | 1.20.1 / 1.20.2 |
| `mc1.20.3-1.20.4` | 1.20.3 / 1.20.4 |
| `mc1.20.5-1.20.6` | 1.20.5 / 1.20.6 |
| `mc1.21-1.21.1` | 1.21 / 1.21.1 |
| `mc1.21.2-1.21.4` | 1.21.2 / 1.21.3 / 1.21.4 |
| `mc1.21.5-1.21.8` | 1.21.5 / 1.21.6 / 1.21.7 / 1.21.8 |
| `mc1.21.9-1.21.11` | 1.21.9 / 1.21.10 / 1.21.11 |
| `mc26.1` / `mc26.1.1` / `mc26.1.2` / `mc26.2` | 26.x 各版本 |

**NeoForge（JEI 集成）：**

| JAR `mc` 范围 | 覆盖版本 |
|---|---|
| `mc1.20.3-1.20.4` | 1.20.3 / 1.20.4 |
| `mc1.20.5-1.20.6` | 1.20.5 / 1.20.6 |
| `mc1.21-1.21.1` | 1.21 / 1.21.1 |
| `mc1.21.2-1.21.4` | 1.21.2 / 1.21.3 / 1.21.4 |
| `mc1.21.5-1.21.8` | 1.21.5 / 1.21.6 / 1.21.7 / 1.21.8 |
| `mc1.21.9-1.21.11` | 1.21.9 / 1.21.10 / 1.21.11 |
| `mc26.1` / `mc26.2` | 26.x |

> NeoForge 构建从 1.20.3 起（NeoForge 无 1.20.1 及更早的稳定版，请用 Fabric）。REI 独立搜索条目仅限 Fabric；NeoForge 请使用 JEI 的 **最强附魔方案** 分类。

**依赖要求：**
- **Fabric**：Fabric Loader + Fabric API，以及 **REI**（推荐）或 **JEI**
- **NeoForge**：对应版本 NeoForge + **JEI**

**下载地址：**
- [Modrinth](https://modrinth.com/mod/enchantpeak)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/enchantpeak)
- [GitHub Releases](https://github.com/zhaojiedi1992/enchantpeak/releases)

**安装步骤：**
1. 下载与你 loader 和游戏版本匹配的 `enchantpeak-fabric-...+mc<范围>.jar` 或 `enchantpeak-neoforge-...+mc<范围>.jar`
2. 放入 `.minecraft/mods/` 文件夹
3. 确保已安装对应 loader 的依赖（Fabric + Fabric API + REI/JEI，或 NeoForge + JEI）
4. 启动游戏，在 JEI/REI 中打开 **最强附魔方案** 分类；REI（Fabric）还可直接搜索每个方案条目

---

## 🎯 使用方法

### 方式一：直接搜索（仅 REI）
在 REI 搜索框输入：
- 当前游戏语言的物品名，例如 `diamond pickaxe` 或 `钻石镐`
- 当前游戏语言的附魔名，例如 `fortune` 或 `时运`
- 找到名称带流派后缀（如 `钻石镐-时运流`）的条目即为 EnchantPeak 方案

### 方式二：信息页（JEI/REI）
1. 在 JEI/REI 中找到任意受支持的可附魔物品
2. 点击后按 `R` 键（或右键 → Uses）
3. 打开 **Information** 页面，查看每个方案的本地化详情

### 方式三：自定义分类（JEI/REI）
1. 打开 JEI/REI
2. 导航到 **最强附魔方案** 分类
3. 可视化浏览所有物品及其附魔方案

---

## 🛠️ 覆盖物品与方案

所有附魔等级和互斥关系均对照 Minecraft 官方 datapack（`data/minecraft/enchantment/*.json`）逐项核对。

### 工具（木 / 石 / 铜 / 铁 / 金 / 钻石 / 下界合金）

| 物品 | 方案 A（时运） | 方案 B（精准） |
|------|---------------|---------------|
| **镐 / 铲 / 锄** | 效率 V、时运 III、耐久 III、修补 I | 效率 V、精准采集 I、耐久 III、修补 I |
| **斧** | 伐木流：效率 V、时运 III、耐久 III、修补 I | 战斗流：锋利 V、效率 V、耐久 III、修补 I |

### 近战武器

| 物品 | 方案 |
|------|------|
| **剑**（全部材质）| **锋利 / 亡灵杀手 / 节肢杀手**（damage 组三选一）+ 击退 II、火焰附加 II、抢夺 III、横扫之刃 III、耐久 III、修补 I |
| **重锤**（Mace）| **密度 V / 破甲 IV / 亡灵杀手 V / 节肢杀手 V**（damage 组四选一）+ 火焰附加 II、疾风 III、耐久 III、修补 I |
| **长矛**（全部材质）| **锋利 / 亡灵杀手 / 节肢杀手** + 突进 III、击退 II、火焰附加 II、抢夺 III、耐久 III、修补 I |

### 远程 / 投掷武器

| 物品 | 方案 |
|------|------|
| **弓** | **无限流**（无限 I）/ **修补流**（修补 I）（bow 组二选一）+ 力量 V、冲击 II、火矢 I、耐久 III |
| **弩** | **穿透流**（穿透 IV）/ **多重流**（多重射击 I）（crossbow 组二选一）+ 快速装填 III、耐久 III、修补 I |
| **三叉戟** | **忠诚流**（忠诚 III + 引雷 I + 穿刺 V）/ **激流流**（激流 III + 穿刺 V）+ 耐久 III、修补 I |

### 防具（皮革 / 锁链 / 铜 / 铁 / 金 / 钻石 / 下界合金）

每件防具提供 **4 种保护流派**（保护 / 火焰保护 / 爆炸保护 / 弹射物保护 —— armor 组四选一）。

| 物品 | 除选定保护外的通用附魔 |
|------|------------------------|
| **头盔**（含海龟壳）| 水下呼吸 III、水下速掘 I、荆棘 III、耐久 III、修补 I |
| **胸甲** | 荆棘 III、耐久 III、修补 I |
| **护腿** | 迅捷潜行 III、荆棘 III、耐久 III、修补 I |
| **靴子** | 摔落保护 IV、灵魂疾行 III、荆棘 III、耐久 III、修补 I + **深海探索者 III** 或 **冰霜行者 II**（boots 组二选一）|

### 特殊与实用道具

| 物品 | 方案 |
|------|------|
| **钓鱼竿** | 海之眷顾 III、引饵 III、耐久 III、修补 I |
| **鞘翅** | 耐久 III、修补 I（鞘翅不在防具 tag 里，无法附保护/荆棘）|
| **盾** | 耐久 III、修补 I |
| **剪刀** | 效率 V、耐久 III、修补 I |
| **刷子 / 打火石 / 胡萝卜钓竿 / 诡异菌钓竿** | 耐久 III、修补 I |
| **雕刻南瓜 / 指南针 / 生物与玩家头颅** | 无非诅咒附魔；不推荐附加诅咒 |

> **合计：92/92 种可附魔物品。**83 种物品有 288 个正向顶配方案，覆盖 41/41 个非诅咒附魔；9 种纯诅咒物品提供明确的空推荐方案。

---

## 🔧 从源码构建

```bash
git clone https://github.com/zhaojiedi1992/enchantpeak.git
cd enchantpeak
./gradlew build
```

默认构建 26.2，输出文件：`build/libs/enchantpeak-mc26.2-x.x.x.jar`。

构建单个目标或一次构建全部目标：

```bash
./gradlew clean build -Ptarget_mc=26.1.2
scripts/build_all_versions.sh
```

全部目标产物位于 `dist/`。受支持目标及其依赖版本统一维护在 `versions/minecraft.json`。

**环境要求：** Java 25（或更高版本）

---

## ❓ 常见问题

**Q: 搜索找不到附魔方案条目？**

A: 独立方案条目仅在 REI 中提供。请确保已进入世界以加载注册表；JEI 用户请打开 **最强附魔方案** 分类查看。

**Q: 只支持 JEI 还是 REI 也支持？**

A: 两者都支持自定义分类、从原物品查看方案、本地化提示和信息页。REI 额外把每个方案注册为全局搜索中的独立条目。

**Q: 是否遗漏了可附魔的原版物品？**

A: 所有至少支持一种非诅咒附魔的物品均已覆盖。雕刻南瓜、指南针和七种生物/玩家头颅只支持诅咒，因此不会生成正向顶配方案。

**Q: 是否支持任意 26.x 版本？**

A: 支持所有当前正式发布并通过构建与官方数据校验的 26.x 版本：26.1、26.1.1、26.1.2、26.2。未来的 26.x 正式版需要加入构建矩阵并校验通过后才会声明支持，不会用宽泛版本范围冒充兼容。

**Q: 会添加模组附魔吗？**

A: 目前仅支持原版附魔。如果有需求，未来版本会考虑模组附魔支持。

**Q: 可以用在整合包里吗？**

A: 可以！EnchantPeak 使用 MIT 协议。欢迎在任何整合包中自由使用。

---

## 📝 开源协议

[MIT License](LICENSE) © 2024-2026 zhaojiedi1992

欢迎在 [GitHub](https://github.com/zhaojiedi1992/enchantpeak) 提交贡献和问题 🎉
