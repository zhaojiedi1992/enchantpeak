package com.zhaojiedi1992.enchantpeak.jei;

import com.zhaojiedi1992.enchantpeak.common.EnchantEntry;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class JeiEnchantCategory implements IRecipeCategory<ItemEnchantRecord> {

    private final IDrawable icon;
    private static final int WIDTH = 200;
    private static final int HEIGHT = 100;

    public JeiEnchantCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE));
    }

    @Override
    @SuppressWarnings("deprecation")
    public mezz.jei.api.recipe.RecipeType<ItemEnchantRecord> getRecipeType() {
        return JeiEnchantPlugin.RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("enchantpeak.jei.category.title");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ItemEnchantRecord recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 5)
                .addItemStack(new ItemStack(recipe.item()));

        int x = 30;
        for (EnchantGroup group : recipe.groups()) {
            ItemStack enchanted = group.applyTo(new ItemStack(recipe.item()));
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, x, 5)
                    .addItemStack(enchanted)
                    .addRichTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.literal("§6▶ " + group.name()));
                        for (EnchantEntry entry : group.enchants()) {
                            String enchName = entry.enchantment().value().description().getString();
                            tooltip.add(Component.literal(" §7" + enchName + " " + entry.levelString()));
                        }
                    });
            x += 20;
        }
    }
}
