package com.zhaojiedi1992.enchantpeak.compat;

import com.zhaojiedi1992.enchantpeak.common.EnchantEntry;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * MC 26.x / 1.21.11+ version rendering utilities.
 * Uses 1.21+ DataComponents API: stack.enchant() and holder.value().description()
 */
public class EnchantStacks {

    /**
     * Apply enchantments to an item stack using 1.21+ DataComponents API
     */
    public static void applyTo(ItemStack stack, EnchantGroup group) {
        for (EnchantEntry entry : group.entries()) {
            stack.enchant(entry.enchantment(), entry.level());
        }
    }

    /**
     * Generate display lines for enchantments using 1.21+ description() API
     */
    public static List<Component> enchantmentLines(EnchantGroup group) {
        List<Component> lines = new ArrayList<>();
        for (EnchantEntry entry : group.entries()) {
            // 原版 tooltip 风格：附魔名 + enchantment.level.N 翻译键拼接，
            // 保留样式与 RTL 语言的正确语序（translatable("%s %s") 会丢样式且乱序）
            net.minecraft.network.chat.MutableComponent desc = entry.enchantment().value().description().copy();
            if (entry.level() > 1) {
                desc.append(Component.translatable("enchantment.level." + entry.level()));
            }
            lines.add(desc);
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
