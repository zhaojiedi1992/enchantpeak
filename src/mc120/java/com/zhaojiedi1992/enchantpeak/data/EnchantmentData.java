package com.zhaojiedi1992.enchantpeak.data;

import com.zhaojiedi1992.enchantpeak.common.EnchantEntry;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.List;

/**
 * 原版附魔顶配数据表（MC 1.20.1）。
 *
 * <p>1.20.1 特性：Enchantments.XXX 是 Enchantment 实例（非 ResourceKey），
 * 使用 1.20 期前的旧字段名（ALL_DAMAGE_PROTECTION/BLOCK_EFFICIENCY 等）。
 * 有 SWIFT_SNEAK（1.19+）、有 BRUSH（1.20 新增）、无 MACE（1.21 新增）、
 * 无 Spear/Copper 工具（26.x 新增）。
 */
public class EnchantmentData {

    private final List<ItemEnchantRecord> records = new ArrayList<>();

    public EnchantmentData(RegistryAccess registryAccess) {
        buildAll();
    }

    private static EnchantEntry e(Enchantment ench, int level) {
        return new EnchantEntry(Holder.direct(ench), level);
    }

    private void buildAll() {
        buildPickaxes();
        buildAxes();
        buildShovels();
        buildHoes();
        buildSwords();
        buildBows();
        buildCrossbows();
        buildTridents();
        buildHelmet();
        buildChestplate();
        buildLeggings();
        buildBoots();
        buildShields();
        buildElytra();
        buildCarrotStick();
        buildWarpedFungusStick();
        buildFishingRods();
        buildBrushes();
        buildShears();
        buildUtilityItems();
        buildCurseOnlyItems();
    }

