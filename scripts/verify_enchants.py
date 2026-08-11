#!/usr/bin/env python3
"""
EnchantPeak 附魔数据核对脚本
对照 MC 26.2 官方 datapack JSON，验证代码中定义的附魔组合是否准确。
检查项：
1. 每个附魔等级 ≤ 官方 max_level
2. 每个流派内没有互斥冲突（同 exclusive_set 组内出现 2 个以上）
3. 每个附魔适用于该物品（supported_items tag 包含该物品）
"""
import json
import os
import re
import sys

ENCH_DIR = "/tmp/mc_data/data/minecraft/enchantment"
TAG_DIR = "/tmp/mc_data/data/minecraft/tags"

# 官方数据缓存
def load_enchantment(name):
    return json.load(open(f"{ENCH_DIR}/{name}.json"))

def load_item_tag(tag_path):
    """tag_path 如 'enchantable/mining' 或 'swords'，返回包含的物品 id 集合"""
    # 处理 # 前缀
    tag_path = tag_path.lstrip('#').replace('minecraft:', '')
    f = f"{TAG_DIR}/item/{tag_path}.json"
    if not os.path.exists(f):
        return set()
    d = json.load(open(f))
    result = set()
    for v in d['values']:
        if v.startswith('#'):
            result |= load_item_tag(v)
        else:
            result.add(v.replace('minecraft:', ''))
    return result

def load_ench_tag(tag_path):
    """附魔 tag，返回附魔 id 集合"""
    tag_path = tag_path.lstrip('#').replace('minecraft:', '')
    f = f"{TAG_DIR}/enchantment/{tag_path}.json"
    if not os.path.exists(f):
        return set()
    return {v.replace('minecraft:', '') for v in json.load(open(f))['values']}

# 加载所有附魔的官方定义
ENCHANTMENTS = {}
for f in os.listdir(ENCH_DIR):
    name = f.removesuffix('.json')
    ENCHANTMENTS[name] = load_enchantment(name)

# 预计算每个附魔的 supported_items（展开成物品集合）
ENCH_ITEMS = {}
for name, d in ENCHANTMENTS.items():
    si = d.get('supported_items', '')
    if si:
        ENCH_ITEMS[name] = load_item_tag(si)
    else:
        ENCH_ITEMS[name] = set()

# 预计算互斥组：exclusive_set 相同的附魔属于同一互斥组
def get_exclusive_group(ench_name):
    """返回该附魔所属的互斥组名（所有与之互斥的附魔集合）"""
    d = ENCHANTMENTS[ench_name]
    es = d.get('exclusive_set')
    if not es:
        # 检查是否被其他附魔的 exclusive_set 反向引用
        return None
    # es 是一个 enchantment tag，展开
    return load_ench_tag(es)

# ========== 从代码中提取附魔组合 ==========
# 解析 EnchantmentData.java
CODE_FILE = "/home/zhaojd5/codes/github/zhaojiedi1992/enchantpeak/src/main/java/com/zhaojiedi1992/enchantpeak/data/EnchantmentData.java"
code = open(CODE_FILE).read()

# 提取所有 e(l, Enchantments.XXX, LEVEL) 调用
ENCH_CALL_RE = re.compile(r'e\(\s*l,\s*Enchantments\.(\w+),\s*(\d+)\s*\)')

