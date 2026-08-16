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
# 1.21 族（mc121/mc1214/mc1216）没有 spear / lunge / 铜质工具（26.x 新增）
IS_121_FAMILY = args.source.startswith("mc121")
_item_types = ("pickaxe", "shovel", "hoe", "axe", "sword") + (() if IS_121_FAMILY else ("spear",))
_materials = ("wooden", "stone", "copper", "iron", "golden", "netherite") \
    if not IS_121_FAMILY else ("wooden", "stone", "iron", "golden", "netherite")

ITEM_ALIASES = {}
for item_type in _item_types:
    template = f"diamond_{item_type}"
    for material in _materials:
        ITEM_ALIASES[f"{material}_{item_type}"] = template
for armor_type in ("helmet", "chestplate", "leggings", "boots"):
    template = f"diamond_{armor_type}"
    _armor_materials = ("leather", "chainmail", "copper", "iron", "golden", "netherite") \
        if not IS_121_FAMILY else ("leather", "chainmail", "iron", "golden", "netherite")
    for material in _armor_materials:
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
_121_exclude_items = {"diamond_spear"} if IS_121_FAMILY else set()
for item_id in _121_exclude_items:
    all_items.pop(item_id, None)
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
java_source = DATA_SOURCE.read_text(encoding="utf-8")
java_registered_items = {item.lower() for item in re.findall(r"Items\.([A-Z0-9_]+)", java_source)}

# Java 分组内容解析：支持字面量与参数化工厂方法，生成 {item_id: set[frozenset]}
# 使用括号平衡计数处理嵌套方法调用（如 toolFortune(l)）

def _paren_close(s, open_at):
    depth, i = 1, open_at + 1
    while i < len(s) and depth > 0:
        if s[i] == '(': depth += 1
        elif s[i] == ')': depth -= 1
        i += 1
    return i - 1

def _top_comma(s):
    depth = 0
    for i, c in enumerate(s):
        if c == '(': depth += 1
        elif c == ')': depth -= 1
        elif c == ',' and depth == 0: return i
    return -1

def _parse_e_calls(block):
    return [(m.group(1).lower(), int(m.group(2)))
            for m in re.finditer(
                r'e\([^,]+,\s*Enchantments\.([A-Z0-9_]+)\s*,\s*(\d+)\s*\)', block)]

java_item_builds = {}  # {item_id: set[frozenset[(ench, level)]]}

# Step 1: All literal EnchantGroup("name", List.of(...)) using paren-balance
all_groups = {}
for m in re.finditer(r'new\s+EnchantGroup\s*\(\s*"([a-z0-9_]+)"\s*,\s*(?:List\.of|nonNull)\(', java_source):
    gname = m.group(1)
    lopen = m.end() - 1
    lclose = _paren_close(java_source, lopen)
    enchants = _parse_e_calls(java_source[lopen+1:lclose])
    if enchants:
        all_groups[gname] = frozenset(enchants)

# Step 2: Parameterized factory methods
factories = {}
for m in re.finditer(
    r'private\s+(?:static\s+)?EnchantGroup\s+([a-zA-Z0-9_]+)\s*\(([^)]+)\)\s*\{(.*?)\n    \}',
    java_source, re.DOTALL
):
    mname, params_str, body = m.group(1), m.group(2), m.group(3)
    static_e, has_var = [], False
    for ec in re.finditer(r'e\([^,]+,\s*([^,]+)\s*,\s*([^)]+)\s*\)', body):
        ek, el = ec.group(1).strip(), ec.group(2).strip()
        if ek.startswith('Enchantments.'):
            try: static_e.append((ek.replace('Enchantments.', '').lower(), int(el)))
            except ValueError: has_var = True
        else: has_var = True
    if has_var:
        factories[mname] = static_e

# Step 3: （工厂调用不预先展开到全局表——同名组如 sharpness 在 sword/spear/mace
# 间复用，全局键会互相覆盖；改为在 _extract_builds_from_listof 内按调用点就地展开）

# Step 4: Method name -> group name mapping
method_gname = {}
for m in re.finditer(
    r'private\s+(?:static\s+)?EnchantGroup\s+([a-zA-Z0-9_]+)\s*\([^)]*\)\s*\{[^}]*?return\s+new\s+EnchantGroup\s*\(\s*"([a-z0-9_]+)"',
    java_source, re.DOTALL
):
    method_gname[m.group(1)] = m.group(2)
