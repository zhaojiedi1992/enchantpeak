#!/usr/bin/env python3
"""
EnchantPeak 附魔数据深度校验脚本（整合包分发级）
对照 MC 26.2 官方 datapack JSON，执行 4 项检查：

1. 【无冲突】每个流派内没有互斥附魔组合（同 exclusive_set 组出现 2 个以上）
2. 【顶配】每个出现的附魔，等级都等于官方 max_level（不能低配）
3. 【组合打满】每个物品能附的所有非诅咒附魔，都必须出现在至少一个流派里
4. 【适用性】每个附魔确实适用于该物品（supported_items tag 包含该物品）

用法：python3 scripts/verify_enchants_deep.py
前置：需要解压 MC client.jar 到 /tmp/mc_data（或修改下方路径）
"""
import json
import os
import sys

DATA_DIR = os.environ.get("MC_DATA_DIR", "/tmp/mc_data")
ENCH_DIR = f"{DATA_DIR}/data/minecraft/enchantment"
TAG_DIR = f"{DATA_DIR}/data/minecraft/tags"

def load_enchantment(name):
    return json.load(open(f"{ENCH_DIR}/{name}.json"))

def load_item_tag(tag_path):
    tag_path = tag_path.lstrip('#').replace('minecraft:', '')
    f = f"{TAG_DIR}/item/{tag_path}.json"
    if not os.path.exists(f):
        return set()
    result = set()
    for v in json.load(open(f))['values']:
        if v.startswith('#'):
            result |= load_item_tag(v)
        else:
            result.add(v.replace('minecraft:', ''))
    return result

def load_ench_tag(tag_path):
    tag_path = tag_path.lstrip('#').replace('minecraft:', '')
    f = f"{TAG_DIR}/enchantment/{tag_path}.json"
    if not os.path.exists(f):
        return set()
    return {v.replace('minecraft:', '') for v in json.load(open(f))['values']}

# 加载官方数据
ENCHANTMENTS = {f.removesuffix('.json'): load_enchantment(f.removesuffix('.json'))
                for f in os.listdir(ENCH_DIR) if f.endswith('.json')}
TREASURE = load_ench_tag('#minecraft:treasure') if os.path.exists(f"{TAG_DIR}/enchantment/treasure.json") else set()

ENCH_ITEMS = {}
for name, d in ENCHANTMENTS.items():
    si = d.get('supported_items', '')
    ENCH_ITEMS[name] = load_item_tag(si) if si else set()

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
}
# 下界合金复用钻石规格（代码里也是复用同一组 EnchantGroup 对象）
DIAMOND_TO_NETHERITE = {
    "diamond_pickaxe": "netherite_pickaxe", "diamond_shovel": "netherite_shovel",
    "diamond_hoe": "netherite_hoe", "diamond_axe": "netherite_axe",
    "diamond_sword": "netherite_sword", "diamond_spear": "netherite_spear",
    "diamond_helmet": "netherite_helmet", "diamond_chestplate": "netherite_chestplate",
    "diamond_leggings": "netherite_leggings", "diamond_boots": "netherite_boots",
}

CURSE = {"binding_curse", "vanishing_curse"}
errors = []

# ========== 检查 1：无冲突 + 检查 2：顶配 + 检查 4：适用性 ==========
print("【检查 1/4】无冲突 + 顶配 + 适用性...")
all_items = dict(ITEMS)
for d, n in DIAMOND_TO_NETHERITE.items():
    all_items[n] = ITEMS[d]

for item_id, groups in all_items.items():
    for group_name, enchants in groups:
        seen_groups = {}
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
            # 检查 1：无冲突
            es = official.get('exclusive_set')
            if es:
                eg = es.split('/')[-1]
                if eg in seen_groups:
                    errors.append(f"{item_id}/{group_name}: 互斥冲突！{ench_name} 与 {seen_groups[eg]} 同属 {eg} 组")
                else:
                    seen_groups[eg] = ench_name
            if ench_name == 'riptide':
                seen_groups.setdefault('riptide', ench_name)
            if ench_name in ('loyalty','channeling') and 'riptide' in seen_groups:
                errors.append(f"{item_id}/{group_name}: {ench_name} 与 riptide 互斥")
print("  完成")

# ========== 检查 3：组合打满 ==========
# 对每个物品，官方规定它能附的所有非诅咒附魔，都必须出现在至少一个流派里
print("【检查 3/4】组合打满（每个可附附魔都出现在某流派）...")
for item_id, groups in all_items.items():
    # 官方规定该物品能附的全部非诅咒附魔
    official_supported = set()
    for ench_name in ENCHANTMENTS:
        if ench_name in CURSE:
            continue
        if item_id in ENCH_ITEMS.get(ench_name, set()):
            official_supported.add(ench_name)
    # 代码里该物品所有流派出现的附魔并集
    code_used = set()
    for _, enchants in groups:
        for ench_name, _ in enchants:
            code_used.add(ench_name)
    missing = official_supported - code_used
    if missing:
        errors.append(f"{item_id}: 组合未打满！官方支持但代码未覆盖的附魔：{sorted(missing)}")
print("  完成")

# ========== 附魔书完整性（bonus：确认所有非诅咒附魔至少在某个物品的某流派里出现）==========
print("【检查 4/4】附魔书覆盖（所有非诅咒附魔都被用到）...")
all_used = set()
for groups in all_items.values():
    for _, enchants in groups:
        for ench_name, _ in enchants:
            all_used.add(ench_name)
all_official_non_curse = set(ENCHANTMENTS.keys()) - CURSE
unused = all_official_non_curse - all_used
if unused:
    errors.append(f"以下官方附魔在所有物品的所有流派中从未出现：{sorted(unused)}")
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
    print(f"✅ 四项检查全部通过！")
    print(f"   • {total_items} 个物品（钻石+下界合金各算）")
    print(f"   • {total_builds} 个流派方案")
    print(f"   • {total_enchants_used}/{len(all_official_non_curse)} 个非诅咒附魔被使用")
    print(f"   • 所有附魔均达官方 max_level（顶配）")
    print(f"   • 所有流派内无互斥冲突")
    print(f"   • 每个物品的所有可附附魔都已在某流派中覆盖（组合打满）")
    sys.exit(0)
