#!/usr/bin/env python3
"""
EnchantPeak 附魔数据深度校验脚本（整合包分发级）
对照目标 Minecraft 版本的官方 datapack JSON，执行 5 项检查：

1. 【方案顶配】每个方案都已加入所有仍可兼容的非诅咒附魔
2. 【等级最大】每个附魔等级都等于官方 max_level
3. 【组合完整】每件物品的全部极大兼容组合均已枚举，且没有多余组合
4. 【无冲突】每个方案内任意两个附魔均不互斥
5. 【无遗漏】所有原版非诅咒附魔都至少被一个方案覆盖

通常通过 python3 scripts/verify_enchants.py 调用。
直接运行时必须通过 MC_DATA_DIR 指定已解压的 Minecraft 数据目录。
"""
import hashlib
import json
import os
from pathlib import Path
import re
import sys

import argparse

REPO_ROOT = Path(__file__).resolve().parents[1]

DATA_DIR_VALUE = os.environ.get("MC_DATA_DIR")
if not DATA_DIR_VALUE:
    print("缺少 MC_DATA_DIR；请改用 scripts/verify_enchants.py 运行校验", file=sys.stderr)
    sys.exit(1)

DATA_DIR = Path(DATA_DIR_VALUE).resolve()
ENCH_DIR = DATA_DIR / "data/minecraft/enchantment"
TAG_DIR = DATA_DIR / "data/minecraft/tags"
if not ENCH_DIR.is_dir() or not TAG_DIR.is_dir():
    print(f"MC_DATA_DIR 不包含有效的 Minecraft datapack：{DATA_DIR}", file=sys.stderr)
    sys.exit(1)


parser = argparse.ArgumentParser()
parser.add_argument("--source", default="mc26", help="version family source dir name under src/")
args, _ = parser.parse_known_args()

DATA_SOURCE = REPO_ROOT / f"src/{args.source}/java/com/zhaojiedi1992/enchantpeak/data/EnchantmentData.java"
if not DATA_SOURCE.is_file():
    print(f"找不到源码文件：{DATA_SOURCE}", file=sys.stderr)
    sys.exit(1)

def load_enchantment(name):
    with (ENCH_DIR / f"{name}.json").open(encoding="utf-8") as handle:
        return json.load(handle)

def load_item_tag(tag_path):
    tag_path = tag_path.lstrip('#').replace('minecraft:', '')
    f = TAG_DIR / "item" / f"{tag_path}.json"
    if not f.exists():
        return set()
    result = set()
    with f.open(encoding="utf-8") as handle:
        values = json.load(handle)['values']
    for v in values:
        if v.startswith('#'):
            result |= load_item_tag(v)
        else:
            result.add(v.replace('minecraft:', ''))
    return result

def load_ench_tag(tag_path, visited=None):
    tag_path = tag_path.lstrip('#').replace('minecraft:', '')
    visited = set() if visited is None else visited
    if tag_path in visited:
        return set()
    visited.add(tag_path)
    f = TAG_DIR / "enchantment" / f"{tag_path}.json"
    if not f.exists():
        return set()
    with f.open(encoding="utf-8") as handle:
        values = json.load(handle)['values']
    result = set()
    for value in values:
        value = value.get('id', '') if isinstance(value, dict) else value
        if value.startswith('#'):
            result |= load_ench_tag(value, visited)
        elif value:
            result.add(value.replace('minecraft:', ''))
    return result

# 加载官方数据
ENCHANTMENTS = {f.stem: load_enchantment(f.stem) for f in ENCH_DIR.glob("*.json")}

ENCH_ITEMS = {}
for name, d in ENCHANTMENTS.items():
    si = d.get('supported_items', '')
    ENCH_ITEMS[name] = load_item_tag(si) if si else set()

ENCH_EXCLUSIONS = {}
for name, definition in ENCHANTMENTS.items():
    exclusive_set = definition.get('exclusive_set')
    ENCH_EXCLUSIONS[name] = load_ench_tag(exclusive_set) if exclusive_set else set()

