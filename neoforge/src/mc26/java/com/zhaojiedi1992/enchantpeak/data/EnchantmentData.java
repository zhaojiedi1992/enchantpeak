package com.zhaojiedi1992.enchantpeak.data;

import com.zhaojiedi1992.enchantpeak.common.EnchantEntry;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.List;

/**
 * 原版附魔顶配数据表（MC 26.2）。
 *
 * <p>所有附魔等级、互斥关系、适用物品均严格依据 Minecraft 客户端 jar 内置的官方 datapack
 * （{@code data/minecraft/enchantment/*.json} 和 {@code data/minecraft/tags/enchantment/exclusive_set/*.json}），
 * 已逐项核对，确保与原版完全一致。
 *
 * <h2>互斥关系（exclusive_set，同一组内只能选一个）</h2>
 * <ul>
 *   <li><b>damage 组</b>：sharpness / smite / bane_of_arthropods / impaling / density / breach（剑/三叉戟/重锤伤害附魔六选一）</li>
 *   <li><b>armor 组</b>：protection / fire_protection / blast_protection / projectile_protection（防具保护四选一）</li>
 *   <li><b>mining 组</b>：fortune / silk_touch（时运与精准采集二选一）</li>
 *   <li><b>bow 组</b>：infinity / mending（弓的无限与修补二选一，仅限弓）</li>
 *   <li><b>crossbow 组</b>：piercing / multishot（弩的穿透与多重二选一）</li>
 *   <li><b>boots 组</b>：depth_strider / frost_walker（靴子的深海探索者与冰霜行者二选一）</li>
 *   <li><b>riptide 组</b>：riptide 与 {loyalty, channeling}（激流与忠诚/引雷互斥，但忠诚与引雷可共存）</li>
 * </ul>
 */
public class EnchantmentData {

    private final List<ItemEnchantRecord> records = new ArrayList<>();

    public EnchantmentData(RegistryAccess registryAccess) {
        HolderLookup.RegistryLookup<Enchantment> lookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        buildAll(lookup);
    }

    private static EnchantEntry e(HolderLookup.RegistryLookup<Enchantment> l, ResourceKey<Enchantment> key, int level) {
        java.util.Optional<Holder.Reference<Enchantment>> holder = l.get(key);
        if (holder.isEmpty()) {
            com.zhaojiedi1992.enchantpeak.EnchantPeakMod.LOGGER.warn(
                    "[EnchantPeak] 官方附魔 {} 在当前注册表中缺失（可能被数据包移除），包含它的方案将被跳过",
                    key);
            return null;
        }
        return new EnchantEntry(holder.get(), level);
    }

    /** 容错：过滤被数据包移除的附魔（e() 返回 null），避免单个附魔缺失让整个模组失效。 */
    private static List<EnchantEntry> nonNull(EnchantEntry... entries) {
        List<EnchantEntry> valid = new ArrayList<>(entries.length);
        for (EnchantEntry entry : entries) {
            if (entry != null) {
                valid.add(entry);
            }
        }
        return valid;
    }


    private void buildAll(HolderLookup.RegistryLookup<Enchantment> l) {
        // 工具与近战武器（全部原版材质）
        buildPickaxes(l);
        buildAxes(l);
        buildShovels(l);
        buildHoes(l);
        buildSwords(l);
        buildMaces(l);
        buildSpears(l);
        // 远程 / 投掷武器（无材质区分，单一物品）
        buildBows(l);
        buildCrossbows(l);
        buildTridents(l);
        buildFishingRods(l);
        // 防具（全部原版材质，头盔额外包含海龟壳）
        buildHelmets(l);
        buildChestplates(l);
        buildLeggings(l);
        buildBoots(l);
        // 实用道具
        buildElytra(l);
        buildShields(l);
        buildShears(l);
        buildUtilityItems(l);
        buildCurseOnlyItems();
    }

    private void addRecords(List<EnchantGroup> groups, Item... items) {
        for (Item item : items) {
            records.add(new ItemEnchantRecord(item, groups));
        }
    }

    // ==================== 工具 ====================
    // 镐/铲/锄：时运流 vs 精准流（mining 互斥组），工具类共通：效率 V + 耐久 III + 修补 I

