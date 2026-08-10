package com.zhaojiedi1992.enchantpeak.rei;

import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.EnchantEntry;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * REI Category：展示附魔顶配方案
 */
public class ReiEnchantCategory implements DisplayCategory<ReiEnchantDisplay> {

    public static final CategoryIdentifier<ReiEnchantDisplay> CATEGORY_ID =
            CategoryIdentifier.of("enchantpeak", "best_enchantments");

    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;
    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 4;

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
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public List<Widget> setupDisplay(ReiEnchantDisplay display, me.shedaniel.math.Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        var record = display.getRecord();

        // 基础物品槽
        int baseX = bounds.x + PADDING;
        int baseY = bounds.y + PADDING;
        widgets.add(Widgets.createSlot(new me.shedaniel.math.Point(baseX, baseY))
                .entry(EntryStacks.of(new ItemStack(record.item())))
                .disableBackground(false)
                .markInput());

        // 各流派附魔后物品槽，每个带 tooltip
        int x = baseX + SLOT_SIZE + 6;
        for (EnchantGroup group : record.groups()) {
            ItemStack enchanted = group.applyTo(new ItemStack(record.item()));
            final int slotX = x;

            var slot = Widgets.createSlot(new me.shedaniel.math.Point(slotX, baseY))
                    .entry(EntryStacks.of(enchanted))
                    .disableBackground(false)
                    .markOutput();

            widgets.add(slot);

            // 流派标签
            widgets.add(Widgets.createLabel(
                    new me.shedaniel.math.Point(slotX, baseY + SLOT_SIZE + 2),
                    Component.literal(group.name())
            ).color(0xFFFFAA00).noShadow());

            x += SLOT_SIZE + 2;
        }

        return widgets;
    }
}
