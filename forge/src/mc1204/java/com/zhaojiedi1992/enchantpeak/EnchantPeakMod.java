package com.zhaojiedi1992.enchantpeak;

import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 纯客户端 mod（mods.toml 依赖均为 side=CLIENT）；老 FML 的 @Mod 不支持 dist 标记，
// 专用服务器上仅构造函数打一条日志，无任何注册副作用
@Mod(EnchantPeakMod.MOD_ID)
public class EnchantPeakMod {

    public static final String MOD_ID = "enchantpeak";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public EnchantPeakMod() {
        LOGGER.info("[EnchantPeak] Forge mod initialized. Showing best enchantments for all vanilla enchantable items.");
    }
}
