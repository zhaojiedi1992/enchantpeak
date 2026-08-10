package com.zhaojiedi1992.enchantpeak.jei;

import com.zhaojiedi1992.enchantpeak.EnchantPeakMod;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import com.zhaojiedi1992.enchantpeak.data.EnchantmentData;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * JEI 插件入口，注册附魔顶配 Category 和 Recipe 数据
 */
@JeiPlugin
public class JeiEnchantPlugin implements IModPlugin {

    public static final RecipeType<ItemEnchantRecord> RECIPE_TYPE =
            RecipeType.create(EnchantPeakMod.MOD_ID, "best_enchantments", ItemEnchantRecord.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(EnchantPeakMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new JeiEnchantCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 需要注册表访问，从客户端获取
        var registries = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.registryAccess()
                : null;
        if (registries == null) {
            EnchantPeakMod.LOGGER.warn("[EnchantPeak] JEI: registry not available during recipe registration");
            return;
        }

        EnchantmentData data = new EnchantmentData(registries);
        registration.addRecipes(RECIPE_TYPE, data.getAllRecords());

        // 注册物品 → recipe 的关联，让玩家点击任意物品能跳转到 EnchantPeak 页面
        for (ItemEnchantRecord record : data.getAllRecords()) {
            registration.addItemStackInfo(
                    List.of(new ItemStack(record.item())),
                    net.minecraft.network.chat.Component.translatable("enchantpeak.jei.info")
            );
        }
    }
}
