package com.zhaojiedi1992.enchantpeak.e2e;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;

/**
 * E2E 的 REI 探针：仅作为 rei_client 入口存在（保证本类在 REI 插件扫描
 * 阶段被加载）；真正的断言在 E2eReiChecks 中经静态 getInstance() 执行。
 */
public class E2eReiPlugin implements REIClientPlugin {
}
