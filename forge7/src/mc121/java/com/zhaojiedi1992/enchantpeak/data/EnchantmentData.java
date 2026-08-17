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
 * 原版附魔顶配数据表（MC 1.21.1）。
 *
 * <p>1.21.1 特性：有 Mace（重锤），无 Spear（矛）/ Copper 工具（26.x 新增）。
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
        // 工具与近战武器
        buildPickaxes(l);
        buildAxes(l);
        buildShovels(l);
        buildHoes(l);
        buildSwords(l);
        buildMaces(l);  // 1.21 新增
        // 远程武器
        buildBows(l);
        buildCrossbows(l);
        buildTridents(l);
        // 防具
        buildHelmet(l);
        buildChestplate(l);
        buildLeggings(l);
        buildBoots(l);
        // 盾牌与其他
        buildShields(l);
        buildElytra(l);
        buildCarrotStick(l);
        buildWarpedFungusStick(l);
        buildFishingRods(l);
        buildBrushes(l);
        buildShears(l);
        buildUtilityItems(l);
        buildCurseOnlyItems();
    }

    // ========== 工具 ==========

    private void buildPickaxes(HolderLookup.RegistryLookup<Enchantment> l) {
        List<Item> pickaxes = List.of(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE,
                Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
        for (Item item : pickaxes) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("fortune", nonNull(e(l, Enchantments.FORTUNE, 3), e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("silk_touch", nonNull(e(l, Enchantments.SILK_TOUCH, 1), e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildAxes(HolderLookup.RegistryLookup<Enchantment> l) {
        List<Item> axes = List.of(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE,
                Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
        for (Item item : axes) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("axe_fortune_sharpness", nonNull(e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.FORTUNE, 3), e(l, Enchantments.SHARPNESS, 5), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("axe_fortune_smite", nonNull(e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.FORTUNE, 3), e(l, Enchantments.SMITE, 5), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("axe_fortune_bane", nonNull(e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.FORTUNE, 3), e(l, Enchantments.BANE_OF_ARTHROPODS, 5), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("axe_silk_sharpness", nonNull(e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.SILK_TOUCH, 1), e(l, Enchantments.SHARPNESS, 5), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("axe_silk_smite", nonNull(e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.SILK_TOUCH, 1), e(l, Enchantments.SMITE, 5), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("axe_silk_bane", nonNull(e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.SILK_TOUCH, 1), e(l, Enchantments.BANE_OF_ARTHROPODS, 5), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildShovels(HolderLookup.RegistryLookup<Enchantment> l) {
        List<Item> shovels = List.of(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL,
                Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);
        for (Item item : shovels) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("fortune", nonNull(e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.FORTUNE, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("silk_touch", nonNull(e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.SILK_TOUCH, 1), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildHoes(HolderLookup.RegistryLookup<Enchantment> l) {
        List<Item> hoes = List.of(Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE,
                Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE);
        for (Item item : hoes) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("fortune", nonNull(e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.FORTUNE, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("silk_touch", nonNull(e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.SILK_TOUCH, 1), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildSwords(HolderLookup.RegistryLookup<Enchantment> l) {
        List<Item> swords = List.of(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD,
                Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
        for (Item item : swords) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("sharpness", nonNull(e(l, Enchantments.SHARPNESS, 5), e(l, Enchantments.KNOCKBACK, 2), e(l, Enchantments.FIRE_ASPECT, 2), e(l, Enchantments.LOOTING, 3), e(l, Enchantments.SWEEPING_EDGE, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("smite", nonNull(e(l, Enchantments.SMITE, 5), e(l, Enchantments.KNOCKBACK, 2), e(l, Enchantments.FIRE_ASPECT, 2), e(l, Enchantments.LOOTING, 3), e(l, Enchantments.SWEEPING_EDGE, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("bane", nonNull(e(l, Enchantments.BANE_OF_ARTHROPODS, 5), e(l, Enchantments.KNOCKBACK, 2), e(l, Enchantments.FIRE_ASPECT, 2), e(l, Enchantments.LOOTING, 3), e(l, Enchantments.SWEEPING_EDGE, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildMaces(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.MACE, List.of(
                new EnchantGroup("density", nonNull(e(l, Enchantments.DENSITY, 5), e(l, Enchantments.FIRE_ASPECT, 2), e(l, Enchantments.WIND_BURST, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                new EnchantGroup("breach", nonNull(e(l, Enchantments.BREACH, 4), e(l, Enchantments.FIRE_ASPECT, 2), e(l, Enchantments.WIND_BURST, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                new EnchantGroup("smite", nonNull(e(l, Enchantments.SMITE, 5), e(l, Enchantments.FIRE_ASPECT, 2), e(l, Enchantments.WIND_BURST, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                new EnchantGroup("bane", nonNull(e(l, Enchantments.BANE_OF_ARTHROPODS, 5), e(l, Enchantments.FIRE_ASPECT, 2), e(l, Enchantments.WIND_BURST, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
            )));
    }

    // ========== 远程武器 ==========

    private void buildBows(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.BOW, List.of(
                new EnchantGroup("infinity", nonNull(e(l, Enchantments.INFINITY, 1), e(l, Enchantments.POWER, 5), e(l, Enchantments.FLAME, 1), e(l, Enchantments.PUNCH, 2), e(l, Enchantments.UNBREAKING, 3))),
                new EnchantGroup("mending", nonNull(e(l, Enchantments.MENDING, 1), e(l, Enchantments.POWER, 5), e(l, Enchantments.FLAME, 1), e(l, Enchantments.PUNCH, 2), e(l, Enchantments.UNBREAKING, 3)))
        )));
    }

    private void buildCrossbows(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.CROSSBOW, List.of(
                new EnchantGroup("piercing", nonNull(e(l, Enchantments.PIERCING, 4), e(l, Enchantments.QUICK_CHARGE, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                new EnchantGroup("multishot", nonNull(e(l, Enchantments.MULTISHOT, 1), e(l, Enchantments.QUICK_CHARGE, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
        )));
    }

    private void buildTridents(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.TRIDENT, List.of(
                new EnchantGroup("loyalty", nonNull(e(l, Enchantments.LOYALTY, 3), e(l, Enchantments.CHANNELING, 1), e(l, Enchantments.IMPALING, 5), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                new EnchantGroup("riptide", nonNull(e(l, Enchantments.RIPTIDE, 3), e(l, Enchantments.IMPALING, 5), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
        )));
    }

    // ========== 防具 ==========

    private void buildHelmet(HolderLookup.RegistryLookup<Enchantment> l) {
        List<Item> helmets = List.of(Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET,
                Items.GOLDEN_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET, Items.TURTLE_HELMET);
        for (Item item : helmets) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("protection", nonNull(e(l, Enchantments.PROTECTION, 4), e(l, Enchantments.AQUA_AFFINITY, 1), e(l, Enchantments.RESPIRATION, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("fire_protection", nonNull(e(l, Enchantments.FIRE_PROTECTION, 4), e(l, Enchantments.AQUA_AFFINITY, 1), e(l, Enchantments.RESPIRATION, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("blast_protection", nonNull(e(l, Enchantments.BLAST_PROTECTION, 4), e(l, Enchantments.AQUA_AFFINITY, 1), e(l, Enchantments.RESPIRATION, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("projectile_protection", nonNull(e(l, Enchantments.PROJECTILE_PROTECTION, 4), e(l, Enchantments.AQUA_AFFINITY, 1), e(l, Enchantments.RESPIRATION, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildChestplate(HolderLookup.RegistryLookup<Enchantment> l) {
        List<Item> chestplates = List.of(Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE,
                Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE);
        for (Item item : chestplates) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("protection", nonNull(e(l, Enchantments.PROTECTION, 4), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("fire_protection", nonNull(e(l, Enchantments.FIRE_PROTECTION, 4), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("blast_protection", nonNull(e(l, Enchantments.BLAST_PROTECTION, 4), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("projectile_protection", nonNull(e(l, Enchantments.PROJECTILE_PROTECTION, 4), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildLeggings(HolderLookup.RegistryLookup<Enchantment> l) {
        List<Item> leggings = List.of(Items.LEATHER_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS,
                Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS);
        for (Item item : leggings) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("protection", nonNull(e(l, Enchantments.PROTECTION, 4), e(l, Enchantments.SWIFT_SNEAK, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("fire_protection", nonNull(e(l, Enchantments.FIRE_PROTECTION, 4), e(l, Enchantments.SWIFT_SNEAK, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("blast_protection", nonNull(e(l, Enchantments.BLAST_PROTECTION, 4), e(l, Enchantments.SWIFT_SNEAK, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("projectile_protection", nonNull(e(l, Enchantments.PROJECTILE_PROTECTION, 4), e(l, Enchantments.SWIFT_SNEAK, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildBoots(HolderLookup.RegistryLookup<Enchantment> l) {
        List<Item> boots = List.of(Items.LEATHER_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS,
                Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);
        for (Item item : boots) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("protection_depth", nonNull(e(l, Enchantments.PROTECTION, 4), e(l, Enchantments.DEPTH_STRIDER, 3), e(l, Enchantments.FEATHER_FALLING, 4), e(l, Enchantments.SOUL_SPEED, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("protection_frost", nonNull(e(l, Enchantments.PROTECTION, 4), e(l, Enchantments.FROST_WALKER, 2), e(l, Enchantments.FEATHER_FALLING, 4), e(l, Enchantments.SOUL_SPEED, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("fire_protection_depth", nonNull(e(l, Enchantments.FIRE_PROTECTION, 4), e(l, Enchantments.DEPTH_STRIDER, 3), e(l, Enchantments.FEATHER_FALLING, 4), e(l, Enchantments.SOUL_SPEED, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("fire_protection_frost", nonNull(e(l, Enchantments.FIRE_PROTECTION, 4), e(l, Enchantments.FROST_WALKER, 2), e(l, Enchantments.FEATHER_FALLING, 4), e(l, Enchantments.SOUL_SPEED, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("blast_protection_depth", nonNull(e(l, Enchantments.BLAST_PROTECTION, 4), e(l, Enchantments.DEPTH_STRIDER, 3), e(l, Enchantments.FEATHER_FALLING, 4), e(l, Enchantments.SOUL_SPEED, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("blast_protection_frost", nonNull(e(l, Enchantments.BLAST_PROTECTION, 4), e(l, Enchantments.FROST_WALKER, 2), e(l, Enchantments.FEATHER_FALLING, 4), e(l, Enchantments.SOUL_SPEED, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("projectile_protection_depth", nonNull(e(l, Enchantments.PROJECTILE_PROTECTION, 4), e(l, Enchantments.DEPTH_STRIDER, 3), e(l, Enchantments.FEATHER_FALLING, 4), e(l, Enchantments.SOUL_SPEED, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1))),
                    new EnchantGroup("projectile_protection_frost", nonNull(e(l, Enchantments.PROJECTILE_PROTECTION, 4), e(l, Enchantments.FROST_WALKER, 2), e(l, Enchantments.FEATHER_FALLING, 4), e(l, Enchantments.SOUL_SPEED, 3), e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
            )));
        }
    }

    // ========== 其他 ==========

    private void buildShields(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.SHIELD, List.of(
                new EnchantGroup("full_build", nonNull(e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
        )));
    }

    private void buildElytra(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.ELYTRA, List.of(
                new EnchantGroup("full_build", nonNull(e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
        )));
    }

    private void buildCarrotStick(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.CARROT_ON_A_STICK, List.of(
                new EnchantGroup("full_build", nonNull(e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
        )));
    }

    private void buildWarpedFungusStick(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.WARPED_FUNGUS_ON_A_STICK, List.of(
                new EnchantGroup("full_build", nonNull(e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
        )));
    }

    private void buildFishingRods(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.FISHING_ROD, List.of(
                new EnchantGroup("full_build", nonNull(e(l, Enchantments.LUCK_OF_THE_SEA, 3), e(l, Enchantments.LURE, 3), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
        )));
    }

    private void buildBrushes(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.BRUSH, List.of(
                new EnchantGroup("full_build", nonNull(e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
        )));
    }

    private void buildShears(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.SHEARS, List.of(
                new EnchantGroup("full_build", nonNull(e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
        )));
    }


    private void buildUtilityItems(HolderLookup.RegistryLookup<Enchantment> l) {
        records.add(new ItemEnchantRecord(Items.FLINT_AND_STEEL, List.of(
                new EnchantGroup("full_build", nonNull(e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)))
        )));
    }

    private void buildCurseOnlyItems() {
        records.add(new ItemEnchantRecord(Items.CARVED_PUMPKIN, List.of(
                new EnchantGroup("no_positive_enchantments", nonNull()))));
        records.add(new ItemEnchantRecord(Items.COMPASS, List.of(
                new EnchantGroup("no_positive_enchantments", nonNull()))));
        for (Item item : List.of(Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.PIGLIN_HEAD,
                Items.PLAYER_HEAD, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.ZOMBIE_HEAD)) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("no_positive_enchantments", nonNull()))));
        }
    }

    public List<ItemEnchantRecord> getAllRecords() {
        return records;
    }

    public static List<ItemEnchantRecord> getAllRecords(RegistryAccess registryAccess) {
        return new EnchantmentData(registryAccess).getAllRecords();
    }
}
