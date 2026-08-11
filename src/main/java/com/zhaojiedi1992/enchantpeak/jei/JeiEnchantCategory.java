package com.zhaojiedi1992.enchantpeak.jei;

import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class JeiEnchantCategory implements IRecipeCategory<ItemEnchantRecord> {

    private final IDrawable icon;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_GAP = 2;
    // JEI 的 getWidth()/getHeight() 是固定值（不像 REI 按 display 动态计算），
    // 必须覆盖流派数最多的物品：靴子（4 保护 × 2 移动 = 8 个流派）
    private static final int MAX_GROUPS = 8;
    private static final int INPUT_SLOT_X = 5;
    private static final int OUTPUT_START_X = INPUT_SLOT_X + SLOT_SIZE + 12;
    private static final int WIDTH = OUTPUT_START_X + MAX_GROUPS * (SLOT_SIZE + SLOT_GAP) + 5;
    private static final int HEIGHT = 28;

    public JeiEnchantCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.DIAMOND_PICKAXE));
    }

    @Override
    public IRecipeType<ItemEnchantRecord> getRecipeType() {
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
        int slotY = (HEIGHT - SLOT_SIZE) / 2;
        builder.addSlot(RecipeIngredientRole.INPUT, INPUT_SLOT_X, slotY)
                .addItemStack(new ItemStack(recipe.item()));

        int x = OUTPUT_START_X;
        for (EnchantGroup group : recipe.groups()) {
            // 真实附魔物品：原版会自动渲染附魔词条（时运 III 等），这里只追加流派名称作为区分，
            // 不重复罗列附魔内容，避免鼠标悬停 tooltip 中信息出现两次
            ItemStack enchanted = group.applyTo(new ItemStack(recipe.item()));
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, x, slotY)
                    .addItemStack(enchanted)
                    .addRichTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.literal("§6▶ " + group.name())));
            x += SLOT_SIZE + SLOT_GAP;
        }
    }
}
