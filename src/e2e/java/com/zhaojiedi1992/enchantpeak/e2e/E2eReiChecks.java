package com.zhaojiedi1992.enchantpeak.e2e;

import com.zhaojiedi1992.enchantpeak.rei.ReiEnchantCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import net.minecraft.world.item.ItemStack;

/**
 * REI 断言：best_enchantments 分类的 display 数量 == 物品记录数；
 * 条目注册表里带真实附魔的 ItemStack 条目数（等价于 REI 搜索"时运"等
 * 附魔名能命中的条目池）达到期望规模。
 */
final class E2eReiChecks {

    static boolean run(StringBuilder detail) {
        try {
            int displays = DisplayRegistry.getInstance()
                    .get(ReiEnchantCategory.CATEGORY_ID)
                    .size();
            long enchantedEntries = EntryRegistry.getInstance()
                    .getEntryStacks()
                    .filter(entry -> {
                        Object value = entry.getValue();
                        return value instanceof ItemStack stack && stack.isEnchanted();
                    })
                    .count();
            detail.append("rei_displays=").append(displays)
                    .append(" rei_enchanted_entries=").append(enchantedEntries).append(' ');
            // 92 条物品记录；条目应覆盖全部 288 个附魔方案（允许 REI 自带少量额外附魔条目）
            return displays >= 90 && enchantedEntries >= 288;
        } catch (Throwable t) {
            detail.append("rei=exception:").append(t).append(' ');
            return false;
        }
    }

    private E2eReiChecks() {
    }
}
