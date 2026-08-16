package com.zhaojiedi1992.enchantpeak.compat;

import com.zhaojiedi1992.enchantpeak.common.EnchantEntry;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * MC 26.x / 1.21.11+ version rendering utilities.
 * 1.20.5/1.20.6 API: Enchantments 常量为 Enchantment 实例，走 enchant(Enchantment,int) + getFullname
 */
public class EnchantStacks {

    /**
     * Apply via stack.enchant(Enchantment, int) (1.20.5/1.20.6 API)
     */
    public static void applyTo(ItemStack stack, EnchantGroup group) {
        for (EnchantEntry entry : group.entries()) {
            stack.enchant(entry.enchantment().value(), entry.level());
        }
    }

    /**
     * Generate display lines via getFullname(level) (1.20.5/1.20.6 API)
     */
    public static List<Component> enchantmentLines(EnchantGroup group) {
        List<Component> lines = new ArrayList<>();
        for (EnchantEntry entry : group.entries()) {
            // 1.20.5/1.20.6 无 description()（1.21 数据驱动附魔新增），用 getFullname(level)
            lines.add(entry.enchantment().value().getFullname(entry.level()));
        }
        return lines;
    }

    /**
     * Generate display name for an enchantment group
     */
    public static Component displayName(EnchantGroup group) {
        return Component.translatable("enchantpeak.build." + group.name());
    }

    /**
     * Generate heading for an enchantment group (with arrow prefix)
     */
    public static Component displayHeading(EnchantGroup group) {
        return Component.literal("▶ ").append(displayName(group));
    }
}
