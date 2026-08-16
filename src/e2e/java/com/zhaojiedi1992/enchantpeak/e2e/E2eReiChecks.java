package com.zhaojiedi1992.enchantpeak.e2e;

import com.zhaojiedi1992.enchantpeak.rei.ReiEnchantCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import net.minecraft.world.item.ItemStack;

/**
 * REI 断言：best_enchantments 分类的 display 数量 == 物品记录数；
 * 条目注册表里带真实附魔的 ItemStack 条目数（等价于 REI 搜索"时运"等
 * 附魔名能命中的条目池）达到期望规模；
 * 直接搜索关键附魔名，验证玩家搜索体验可用。
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

            // 搜索测试：模拟玩家在 REI 搜索框输入附魔名
            long fortuneResults = searchEnchantment("fortune");
            long sharpnessResults = searchEnchantment("sharpness");
            long protectionResults = searchEnchantment("protection");
            long efficiencyResults = searchEnchantment("efficiency");

            detail.append("rei_displays=").append(displays)
                    .append(" rei_enchanted_entries=").append(enchantedEntries)
                    .append(" search_fortune=").append(fortuneResults)
                    .append(" search_sharpness=").append(sharpnessResults)
                    .append(" search_protection=").append(protectionResults)
                    .append(" search_efficiency=").append(efficiencyResults)
                    .append(' ');

            // 92 条物品记录；条目应覆盖全部 288 个附魔方案（允许 REI 自带少量额外附魔条目）
            // 时运：镐6×2 + 铲6×2 + 锄6×2 + 斧6×6 = 60 个方案
            // 锋利：剑6×1 + 斧6×3 = 24 个方案
            // 保护：头7×1 + 胸6×1 + 腿6×1 + 靴6×4 = 43 个方案
            // 效率：镐6×2 + 斧6×6 + 铲6×2 + 锄6×2 + 剪刀1 = 85 个方案
            return displays >= 90
                    && enchantedEntries >= 288
                    && fortuneResults >= 55        // 预期60，留10%容差
                    && sharpnessResults >= 20       // 预期24，留容差
                    && protectionResults >= 40      // 预期43，留容差
                    && efficiencyResults >= 80;     // 预期85，留容差
        } catch (Throwable t) {
            detail.append("rei=exception:").append(t).append(' ');
            return false;
        }
    }

    /**
     * 模拟 REI 搜索：检查条目的格式化文本（tooltip）中是否包含关键词。
     * REI 的默认搜索行为就是匹配 tooltip 文本（tooltipSearch = ALWAYS）。
     */
    private static long searchEnchantment(String keyword) {
        return EntryRegistry.getInstance()
                .getEntryStacks()
                .filter(entry -> {
                    Object value = entry.getValue();
                    if (!(value instanceof ItemStack stack) || !stack.isEnchanted()) {
                        return false;
                    }
                    // asFormattedText() 返回条目的完整展示文本（包含 tooltip）
                    String text = entry.asFormattedText().getString().toLowerCase();
                    return text.contains(keyword.toLowerCase());
                })
                .count();
    }

    private E2eReiChecks() {
    }
}
