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

            // 验证预期值：镐2方案、斧6方案、剑3方案、头盔4方案、靴子8方案、弓2方案、三叉戟2方案、钓鱼竿1方案
            return recipes.size() >= 90
                    && diamondPickaxe == 2
                    && diamondAxe == 6
                    && diamondSword == 3
                    && diamondHelmet == 4
                    && diamondBoots == 8
                    && bow == 2
                    && trident == 2
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
