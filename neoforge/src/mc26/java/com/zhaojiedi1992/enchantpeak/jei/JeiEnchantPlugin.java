package com.zhaojiedi1992.enchantpeak.jei;

import com.zhaojiedi1992.enchantpeak.EnchantPeakMod;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import com.zhaojiedi1992.enchantpeak.compat.EnchantStacks;
import com.zhaojiedi1992.enchantpeak.data.EnchantmentData;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JeiEnchantPlugin implements IModPlugin {

    static final IRecipeType<ItemEnchantRecord> RECIPE_TYPE =
            IRecipeType.create(EnchantPeakMod.MOD_ID, "best_enchantments", ItemEnchantRecord.class);

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
        RegistryAccess registryAccess = getRegistryAccess();
        if (registryAccess == null) {
            EnchantPeakMod.LOGGER.warn("[EnchantPeak] JEI: registry access not available, skipping recipe registration");
            return;
        }

        EnchantmentData data = new EnchantmentData(registryAccess);
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

    private static RegistryAccess getRegistryAccess() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            return minecraft.level.registryAccess();
        }
        return minecraft.getConnection() != null ? minecraft.getConnection().registryAccess() : null;
    }
}
