package com.zhaojiedi1992.enchantpeak.common;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个附魔流派：name 为翻译 key 后缀（enchantpeak.build.&lt;name&gt;）+ 附魔词条列表
 */
public record EnchantGroup(String name, List<EnchantEntry> enchants) {

    /**
     * 流派展示名（跟随当前游戏语言本地化）
     */
    public Component displayName() {
        return Component.translatable("enchantpeak.build." + name);
    }

    public Component displayHeading() {
        return Component.literal("▶ ").withStyle(ChatFormatting.GOLD).append(displayName());
    }

    /**
     * 将本流派附魔应用到物品（副本），返回附魔后的物品
     */
    public ItemStack applyTo(ItemStack base) {
        ItemStack copy = base.copy();
        for (EnchantEntry entry : enchants) {
            copy.enchant(entry.enchantment(), entry.level());
        }
        copy.set(DataComponents.CUSTOM_NAME, Component.empty()
                .append(base.getHoverName())
                .append("-")
                .append(displayName()));
        return copy;
    }

    /** 构建 JEI/REI 共用的本地化附魔说明行。 */
    public List<Component> enchantmentLines() {
        List<Component> lines = new ArrayList<>(enchants.size());
        for (EnchantEntry entry : enchants) {
            lines.add(Component.empty()
                    .append(entry.enchantment().value().description())
                    .append(" " + entry.levelString())
                    .withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }
}
