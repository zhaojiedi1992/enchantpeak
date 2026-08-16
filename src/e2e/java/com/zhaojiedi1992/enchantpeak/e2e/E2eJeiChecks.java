package com.zhaojiedi1992.enchantpeak.e2e;

import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * JEI 断言：best_enchantments 配方类型必须包含当前族全部物品记录，
 * 且通过"基础物品"能查到对应配方（等价于玩家在 JEI 里点物品看配方）。
 * 验证多种代表性物品以确保不同类别的覆盖。
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
            // RECIPE_TYPE 是包私有字段，必须 getDeclaredField + setAccessible
            java.lang.reflect.Field field = pluginClass.getDeclaredField("RECIPE_TYPE");
            field.setAccessible(true);
            Object recipeType = field.get(null);
            var lookup = runtime.getRecipeManager().createRecipeLookup(
                    (mezz.jei.api.recipe.types.IRecipeType<ItemEnchantRecord>) recipeType);
            // JEI 30+（26.x）的 IRecipeLookup.get() 返回 Stream；兼容起见统一 toList()
            List<ItemEnchantRecord> recipes = lookup.get().toList();

            // 验证多种代表性物品：工具、武器、防具
            long diamondPickaxe = countRecordsForItem(recipes, Items.DIAMOND_PICKAXE);
            long diamondAxe = countRecordsForItem(recipes, Items.DIAMOND_AXE);
            long diamondSword = countRecordsForItem(recipes, Items.DIAMOND_SWORD);
            long diamondHelmet = countRecordsForItem(recipes, Items.DIAMOND_HELMET);
            long diamondBoots = countRecordsForItem(recipes, Items.DIAMOND_BOOTS);
            long bow = countRecordsForItem(recipes, Items.BOW);
            long trident = countRecordsForItem(recipes, Items.TRIDENT);
            long fishingRod = countRecordsForItem(recipes, Items.FISHING_ROD);

            detail.append("jei_recipes=").append(recipes.size())
                    .append(" pickaxe=").append(diamondPickaxe)
                    .append(" axe=").append(diamondAxe)
                    .append(" sword=").append(diamondSword)
                    .append(" helmet=").append(diamondHelmet)
                    .append(" boots=").append(diamondBoots)
                    .append(" bow=").append(bow)
                    .append(" trident=").append(trident)
                    .append(" fishing_rod=").append(fishingRod)
                    .append(' ');

            // JEI 配方 = 每条物品记录一个（92 条记录，流派在 category 的槽位里渲染），
            // 因此每件代表物品的记录数应为 1
            return recipes.size() >= 90
                    && diamondPickaxe == 1
                    && diamondAxe == 1
                    && diamondSword == 1
                    && diamondHelmet == 1
                    && diamondBoots == 1
                    && bow == 1
                    && trident == 1
                    && fishingRod == 1;
        } catch (Throwable t) {
            detail.append("jei=exception:").append(t).append(' ');
            return false;
        }
    }

    private static long countRecordsForItem(List<ItemEnchantRecord> recipes, net.minecraft.world.item.Item item) {
        return recipes.stream()
                .filter(r -> r.item() == item)
                .count();
    }

    private E2eJeiChecks() {
    }
}