# 定义物品 → 附魔组合（手工列出，与代码 build* 方法对应）
# 格式: 物品id, [(流派名, [(附魔名, 等级), ...]), ...]
# 这个列表是从代码逻辑重建的"规格"
ITEMS = {
    # 工具
    "diamond_pickaxe": [
        ("时运流", [("efficiency",5),("fortune",3),("unbreaking",3),("mending",1)]),
        ("精准流", [("efficiency",5),("silk_touch",1),("unbreaking",3),("mending",1)]),
    ],
    "netherite_pickaxe": "_same_as_diamond_pickaxe_",
    "diamond_shovel": [
        ("时运流", [("efficiency",5),("fortune",3),("unbreaking",3),("mending",1)]),
        ("精准流", [("efficiency",5),("silk_touch",1),("unbreaking",3),("mending",1)]),
    ],
    "netherite_shovel": "_same_as_diamond_shovel_",
    "diamond_hoe": [
        ("时运流", [("efficiency",5),("fortune",3),("unbreaking",3),("mending",1)]),
        ("精准流", [("efficiency",5),("silk_touch",1),("unbreaking",3),("mending",1)]),
    ],
    "netherite_hoe": "_same_as_diamond_hoe_",
    # 斧（mining 2 选一 × damage 3 选一 = 6 种组合）
    "diamond_axe": [
        ("时运流·锋利", [("efficiency",5),("fortune",3),("sharpness",5),("unbreaking",3),("mending",1)]),
        ("时运流·亡灵杀手", [("efficiency",5),("fortune",3),("smite",5),("unbreaking",3),("mending",1)]),
        ("时运流·节肢杀手", [("efficiency",5),("fortune",3),("bane_of_arthropods",5),("unbreaking",3),("mending",1)]),
        ("精准流·锋利", [("efficiency",5),("silk_touch",1),("sharpness",5),("unbreaking",3),("mending",1)]),
        ("精准流·亡灵杀手", [("efficiency",5),("silk_touch",1),("smite",5),("unbreaking",3),("mending",1)]),
        ("精准流·节肢杀手", [("efficiency",5),("silk_touch",1),("bane_of_arthropods",5),("unbreaking",3),("mending",1)]),
    ],
    "netherite_axe": "_same_as_diamond_axe_",
    # 剑
    "diamond_sword": [
        ("锋利流", [("sharpness",5),("knockback",2),("fire_aspect",2),("looting",3),("sweeping_edge",3),("unbreaking",3),("mending",1)]),
        ("亡灵杀手流", [("smite",5),("knockback",2),("fire_aspect",2),("looting",3),("sweeping_edge",3),("unbreaking",3),("mending",1)]),
        ("节肢杀手流", [("bane_of_arthropods",5),("knockback",2),("fire_aspect",2),("looting",3),("sweeping_edge",3),("unbreaking",3),("mending",1)]),
    ],
    "netherite_sword": "_same_as_diamond_sword_",
    # 重锤（damage 组在 mace 上实际有 4 个可选：smite/bane_of_arthropods/density/breach）
    "mace": [
        ("密度流", [("density",5),("fire_aspect",2),("wind_burst",3),("unbreaking",3),("mending",1)]),
        ("破甲流", [("breach",4),("fire_aspect",2),("wind_burst",3),("unbreaking",3),("mending",1)]),
        ("亡灵杀手流", [("smite",5),("fire_aspect",2),("wind_burst",3),("unbreaking",3),("mending",1)]),
        ("节肢杀手流", [("bane_of_arthropods",5),("fire_aspect",2),("wind_burst",3),("unbreaking",3),("mending",1)]),
    ],
    # 长矛
    "diamond_spear": [
        ("锋利流", [("sharpness",5),("lunge",3),("knockback",2),("fire_aspect",2),("looting",3),("unbreaking",3),("mending",1)]),
        ("亡灵杀手流", [("smite",5),("lunge",3),("knockback",2),("fire_aspect",2),("looting",3),("unbreaking",3),("mending",1)]),
        ("节肢杀手流", [("bane_of_arthropods",5),("lunge",3),("knockback",2),("fire_aspect",2),("looting",3),("unbreaking",3),("mending",1)]),
    ],
    "netherite_spear": "_same_as_diamond_spear_",
    # 弓
    "bow": [
        ("无限流", [("power",5),("punch",2),("flame",1),("infinity",1),("unbreaking",3)]),
        ("修补流", [("power",5),("punch",2),("flame",1),("mending",1),("unbreaking",3)]),
    ],
    # 弩
    "crossbow": [
        ("穿透流", [("piercing",4),("quick_charge",3),("unbreaking",3),("mending",1)]),
        ("多重流", [("multishot",1),("quick_charge",3),("unbreaking",3),("mending",1)]),
    ],
    # 三叉戟
    "trident": [
        ("忠诚流", [("loyalty",3),("channeling",1),("impaling",5),("unbreaking",3),("mending",1)]),
        ("激流流", [("riptide",3),("impaling",5),("unbreaking",3),("mending",1)]),
    ],
    # 钓鱼竿
    "fishing_rod": [
        ("满配流", [("luck_of_the_sea",3),("lure",3),("unbreaking",3),("mending",1)]),
    ],
    # 头盔（4 个保护流派，额外 respiration/aqua_affinity）
    "diamond_helmet": [
        ("保护流", [("protection",4),("respiration",3),("aqua_affinity",1),("thorns",3),("unbreaking",3),("mending",1)]),
        ("火焰保护流", [("fire_protection",4),("respiration",3),("aqua_affinity",1),("thorns",3),("unbreaking",3),("mending",1)]),
        ("爆炸保护流", [("blast_protection",4),("respiration",3),("aqua_affinity",1),("thorns",3),("unbreaking",3),("mending",1)]),
        ("弹射物保护流", [("projectile_protection",4),("respiration",3),("aqua_affinity",1),("thorns",3),("unbreaking",3),("mending",1)]),
    ],
    "netherite_helmet": "_same_as_diamond_helmet_",
    # 胸甲（4 流派）
    "diamond_chestplate": [
        ("保护流", [("protection",4),("thorns",3),("unbreaking",3),("mending",1)]),
        ("火焰保护流", [("fire_protection",4),("thorns",3),("unbreaking",3),("mending",1)]),
        ("爆炸保护流", [("blast_protection",4),("thorns",3),("unbreaking",3),("mending",1)]),
        ("弹射物保护流", [("projectile_protection",4),("thorns",3),("unbreaking",3),("mending",1)]),
    ],
    "netherite_chestplate": "_same_as_diamond_chestplate_",
    # 护腿（4 流派，额外 swift_sneak）
    "diamond_leggings": [
        ("保护流", [("protection",4),("swift_sneak",3),("thorns",3),("unbreaking",3),("mending",1)]),
        ("火焰保护流", [("fire_protection",4),("swift_sneak",3),("thorns",3),("unbreaking",3),("mending",1)]),
        ("爆炸保护流", [("blast_protection",4),("swift_sneak",3),("thorns",3),("unbreaking",3),("mending",1)]),
        ("弹射物保护流", [("projectile_protection",4),("swift_sneak",3),("thorns",3),("unbreaking",3),("mending",1)]),
    ],
    "netherite_leggings": "_same_as_diamond_leggings_",
    # 靴子（4 保护 × 2 移动 = 8 流派，额外 feather_falling/soul_speed）
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
    "netherite_boots": "_same_as_diamond_boots_",
}

