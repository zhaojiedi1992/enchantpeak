package com.zhaojiedi1992.enchantpeak;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantPeakMod implements ClientModInitializer {

    public static final String MOD_ID = "enchantpeak";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[EnchantPeak] Initialized. Showing best enchantments for diamond & netherite items.");
    }
}
