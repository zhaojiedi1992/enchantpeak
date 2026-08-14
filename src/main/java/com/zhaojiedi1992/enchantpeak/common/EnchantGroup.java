package com.zhaojiedi1992.enchantpeak.common;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

/**
 * 附魔组纯数据模型（版本无关）
 * 渲染逻辑已移至各版本族的 EnchantStacks 工具类
 */
public record EnchantGroup(
    String name,
    List<EnchantEntry> entries
) {
    // 无方法，纯数据容器
}