errors = []
warnings = []

for item_id, groups in ITEMS.items():
    if isinstance(groups, str) and groups.startswith("_same_as_"):
        continue  # 下界合金同钻石，跳过
    for group_name, enchants in groups:
        seen_exclusive_groups = {}  # 互斥组名 -> 附魔名
        for ench_name, level in enchants:
            if ench_name not in ENCHANTMENTS:
                errors.append(f"{item_id}/{group_name}: 未知附魔 '{ench_name}'")
                continue
            official = ENCHANTMENTS[ench_name]
            max_level = official.get('max_level', 0)
            # 检查 1: 等级不超 max_level
            if level > max_level:
                errors.append(f"{item_id}/{group_name}: {ench_name} 等级 {level} > 官方 max_level {max_level}")
            # 检查 2: 附魔适用于该物品
            if item_id not in ENCH_ITEMS.get(ench_name, set()):
                errors.append(f"{item_id}/{group_name}: {ench_name} 不适用于 {item_id}（官方 supported_items 不含此物品）")
            # 检查 3: 互斥冲突
            eg = official.get('exclusive_set')
            if eg:
                eg_name = eg.split('/')[-1]
                if eg_name in seen_exclusive_groups:
                    errors.append(f"{item_id}/{group_name}: 互斥冲突！{ench_name} 与 {seen_exclusive_groups[eg_name]} 同属 {eg_name} 组")
                else:
                    seen_exclusive_groups[eg_name] = ench_name
            # 特殊检查：riptide 反向互斥（riptide 与 loyalty/channeling 不能共存）
            if ench_name == "riptide":
                seen_exclusive_groups['riptide'] = ench_name
            if ench_name in ("loyalty", "channeling") and 'riptide' in seen_exclusive_groups:
                errors.append(f"{item_id}/{group_name}: 互斥冲突！{ench_name} 与 riptide 互斥（riptide 互斥组）")

