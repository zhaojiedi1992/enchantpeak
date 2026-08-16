package com.zhaojiedi1992.enchantpeak.jei;

import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import com.zhaojiedi1992.enchantpeak.compat.EnchantStacks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class JeiEnchantCategory implements IRecipeCategory<ItemEnchantRecord> {

    private final IDrawable icon;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_GAP = 2;
    // JEI 的 getWidth()/getHeight() 是固定值（不像 REI 按 display 动态计算），
    // 必须覆盖流派数最多的物品：靴子（4 保护 × 2 移动 = 8 个流派）
    // 布局硬上限：若未来某物品的流派数超过该值，槽位会溢出。
    // 修改时必须同步检查所有 EnchantmentData 族的方案数（当前最大为靴子 8）。
    private static final int MAX_GROUPS = 8;
    private static final int INPUT_SLOT_X = 5;
    private static final int OUTPUT_START_X = INPUT_SLOT_X + SLOT_SIZE + 12;
    private static final int WIDTH = OUTPUT_START_X + MAX_GROUPS * (SLOT_SIZE + SLOT_GAP) + 5;
    private static final int HEIGHT = 28;

    public JeiEnchantCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.DIAMOND_PICKAXE));
    }

    @Override
    public RecipeType<ItemEnchantRecord> getRecipeType() {
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
            // 使用 OUTPUT 而非 RENDER_ONLY：JEI 搜索/recipe lookup 会忽略 RENDER_ONLY 槽位，
            // 导致按附魔后的物品搜索时完全找不到结果。OUTPUT 槽位会正常参与搜索索引。
            ItemStack enchanted = new ItemStack(recipe.item());
            EnchantStacks.applyTo(enchanted, group);
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, slotY)
                    .addItemStack(enchanted)
                    .addRichTooltipCallback((slotView, tooltip) ->
                            tooltip.add(Component.literal("▶ ").append(EnchantStacks.displayName(group))));
            x += SLOT_SIZE + SLOT_GAP;
        }
    }
}
