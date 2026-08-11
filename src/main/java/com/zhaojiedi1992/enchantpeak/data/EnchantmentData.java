package com.zhaojiedi1992.enchantpeak.data;

import com.zhaojiedi1992.enchantpeak.common.EnchantEntry;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.List;

/**
 * 原版附魔顶配数据表（MC 26.2）
 */
public class EnchantmentData {

    private final List<ItemEnchantRecord> records = new ArrayList<>();

    public EnchantmentData(RegistryAccess registryAccess) {
        HolderLookup.RegistryLookup<Enchantment> lookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
        buildAll(lookup);
    }

    private static EnchantEntry e(HolderLookup.RegistryLookup<Enchantment> l, ResourceKey<Enchantment> key, int level) {
        Holder<Enchantment> holder = l.getOrThrow(key);
        return new EnchantEntry(holder, level);
    }

    private void buildAll(HolderLookup.RegistryLookup<Enchantment> l) {
        buildPickaxes(l);
        buildAxes(l);
        buildShovels(l);
        buildHoes(l);
        buildSwords(l);
        buildBows(l);
        buildCrossbows(l);
        buildTridents(l);
        buildFishingRods(l);
        buildHelmets(l);
        buildChestplates(l);
        buildLeggings(l);
        buildBoots(l);
    }

    private void buildPickaxes(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup fortune = new EnchantGroup("时运流", List.of(
                e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.FORTUNE, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        EnchantGroup silk = new EnchantGroup("精准流", List.of(
                e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.SILK_TOUCH, 1),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.DIAMOND_PICKAXE, List.of(fortune, silk)));
        records.add(new ItemEnchantRecord(Items.NETHERITE_PICKAXE, List.of(fortune, silk)));
    }

    private void buildAxes(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup logging = new EnchantGroup("伐木流", List.of(
                e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.FORTUNE, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        EnchantGroup combat = new EnchantGroup("战斗流", List.of(
                e(l, Enchantments.SHARPNESS, 5), e(l, Enchantments.EFFICIENCY, 5),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.DIAMOND_AXE, List.of(logging, combat)));
        records.add(new ItemEnchantRecord(Items.NETHERITE_AXE, List.of(logging, combat)));
    }

    private void buildShovels(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup fortune = new EnchantGroup("时运流", List.of(
                e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.FORTUNE, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        EnchantGroup silk = new EnchantGroup("精准流", List.of(
                e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.SILK_TOUCH, 1),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.DIAMOND_SHOVEL, List.of(fortune, silk)));
        records.add(new ItemEnchantRecord(Items.NETHERITE_SHOVEL, List.of(fortune, silk)));
    }

    private void buildHoes(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup fortune = new EnchantGroup("时运流", List.of(
                e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.FORTUNE, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        EnchantGroup silk = new EnchantGroup("精准流", List.of(
                e(l, Enchantments.EFFICIENCY, 5), e(l, Enchantments.SILK_TOUCH, 1),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.DIAMOND_HOE, List.of(fortune, silk)));
        records.add(new ItemEnchantRecord(Items.NETHERITE_HOE, List.of(fortune, silk)));
    }

    private void buildSwords(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup standard = new EnchantGroup("满配流", List.of(
                e(l, Enchantments.SHARPNESS, 5), e(l, Enchantments.KNOCKBACK, 2),
                e(l, Enchantments.FIRE_ASPECT, 2), e(l, Enchantments.LOOTING, 3),
                e(l, Enchantments.SWEEPING_EDGE, 3), e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.DIAMOND_SWORD, List.of(standard)));
        records.add(new ItemEnchantRecord(Items.NETHERITE_SWORD, List.of(standard)));
    }

    private void buildBows(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup infinite = new EnchantGroup("无限流", List.of(
                e(l, Enchantments.POWER, 5), e(l, Enchantments.PUNCH, 2),
                e(l, Enchantments.FLAME, 1), e(l, Enchantments.INFINITY, 1),
                e(l, Enchantments.UNBREAKING, 3)
        ));
        EnchantGroup mending = new EnchantGroup("修补流", List.of(
                e(l, Enchantments.POWER, 5), e(l, Enchantments.PUNCH, 2),
                e(l, Enchantments.FLAME, 1), e(l, Enchantments.MENDING, 1),
                e(l, Enchantments.UNBREAKING, 3)
        ));
        records.add(new ItemEnchantRecord(Items.BOW, List.of(infinite, mending)));
    }

    private void buildCrossbows(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup piercing = new EnchantGroup("穿透流", List.of(
                e(l, Enchantments.PIERCING, 4), e(l, Enchantments.QUICK_CHARGE, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        EnchantGroup multishot = new EnchantGroup("多重流", List.of(
                e(l, Enchantments.MULTISHOT, 1), e(l, Enchantments.QUICK_CHARGE, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.CROSSBOW, List.of(piercing, multishot)));
    }

    private void buildTridents(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup loyalty = new EnchantGroup("忠诚流", List.of(
                e(l, Enchantments.LOYALTY, 3), e(l, Enchantments.IMPALING, 5),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        EnchantGroup riptide = new EnchantGroup("激流流", List.of(
                e(l, Enchantments.RIPTIDE, 3), e(l, Enchantments.IMPALING, 5),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.TRIDENT, List.of(loyalty, riptide)));
    }

    private void buildFishingRods(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup standard = new EnchantGroup("满配流", List.of(
                e(l, Enchantments.LUCK_OF_THE_SEA, 3), e(l, Enchantments.LURE, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.FISHING_ROD, List.of(standard)));
    }

    private void buildHelmets(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup standard = new EnchantGroup("标准流", List.of(
                e(l, Enchantments.PROTECTION, 4), e(l, Enchantments.RESPIRATION, 3),
                e(l, Enchantments.AQUA_AFFINITY, 1), e(l, Enchantments.THORNS, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.DIAMOND_HELMET, List.of(standard)));
        records.add(new ItemEnchantRecord(Items.NETHERITE_HELMET, List.of(standard)));
    }

    private void buildChestplates(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup standard = new EnchantGroup("标准流", List.of(
                e(l, Enchantments.PROTECTION, 4), e(l, Enchantments.THORNS, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.DIAMOND_CHESTPLATE, List.of(standard)));
        records.add(new ItemEnchantRecord(Items.NETHERITE_CHESTPLATE, List.of(standard)));
    }

    private void buildLeggings(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup standard = new EnchantGroup("标准流", List.of(
                e(l, Enchantments.PROTECTION, 4), e(l, Enchantments.THORNS, 3),
                e(l, Enchantments.UNBREAKING, 3), e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.DIAMOND_LEGGINGS, List.of(standard)));
        records.add(new ItemEnchantRecord(Items.NETHERITE_LEGGINGS, List.of(standard)));
    }

    private void buildBoots(HolderLookup.RegistryLookup<Enchantment> l) {
        EnchantGroup standard = new EnchantGroup("标准流", List.of(
                e(l, Enchantments.PROTECTION, 4), e(l, Enchantments.FEATHER_FALLING, 4),
                e(l, Enchantments.SOUL_SPEED, 3), e(l, Enchantments.DEPTH_STRIDER, 3),
                e(l, Enchantments.THORNS, 3), e(l, Enchantments.UNBREAKING, 3),
                e(l, Enchantments.MENDING, 1)
        ));
        records.add(new ItemEnchantRecord(Items.DIAMOND_BOOTS, List.of(standard)));
        records.add(new ItemEnchantRecord(Items.NETHERITE_BOOTS, List.of(standard)));
    }

    public List<ItemEnchantRecord> getAllRecords() {
        return records;
    }
}