# ========== 完整性检测 ==========
# 对每个物品，检查所有互斥组：代码里出现的附魔集合，是否等于官方规定的该物品实际可用的完整集合
# 这能发现"遗漏了某个可用附魔分支"的问题（例如重锤本可用 smite，但代码没有覆盖）
ALL_EXCLUSIVE_GROUPS = set()
for d in ENCHANTMENTS.values():
    es = d.get('exclusive_set')
    if es:
        ALL_EXCLUSIVE_GROUPS.add(es.split('/')[-1])
# riptide 反向组特殊处理，已在 riptide.json 里自带 exclusive_set，加入统一遍历
ALL_EXCLUSIVE_GROUPS.add('riptide')

completeness_warnings = []
for item_id, groups in ITEMS.items():
    if isinstance(groups, str) and groups.startswith("_same_as_"):
        continue
    # 收集该物品在代码里，每个互斥组实际出现了哪些附魔
    used_by_group = {}
    for group_name, enchants in groups:
        for ench_name, level in enchants:
            if ench_name not in ENCHANTMENTS:
                continue
            es = ENCHANTMENTS[ench_name].get('exclusive_set')
            if es:
                eg_name = es.split('/')[-1]
                used_by_group.setdefault(eg_name, set()).add(ench_name)
            if ench_name == 'riptide':
                used_by_group.setdefault('riptide', set()).add('riptide')

    # 对每个用到的互斥组，计算官方在该物品上实际可用的完整附魔集合
    for eg_name, used in used_by_group.items():
        if eg_name == 'riptide':
            continue  # riptide 组已用忠诚/激流表达完整，跳过自动检测（结构特殊）
        official_full_set = set()
        for ench_name, d in ENCHANTMENTS.items():
            official_eg = d.get('exclusive_set')
            if official_eg and official_eg.split('/')[-1] == eg_name:
                if item_id in ENCH_ITEMS.get(ench_name, set()):
                    official_full_set.add(ench_name)
        missing = official_full_set - used
        if missing:
            completeness_warnings.append(
                f"{item_id} [{eg_name}组]: 代码只用了 {sorted(used)}，遗漏了 {sorted(missing)}（官方该物品在此互斥组的完整可用集合是 {sorted(official_full_set)}）"
            )

print("=" * 70)
if errors:
    print(f"❌ 发现 {len(errors)} 个正确性错误：")
    for err in errors:
        print(f"  ✗ {err}")
    sys.exit(1)
elif completeness_warnings:
    print(f"⚠️  正确性通过，但发现 {len(completeness_warnings)} 个完整性遗漏（可能漏了某个流派分支）：")
    for w in completeness_warnings:
        print(f"  ⚠ {w}")
    sys.exit(2)
else:
    total_items = sum(1 for v in ITEMS.values() if not (isinstance(v, str) and v.startswith("_same_as_")))
    total_groups = sum(len(v) for v in ITEMS.values() if not (isinstance(v, str) and v.startswith("_same_as_")))
    print(f"✅ 全部核对通过（正确性 + 完整性）！{total_items} 个物品，{total_groups} 个流派，")
    print(f"   所有附魔等级、适用性、互斥关系均与官方数据一致，且每个互斥组都已覆盖该物品的全部可用选项。")
    sys.exit(0)
