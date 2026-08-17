package com.zhaojiedi1992.enchantpeak.jei;

import com.zhaojiedi1992.enchantpeak.EnchantPeakMod;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import com.zhaojiedi1992.enchantpeak.compat.EnchantStacks;
import com.zhaojiedi1992.enchantpeak.data.EnchantmentData;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JeiEnchantPlugin implements IModPlugin {

    static final RecipeType<ItemEnchantRecord> RECIPE_TYPE =
            RecipeType.create(EnchantPeakMod.MOD_ID, "best_enchantments", ItemEnchantRecord.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(EnchantPeakMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new JeiEnchantCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 1.19.x 的附魔常量直接持有 Enchantment 实例，无需 RegistryAccess
        EnchantmentData data = new EnchantmentData(null);
        List<ItemEnchantRecord> records = data.getAllRecords();
        registration.addRecipes(RECIPE_TYPE, records);
        for (ItemEnchantRecord record : records) {
            for (EnchantGroup group : record.groups()) {
                List<Component> lines = new ArrayList<>();
                lines.add(EnchantStacks.displayHeading(group));
                lines.addAll(EnchantStacks.enchantmentLines(group));
                registration.addItemStackInfo(
                        new ItemStack(record.item()),
                        lines.toArray(Component[]::new)
                );
            }
        }
        EnchantPeakMod.LOGGER.info("[EnchantPeak] JEI recipes registered: {}", records.size());
    }
}
