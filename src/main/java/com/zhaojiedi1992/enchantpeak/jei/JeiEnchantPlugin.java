package com.zhaojiedi1992.enchantpeak.jei;

import com.zhaojiedi1992.enchantpeak.EnchantPeakMod;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import com.zhaojiedi1992.enchantpeak.data.EnchantmentData;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.component.DataComponents;
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
        // JEI 在 registerRecipes 时 registryAccess 应该可用
        var level = net.minecraft.client.Minecraft.getInstance().level;
        if (level == null) {
            EnchantPeakMod.LOGGER.warn("[EnchantPeak] JEI: level not available, skipping recipe registration");
            return;
        }

        EnchantmentData data = new EnchantmentData(level.registryAccess());
        List<ItemEnchantRecord> records = data.getAllRecords();
        registration.addRecipes(RECIPE_TYPE, records);
        EnchantPeakMod.LOGGER.info("[EnchantPeak] JEI recipes registered: {}", records.size());
    }

    /**
     * 将附魔后的物品注册为 JEI 全局可搜索条目（对应 REI 的 registerEntries）。
     * 这样用户可以直接在 JEI 搜索栏按物品名/附魔名搜索到附魔方案。
     */
    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        try {
            var level = net.minecraft.client.Minecraft.getInstance().level;
            if (level == null) {
                EnchantPeakMod.LOGGER.warn("[EnchantPeak] JEI: level not available, skipping extra ingredients");
                return;
            }

            EnchantmentData data = new EnchantmentData(level.registryAccess());
            List<ItemStack> extraStacks = new ArrayList<>();

            for (ItemEnchantRecord record : data.getAllRecords()) {
                for (EnchantGroup group : record.groups()) {
                    ItemStack base = new ItemStack(record.item());
                    ItemStack enchanted = group.applyTo(base);
                    String itemName = record.item().getName(base).getString();
                    enchanted.set(DataComponents.CUSTOM_NAME, Component.literal(itemName + "-" + group.name()));
                    extraStacks.add(enchanted);
                }
            }

            registration.addExtraItemStacks(extraStacks);
            EnchantPeakMod.LOGGER.info("[EnchantPeak] JEI extra ingredients added: {}", extraStacks.size());
        } catch (Throwable e) {
            EnchantPeakMod.LOGGER.error("[EnchantPeak] Failed to register JEI extra ingredients", e);
        }
    }
}
