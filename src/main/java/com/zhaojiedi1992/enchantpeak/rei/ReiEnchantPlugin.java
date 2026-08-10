package com.zhaojiedi1992.enchantpeak.rei;

import com.zhaojiedi1992.enchantpeak.EnchantPeakMod;
import com.zhaojiedi1992.enchantpeak.data.EnchantmentData;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import net.minecraft.client.Minecraft;

/**
 * REI 客户端插件入口，注册 Category 和 Display 数据
 */
public class ReiEnchantPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new ReiEnchantCategory());
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            EnchantPeakMod.LOGGER.warn("[EnchantPeak] REI: registry not available during display registration");
            return;
        }

        EnchantmentData data = new EnchantmentData(level.registryAccess());
        data.getAllRecords().forEach(record ->
                registry.add(new ReiEnchantDisplay(record))
        );
    }
}
