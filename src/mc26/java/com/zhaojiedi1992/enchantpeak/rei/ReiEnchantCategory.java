package com.zhaojiedi1992.enchantpeak.rei;

import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import me.shedaniel.math.Point;
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
 * 自定义 Category：可视化展示附魔顶配方案。
 *
 * 布局完全交给原生 Slot 渲染：左边输入槽放原始物品，右边若干输出槽放各流派附魔后的物品，
 * 中间用原生箭头连接。不额外绘制任何文字——物品名称、附魔词条等信息由 REI 的原生 tooltip
 * （鼠标悬停）自动展示，避免和原生渲染重复。
 */
public class ReiEnchantCategory implements DisplayCategory<ReiEnchantDisplay> {

    public static final CategoryIdentifier<ReiEnchantDisplay> CATEGORY_ID =
            CategoryIdentifier.of("enchantpeak", "best_enchantments");

    private static final int SLOT_SIZE = 18;
    private static final int GAP = 4;

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
        int outputCount = display.getRecord().groups().size();
        // 输入槽 + 箭头 + N 个输出槽，两两之间留间隙
        return SLOT_SIZE + 24 + outputCount * SLOT_SIZE + (outputCount - 1) * GAP + 12;
    }

    @Override
    public int getDisplayHeight() {
        return SLOT_SIZE + 12;
    }

    @Override
    public List<Widget> setupDisplay(ReiEnchantDisplay display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();
        widgets.add(Widgets.createRecipeBase(bounds));

        var record = display.getRecord();
        int centerY = bounds.y + (bounds.height - SLOT_SIZE) / 2;
        int x = bounds.x + 6;

        // 输入槽：原始物品（悬停显示原生名称 + tooltip）
        widgets.add(Widgets.createSlot(new Point(x, centerY))
                .entries(display.getInputEntries().get(0))
                .markInput());
        x += SLOT_SIZE + 6;

        // 箭头
        widgets.add(Widgets.createArrow(new Point(x, centerY - 1)));
        x += 24;

        // 输出槽：每个流派一个，悬停即可看到原生附魔 tooltip（时运 III 等）
        List<EnchantGroup> groups = record.groups();
        for (int i = 0; i < groups.size(); i++) {
            widgets.add(Widgets.createSlot(new Point(x, centerY))
                    .entries(display.getOutputEntries().get(i))
                    .markOutput());
            x += SLOT_SIZE + GAP;
        }

        return widgets;
    }
}