for m in re.finditer(
    r'private\s+(?:static\s+)?EnchantGroup\s+([a-zA-Z0-9_]+)\s*\([^)]*\)\s*\{\s*return\s+[a-zA-Z0-9_]+\s*\([^"]*"([a-z0-9_]+)"',
    java_source, re.DOTALL
):
    method_gname[m.group(1)] = m.group(2)
changed = True
while changed:
    changed = False
    for m in re.finditer(
        r'private\s+(?:static\s+)?EnchantGroup\s+([a-zA-Z0-9_]+)\s*\([^)]*\)\s*\{\s*return\s+([a-zA-Z0-9_]+)\s*\(',
        java_source, re.DOTALL
    ):
        w, c2 = m.group(1), m.group(2)
        if w not in method_gname and c2 in method_gname:
            method_gname[w] = method_gname[c2]; changed = True

def _enclosing_method(source, pos):
    """返回 pos 所在的 private void buildXxx(...) 方法体文本。"""
    best = None
    for m in re.finditer(
        r'private\s+void\s+build[A-Z][a-zA-Z]+\s*\([^)]*\)\s*\{', source
    ):
        if m.start() <= pos:
            best = m
        else:
            break
    if best is None:
        return None
    open_at = best.end() - 1
    close = _paren_close(source, open_at)
    return source[best.start():close + 1]


def _resolve_method_build(method_name):
    """解析返回 EnchantGroup 的方法体，返回 frozenset 或 None。
    支持: return new EnchantGroup("name", List.of/nonNull(...e()...)) 与
          return factory(l, "name", KEY, level, ...) 两类。"""
    mm = re.search(
        r'private\s+(?:static\s+)?EnchantGroup\s+' + re.escape(method_name) +
        r'\s*\([^)]*\)\s*\{\s*return\s+', java_source)
    if not mm:
        return None
    rest = java_source[mm.end():]
    # 分支 1：直接 return new EnchantGroup("name", List.of/nonNull(...))
    lit = re.match(r'new\s+EnchantGroup\s*\(\s*"([a-z0-9_]+)"\s*,\s*(?:List\.of|nonNull)\(', rest)
    if lit:
        lopen = mm.end() + lit.end() - 1
        lclose = _paren_close(java_source, lopen)
        enchants = _parse_e_calls(java_source[lopen + 1:lclose])
        return frozenset(enchants) if enchants else None
    # 分支 2：return factory(l, "name", ...)
    fac = re.match(r'([a-zA-Z0-9_]+)\s*\(([^)]*)\)\s*;', rest)
    if fac and fac.group(1) in factories:
        call_args = fac.group(2)
        keys = [k.lower() for k in re.findall(r'Enchantments\.([A-Z0-9_]+)', call_args)]
        levels = [int(v) for v in re.findall(r',\s*(\d+)\b', call_args)]
        pairs = list(zip(keys, levels))
        if len(levels) < len(keys):
            INFERRED = {'frost_walker': 2, 'depth_strider': 3, 'silk_touch': 1,
                        'protection': 4, 'fire_protection': 4,
                        'blast_protection': 4, 'projectile_protection': 4}
            merged, li = [], 0
            for k in keys:
                if li < len(levels) and k not in INFERRED:
                    merged.append((k, levels[li])); li += 1
                elif k in INFERRED:
                    merged.append((k, INFERRED[k]))
            pairs = merged
        return frozenset(factories[fac.group(1)] + pairs)
    return None