    private void buildPickaxes() {
        List<Item> pickaxes = List.of(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE,
                Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
        for (Item item : pickaxes) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("fortune", List.of(e(Enchantments.BLOCK_FORTUNE, 3), e(Enchantments.BLOCK_EFFICIENCY, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("silk_touch", List.of(e(Enchantments.SILK_TOUCH, 1), e(Enchantments.BLOCK_EFFICIENCY, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildAxes() {
        List<Item> axes = List.of(Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE,
                Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
        for (Item item : axes) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("axe_fortune_sharpness", List.of(e(Enchantments.BLOCK_EFFICIENCY, 5), e(Enchantments.BLOCK_FORTUNE, 3), e(Enchantments.SHARPNESS, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("axe_fortune_smite", List.of(e(Enchantments.BLOCK_EFFICIENCY, 5), e(Enchantments.BLOCK_FORTUNE, 3), e(Enchantments.SMITE, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("axe_fortune_bane", List.of(e(Enchantments.BLOCK_EFFICIENCY, 5), e(Enchantments.BLOCK_FORTUNE, 3), e(Enchantments.BANE_OF_ARTHROPODS, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("axe_silk_sharpness", List.of(e(Enchantments.BLOCK_EFFICIENCY, 5), e(Enchantments.SILK_TOUCH, 1), e(Enchantments.SHARPNESS, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("axe_silk_smite", List.of(e(Enchantments.BLOCK_EFFICIENCY, 5), e(Enchantments.SILK_TOUCH, 1), e(Enchantments.SMITE, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("axe_silk_bane", List.of(e(Enchantments.BLOCK_EFFICIENCY, 5), e(Enchantments.SILK_TOUCH, 1), e(Enchantments.BANE_OF_ARTHROPODS, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildShovels() {
        List<Item> shovels = List.of(Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL,
                Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL);
        for (Item item : shovels) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("fortune", List.of(e(Enchantments.BLOCK_FORTUNE, 3), e(Enchantments.BLOCK_EFFICIENCY, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("silk_touch", List.of(e(Enchantments.SILK_TOUCH, 1), e(Enchantments.BLOCK_EFFICIENCY, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildHoes() {
        List<Item> hoes = List.of(Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE,
                Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE);
        for (Item item : hoes) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("fortune", List.of(e(Enchantments.BLOCK_FORTUNE, 3), e(Enchantments.BLOCK_EFFICIENCY, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("silk_touch", List.of(e(Enchantments.SILK_TOUCH, 1), e(Enchantments.BLOCK_EFFICIENCY, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildSwords() {
        List<Item> swords = List.of(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD,
                Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD);
        for (Item item : swords) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("sharpness", List.of(e(Enchantments.SHARPNESS, 5), e(Enchantments.KNOCKBACK, 2), e(Enchantments.FIRE_ASPECT, 2), e(Enchantments.MOB_LOOTING, 3), e(Enchantments.SWEEPING_EDGE, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("smite", List.of(e(Enchantments.SMITE, 5), e(Enchantments.KNOCKBACK, 2), e(Enchantments.FIRE_ASPECT, 2), e(Enchantments.MOB_LOOTING, 3), e(Enchantments.SWEEPING_EDGE, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("bane", List.of(e(Enchantments.BANE_OF_ARTHROPODS, 5), e(Enchantments.KNOCKBACK, 2), e(Enchantments.FIRE_ASPECT, 2), e(Enchantments.MOB_LOOTING, 3), e(Enchantments.SWEEPING_EDGE, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildBows() {
        records.add(new ItemEnchantRecord(Items.BOW, List.of(
                new EnchantGroup("infinity", List.of(e(Enchantments.INFINITY_ARROWS, 1), e(Enchantments.POWER_ARROWS, 5), e(Enchantments.FLAMING_ARROWS, 1), e(Enchantments.PUNCH_ARROWS, 2), e(Enchantments.UNBREAKING, 3))),
                new EnchantGroup("mending", List.of(e(Enchantments.MENDING, 1), e(Enchantments.POWER_ARROWS, 5), e(Enchantments.FLAMING_ARROWS, 1), e(Enchantments.PUNCH_ARROWS, 2), e(Enchantments.UNBREAKING, 3)))
        )));
    }

    private void buildCrossbows() {
        records.add(new ItemEnchantRecord(Items.CROSSBOW, List.of(
                new EnchantGroup("piercing", List.of(e(Enchantments.PIERCING, 4), e(Enchantments.QUICK_CHARGE, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                new EnchantGroup("multishot", List.of(e(Enchantments.MULTISHOT, 1), e(Enchantments.QUICK_CHARGE, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
        )));
    }

    private void buildTridents() {
        records.add(new ItemEnchantRecord(Items.TRIDENT, List.of(
                new EnchantGroup("loyalty", List.of(e(Enchantments.LOYALTY, 3), e(Enchantments.CHANNELING, 1), e(Enchantments.IMPALING, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                new EnchantGroup("riptide", List.of(e(Enchantments.RIPTIDE, 3), e(Enchantments.IMPALING, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
        )));
    }

    private void buildHelmet() {
        List<Item> helmets = List.of(Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET,
                Items.GOLDEN_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET, Items.TURTLE_HELMET);
        for (Item item : helmets) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("protection", List.of(e(Enchantments.ALL_DAMAGE_PROTECTION, 4), e(Enchantments.AQUA_AFFINITY, 1), e(Enchantments.RESPIRATION, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("fire_protection", List.of(e(Enchantments.FIRE_PROTECTION, 4), e(Enchantments.AQUA_AFFINITY, 1), e(Enchantments.RESPIRATION, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("blast_protection", List.of(e(Enchantments.BLAST_PROTECTION, 4), e(Enchantments.AQUA_AFFINITY, 1), e(Enchantments.RESPIRATION, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("projectile_protection", List.of(e(Enchantments.PROJECTILE_PROTECTION, 4), e(Enchantments.AQUA_AFFINITY, 1), e(Enchantments.RESPIRATION, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildChestplate() {
        List<Item> chestplates = List.of(Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE,
                Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE);
        for (Item item : chestplates) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("protection", List.of(e(Enchantments.ALL_DAMAGE_PROTECTION, 4), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("fire_protection", List.of(e(Enchantments.FIRE_PROTECTION, 4), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("blast_protection", List.of(e(Enchantments.BLAST_PROTECTION, 4), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("projectile_protection", List.of(e(Enchantments.PROJECTILE_PROTECTION, 4), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildLeggings() {
        List<Item> leggings = List.of(Items.LEATHER_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS,
                Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS);
        for (Item item : leggings) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("protection", List.of(e(Enchantments.ALL_DAMAGE_PROTECTION, 4), e(Enchantments.SWIFT_SNEAK, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("fire_protection", List.of(e(Enchantments.FIRE_PROTECTION, 4), e(Enchantments.SWIFT_SNEAK, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("blast_protection", List.of(e(Enchantments.BLAST_PROTECTION, 4), e(Enchantments.SWIFT_SNEAK, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("projectile_protection", List.of(e(Enchantments.PROJECTILE_PROTECTION, 4), e(Enchantments.SWIFT_SNEAK, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildBoots() {
        List<Item> boots = List.of(Items.LEATHER_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS,
                Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);
        for (Item item : boots) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("protection_depth", List.of(e(Enchantments.ALL_DAMAGE_PROTECTION, 4), e(Enchantments.DEPTH_STRIDER, 3), e(Enchantments.FALL_PROTECTION, 4), e(Enchantments.SOUL_SPEED, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("protection_frost", List.of(e(Enchantments.ALL_DAMAGE_PROTECTION, 4), e(Enchantments.FROST_WALKER, 2), e(Enchantments.FALL_PROTECTION, 4), e(Enchantments.SOUL_SPEED, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("fire_protection_depth", List.of(e(Enchantments.FIRE_PROTECTION, 4), e(Enchantments.DEPTH_STRIDER, 3), e(Enchantments.FALL_PROTECTION, 4), e(Enchantments.SOUL_SPEED, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("fire_protection_frost", List.of(e(Enchantments.FIRE_PROTECTION, 4), e(Enchantments.FROST_WALKER, 2), e(Enchantments.FALL_PROTECTION, 4), e(Enchantments.SOUL_SPEED, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("blast_protection_depth", List.of(e(Enchantments.BLAST_PROTECTION, 4), e(Enchantments.DEPTH_STRIDER, 3), e(Enchantments.FALL_PROTECTION, 4), e(Enchantments.SOUL_SPEED, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("blast_protection_frost", List.of(e(Enchantments.BLAST_PROTECTION, 4), e(Enchantments.FROST_WALKER, 2), e(Enchantments.FALL_PROTECTION, 4), e(Enchantments.SOUL_SPEED, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("projectile_protection_depth", List.of(e(Enchantments.PROJECTILE_PROTECTION, 4), e(Enchantments.DEPTH_STRIDER, 3), e(Enchantments.FALL_PROTECTION, 4), e(Enchantments.SOUL_SPEED, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1))),
                    new EnchantGroup("projectile_protection_frost", List.of(e(Enchantments.PROJECTILE_PROTECTION, 4), e(Enchantments.FROST_WALKER, 2), e(Enchantments.FALL_PROTECTION, 4), e(Enchantments.SOUL_SPEED, 3), e(Enchantments.THORNS, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
            )));
        }
    }

    private void buildShields() {
        records.add(new ItemEnchantRecord(Items.SHIELD, List.of(
                new EnchantGroup("full_build", List.of(e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
        )));
    }

    private void buildElytra() {
        records.add(new ItemEnchantRecord(Items.ELYTRA, List.of(
                new EnchantGroup("full_build", List.of(e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
        )));
    }

    private void buildCarrotStick() {
        records.add(new ItemEnchantRecord(Items.CARROT_ON_A_STICK, List.of(
                new EnchantGroup("full_build", List.of(e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
        )));
    }

    private void buildWarpedFungusStick() {
        records.add(new ItemEnchantRecord(Items.WARPED_FUNGUS_ON_A_STICK, List.of(
                new EnchantGroup("full_build", List.of(e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
        )));
    }

    private void buildFishingRods() {
        records.add(new ItemEnchantRecord(Items.FISHING_ROD, List.of(
                new EnchantGroup("full_build", List.of(e(Enchantments.FISHING_LUCK, 3), e(Enchantments.FISHING_SPEED, 3), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
        )));
    }

    private void buildBrushes() {
        records.add(new ItemEnchantRecord(Items.BRUSH, List.of(
                new EnchantGroup("full_build", List.of(e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
        )));
    }

    private void buildShears() {
        records.add(new ItemEnchantRecord(Items.SHEARS, List.of(
                new EnchantGroup("full_build", List.of(e(Enchantments.BLOCK_EFFICIENCY, 5), e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
        )));
    }


    private void buildUtilityItems() {
        records.add(new ItemEnchantRecord(Items.FLINT_AND_STEEL, List.of(
                new EnchantGroup("full_build", List.of(e(Enchantments.UNBREAKING, 3), e(Enchantments.MENDING, 1)))
        )));
    }

    private void buildCurseOnlyItems() {
        records.add(new ItemEnchantRecord(Items.CARVED_PUMPKIN, List.of(
                new EnchantGroup("no_positive_enchantments", List.of()))));
        records.add(new ItemEnchantRecord(Items.COMPASS, List.of(
                new EnchantGroup("no_positive_enchantments", List.of()))));
        for (Item item : List.of(Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.PIGLIN_HEAD,
                Items.PLAYER_HEAD, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.ZOMBIE_HEAD)) {
            records.add(new ItemEnchantRecord(item, List.of(
                    new EnchantGroup("no_positive_enchantments", List.of()))));
        }
    }

    public List<ItemEnchantRecord> getAllRecords() {
        return records;
    }

    public static List<ItemEnchantRecord> getAllRecords(RegistryAccess registryAccess) {
        return new EnchantmentData(registryAccess).getAllRecords();
    }
}
