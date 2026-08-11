package com.zhaojiedi1992.enchantpeak.common;

import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * 一个附魔流派：名称 + 附魔词条列表
 */
public record EnchantGroup(String name, List<EnchantEntry> enchants) {

    /**
     * 将本流派附魔应用到物品（副本），返回附魔后的物品
     */
    public ItemStack applyTo(ItemStack base) {
        ItemStack copy = base.copy();
        for (EnchantEntry entry : enchants) {
            copy.enchant(entry.enchantment(), entry.level());
        }
        return copy;
    }
}