# 从代码重建规格（与 verify_enchants.py 同源，手工列出，代表 EnchantmentData.java 的逻辑）
ITEMS = {
    "diamond_pickaxe": [
        ("时运流", [("efficiency",5),("fortune",3),("unbreaking",3),("mending",1)]),
        ("精准流", [("efficiency",5),("silk_touch",1),("unbreaking",3),("mending",1)]),
    ],
    "diamond_shovel": [
        ("时运流", [("efficiency",5),("fortune",3),("unbreaking",3),("mending",1)]),
        ("精准流", [("efficiency",5),("silk_touch",1),("unbreaking",3),("mending",1)]),
    ],
    "diamond_hoe": [
        ("时运流", [("efficiency",5),("fortune",3),("unbreaking",3),("mending",1)]),
        ("精准流", [("efficiency",5),("silk_touch",1),("unbreaking",3),("mending",1)]),
    ],
    "diamond_axe": [
        ("时运流·锋利", [("efficiency",5),("fortune",3),("sharpness",5),("unbreaking",3),("mending",1)]),
        ("时运流·亡灵杀手", [("efficiency",5),("fortune",3),("smite",5),("unbreaking",3),("mending",1)]),
        ("时运流·节肢杀手", [("efficiency",5),("fortune",3),("bane_of_arthropods",5),("unbreaking",3),("mending",1)]),
        ("精准流·锋利", [("efficiency",5),("silk_touch",1),("sharpness",5),("unbreaking",3),("mending",1)]),
        ("精准流·亡灵杀手", [("efficiency",5),("silk_touch",1),("smite",5),("unbreaking",3),("mending",1)]),
        ("精准流·节肢杀手", [("efficiency",5),("silk_touch",1),("bane_of_arthropods",5),("unbreaking",3),("mending",1)]),
    ],
    "diamond_sword": [
        ("锋利流", [("sharpness",5),("knockback",2),("fire_aspect",2),("looting",3),("sweeping_edge",3),("unbreaking",3),("mending",1)]),
        ("亡灵杀手流", [("smite",5),("knockback",2),("fire_aspect",2),("looting",3),("sweeping_edge",3),("unbreaking",3),("mending",1)]),
        ("节肢杀手流", [("bane_of_arthropods",5),("knockback",2),("fire_aspect",2),("looting",3),("sweeping_edge",3),("unbreaking",3),("mending",1)]),
    ],
    "diamond_spear": [
        ("锋利流", [("sharpness",5),("lunge",3),("knockback",2),("fire_aspect",2),("looting",3),("unbreaking",3),("mending",1)]),
        ("亡灵杀手流", [("smite",5),("lunge",3),("knockback",2),("fire_aspect",2),("looting",3),("unbreaking",3),("mending",1)]),
        ("节肢杀手流", [("bane_of_arthropods",5),("lunge",3),("knockback",2),("fire_aspect",2),("looting",3),("unbreaking",3),("mending",1)]),
    ],
    "mace": [
        ("密度流", [("density",5),("fire_aspect",2),("wind_burst",3),("unbreaking",3),("mending",1)]),
        ("破甲流", [("breach",4),("fire_aspect",2),("wind_burst",3),("unbreaking",3),("mending",1)]),
        ("亡灵杀手流", [("smite",5),("fire_aspect",2),("wind_burst",3),("unbreaking",3),("mending",1)]),
        ("节肢杀手流", [("bane_of_arthropods",5),("fire_aspect",2),("wind_burst",3),("unbreaking",3),("mending",1)]),
    ],
    "bow": [
        ("无限流", [("power",5),("punch",2),("flame",1),("infinity",1),("unbreaking",3)]),
        ("修补流", [("power",5),("punch",2),("flame",1),("mending",1),("unbreaking",3)]),
    ],
    "crossbow": [
        ("穿透流", [("piercing",4),("quick_charge",3),("unbreaking",3),("mending",1)]),
        ("多重流", [("multishot",1),("quick_charge",3),("unbreaking",3),("mending",1)]),
    ],
    "trident": [
        ("忠诚流", [("loyalty",3),("channeling",1),("impaling",5),("unbreaking",3),("mending",1)]),
        ("激流流", [("riptide",3),("impaling",5),("unbreaking",3),("mending",1)]),
    ],
    "fishing_rod": [
        ("满配流", [("luck_of_the_sea",3),("lure",3),("unbreaking",3),("mending",1)]),
    ],
    "diamond_helmet": [
        ("保护流", [("protection",4),("respiration",3),("aqua_affinity",1),("thorns",3),("unbreaking",3),("mending",1)]),
        ("火焰保护流", [("fire_protection",4),("respiration",3),("aqua_affinity",1),("thorns",3),("unbreaking",3),("mending",1)]),
        ("爆炸保护流", [("blast_protection",4),("respiration",3),("aqua_affinity",1),("thorns",3),("unbreaking",3),("mending",1)]),
        ("弹射物保护流", [("projectile_protection",4),("respiration",3),("aqua_affinity",1),("thorns",3),("unbreaking",3),("mending",1)]),
    ],
    "diamond_chestplate": [
        ("保护流", [("protection",4),("thorns",3),("unbreaking",3),("mending",1)]),
        ("火焰保护流", [("fire_protection",4),("thorns",3),("unbreaking",3),("mending",1)]),
        ("爆炸保护流", [("blast_protection",4),("thorns",3),("unbreaking",3),("mending",1)]),
        ("弹射物保护流", [("projectile_protection",4),("thorns",3),("unbreaking",3),("mending",1)]),
    ],
    "diamond_leggings": [
        ("保护流", [("protection",4),("swift_sneak",3),("thorns",3),("unbreaking",3),("mending",1)]),
        ("火焰保护流", [("fire_protection",4),("swift_sneak",3),("thorns",3),("unbreaking",3),("mending",1)]),
        ("爆炸保护流", [("blast_protection",4),("swift_sneak",3),("thorns",3),("unbreaking",3),("mending",1)]),
        ("弹射物保护流", [("projectile_protection",4),("swift_sneak",3),("thorns",3),("unbreaking",3),("mending",1)]),
    ],
    "diamond_boots": [
        ("保护流·深海", [("protection",4),("feather_falling",4),("soul_speed",3),("depth_strider",3),("thorns",3),("unbreaking",3),("mending",1)]),
        ("保护流·冰霜", [("protection",4),("feather_falling",4),("soul_speed",3),("frost_walker",2),("thorns",3),("unbreaking",3),("mending",1)]),
        ("火焰保护流·深海", [("fire_protection",4),("feather_falling",4),("soul_speed",3),("depth_strider",3),("thorns",3),("unbreaking",3),("mending",1)]),
        ("火焰保护流·冰霜", [("fire_protection",4),("feather_falling",4),("soul_speed",3),("frost_walker",2),("thorns",3),("unbreaking",3),("mending",1)]),
        ("爆炸保护流·深海", [("blast_protection",4),("feather_falling",4),("soul_speed",3),("depth_strider",3),("thorns",3),("unbreaking",3),("mending",1)]),
        ("爆炸保护流·冰霜", [("blast_protection",4),("feather_falling",4),("soul_speed",3),("frost_walker",2),("thorns",3),("unbreaking",3),("mending",1)]),
        ("弹射物保护流·深海", [("projectile_protection",4),("feather_falling",4),("soul_speed",3),("depth_strider",3),("thorns",3),("unbreaking",3),("mending",1)]),
        ("弹射物保护流·冰霜", [("projectile_protection",4),("feather_falling",4),("soul_speed",3),("frost_walker",2),("thorns",3),("unbreaking",3),("mending",1)]),
    ],
    "elytra": [
        ("满配流", [("unbreaking",3),("mending",1)]),
    ],
    "shield": [
        ("满配流", [("unbreaking",3),("mending",1)]),
    ],
    "shears": [
        ("满配流", [("efficiency",5),("unbreaking",3),("mending",1)]),
    ],
}
# 同类材质复用同一套方案。键为目标物品，值为上方的方案模板物品。
ITEM_ALIASES = {}
for item_type in ("pickaxe", "shovel", "hoe", "axe", "sword", "spear"):
    template = f"diamond_{item_type}"
    for material in ("wooden", "stone", "copper", "iron", "golden", "netherite"):
        ITEM_ALIASES[f"{material}_{item_type}"] = template
