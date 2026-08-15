package com.zhaojiedi1992.enchantpeak;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(EnchantPeakMod.MOD_ID)
public class EnchantPeakMod {

    public static final String MOD_ID = "enchantpeak";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public EnchantPeakMod() {
        LOGGER.info("[EnchantPeak] NeoForge mod initialized. Showing best enchantments for all vanilla enchantable items.");
    }
}
