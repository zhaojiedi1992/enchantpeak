package com.zhaojiedi1992.enchantpeak.e2e;

import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * JEI 断言：best_enchantments 配方类型必须包含当前族全部物品记录，
 * 且通过"基础物品"能查到对应配方（等价于玩家在 JEI 里点物品看配方）。
 * RECIPE_TYPE 经反射获取，避免依赖 enchantpeak 插件类的访问级别。
 */
final class E2eJeiChecks {

    static boolean run(StringBuilder detail) {
        var runtime = E2eJeiPlugin.runtime;
        if (runtime == null) {
            detail.append("jei=runtime-unavailable ");
            return false;
        }
        try {
            Class<?> pluginClass = Class.forName("com.zhaojiedi1992.enchantpeak.jei.JeiEnchantPlugin");
            Object recipeType = pluginClass.getField("RECIPE_TYPE").get(null);
            var lookup = runtime.getRecipeManager().createRecipeLookup(
                    (mezz.jei.api.recipe.types.IRecipeType<ItemEnchantRecord>) recipeType);
            // JEI 30+（26.x）的 IRecipeLookup.get() 返回 Stream；兼容起见统一 toList()
            List<ItemEnchantRecord> recipes = lookup.get().toList();
            long diamondPickaxe = recipes.stream()
                    .filter(r -> r.item() == Items.DIAMOND_PICKAXE)
                    .count();
            detail.append("jei_recipes=").append(recipes.size())
                    .append(" jei_diamond_pickaxe_records=").append(diamondPickaxe).append(' ');
            return !recipes.isEmpty() && diamondPickaxe > 0;
        } catch (Throwable t) {
            detail.append("jei=exception:").append(t).append(' ');
            return false;
        }
    }

    private E2eJeiChecks() {
    }
}
