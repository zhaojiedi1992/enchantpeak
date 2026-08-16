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
            // 搜索阈值按 26.2（7 材质，含铜）实测校准：
            // fortune：镐7×1 + 铲7×1 + 锄7×1 + 斧7×3 = 42（实测 42）
            // sharpness：剑7×1 + 斧7×2 + 矛7×1 = 28（实测 28）
            // protection：子串命中 protection/fire/blast/projectile_protection = 144（实测 144，
            //   与 REI 子串搜索行为一致，正是玩家体验）
            // efficiency：镐/铲/锄各14 + 斧42 + 剪刀1 = 85 起（实测 113，含 26.x 属性行文本）
            return displays >= 90
                    && enchantedEntries >= 288
                    && fortuneResults >= 40
                    && sharpnessResults >= 25
                    && protectionResults >= 140
                    && efficiencyResults >= 85;
        } catch (Throwable t) {
            detail.append("rei=exception:").append(t).append(' ');
            return false;
        }
    }

    /**
     * 模拟 REI 搜索：REI 的 tooltipSearch=ALWAYS 索引的是条目堆的**完整
     * tooltip 文本**（附魔名如 "Fortune III" 就在其中），而不是条目显示名。
     * 这里生成原版 tooltip 再匹配关键词，与 REI 搜索框行为等价。
     */
    private static long searchEnchantment(String keyword) {
        return EntryRegistry.getInstance()
                .getEntryStacks()
                .filter(entry -> {
                    Object value = entry.getValue();
                    if (!(value instanceof ItemStack stack) || !stack.isEnchanted()) {
                        return false;
                    }
                    java.util.List<net.minecraft.network.chat.Component> lines = stack.getTooltipLines(
                            net.minecraft.world.item.Item.TooltipContext.EMPTY,
                            null,
                            net.minecraft.world.item.TooltipFlag.NORMAL);
                    return lines.stream()
                            .map(net.minecraft.network.chat.Component::getString)
                            .anyMatch(s -> s.toLowerCase(java.util.Locale.ROOT)
                                    .contains(keyword.toLowerCase(java.util.Locale.ROOT)));
                })
                .count();
    }

    private E2eReiChecks() {
    }
}
