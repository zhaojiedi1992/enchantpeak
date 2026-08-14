package com.zhaojiedi1992.enchantpeak.rei;

import com.zhaojiedi1992.enchantpeak.EnchantPeakMod;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import com.zhaojiedi1992.enchantpeak.data.EnchantmentData;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * REI 插件入口
 * 参考 REI 官方 DefaultClientPlugin（26.2 分支）实现模式
 *
 * 三层展示策略：
 * 1. registerEntries  —— 可搜索条目：真实附魔物品，附魔词条由原版 tooltip 原生渲染
 * 2. registerDisplays —— DefaultInformationDisplay（点击物品 → Information 标签）
 * 3. registerDisplays —— 自定义 Category Display（原生 Slot 布局，可视化展示）
 *
 * 原则：不重复手写附魔内容。物品一旦附魔，Minecraft 会自动在 tooltip 中渲染附魔词条
 * （如"时运 III"），REI 的 tooltip 搜索（默认 tooltipSearch = ALWAYS）会直接索引这段
 * 原生文本，因此搜索"时运"依然有效，无需额外维护一份重复的文字或映射表。
 */
public class ReiEnchantPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        try {
            registry.add(new ReiEnchantCategory());
            EnchantPeakMod.LOGGER.info("[EnchantPeak] REI category registered");
        } catch (Throwable e) {
            EnchantPeakMod.LOGGER.error("[EnchantPeak] Failed to register REI category", e);
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        try {
            // BasicDisplay.registryAccess() 委托到 REI Internals，未初始化时抛 AssertionError（非 Exception）
            // 官方 DefaultClientPlugin 在 registerDisplays 阶段用它是安全的（Internals 此时已就绪）
            EnchantmentData data = new EnchantmentData(BasicDisplay.registryAccess());
            int infoCount = 0;
            int displayCount = 0;

            for (ItemEnchantRecord record : data.getAllRecords()) {
                // 1. DefaultInformationDisplay：点击物品后 Information 标签显示附魔方案
                for (EnchantGroup group : record.groups()) {
                    DefaultInformationDisplay info = DefaultInformationDisplay.createFromEntries(
                            EntryIngredients.of(record.item()),
                            group.displayHeading()
                    );
                    for (Component line : group.enchantmentLines()) {
                        info.line(line);
                    }
                    registry.add(info);
                    infoCount++;
                }

                // 2. 自定义 Category Display：可视化展示所有流派
                registry.add(new ReiEnchantDisplay(record));
                displayCount++;
            }

            EnchantPeakMod.LOGGER.info("[EnchantPeak] REI displays registered: {} info, {} custom", infoCount, displayCount);
        } catch (Throwable e) {
            EnchantPeakMod.LOGGER.error("[EnchantPeak] Failed to register REI displays", e);
        }
    }

    @Override
    public void registerEntries(EntryRegistry registry) {
        try {
            // 官方 DefaultClientPlugin.registerEntries() 从不调用 BasicDisplay.registryAccess()，
            // 只在 registerDisplays 阶段才用。这里 catch Throwable 兜住潜在的 AssertionError，
            // 避免注册表未就绪时崩游戏（参考 REI Internals.throwNotSetup()）
            EnchantmentData data = new EnchantmentData(BasicDisplay.registryAccess());
            int count = 0;

            for (ItemEnchantRecord record : data.getAllRecords()) {
                for (EnchantGroup group : record.groups()) {
                    ItemStack base = new ItemStack(record.item());
                    // 真实附魔：原版会自动在 tooltip 中渲染附魔词条（时运 III 等），
                    // 不再手动写 Lore 重复这些信息，保持原生 tooltip 展示，避免鼠标悬停时内容重复
                    ItemStack enchanted = group.applyTo(base);

                    EntryStack<?> entryStack = EntryStacks.of(enchanted);
                    registry.addEntry(entryStack);
                    count++;
                }
            }

            EnchantPeakMod.LOGGER.info("[EnchantPeak] REI entries added: {}", count);
        } catch (Throwable e) {
            EnchantPeakMod.LOGGER.error("[EnchantPeak] Failed to register REI entries", e);
        }
    }
}
