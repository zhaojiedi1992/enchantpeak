package com.zhaojiedi1992.enchantpeak.common;

import net.minecraft.world.item.Item;
import java.util.List;

/**
 * 一个物品的完整展示记录：基础物品 + 所有流派方案
 */
public record ItemEnchantRecord(Item item, List<EnchantGroup> groups) {}
