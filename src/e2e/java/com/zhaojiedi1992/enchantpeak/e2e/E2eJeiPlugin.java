package com.zhaojiedi1992.enchantpeak.e2e;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * E2E 的 JEI 探针：在 JEI 运行时就绪后捕获 IJeiRuntime，
 * 断言 enchantpeak 的 best_enchantments 配方类型注册数量与内容。
 */
@JeiPlugin
public class E2eJeiPlugin implements IModPlugin {

    static volatile IJeiRuntime runtime;

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath("enchantpeak", "e2e");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }
}
