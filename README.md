# EnchantPeak

> **[中文版](README.zh_CN.md)** · **English**

A Minecraft mod that covers every vanilla enchantable item in JEI/REI. It shows maximal non-curse enchantment combinations where available and identifies curse-only items. REI also exposes every build as a standalone searchable entry.

> 💡 **Recommended setup: Fabric + REI** — the best experience with standalone searchable build entries. NeoForge builds are also provided for every supported cluster where JEI has an adapter.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.18.2%20~%2026.2-brightgreen)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-blue)](https://fabricmc.net/)
[![NeoForge](https://img.shields.io/badge/Loader-NeoForge-orange)](https://neoforged.net/)

---

## ✨ Features

- 🔍 **Build Search (REI)** — Find standalone build entries by localized item or enchantment names
- ⭐ **Dedicated Entries (REI)** — Each enchantment build appears as a separate searchable entry named `Item-Build` (e.g. `Diamond Pickaxe-时运流`)
- 📖 **Information Tab** — Press `R` on any supported item to see all builds in the REI Information panel
- 🗂️ **Build-Based Grouping** — Items with conflicting enchantments (e.g., Fortune vs Silk Touch) are split into separate builds
- ⚔️ **Comprehensive Coverage** — All 92 vanilla enchantable items: 288 maximal compatible builds for 83 items, plus explicit entries for 9 curse-only items
- 🌐 **Multilingual** — UI available in English, Simplified Chinese, Japanese, Korean, German, French, and Spanish

---

## 🎬 Video

[▶ Watch the EnchantPeak introduction on Bilibili](https://www.bilibili.com/video/BV1ChgW6vELA/)

---

## 📸 Screenshots

![Builds overview](https://raw.githubusercontent.com/zhaojiedi1992/enchantpeak/main/.github/screenshots/rei-overview.png)

*Each item shows all its enchantment builds as separate enchanted item slots.*

![Search](https://raw.githubusercontent.com/zhaojiedi1992/enchantpeak/main/.github/screenshots/rei-search.png)

*REI indexes each build's localized item name and native enchantment tooltip.*

![Build detail](https://raw.githubusercontent.com/zhaojiedi1992/enchantpeak/main/.github/screenshots/rei-detail.png)

*Hover any slot to see the full enchantment list rendered natively — no duplicate text.*

---

## 📦 Download & Installation

Each JAR covers an API-compatible version cluster (shown by the `mc` range in its filename, e.g. `mc1.19.2-1.19.4`). Pick the row that contains your game version.

**Fabric (recommended — REI search entries included):**

| JAR `mc` range | Covers |
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
| `mc26.1` / `mc26.1.1` / `mc26.1.2` / `mc26.2` | 26.x each |

**NeoForge (JEI integration):**

| JAR `mc` range | Covers |
|---|---|
| `mc1.20.3-1.20.4` | 1.20.3 / 1.20.4 |
| `mc1.20.5-1.20.6` | 1.20.5 / 1.20.6 |
| `mc1.21-1.21.1` | 1.21 / 1.21.1 |
| `mc1.21.2-1.21.4` | 1.21.2 / 1.21.3 / 1.21.4 |
| `mc1.21.5-1.21.8` | 1.21.5 / 1.21.6 / 1.21.7 / 1.21.8 |
| `mc1.21.9-1.21.11` | 1.21.9 / 1.21.10 / 1.21.11 |
| `mc26.1` / `mc26.2` | 26.x |

> NeoForge builds start at 1.20.3 — NeoForge itself has no stable release for 1.20.1 or earlier (use Fabric there). REI's standalone search entries are Fabric-only; on NeoForge use the JEI **Best Enchantments** category.

**Requirements:**
- **Fabric**: Fabric Loader + Fabric API, plus **REI** (recommended) or **JEI**
- **NeoForge**: matching NeoForge, plus **JEI**

**Download from:**
- [Modrinth](https://modrinth.com/mod/enchantpeak)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/enchantpeak)
- [GitHub Releases](https://github.com/zhaojiedi1992/enchantpeak/releases)

**Installation:**
1. Download the `enchantpeak-fabric-...+mc<your-range>.jar` or `enchantpeak-neoforge-...+mc<your-range>.jar` matching your loader and game version
2. Place it in your `.minecraft/mods/` folder
3. Launch the game with your loader's requirements installed (Fabric + Fabric API + REI/JEI, or NeoForge + JEI)
4. Open the **Best Enchantments** category in JEI/REI; REI (Fabric) also exposes each build as a searchable entry

---

## 🎯 How to Use

### Method 1: Direct Search (REI only)
Type in the REI search bar:
- Item names in the active game language, such as `diamond pickaxe` or `钻石镐`
- Enchantment names in the active game language, such as `fortune` or `时运`
- Look for entries named `Item-Build` (e.g. `钻石镐-时运流`) — those are EnchantPeak build entries

### Method 2: Information Page (JEI/REI)
1. Find any supported enchantable item in JEI/REI
2. Click it and press `R` (or right-click → Uses)
3. Open the **Information** page to view localized details for every build

### Method 3: Custom Category (JEI/REI)
1. Open JEI/REI
2. Navigate to the **Best Enchantments** category
3. Browse all items and their builds visually

---

## 🛠️ Covered Items & Builds

All enchantment levels and incompatibility rules are verified against Minecraft's official datapack (`data/minecraft/enchantment/*.json`).

### Tools (Wood / Stone / Copper / Iron / Gold / Diamond / Netherite)

| Item | Build A (Fortune) | Build B (Silk Touch) |
|------|-------------------|----------------------|
| **Pickaxe / Shovel / Hoe** | Efficiency V, Fortune III, Unbreaking III, Mending I | Efficiency V, Silk Touch I, Unbreaking III, Mending I |
| **Axe** | Logging: Efficiency V, Fortune III, Unbreaking III, Mending I | Combat: Sharpness V, Efficiency V, Unbreaking III, Mending I |

### Melee Weapons

| Item | Builds |
|------|--------|
| **Sword** (all materials) | **Sharpness** / **Smite** / **Bane of Arthropods** (damage group, pick one): + Knockback II, Fire Aspect II, Looting III, Sweeping Edge III, Unbreaking III, Mending I |
| **Mace** | **Density** (V) / **Breach** (IV) / **Smite** (V) / **Bane of Arthropods** (V) (damage group, pick one): + Fire Aspect II, Wind Burst III, Unbreaking III, Mending I |
| **Spear** (all materials) | **Sharpness** / **Smite** / **Bane of Arthropods** + Lunge III, Knockback II, Fire Aspect II, Looting III, Unbreaking III, Mending I |

### Ranged & Thrown Weapons

| Item | Builds |
|------|--------|
| **Bow** | **Infinity** (Infinity I) / **Mending** (Mending I) (bow group): + Power V, Punch II, Flame I, Unbreaking III |
| **Crossbow** | **Piercing** (IV) / **Multishot** (I) (crossbow group): + Quick Charge III, Unbreaking III, Mending I |
| **Trident** | **Loyalty** (Loyalty III + Channeling I + Impaling V) / **Riptide** (Riptide III + Impaling V): + Unbreaking III, Mending I |

### Armor (Leather / Chainmail / Copper / Iron / Gold / Diamond / Netherite)

Each armor piece offers **4 protection builds** (Protection / Fire Protection / Blast Protection / Projectile Protection — armor group, pick one).

| Item | Common enchants on top of chosen protection |
|------|----------------------------------------------|
| **Helmet** (including Turtle Shell) | Respiration III, Aqua Affinity I, Thorns III, Unbreaking III, Mending I |
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
| **Brush / Flint and Steel / Carrot on a Stick / Warped Fungus on a Stick** | Unbreaking III, Mending I |
| **Carved Pumpkin / Compass / Mob and Player Heads** | No non-curse enchantments; curses are intentionally not recommended |

> **Total: 92/92 enchantable items.** 83 items have 288 maximal non-curse builds covering 41/41 non-curse enchantments; 9 curse-only items have an explicit empty recommendation.

---

## 🔧 Building from Source

**Fabric (root project):**

```bash
git clone https://github.com/zhaojiedi1992/enchantpeak.git
cd enchantpeak
./gradlew build                                  # 默认 26.2
./gradlew clean build -Ptarget_mc=1.21           # 指定簇（如 1.21 = 1.21/1.21.1）
scripts/build_all_versions.sh                    # 全部 Fabric + NeoForge 簇，产物在 dist/
```

Supported targets and dependency versions are defined once in `versions/minecraft.json`.

**NeoForge (`neoforge/` independent build, Gradle 8.14 + NeoGradle 7.1):**

```bash
cd neoforge
./gradlew build -Ptarget_mc=1.21.8               # 指定 NeoForge 簇
```

NeoForge clusters and NeoForge/JEI versions are defined in `neoforge/targets.json`.

**Requirements:** Java 17 (for MC 1.18.2–1.20.1), Java 21 (for MC 1.21.x), and Java 25 (for MC 26.x). Gradle automatically selects the correct JDK via toolchain.

---

## ❓ FAQ

**Q: Search doesn't find the build entries?**

A: Dedicated build entries are available in REI only. Make sure you've entered a world so the registry is available; in JEI, open the **Best Enchantments** category instead.

**Q: Does it work with JEI or only REI?**

A: Both provide the custom category, item-to-build lookup, localized tooltips, and information pages. REI additionally exposes each build as a standalone global-search entry.

**Q: Are any enchantable vanilla items excluded?**

A: Every item that supports at least one non-curse enchantment is covered. The carved pumpkin, compass, and seven mob/player heads support curses only, so no positive best-build entry is generated for them.

**Q: Does this support any Minecraft 26.x version?**

A: It supports every currently released stable 26.x version that passed compilation and official-data verification: 26.1, 26.1.1, 26.1.2, and 26.2. Future 26.x releases are added to the build matrix only after the same checks pass; compatibility is never inferred from a broad metadata range.

**Q: Will you add modded enchantments?**

A: Currently vanilla-only. Modded enchantment support is planned for future versions if there's demand.

**Q: Can I use this in modpacks?**

A: Yes! EnchantPeak is licensed under MIT. Include it freely in any modpack.

---

## 📝 License

[MIT License](LICENSE) © 2024-2026 zhaojiedi1992

Contributions and issues welcome at [GitHub](https://github.com/zhaojiedi1992/enchantpeak) 🎉