def _extract_builds_from_listof(inner):
    """Given the content inside List.of(...), extract frozensets for each element."""
    builds = set()
    # Split by top-level commas to get individual expressions
    parts, depth, last = [], 0, 0
    for i, c in enumerate(inner):
        if c == '(': depth += 1
        elif c == ')': depth -= 1
        elif c == ',' and depth == 0:
            parts.append(inner[last:i].strip()); last = i + 1
    parts.append(inner[last:].strip())
    for part in parts:
        if not part: continue
        # Literal EnchantGroup: 直接从 part 解析完整内容。
        # 注意同名组（helmet/chestplate/leggings 的 protection）内容不同，
        # 绝不能查全局组表（会被最后一个写入的同名组覆盖）
        lm = re.search(r'new\s+EnchantGroup\s*\(\s*"([a-z0-9_]+)"\s*,\s*(?:List\.of|nonNull)\(', part)
        if lm:
            plo = lm.end() - 1
            plc = _paren_close(part, plo)
            p_enchants = _parse_e_calls(part[plo + 1:plc])
            if p_enchants:
                builds.add(frozenset(p_enchants))
                continue
        # Factory call with name literal: axeGroup(l,"name",...) - expand in place
        for fname, static_e in factories.items():
            fm = re.search(
                fname + r'\s*\(\s*[^,]+\s*,\s*"([a-z0-9_]+)"\s*,\s*((?:[^)]+))\)', part)
            if fm:
                rem = fm.group(2)
                keys = [k.lower() for k in re.findall(r'Enchantments\.([A-Z0-9_]+)', rem)]
                levels = [int(v) for v in re.findall(r',\s*(\d+)\b', rem)]
                pairs = list(zip(keys, levels))
                if len(levels) < len(keys):
                    INFERRED = {'frost_walker': 2, 'depth_strider': 3, 'silk_touch': 1,
                                'protection': 4, 'fire_protection': 4,
                                'blast_protection': 4, 'projectile_protection': 4}
                    merged, li = [], 0
                    for k in keys:
                        if li < len(levels) and k not in INFERRED:
                            merged.append((k, levels[li])); li += 1
                        elif k in INFERRED:
                            merged.append((k, INFERRED[k]))
                    pairs = merged
                builds.add(frozenset(static_e + pairs))
                break
        # Method call: toolFortune(l), swordSharp(l), maceDensity(l) ...
        # 直接解析该方法体的 return 语句（同名组如 sharpness 在 sword/spear 间内容不同，
        # 不能走全局组表；必须按方法体解析）
        mm = re.search(r'\b([a-zA-Z0-9_]+)\s*\(', part)
        if mm:
            resolved = _resolve_method_build(mm.group(1))
            if resolved is not None:
                builds.add(resolved)
    return builds

# Step 5: Walk every build method body, resolve groups and items
for bm in re.finditer(
    r'private void build[A-Z][a-zA-Z]+\s*\([^)]*\)\s*\{(.*?)\n    \}',
    java_source, re.DOTALL
):
    body = bm.group(1)
    # Collect all local variable assignments
    # Single: EnchantGroup varname = expr;
    local_vars = {}
    for vm in re.finditer(r'\bEnchantGroup\s+([a-zA-Z0-9_]+)\s*=\s*(.+?);\s*\n', body, re.DOTALL):
        vname, expr = vm.group(1), vm.group(2)
        lm = re.search(r'new\s+EnchantGroup\s*\(\s*"([a-z0-9_]+)"', expr)
        if lm and lm.group(1) in all_groups:
            local_vars[vname] = frozenset([all_groups[lm.group(1)]])
            continue
        mm = re.search(r'\b([a-zA-Z0-9_]+)\s*\(', expr)
        if mm:
            mn = mm.group(1)
            if mn in method_gname and method_gname[mn] in all_groups:
                local_vars[vname] = frozenset([all_groups[method_gname[mn]]])
    # Group list: List<EnchantGroup> groups = List.of(...)
    group_lists = {}  # var_name -> set[frozenset]
    for vm in re.finditer(r'\bList\s*<EnchantGroup>\s+([a-zA-Z0-9_]+)\s*=\s*List\.of\(', body):
        vname = vm.group(1)
        lopen = vm.end() - 1
        lclose = _paren_close(body, lopen)
        inner = body[lopen+1:lclose]
        group_lists[vname] = _extract_builds_from_listof(inner)

    # Parse addRecords calls
    pos = 0
    while True:
        idx = body.find('addRecords(', pos)
        if idx == -1: break
        start = idx + len('addRecords(')
        close = _paren_close(body, start - 1)
        call = body[start:close]
        first = _top_comma(call)
        if first == -1: pos = close + 1; continue
        grp_arg, item_arg = call[:first].strip(), call[first+1:]
        item_ids = [mm.lower() for mm in re.findall(r'Items\.([A-Z0-9_]+)', item_arg)]
        if not item_ids: pos = close + 1; continue
        builds = set()
        if 'List.of(' in grp_arg:
            lo = grp_arg.find('List.of(')
            lo_open = lo + len('List.of(') - 1
            lo_close = _paren_close(grp_arg, lo_open)
            builds = _extract_builds_from_listof(grp_arg[lo_open+1:lo_close])
        elif grp_arg in group_lists:
            builds = group_lists[grp_arg]
        elif grp_arg in local_vars:
            builds = local_vars[grp_arg]
        for item_id in item_ids:
            java_item_builds.setdefault(item_id, set()).update(builds)
        pos = close + 1