for armor_type in ("helmet", "chestplate", "leggings", "boots"):
    template = f"diamond_{armor_type}"
    for material in ("leather", "chainmail", "copper", "iron", "golden", "netherite"):
        ITEM_ALIASES[f"{material}_{armor_type}"] = template
ITEM_ALIASES["turtle_helmet"] = "diamond_helmet"
for utility_item in ("brush", "flint_and_steel", "carrot_on_a_stick", "warped_fungus_on_a_stick"):
    ITEM_ALIASES[utility_item] = "elytra"
CURSE_ONLY_ITEMS = {
    "carved_pumpkin", "compass", "creeper_head", "dragon_head", "piglin_head",
    "player_head", "skeleton_skull", "wither_skeleton_skull", "zombie_head",
}

CURSE = {"binding_curse", "vanishing_curse"}
errors = []
all_items = dict(ITEMS)
for item_id, template_id in ITEM_ALIASES.items():
    all_items[item_id] = ITEMS[template_id]
for item_id in CURSE_ONLY_ITEMS:
    all_items[item_id] = [("无非诅咒附魔", [])]


def incompatible(first, second):
    """官方互斥声明可能只写在一侧，因此按双向关系判断。"""
    return second in ENCH_EXCLUSIONS.get(first, set()) or first in ENCH_EXCLUSIONS.get(second, set())


