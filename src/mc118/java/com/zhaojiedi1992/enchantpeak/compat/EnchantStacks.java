package com.zhaojiedi1992.enchantpeak.compat;

import com.zhaojiedi1992.enchantpeak.common.EnchantEntry;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * MC 1.18.x 渲染工具。
 * 1.18.2 的 Enchantments.XXX 是 Enchantment 实例（非 Holder/ResourceKey），
 * EnchantmentData 中已用 Holder.direct() 包装，这里拆包后走 1.18 的 enchant(Enchantment,int)。
 */
public class EnchantStacks {

    public static void applyTo(ItemStack stack, EnchantGroup group) {
        for (EnchantEntry entry : group.entries()) {
            stack.enchant(entry.enchantment().value(), entry.level());
        }
    }

    public static List<Component> enchantmentLines(EnchantGroup group) {
        List<Component> lines = new ArrayList<>();
        for (EnchantEntry entry : group.entries()) {
            // 1.18/1.19 无 description()（1.21 数据驱动附魔新增），getFullname(level) 返回 "时运 III" 全名
            lines.add(entry.enchantment().value().getFullname(entry.level()));
        }
        return lines;
    }

    public static Component displayName(EnchantGroup group) {
        return new TranslatableComponent("enchantpeak.build." + group.name());
    }

    public static Component displayHeading(EnchantGroup group) {
        return new TextComponent("▶ ").append(displayName(group));
    }
}