# 形式 D：records.add(new ItemEnchantRecord(<item>, List.of(group1, group2)))
# <item> 可能是 Items.X 常量，也可能是 for 循环变量（mc121 等族的形态 E）
pos = 0
while True:
    idx = java_source.find('records.add(new ItemEnchantRecord(', pos)
    if idx == -1:
        break
    open_at = idx + len('records.add(') - 1
    close = _paren_close(java_source, open_at)
    call = java_source[open_at + 1:close]
    enclosing = _enclosing_method(java_source, open_at)
    # 第一个参数：Items.X 常量
    im = re.match(r'\s*new\s+ItemEnchantRecord\s*\(\s*Items\.([A-Z0-9_]+)\s*,', call)
    item_ids = []
    if im:
        item_ids = [im.group(1).lower()]
    else:
        # 形式 E：for (Item item : <listVar>) { records.add(new ItemEnchantRecord(item, ...)) }
        vm = re.match(r'\s*new\s+ItemEnchantRecord\s*\(\s*([a-zA-Z0-9_]+)\s*,', call)
        if vm and enclosing:
            # 找 for 循环头里的集合变量名
            loop_var = vm.group(1)
            fm = re.search(
                r'for\s*\(\s*Item\s+' + re.escape(loop_var) +
                r'\s*:\s*([a-zA-Z0-9_]+)\s*\)', enclosing)
            if fm:
                list_var = fm.group(1)
                lm = re.search(
                    r'List\s*<\s*Item\s*>\s+' + re.escape(list_var) +
                    r'\s*=\s*List\.of\(', enclosing)
                if lm:
                    lvo = lm.end() - 1
                    lvc = _paren_close(enclosing, lvo)
                    item_ids = [x.lower() for x in re.findall(
                        r'Items\.([A-Z0-9_]+)', enclosing[lvo + 1:lvc])]
    if not item_ids:
        pos = close + 1
        continue
    # 第二个参数：List.of(...) -- 用括号平衡定位（不依赖第一个参数的匹配对象）
    lst = re.search(r',\s*List\.of\(', call)
    if not lst:
        pos = close + 1
        continue
    lo_open = lst.end() - 1
    lo_close = _paren_close(call, lo_open)
    inner = call[lo_open + 1:lo_close]
    builds = _extract_builds_from_listof(inner)
    # List.of(standard) 等局部变量引用：找到调用点所在的 build 方法体，解析其局部变量
    for var_m in re.finditer(r'\b([a-zA-Z0-9_]+)\b', inner):
        vname = var_m.group(1)
        if vname in ('List', 'of'):
            continue
        if not re.search(r'\b' + re.escape(vname) + r'\s*\(', inner):
            # 是变量引用而非方法调用：定位所在方法体
            enclosing = _enclosing_method(java_source, open_at)
            if enclosing:
                vassign = re.search(
                    r'\bEnchantGroup\s+' + re.escape(vname) +
                    r'\s*=\s*new\s+EnchantGroup\s*\(\s*"([a-z0-9_]+)"\s*,\s*(?:List\.of|nonNull)\(',
                    enclosing)
                if vassign:
                    vlopen = vassign.end() - 1
                    vlclose = _paren_close(enclosing, vlopen)
                    venchants = _parse_e_calls(enclosing[vlopen + 1:vlclose])
                    if venchants:
                        builds.add(frozenset(venchants))
    for item_id in item_ids:
        java_item_builds.setdefault(item_id, set()).update(builds)
    pos = close + 1


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


# ========== 检查 0：Java 分组内容与 spec 一致（闭环验证）==========
print("【检查 0/5】Java 分组内容与 spec 一致（源码 ↔ 规格闭环）...")
for item_id, spec_groups in all_items.items():
    # 将 spec 的方案列表转为 set[frozenset]
    spec_builds = {frozenset(enchants) for _, enchants in spec_groups}
    java_builds = java_item_builds.get(item_id, set())

    # 过滤空方案（curse-only 物品的占位标记）
    spec_builds = {b for b in spec_builds if b}
    java_builds = {b for b in java_builds if b}

    if spec_builds != java_builds:
        only_spec = spec_builds - java_builds
        only_java = java_builds - spec_builds
        msg = f"{item_id}: Java 方案集合与 spec 不一致"
        if only_spec:
            msg += f"\n  仅在 spec: {sorted([sorted(b) for b in only_spec])}"
        if only_java:
            msg += f"\n  仅在 Java: {sorted([sorted(b) for b in only_java])}"
        errors.append(msg)

# 检查 Java 中注册但 spec 未涵盖的物品
java_only_items = set(java_item_builds.keys()) - configured_items
if java_only_items:
    errors.append(f"Java 源码注册了 spec 未涵盖的物品：{sorted(java_only_items)}")

print("  完成")


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
