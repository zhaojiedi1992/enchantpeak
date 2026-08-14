# EnchantPeak

> **[中文版](README.zh_CN.md)** · **English**

A Minecraft Fabric mod that shows **best enchantment combinations** for diamond and netherite items directly in JEI/REI. Search by item name (English or Chinese) or enchantment name (including **pinyin** support) to quickly find optimal builds.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1%20~%2026.2-brightgreen)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric%200.19.3+-blue)](https://fabricmc.net/)

---

## ✨ Features

- 🔍 **Smart Search** — Find items and builds by typing English names, Chinese names, or pinyin (e.g., `zuanshijian` → Diamond Sword)
- ⭐ **Dedicated Entries** — Each enchantment build appears as a separate entry in REI/JEI with a `★` prefix
- 📖 **Information Tab** — Press `R` on any diamond/netherite item to see all builds in the REI Information panel
- 🗂️ **Build-Based Grouping** — Items with conflicting enchantments (e.g., Fortune vs Silk Touch) are split into separate builds
- ⚔️ **Comprehensive Coverage** — 28 items (tools, weapons, armor, elytra, shield, fishing rod, shears) with 90 builds; all 41 non-curse vanilla enchantments included
- 🌐 **Bilingual** — Supports English and Simplified Chinese UI; search works in both languages plus pinyin

---

## 🎬 Video

[▶ Watch the EnchantPeak introduction on Bilibili](https://www.bilibili.com/video/BV1ChgW6vELA/)

---

## 📸 Screenshots

![Builds overview](https://raw.githubusercontent.com/zhaojiedi1992/enchantpeak/main/.github/screenshots/rei-overview.png)

*Each item shows all its enchantment builds as separate enchanted item slots.*

![Search](https://raw.githubusercontent.com/zhaojiedi1992/enchantpeak/main/.github/screenshots/rei-search.png)

*Search by item name (钻石镐 / `diamond_pickaxe`), enchantment name (时运 / `Fortune`), or even pinyin (`zuanshigao`).*

![Build detail](https://raw.githubusercontent.com/zhaojiedi1992/enchantpeak/main/.github/screenshots/rei-detail.png)

*Hover any slot to see the full enchantment list rendered natively — no duplicate text.*

---

## 📦 Download & Installation

**Requirements:**
- Minecraft **26.1 ~ 26.2** (1.21.x series)
- Fabric Loader ≥ 0.19.3
- Fabric API ≥ 0.152.0
- **JEI** (≥ 30.18.0) **OR** **REI** (≥ 26.1) — at least one is required

**Download from:**
- [Modrinth](https://modrinth.com/mod/enchantpeak)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/enchantpeak)
- [GitHub Releases](https://github.com/zhaojiedi1992/enchantpeak/releases)

**Installation:**
1. Download the latest `enchantpeak-x.x.x.jar`
2. Place it in your `.minecraft/mods/` folder
3. Launch the game with Fabric + Fabric API + JEI/REI installed
4. Open JEI/REI in-game and search for items — enchanted builds will appear automatically

---

## 🎯 How to Use

### Method 1: Direct Search
Type in the REI/JEI search bar:
- Item names: `diamond pickaxe`, `钻石镐`, `zuanshijian` (pinyin)
- Enchantment names: `fortune`, `时运`, `shiyun` (pinyin)
- Look for entries with the `★` prefix — those are EnchantPeak build entries

### Method 2: Information Tab (REI only)
1. Find any diamond or netherite item in REI
2. Click it and press `R` (or right-click → Uses)
3. Switch to the **Information** tab to view all enchantment builds for that item

### Method 3: Custom Category (JEI/REI)
1. Open JEI/REI
2. Navigate to the **Best Enchantments** category
3. Browse all items and their builds visually

---

## 🛠️ Covered Items & Builds

All enchantment levels and incompatibility rules are verified against Minecraft's official datapack (`data/minecraft/enchantment/*.json`).

### Tools (Diamond / Netherite)

| Item | Build A (Fortune) | Build B (Silk Touch) |
|------|-------------------|----------------------|
| **Pickaxe / Shovel / Hoe** | Efficiency V, Fortune III, Unbreaking III, Mending I | Efficiency V, Silk Touch I, Unbreaking III, Mending I |
| **Axe** | Logging: Efficiency V, Fortune III, Unbreaking III, Mending I | Combat: Sharpness V, Efficiency V, Unbreaking III, Mending I |

### Melee Weapons

| Item | Builds |
|------|--------|
| **Sword** (Diamond/Netherite) | **Sharpness** / **Smite** / **Bane of Arthropods** (damage group, pick one): + Knockback II, Fire Aspect II, Looting III, Sweeping Edge III, Unbreaking III, Mending I |
| **Mace** | **Density** (V) / **Breach** (IV) (damage group, pick one): + Fire Aspect II, Wind Burst III, Unbreaking III, Mending I |
| **Spear** (Diamond/Netherite) | **Sharpness** / **Smite** / **Bane of Arthropods** + Lunge III, Knockback II, Fire Aspect II, Looting III, Unbreaking III, Mending I |

### Ranged & Thrown Weapons

| Item | Builds |
|------|--------|
| **Bow** | **Infinity** (Infinity I) / **Mending** (Mending I) (bow group): + Power V, Punch II, Flame I, Unbreaking III |
| **Crossbow** | **Piercing** (IV) / **Multishot** (I) (crossbow group): + Quick Charge III, Unbreaking III, Mending I |
| **Trident** | **Loyalty** (Loyalty III + Channeling I + Impaling V) / **Riptide** (Riptide III + Impaling V): + Unbreaking III, Mending I |

### Armor (Diamond / Netherite)

Each armor piece offers **4 protection builds** (Protection / Fire Protection / Blast Protection / Projectile Protection — armor group, pick one).

| Item | Common enchants on top of chosen protection |
|------|----------------------------------------------|
| **Helmet** | Respiration III, Aqua Affinity I, Thorns III, Unbreaking III, Mending I |
| **Chestplate** | Thorns III, Unbreaking III, Mending I |
| **Leggings** | Swift Sneak III, Thorns III, Unbreaking III, Mending I |
| **Boots** | Feather Falling IV, Soul Speed III, Thorns III, Unbreaking III, Mending I + **Depth Strider III** or **Frost Walker II** (boots group) |

### Special & Utility

| Item | Build |
|------|-------|
| **Fishing Rod** | Luck of the Sea III, Lure III, Unbreaking III, Mending I |
| **Elytra** | Unbreaking III, Mending I (elytra isn't in the armor tag — no Protection/Thorns) |
| **Shield** | Unbreaking III, Mending I |
| **Shears** | Efficiency V, Unbreaking III, Mending I |

> **Total: 28 items, 90 builds.** All 41 non-curse vanilla enchantments are covered somewhere.

---

## 🔧 Building from Source

```bash
git clone https://github.com/zhaojiedi1992/enchantpeak.git
cd enchantpeak
./gradlew build
```

Output: `build/libs/enchantpeak-x.x.x.jar`

**Requirements:** Java 25 (or higher)

---

## ❓ FAQ

**Q: Search doesn't find `★` entries?**  
A: Make sure you've entered a world (singleplayer or multiplayer). REI/JEI entries are registered after the game loads registries.

**Q: Does it work with JEI or only REI?**  
A: Both are supported. REI has full features (search entries + Information tab + custom category). JEI provides the custom category view.

**Q: Why no stone/iron equipment?**  
A: This mod focuses on endgame gear (diamond and netherite). Stone and iron items have limited enchantment value.

**Q: Will you add modded enchantments?**  
A: Currently vanilla-only. Modded enchantment support is planned for future versions if there's demand.

**Q: Can I use this in modpacks?**  
A: Yes! EnchantPeak is licensed under MIT. Include it freely in any modpack.

---

## 📝 License

[MIT License](LICENSE) © 2024-2026 zhaojiedi1992

Contributions and issues welcome at [GitHub](https://github.com/zhaojiedi1992/enchantpeak) 🎉
