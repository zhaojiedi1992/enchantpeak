# EnchantPeak — 最强附魔方案速查

> **中文版** · **[English](README.md)**

Minecraft Fabric 模组，在 JEI/REI 中一键查看钻石与下界合金装备的顶配附魔方案。支持中文、英文、**拼音**搜索，快速定位最优配置。

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1%20~%2026.2-brightgreen)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric%200.19.3+-blue)](https://fabricmc.net/)

---

## ✨ 功能特性

- 🔍 **智能搜索** — 支持中文、英文、拼音搜索（如 `zuanshijian` → 钻石剑）
- ⭐ **独立条目** — 每个附魔方案作为独立条目出现在 REI/JEI 中，名称带 `★` 标记
- 📖 **信息面板** — 在任意钻石/下界合金装备上按 `R` 键，在 Information 标签查看所有方案
- 🗂️ **按用途分流** — 有冲突附魔的物品（如时运 vs 精准采集）分为多个独立流派
- ⚔️ **全面覆盖** — 22 种物品（工具、武器、防具、钓鱼竿），36 种附魔方案
- 🌐 **中英双语** — 界面支持中英文，搜索支持两种语言 + 拼音

---

## 📸 游戏截图

![流派方案总览](.github/screenshots/rei-overview.png)

*每个物品的所有附魔流派以独立的附魔物品槽位展示。*

![搜索](.github/screenshots/rei-search.png)

*支持按物品名（钻石镐 / `diamond_pickaxe`）、附魔名（时运 / `Fortune`）、甚至拼音（`zuanshigao`）搜索。*

![悬停详情](.github/screenshots/rei-detail.png)

*鼠标悬停任一槽位，原生渲染完整的附魔列表，无重复文字。*

---

## 📦 下载与安装

**依赖要求：**
- Minecraft **26.1 ~ 26.2**（1.21.x 系列）
- Fabric Loader ≥ 0.19.3
- Fabric API ≥ 0.152.0
- **JEI**（≥ 30.18.0）**或** **REI**（≥ 26.1）— 至少安装一个

**下载地址：**
- [Modrinth](https://modrinth.com/mod/enchantpeak)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/enchantpeak)
- [GitHub Releases](https://github.com/zhaojiedi1992/enchantpeak/releases)

**安装步骤：**
1. 下载最新的 `enchantpeak-x.x.x.jar`
2. 放入 `.minecraft/mods/` 文件夹
3. 确保已安装 Fabric + Fabric API + JEI/REI
4. 启动游戏，打开 JEI/REI 搜索即可看到附魔方案

---

## 🎯 使用方法

### 方式一：直接搜索
在 REI/JEI 搜索框输入：
- 物品名：`diamond pickaxe`、`钻石镐`、`zuanshigao`（拼音）
- 附魔名：`fortune`、`时运`、`shiyun`（拼音）
- 找到带 `★` 前缀的条目即为 EnchantPeak 方案

### 方式二：信息面板（仅 REI）
1. 在 REI 中找到任意钻石或下界合金装备
2. 点击后按 `R` 键（或右键 → Uses）
3. 切换到 **Information** 标签页即可查看该物品的所有附魔方案

### 方式三：自定义分类（JEI/REI）
1. 打开 JEI/REI
2. 导航到 **最强附魔方案** 分类
3. 可视化浏览所有物品及其附魔方案

---

## 🛠️ 覆盖物品与方案

所有附魔等级和互斥关系均对照 Minecraft 官方 datapack（`data/minecraft/enchantment/*.json`）逐项核对。

### 工具（钻石 / 下界合金）

| 物品 | 方案 A（时运） | 方案 B（精准） |
|------|---------------|---------------|
| **镐 / 铲 / 锄** | 效率 V、时运 III、耐久 III、修补 I | 效率 V、精准采集 I、耐久 III、修补 I |
| **斧** | 伐木流：效率 V、时运 III、耐久 III、修补 I | 战斗流：锋利 V、效率 V、耐久 III、修补 I |

### 近战武器

| 物品 | 方案 |
|------|------|
| **剑**（钻石/下界合金）| **锋利 / 亡灵杀手 / 节肢杀手**（damage 组三选一）+ 击退 II、火焰附加 II、抢夺 III、横扫之刃 III、耐久 III、修补 I |
| **重锤**（Mace）| **密度 V / 破甲 IV**（damage 组二选一）+ 火焰附加 II、疾风 III、耐久 III、修补 I |
| **长矛**（钻石/下界合金）| **锋利 / 亡灵杀手 / 节肢杀手** + 突进 III、击退 II、火焰附加 II、抢夺 III、耐久 III、修补 I |

### 远程 / 投掷武器

| 物品 | 方案 |
|------|------|
| **弓** | **无限流**（无限 I）/ **修补流**（修补 I）（bow 组二选一）+ 力量 V、冲击 II、火矢 I、耐久 III |
| **弩** | **穿透流**（穿透 IV）/ **多重流**（多重射击 I）（crossbow 组二选一）+ 快速装填 III、耐久 III、修补 I |
| **三叉戟** | **忠诚流**（忠诚 III + 引雷 I + 穿刺 V）/ **激流流**（激流 III + 穿刺 V）+ 耐久 III、修补 I |

### 防具（钻石 / 下界合金）

每件防具提供 **4 种保护流派**（保护 / 火焰保护 / 爆炸保护 / 弹射物保护 —— armor 组四选一）。

| 物品 | 除选定保护外的通用附魔 |
|------|------------------------|
| **头盔** | 水下呼吸 III、水下速掘 I、荆棘 III、耐久 III、修补 I |
| **胸甲** | 荆棘 III、耐久 III、修补 I |
| **护腿** | 迅捷潜行 III、荆棘 III、耐久 III、修补 I |
| **靴子** | 摔落保护 IV、灵魂疾行 III、荆棘 III、耐久 III、修补 I + **深海探索者 III** 或 **冰霜行者 II**（boots 组二选一）|

### 特殊道具

| 物品 | 方案 |
|------|------|
| **钓鱼竿** | 海之眷顾 III、引饵 III、耐久 III、修补 I |

> **合计：25 种物品，77 个流派方案**（靴子因包含两个独立的互斥组，有 4×2=8 个流派）。

---

## 🔧 从源码构建

```bash
git clone https://github.com/zhaojiedi1992/enchantpeak.git
cd enchantpeak
./gradlew build
```

输出文件：`build/libs/enchantpeak-x.x.x.jar`

**环境要求：** Java 25（或更高版本）

---

## ❓ 常见问题

**Q: 搜索找不到 `★` 条目？**  
A: 确保已进入世界（单人或多人）。REI/JEI 条目在游戏加载注册表后才会注册。

**Q: 只支持 JEI 还是 REI 也支持？**  
A: 两个都支持。REI 功能最完整（搜索条目 + 信息面板 + 自定义分类），JEI 提供自定义分类视图。

**Q: 为什么没有石制/铁制装备？**  
A: 本模组专注于终极装备（钻石和下界合金）。石制和铁制物品附魔价值有限。

**Q: 会添加模组附魔吗？**  
A: 目前仅支持原版附魔。如果有需求，未来版本会考虑模组附魔支持。

**Q: 可以用在整合包里吗？**  
A: 可以！EnchantPeak 使用 MIT 协议。欢迎在任何整合包中自由使用。

---

## 📝 开源协议

[MIT License](LICENSE) © 2024-2026 zhaojiedi1992

欢迎在 [GitHub](https://github.com/zhaojiedi1992/enchantpeak) 提交贡献和问题 🎉