def compatible_set(enchants):
    enchants = list(enchants)
    return all(
        not incompatible(enchants[i], enchants[j])
        for i in range(len(enchants))
        for j in range(i + 1, len(enchants))
    )


def supported_non_curse(item_id):
    return {
        name for name in ENCHANTMENTS
        if name not in CURSE and item_id in ENCH_ITEMS.get(name, set())
    }


official_non_curse_items = set().union(*(ENCH_ITEMS[name] for name in ENCHANTMENTS if name not in CURSE))
official_all_items = set().union(*ENCH_ITEMS.values())
official_curse_only_items = official_all_items - official_non_curse_items
expected_curse_only_items = CURSE_ONLY_ITEMS
configured_items = set(all_items)
java_registered_items = {
    item.lower()
    for item in re.findall(r"Items\.([A-Z0-9_]+)", DATA_SOURCE.read_text(encoding="utf-8"))
}
missing_items = official_all_items - configured_items
extra_items = configured_items - official_all_items
if missing_items:
    errors.append(f"遗漏官方可附魔物品：{sorted(missing_items)}")
if extra_items:
    errors.append(f"配置了官方不可附魔物品：{sorted(extra_items)}")
missing_java_items = configured_items - java_registered_items
extra_java_items = java_registered_items - configured_items
if missing_java_items:
    errors.append(f"Java 实现未注册校验规格中的物品：{sorted(missing_java_items)}")
if extra_java_items:
    errors.append(f"Java 实现注册了校验规格外的物品：{sorted(extra_java_items)}")
if official_curse_only_items != expected_curse_only_items:
    errors.append(
        "官方纯诅咒物品集合发生变化："
        f"expected={sorted(expected_curse_only_items)}, actual={sorted(official_curse_only_items)}"
    )


def maximal_compatible_sets(enchants):
    """枚举附魔集合中的全部极大兼容子集。当前单物品候选量很小，可直接穷举。"""
    ordered = sorted(enchants)
    compatible = []
    for mask in range(1 << len(ordered)):
        candidate = frozenset(ordered[index] for index in range(len(ordered)) if mask & (1 << index))
        if compatible_set(candidate):
            compatible.append(candidate)
    return {
        candidate for candidate in compatible
        if not any(candidate < other for other in compatible)
    }


# ========== 检查 1：每个方案都是无法继续添加兼容附魔的顶配集合 ==========
print("【检查 1/5】方案顶配（每个方案均已加满兼容附魔）...")
for item_id, groups in all_items.items():
    official_supported = supported_non_curse(item_id)
    for group_name, enchants in groups:
        configured = {name for name, _ in enchants}
        addable = {
            name for name in official_supported - configured
            if all(not incompatible(name, existing) for existing in configured)
        }
        if addable:
            errors.append(f"{item_id}/{group_name}: 方案未顶配，仍可加入 {sorted(addable)}")
