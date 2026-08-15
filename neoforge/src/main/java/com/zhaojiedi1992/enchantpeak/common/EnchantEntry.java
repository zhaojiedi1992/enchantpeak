package com.zhaojiedi1992.enchantpeak.common;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 单条附魔词条：附魔 Holder + 等级
 */
public record EnchantEntry(Holder<Enchantment> enchantment, int level) {

    public String levelString() {
        return switch (level) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(level);
        };
    }
}
