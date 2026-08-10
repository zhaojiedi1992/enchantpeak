package com.zhaojiedi1992.enchantpeak.common;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.Holder;

import java.util.List;

/**
 * 单条附魔词条：附魔 + 等级
 */
public record EnchantEntry(Holder<Enchantment> enchantment, int level) {

    /** 返回罗马数字等级字符串，level=1 时仅当最高等级>1 才显示 I */
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