print("  完成")

# ========== 检查 2：等级最大值与适用性 ==========
print("【检查 2/5】等级最大（逐项对照官方 max_level）...")
for item_id, groups in all_items.items():
    for group_name, enchants in groups:
        for ench_name, level in enchants:
            if ench_name not in ENCHANTMENTS:
                errors.append(f"{item_id}/{group_name}: 未知附魔 '{ench_name}'")
                continue
            official = ENCHANTMENTS[ench_name]
            max_level = official.get('max_level', 0)
            # 检查 2：顶配
            if level != max_level:
                errors.append(f"{item_id}/{group_name}: {ench_name} 等级 {level} ≠ 官方 max_level {max_level}（未达顶配）")
            # 检查 4：适用性
            if item_id not in ENCH_ITEMS.get(ench_name, set()):
                errors.append(f"{item_id}/{group_name}: {ench_name} 不适用于 {item_id}")
print("  完成")

# ========== 检查 3：全部极大兼容组合精确匹配 ==========
print("【检查 3/5】组合完整（全部极大兼容组合逐一匹配）...")
for item_id, groups in all_items.items():
    expected = maximal_compatible_sets(supported_non_curse(item_id))
    configured_list = [frozenset(name for name, _ in enchants) for _, enchants in groups]
    configured = set(configured_list)
    if len(configured_list) != len(configured):
        errors.append(f"{item_id}: 存在内容相同的重复方案")
    missing = expected - configured
    extra = configured - expected
    for combination in sorted(missing, key=lambda value: sorted(value)):
        errors.append(f"{item_id}: 遗漏顶配组合 {sorted(combination)}")
    for combination in sorted(extra, key=lambda value: sorted(value)):
        errors.append(f"{item_id}: 存在非顶配或无效组合 {sorted(combination)}")
print("  完成")

# ========== 检查 4：每个方案内部无互斥冲突 ==========
print("【检查 4/5】无冲突（官方 exclusive_set 双向校验）...")
for item_id, groups in all_items.items():
    for group_name, enchants in groups:
        configured = [name for name, _ in enchants]
        for index, first in enumerate(configured):
            for second in configured[index + 1:]:
                if incompatible(first, second):
                    errors.append(f"{item_id}/{group_name}: {first} 与 {second} 互斥")
print("  完成")

# ========== 检查 5：所有原版非诅咒附魔至少覆盖一次 ==========
print("【检查 5/5】无遗漏（所有非诅咒附魔全局覆盖）...")
all_used = set()
for groups in all_items.values():
    for _, enchants in groups:
        for ench_name, _ in enchants:
            all_used.add(ench_name)
all_official_non_curse = set(ENCHANTMENTS.keys()) - CURSE
unused = all_official_non_curse - all_used
if unused:
    errors.append(f"以下官方非诅咒附魔从未出现在任何方案中：{sorted(unused)}")
print("  完成")

# ========== 汇总 ==========
print("=" * 70)
if errors:
    print(f"❌ 发现 {len(errors)} 个问题：")
    for err in errors:
        print(f"  ✗ {err}")
    sys.exit(1)
else:
    total_items = len(all_items)
    total_builds = sum(len(g) for g in all_items.values())
    total_enchants_used = len(all_used)
    print(f"✅ 五项核心检查全部通过！")
    positive_builds = total_builds - len(official_curse_only_items)
    print(f"   • {total_items}/{len(official_all_items)} 个官方可附魔物品")
    print(f"   • {positive_builds} 个正向顶配方案 + {len(official_curse_only_items)} 个纯诅咒物品空方案")
    print(f"   • {total_enchants_used}/{len(all_official_non_curse)} 个非诅咒附魔被使用")
    print(f"   • 每个方案都是极大兼容集合，无法再加入其他兼容附魔")
    print(f"   • 每个附魔等级均等于官方 max_level")
    print(f"   • 每件物品的全部顶配组合均已枚举，无重复、无多余")
    print(f"   • 所有方案均无官方互斥冲突")
    print(f"   • 所有非诅咒附魔均至少覆盖一次")
    sys.exit(0)
