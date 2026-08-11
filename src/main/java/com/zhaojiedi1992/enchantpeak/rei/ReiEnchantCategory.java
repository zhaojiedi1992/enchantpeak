package com.zhaojiedi1992.enchantpeak.rei;

import com.zhaojiedi1992.enchantpeak.common.EnchantEntry;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义 Category：可视化展示附魔顶配方案
 */
public class ReiEnchantCategory implements DisplayCategory<ReiEnchantDisplay> {

    public static final CategoryIdentifier<ReiEnchantDisplay> CATEGORY_ID =
            CategoryIdentifier.of("enchantpeak", "best_enchantments");

    @Override
    public CategoryIdentifier<? extends ReiEnchantDisplay> getCategoryIdentifier() {
        return CATEGORY_ID;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("enchantpeak.rei.category.title");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(new ItemStack(Items.DIAMOND_PICKAXE));
    }

    @Override
    public int getDisplayWidth(ReiEnchantDisplay display) {
        return 200;
    }

    @Override
    public int getDisplayHeight() {
        return 100;
    }

    @Override
    public List<Widget> setupDisplay(ReiEnchantDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        var record = display.getRecord();
        int baseX = bounds.x + 6;
        int baseY = bounds.y + 6;

        // 基础物品图标
        widgets.add(Widgets.createSlot(new me.shedaniel.math.Point(baseX, baseY))
                .entry(EntryStacks.of(new ItemStack(record.item())))
                .markInput());

        // 物品名称
        widgets.add(Widgets.createLabel(
                new me.shedaniel.math.Point(baseX + 22, baseY + 5),
                record.item().getName(new ItemStack(record.item()))
        ).color(0xFFFFFF00).noShadow());

        // 各流派附魔方案（文本展示）
        int yPos = baseY + 22;
        for (EnchantGroup group : record.groups()) {
            widgets.add(Widgets.createLabel(
                    new me.shedaniel.math.Point(bounds.x + 6, yPos),
                    Component.literal("§6▶ " + group.name())
            ).noShadow());
            yPos += 12;

            StringBuilder sb = new StringBuilder("§7");
            for (int i = 0; i < group.enchants().size(); i++) {
                EnchantEntry entry = group.enchants().get(i);
                if (i > 0) sb.append("  ");
                String enchName = entry.enchantment().value().description().getString();
                sb.append(enchName).append(" ").append(entry.levelString());
            }
            widgets.add(Widgets.createLabel(
                    new me.shedaniel.math.Point(bounds.x + 10, yPos),
                    Component.literal(sb.toString())
            ).noShadow());
            yPos += 14;
        }

        return widgets;
    }
}
