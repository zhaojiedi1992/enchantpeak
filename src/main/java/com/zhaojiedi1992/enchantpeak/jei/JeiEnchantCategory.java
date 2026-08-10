package com.zhaojiedi1992.enchantpeak.jei;

import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI Recipe Category：展示一条 ItemEnchantRecord（某物品的所有流派顶配方案）
 */
public class JeiEnchantCategory implements IRecipeCategory<ItemEnchantRecord> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("enchantpeak", "best_enchantments");

    private final IDrawable background;
    private final IDrawable icon;

    // 布局参数
    private static final int WIDTH = 160;
    private static final int HEIGHT = 120;
    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 4;

    public JeiEnchantCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE));
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<ItemEnchantRecord> getRecipeType() {
        return JeiPlugin.RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("enchantpeak.jei.category.title");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ItemEnchantRecord recipe, IFocusGroup focuses) {
        // 输出槽：显示基础物品
        builder.addSlot(RecipeIngredientRole.OUTPUT, PADDING + 1, PADDING + 1)
                .addItemStack(new ItemStack(recipe.item()));

        // 每个流派额外各展示一个附魔后的物品
        int x = PADDING + SLOT_SIZE + 6;
        for (EnchantGroup group : recipe.groups()) {
            ItemStack enchanted = group.applyTo(new ItemStack(recipe.item()));
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, PADDING + 1)
                    .addItemStack(enchanted)
                    .addTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.literal("§6" + group.name()));
                        group.enchants().forEach(entry ->
                                tooltip.add(Component.literal("  §7" +
                                        entry.enchantment().value().description().getString() +
                                        " " + entry.levelString()))
                        );
                    });
            x += SLOT_SIZE + 2;
        }
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }
}