    private void buildPickaxes(HolderLookup.RegistryLookup<Enchantment> l) {
        addRecords(List.of(toolFortune(l), toolSilk(l)),
                Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.COPPER_PICKAXE, Items.IRON_PICKAXE,
                Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
    }

    private void buildShovels(HolderLookup.RegistryLookup<Enchantment> l) {
        addRecords(List.of(toolFortune(l), toolSilk(l)),
                Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.COPPER_SHOVEL, Items.IRON_SHOVEL,
                Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);
    }

    private void buildHoes(HolderLookup.RegistryLookup<Enchantment> l) {
        addRecords(List.of(toolFortune(l), toolSilk(l)),
                Items.WOODEN_HOE, Items.STONE_HOE, Items.COPPER_HOE, Items.IRON_HOE,
                Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE);
    }

    /** 工具时运流：效率 V + 时运 III + 耐久 III + 修补 I */
    private static EnchantGroup toolFortune(HolderLookup.RegistryLookup<Enchantment> l) {
        return new EnchantGroup("fortune", nonNull(
                e(l, Enchantments.EFFICIENCY, 5),
                e(l, Enchantments.FORTUNE, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    /** 工具精准流：效率 V + 精准采集 I + 耐久 III + 修补 I */
    private static EnchantGroup toolSilk(HolderLookup.RegistryLookup<Enchantment> l) {
        return new EnchantGroup("silk_touch", nonNull(
                e(l, Enchantments.EFFICIENCY, 5),
                e(l, Enchantments.SILK_TOUCH, 1),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    // ==================== 斧子 ====================
    // 斧子同时属于 mining_loot（fortune/silk_touch 二选一）和 sharp_weapon（sharpness/smite/
    // bane_of_arthropods 三选一），两个互斥组互相独立，共 2×3=6 种组合
    // 通用：效率 V + 耐久 III + 修补 I

    private void buildAxes(HolderLookup.RegistryLookup<Enchantment> l) {
        List<EnchantGroup> groups = List.of(
                axeGroup(l, "axe_fortune_sharpness", Enchantments.FORTUNE, 3, Enchantments.SHARPNESS, 5),
                axeGroup(l, "axe_fortune_smite", Enchantments.FORTUNE, 3, Enchantments.SMITE, 5),
                axeGroup(l, "axe_fortune_bane", Enchantments.FORTUNE, 3, Enchantments.BANE_OF_ARTHROPODS, 5),
                axeGroup(l, "axe_silk_sharpness", Enchantments.SILK_TOUCH, 1, Enchantments.SHARPNESS, 5),
                axeGroup(l, "axe_silk_smite", Enchantments.SILK_TOUCH, 1, Enchantments.SMITE, 5),
                axeGroup(l, "axe_silk_bane", Enchantments.SILK_TOUCH, 1, Enchantments.BANE_OF_ARTHROPODS, 5)
        );
        addRecords(groups, Items.WOODEN_AXE, Items.STONE_AXE, Items.COPPER_AXE, Items.IRON_AXE,
                Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
    }

    private static EnchantGroup axeGroup(HolderLookup.RegistryLookup<Enchantment> l, String name,
                                         ResourceKey<Enchantment> miningType, int miningLevel,
                                         ResourceKey<Enchantment> damageType, int damageLevel) {
        return new EnchantGroup(name, nonNull(
                e(l, Enchantments.EFFICIENCY, 5),
                e(l, miningType, miningLevel),
                e(l, damageType, damageLevel),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    // ==================== 剑（全部原版材质）====================
    // 伤害附魔 damage 组三选一：锋利 / 亡灵杀手 / 节肢杀手
    // 其余可叠加：击退 II、火焰附加 II、抢夺 III、横扫之刃 III、耐久 III、修补 I

    private void buildSwords(HolderLookup.RegistryLookup<Enchantment> l) {
        List<EnchantGroup> groups = List.of(swordSharp(l), swordSmite(l), swordArthropods(l));
        addRecords(groups, Items.WOODEN_SWORD, Items.STONE_SWORD, Items.COPPER_SWORD, Items.IRON_SWORD,
                Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
    }

    /** 剑·锋利流：通用伤害最高，对亡灵/节肢也有基础加成 */
    private static EnchantGroup swordSharp(HolderLookup.RegistryLookup<Enchantment> l) {
        return new EnchantGroup("sharpness", nonNull(
                e(l, Enchantments.SHARPNESS, 5),
                e(l, Enchantments.KNOCKBACK, 2),
                e(l, Enchantments.FIRE_ASPECT, 2),
                e(l, Enchantments.LOOTING, 3),
                e(l, Enchantments.SWEEPING_EDGE, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    /** 剑·亡灵杀手流：对亡灵生物（僵尸/骷髅等）伤害最高 */
    private static EnchantGroup swordSmite(HolderLookup.RegistryLookup<Enchantment> l) {
        return new EnchantGroup("smite", nonNull(
                e(l, Enchantments.SMITE, 5),
                e(l, Enchantments.KNOCKBACK, 2),
                e(l, Enchantments.FIRE_ASPECT, 2),
                e(l, Enchantments.LOOTING, 3),
                e(l, Enchantments.SWEEPING_EDGE, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    /** 剑·节肢杀手流：对节肢生物（蜘蛛/蠹虫等）伤害最高 */
    private static EnchantGroup swordArthropods(HolderLookup.RegistryLookup<Enchantment> l) {
        return new EnchantGroup("bane", nonNull(
                e(l, Enchantments.BANE_OF_ARTHROPODS, 5),
                e(l, Enchantments.KNOCKBACK, 2),
                e(l, Enchantments.FIRE_ASPECT, 2),
                e(l, Enchantments.LOOTING, 3),
                e(l, Enchantments.SWEEPING_EDGE, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    // ==================== 重锤（Mace，单一材质）====================
    // damage 组在重锤上实际可选 4 个（比剑/长矛多，因为 mace 同时在 weapon tag 和 mace 专属 tag 里）：
    //   smite / bane_of_arthropods / density / breach 四选一（sharpness 不适用于 mace，已排除）
    // 通用可叠加：fire_aspect II、wind_burst III、耐久 III、修补 I
    // 不可附：knockback / looting / sweeping_edge（mace 不在 melee_weapon / sweeping tag）

    private void buildMaces(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.MACE, List.of(
                maceDensity(l), maceBreach(l), maceSmite(l), maceArthropods(l)
        )));
    }

    /** 重锤·密度流：下落攻击伤害最高（density V）*/
    private static EnchantGroup maceDensity(HolderLookup.RegistryLookup<Enchantment> l) {
        return maceGroup(l, "density", Enchantments.DENSITY, 5);
    }

    /** 重锤·破甲流：无视护甲（breach IV）*/
    private static EnchantGroup maceBreach(HolderLookup.RegistryLookup<Enchantment> l) {
        return maceGroup(l, "breach", Enchantments.BREACH, 4);
    }

    /** 重锤·亡灵杀手流：对亡灵生物伤害最高（smite V）*/
    private static EnchantGroup maceSmite(HolderLookup.RegistryLookup<Enchantment> l) {
        return maceGroup(l, "smite", Enchantments.SMITE, 5);
    }

    /** 重锤·节肢杀手流：对节肢生物伤害最高（bane_of_arthropods V）*/
    private static EnchantGroup maceArthropods(HolderLookup.RegistryLookup<Enchantment> l) {
        return maceGroup(l, "bane", Enchantments.BANE_OF_ARTHROPODS, 5);
    }

    private static EnchantGroup maceGroup(HolderLookup.RegistryLookup<Enchantment> l, String name,
                                          ResourceKey<Enchantment> damageType, int damageLevel) {
        return new EnchantGroup(name, nonNull(
                e(l, damageType, damageLevel),
                e(l, Enchantments.FIRE_ASPECT, 2),
                e(l, Enchantments.WIND_BURST, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    // ==================== 长矛（全部原版材质）====================
    // lunge III 专属；长矛在 melee_weapon tag（可附 knockback/looting），在 sharp_weapon tag（可附锋利等 damage 组）
    // 但长矛不在 sweeping tag（不可附横扫之刃），在 fire_aspect tag（可附火焰附加）

    private void buildSpears(HolderLookup.RegistryLookup<Enchantment> l) {
        List<EnchantGroup> groups = List.of(spearSharp(l), spearSmite(l), spearArthropods(l));
        addRecords(groups, Items.WOODEN_SPEAR, Items.STONE_SPEAR, Items.COPPER_SPEAR, Items.IRON_SPEAR,
                Items.GOLDEN_SPEAR, Items.DIAMOND_SPEAR, Items.NETHERITE_SPEAR);
    }

    private static EnchantGroup spearSharp(HolderLookup.RegistryLookup<Enchantment> l) {
        return new EnchantGroup("sharpness", nonNull(
                e(l, Enchantments.SHARPNESS, 5),
                e(l, Enchantments.LUNGE, 3),
                e(l, Enchantments.KNOCKBACK, 2),
                e(l, Enchantments.FIRE_ASPECT, 2),
                e(l, Enchantments.LOOTING, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    private static EnchantGroup spearSmite(HolderLookup.RegistryLookup<Enchantment> l) {
        return new EnchantGroup("smite", nonNull(
                e(l, Enchantments.SMITE, 5),
                e(l, Enchantments.LUNGE, 3),
                e(l, Enchantments.KNOCKBACK, 2),
                e(l, Enchantments.FIRE_ASPECT, 2),
                e(l, Enchantments.LOOTING, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    private static EnchantGroup spearArthropods(HolderLookup.RegistryLookup<Enchantment> l) {
        return new EnchantGroup("bane", nonNull(
                e(l, Enchantments.BANE_OF_ARTHROPODS, 5),
                e(l, Enchantments.LUNGE, 3),
                e(l, Enchantments.KNOCKBACK, 2),
                e(l, Enchantments.FIRE_ASPECT, 2),
                e(l, Enchantments.LOOTING, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    // ==================== 弓 ====================
    // 无限流 vs 修补流（bow 互斥组）：infinity 与 mending 互斥

    private void buildBows(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup infinite = new EnchantGroup("infinity", nonNull(
                e(l, Enchantments.POWER, 5),
                e(l, Enchantments.PUNCH, 2),
                e(l, Enchantments.FLAME, 1),
                e(l, Enchantments.INFINITY, 1),
                e(l, Enchantments.UNBREAKING, 3)
        ));
        EnchantGroup mending = new EnchantGroup("mending", nonNull(
                e(l, Enchantments.POWER, 5),
                e(l, Enchantments.PUNCH, 2),
                e(l, Enchantments.FLAME, 1),
                e(l, Enchantments.MENDING, 1),
                e(l, Enchantments.UNBREAKING, 3)
        ));
        records.add(new ItemEnchantRecord(Items.BOW, List.of(infinite, mending)));
    }

    // ==================== 弩 ====================
    // 穿透流 vs 多重流（crossbow 互斥组）

    private void buildCrossbows(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup piercing = new EnchantGroup("piercing", nonNull(
                e(l, Enchantments.PIERCING, 4),
                e(l, Enchantments.QUICK_CHARGE, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
        EnchantGroup multishot = new EnchantGroup("multishot", nonNull(
                e(l, Enchantments.MULTISHOT, 1),
                e(l, Enchantments.QUICK_CHARGE, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.CROSSBOW, List.of(piercing, multishot)));
    }

    // ==================== 三叉戟 ====================
    // riptide 与 {loyalty, channeling} 互斥（riptide 互斥组）
    // loyalty 与 channeling 可共存（无互斥声明）
    // impaling 属 damage 组（与 sharpness/smite 等互斥，但三叉戟只能附 impaling，无冲突）

    private void buildTridents(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup loyalty = new EnchantGroup("loyalty", nonNull(
                e(l, Enchantments.LOYALTY, 3),
                e(l, Enchantments.CHANNELING, 1),
                e(l, Enchantments.IMPALING, 5),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
        EnchantGroup riptide = new EnchantGroup("riptide", nonNull(
                e(l, Enchantments.RIPTIDE, 3),
                e(l, Enchantments.IMPALING, 5),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.TRIDENT, List.of(loyalty, riptide)));
    }

    // ==================== 钓鱼竿 ====================

    private void buildFishingRods(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup standard = new EnchantGroup("full_build", nonNull(
                e(l, Enchantments.LUCK_OF_THE_SEA, 3),
                e(l, Enchantments.LURE, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.FISHING_ROD, List.of(standard)));
    }

    // ==================== 实用道具 ====================
    // 鞘翅 / 盾：均不在 armor tag，无法附保护类/荆棘；仅支持耐久 III + 修补 I（durability 组）

    private void buildElytra(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.ELYTRA, List.of(utilityGroup(l))));
    }

    private void buildShields(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.SHIELD, List.of(utilityGroup(l))));
    }

    /** 实用道具满配流：耐久 III + 修补 I（鞘翅/盾均只支持这两个非诅咒附魔）*/
    private static EnchantGroup utilityGroup(HolderLookup.RegistryLookup<Enchantment> l) {
        return new EnchantGroup("full_build", nonNull(
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    // ==================== 剪刀 ====================
    // 剪刀在 #minecraft:enchantable/mining 标签（可附效率），也在 #minecraft:enchantable/durability（耐久+修补）
    // 不在 mining_loot（无时运/精准），只有单一满配方案

    private void buildShears(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup standard = new EnchantGroup("full_build", nonNull(
                e(l, Enchantments.EFFICIENCY, 5),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.SHEARS, List.of(standard)));
    }

    /** 仅支持耐久与修补的其他原版耐久物品。 */
    private void buildUtilityItems(HolderLookup.RegistryLookup<Enchantment> l) {
        addRecords(List.of(utilityGroup(l)), Items.BRUSH, Items.FLINT_AND_STEEL,
                Items.CARROT_ON_A_STICK, Items.WARPED_FUNGUS_ON_A_STICK);
    }

    /** 官方仅允许附加绑定/消失诅咒的物品，不推荐任何诅咒。 */
    private void buildCurseOnlyItems() {
        EnchantGroup noPositiveEnchantment = new EnchantGroup("no_positive_enchantments", nonNull());
        addRecords(List.of(noPositiveEnchantment), Items.CARVED_PUMPKIN, Items.COMPASS,
                Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.PIGLIN_HEAD, Items.PLAYER_HEAD,
                Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.ZOMBIE_HEAD);
    }

    // ==================== 防具（全部原版材质）====================
    // armor 互斥组四选一：protection / fire_protection / blast_protection / projectile_protection
    // 因此每件防具提供 4 个保护流派

    private void buildHelmets(HolderLookup.RegistryLookup<Enchantment> l) {
        List<EnchantGroup> groups = List.of(
                helmetProtection(l), helmetFireProtection(l),
                helmetBlastProtection(l), helmetProjectileProtection(l)
        );
        addRecords(groups, Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, Items.COPPER_HELMET,
                Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.DIAMOND_HELMET,
                Items.NETHERITE_HELMET, Items.TURTLE_HELMET);
    }

    private void buildChestplates(HolderLookup.RegistryLookup<Enchantment> l) {
        List<EnchantGroup> groups = List.of(
                chestplateProtection(l), chestplateFireProtection(l),
                chestplateBlastProtection(l), chestplateProjectileProtection(l)
        );
        addRecords(groups, Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.COPPER_CHESTPLATE,
                Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE,
                Items.NETHERITE_CHESTPLATE);
    }

    private void buildLeggings(HolderLookup.RegistryLookup<Enchantment> l) {
        List<EnchantGroup> groups = List.of(
                leggingsProtection(l), leggingsFireProtection(l),
                leggingsBlastProtection(l), leggingsProjectileProtection(l)
        );
        addRecords(groups, Items.LEATHER_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.COPPER_LEGGINGS,
                Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS,
                Items.NETHERITE_LEGGINGS);
    }

    private void buildBoots(HolderLookup.RegistryLookup<Enchantment> l) {
        // 靴子额外有 boots 互斥组：depth_strider vs frost_walker
        // 因此靴子 = 4（armor） × 2（boots） = 8 个流派
        List<EnchantGroup> groups = List.of(
                bootsProtectionDepth(l), bootsProtectionFrost(l),
                bootsFireProtectionDepth(l), bootsFireProtectionFrost(l),
                bootsBlastProtectionDepth(l), bootsBlastProtectionFrost(l),
                bootsProjectileProtectionDepth(l), bootsProjectileProtectionFrost(l)
        );
        addRecords(groups, Items.LEATHER_BOOTS, Items.CHAINMAIL_BOOTS, Items.COPPER_BOOTS,
                Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);
    }

    // ---- 头盔流派（额外：水下呼吸 III + 水下速掘 I）----
    private static EnchantGroup helmetProtection(HolderLookup.RegistryLookup<Enchantment> l) {
        return new EnchantGroup("protection", nonNull(
                e(l, Enchantments.PROTECTION, 4), e(l, Enchantments.RESPIRATION, 3),
                e(l, Enchantments.AQUA_AFFINITY, 1), e(l, Enchantments.THORNS, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
    }
    private static EnchantGroup helmetFireProtection(HolderLookup.RegistryLookup<Enchantment> l) {
        return new EnchantGroup("fire_protection", nonNull(
                e(l, Enchantments.FIRE_PROTECTION, 4), e(l, Enchantments.RESPIRATION, 3),
                e(l, Enchantments.AQUA_AFFINITY, 1), e(l, Enchantments.THORNS, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
    }
    private static EnchantGroup helmetBlastProtection(HolderLookup.RegistryLookup<Enchantment> l) {
        return new EnchantGroup("blast_protection", nonNull(
                e(l, Enchantments.BLAST_PROTECTION, 4), e(l, Enchantments.RESPIRATION, 3),
                e(l, Enchantments.AQUA_AFFINITY, 1), e(l, Enchantments.THORNS, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
    }
    private static EnchantGroup helmetProjectileProtection(HolderLookup.RegistryLookup<Enchantment> l) {
        return new EnchantGroup("projectile_protection", nonNull(
                e(l, Enchantments.PROJECTILE_PROTECTION, 4), e(l, Enchantments.RESPIRATION, 3),
                e(l, Enchantments.AQUA_AFFINITY, 1), e(l, Enchantments.THORNS, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
    }

    // ---- 胸甲流派 ----
    private static EnchantGroup chestplateProtection(HolderLookup.RegistryLookup<Enchantment> l) {
        return armorGroup(l, "protection", Enchantments.PROTECTION);
    }
    private static EnchantGroup chestplateFireProtection(HolderLookup.RegistryLookup<Enchantment> l) {
        return armorGroup(l, "fire_protection", Enchantments.FIRE_PROTECTION);
    }
    private static EnchantGroup chestplateBlastProtection(HolderLookup.RegistryLookup<Enchantment> l) {
        return armorGroup(l, "blast_protection", Enchantments.BLAST_PROTECTION);
    }
    private static EnchantGroup chestplateProjectileProtection(HolderLookup.RegistryLookup<Enchantment> l) {
        return armorGroup(l, "projectile_protection", Enchantments.PROJECTILE_PROTECTION);
    }

    // ---- 护腿流派（额外：迅捷潜行 III，leg_armor 专属）----
    private static EnchantGroup leggingsProtection(HolderLookup.RegistryLookup<Enchantment> l) {
        return leggingsGroup(l, "protection", Enchantments.PROTECTION);
    }
    private static EnchantGroup leggingsFireProtection(HolderLookup.RegistryLookup<Enchantment> l) {
        return leggingsGroup(l, "fire_protection", Enchantments.FIRE_PROTECTION);
    }
    private static EnchantGroup leggingsBlastProtection(HolderLookup.RegistryLookup<Enchantment> l) {
        return leggingsGroup(l, "blast_protection", Enchantments.BLAST_PROTECTION);
    }
    private static EnchantGroup leggingsProjectileProtection(HolderLookup.RegistryLookup<Enchantment> l) {
        return leggingsGroup(l, "projectile_protection", Enchantments.PROJECTILE_PROTECTION);
    }

    /** 胸甲通用：保护类 IV + 荆棘 III + 耐久 III + 修补 I */
    private static EnchantGroup armorGroup(HolderLookup.RegistryLookup<Enchantment> l, String name, ResourceKey<Enchantment> protectionType) {
        return new EnchantGroup(name, nonNull(
                e(l, protectionType, 4),
                e(l, Enchantments.THORNS, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    /** 护腿通用：保护类 IV + 迅捷潜行 III + 荆棘 III + 耐久 III + 修补 I */
    private static EnchantGroup leggingsGroup(HolderLookup.RegistryLookup<Enchantment> l, String name, ResourceKey<Enchantment> protectionType) {
        return new EnchantGroup(name, nonNull(
                e(l, protectionType, 4),
                e(l, Enchantments.SWIFT_SNEAK, 3),
                e(l, Enchantments.THORNS, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    // ---- 靴子流派（额外：摔落保护 IV，foot_armor 专属；以及 boots 互斥组的 depth_strider / frost_walker）----
    private static EnchantGroup bootsProtectionDepth(HolderLookup.RegistryLookup<Enchantment> l) {
        return bootsGroup(l, "protection_depth", Enchantments.PROTECTION, Enchantments.DEPTH_STRIDER);
    }
    private static EnchantGroup bootsProtectionFrost(HolderLookup.RegistryLookup<Enchantment> l) {
        return bootsGroup(l, "protection_frost", Enchantments.PROTECTION, Enchantments.FROST_WALKER);
    }
    private static EnchantGroup bootsFireProtectionDepth(HolderLookup.RegistryLookup<Enchantment> l) {
        return bootsGroup(l, "fire_protection_depth", Enchantments.FIRE_PROTECTION, Enchantments.DEPTH_STRIDER);
    }
    private static EnchantGroup bootsFireProtectionFrost(HolderLookup.RegistryLookup<Enchantment> l) {
        return bootsGroup(l, "fire_protection_frost", Enchantments.FIRE_PROTECTION, Enchantments.FROST_WALKER);
    }
    private static EnchantGroup bootsBlastProtectionDepth(HolderLookup.RegistryLookup<Enchantment> l) {
        return bootsGroup(l, "blast_protection_depth", Enchantments.BLAST_PROTECTION, Enchantments.DEPTH_STRIDER);
    }
    private static EnchantGroup bootsBlastProtectionFrost(HolderLookup.RegistryLookup<Enchantment> l) {
        return bootsGroup(l, "blast_protection_frost", Enchantments.BLAST_PROTECTION, Enchantments.FROST_WALKER);
    }
    private static EnchantGroup bootsProjectileProtectionDepth(HolderLookup.RegistryLookup<Enchantment> l) {
        return bootsGroup(l, "projectile_protection_depth", Enchantments.PROJECTILE_PROTECTION, Enchantments.DEPTH_STRIDER);
    }
    private static EnchantGroup bootsProjectileProtectionFrost(HolderLookup.RegistryLookup<Enchantment> l) {
        return bootsGroup(l, "projectile_protection_frost", Enchantments.PROJECTILE_PROTECTION, Enchantments.FROST_WALKER);
    }

    /**
     * 靴子通用：保护类 IV + 摔落保护 IV + 灵魂疾行 III + 移动类（depth_strider III 或 frost_walker II）
     * + 荆棘 III + 耐久 III + 修补 I
     */
    private static EnchantGroup bootsGroup(HolderLookup.RegistryLookup<Enchantment> l, String name,
                                           ResourceKey<Enchantment> protectionType,
                                           ResourceKey<Enchantment> movementType) {
        int movementLevel = movementType == Enchantments.FROST_WALKER ? 2 : 3;
        return new EnchantGroup(name, nonNull(
                e(l, protectionType, 4),
                e(l, Enchantments.FEATHER_FALLING, 4),
                e(l, Enchantments.SOUL_SPEED, 3),
                e(l, movementType, movementLevel),
                e(l, Enchantments.THORNS, 3),
                e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
    }

    public List<ItemEnchantRecord> getAllRecords() {
        return records;
    }

    public static List<ItemEnchantRecord> getAllRecords(RegistryAccess registryAccess) {
        return new EnchantmentData(registryAccess).getAllRecords();
    }
}
