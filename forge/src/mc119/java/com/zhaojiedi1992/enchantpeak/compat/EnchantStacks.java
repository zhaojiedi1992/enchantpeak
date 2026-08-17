package com.zhaojiedi1992.enchantpeak.compat;

import com.zhaojiedi1992.enchantpeak.common.EnchantEntry;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * MC 26.x / 1.21.11+ version rendering utilities.
 * MC 1.19.x：Enchantments.XXX 是 Enchantment 实例，Holder.direct() 包装后在此拆包使用
 */
public class EnchantStacks {

    /**
     * Apply enchantments to an item stack (1.19.x style)
     */
    public static void applyTo(ItemStack stack, EnchantGroup group) {
        for (EnchantEntry entry : group.entries()) {
            stack.enchant(entry.enchantment().value(), entry.level());
        }
    }

    /**
     * Generate display lines for enchantments (1.19.x style)
     */
    public static List<Component> enchantmentLines(EnchantGroup group) {
        List<Component> lines = new ArrayList<>();
        for (EnchantEntry entry : group.entries()) {
            // 1.18/1.19 无 description()（1.21 数据驱动附魔新增），getFullname(level) 返回 "时运 III" 全名
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
