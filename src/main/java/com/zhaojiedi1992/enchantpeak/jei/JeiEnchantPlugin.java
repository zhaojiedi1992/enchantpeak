package com.zhaojiedi1992.enchantpeak.jei;

import com.zhaojiedi1992.enchantpeak.EnchantPeakMod;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import com.zhaojiedi1992.enchantpeak.data.EnchantmentData;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.Identifier;

@JeiPlugin
public class JeiEnchantPlugin implements IModPlugin {

    @SuppressWarnings("deprecation")
    static final mezz.jei.api.recipe.RecipeType<ItemEnchantRecord> RECIPE_TYPE =
            mezz.jei.api.recipe.RecipeType.create(EnchantPeakMod.MOD_ID, "best_enchantments", ItemEnchantRecord.class);

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(EnchantPeakMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new JeiEnchantCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // JEI 在 registerRecipes 时 registryAccess 应该可用
        var registryAccess = net.minecraft.client.Minecraft.getInstance().level;
        if (registryAccess == null) {
            EnchantPeakMod.LOGGER.warn("[EnchantPeak] JEI: level not available, skipping recipe registration");
            return;
        }

        EnchantmentData data = new EnchantmentData(registryAccess.registryAccess());
        registration.addRecipes(RECIPE_TYPE, data.getAllRecords());
        EnchantPeakMod.LOGGER.info("[EnchantPeak] JEI recipes registered: {}", data.getAllRecords().size());
    }
}
