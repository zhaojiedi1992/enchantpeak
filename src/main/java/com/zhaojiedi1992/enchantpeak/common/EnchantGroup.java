package com.zhaojiedi1992.enchantpeak.common;

import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * 一个附魔流派：名称 + 对应的附魔词条列表
 * 例如：镐子"时运流" = [效率V, 时运III, 耐久III, 修补I]
 */
public record EnchantGroup(String name, List<EnchantEntry> enchants) {

    /**
     * 将本流派的所有附魔应用到给定 ItemStack（副本），返回附魔后的物品
     */
    public ItemStack applyTo(ItemStack base) {
        ItemStack copy = base.copy();
        for (EnchantEntry entry : enchants) {
            copy.enchant(entry.enchantment(), entry.level());
        }
        return copy;
    }
}
