package com.zhaojiedi1992.enchantpeak.e2e;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;

/**
 * E2E 测试入口：等待进入世界（JEI/REI 完成世界级重载）后，在主线程执行
 * JEI/REI 注册结果的 API 级断言，打印机器可读结果行并主动退出游戏。
 *
 * 结果行契约（CI 的 scripts/assert_e2e_log.py 依赖）：
 *   [EnchantPeak E2E] RESULT: OK <details>
 *   [EnchantPeak E2E] RESULT: FAIL <reason>
 * 成功 System.exit(0)，失败 System.exit(1)。
 */
public class E2eClient implements ClientModInitializer {

    private static final long WORLD_WAIT_MS = 180_000;
    private static final long RELOAD_SETTLE_MS = 15_000;

    @Override
    public void onInitializeClient() {
        Thread watchdog = new Thread(() -> {
            long deadline = System.currentTimeMillis() + WORLD_WAIT_MS;
            while (Minecraft.getInstance().level == null && System.currentTimeMillis() < deadline) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    break;
                }
            }
            if (Minecraft.getInstance().level == null) {
                finish(false, "world never loaded within " + WORLD_WAIT_MS + "ms");
                return;
            }
            // 等 JEI/REI 的世界级重载完成（注册表数据在进世界时刷新）
            try {
                Thread.sleep(RELOAD_SETTLE_MS);
            } catch (InterruptedException ignored) {
            }
            // 注册表 API 必须在主线程访问
            Minecraft.getInstance().execute(() -> {
                StringBuilder detail = new StringBuilder();
                boolean ok = true;
                ok &= E2eJeiChecks.run(detail);
                ok &= E2eReiChecks.run(detail);
                finish(ok, detail.toString());
            });
        }, "enchantpeak-e2e-watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    public static void finish(boolean ok, String detail) {
        String line = "[EnchantPeak E2E] RESULT: " + (ok ? "OK " : "FAIL ") + detail;
        // 直接打到 stdout/stderr 与 log4j 之外，确保 HeadlessMc 捕获得到
        System.out.println(line);
        System.err.println(line);
        com.zhaojiedi1992.enchantpeak.EnchantPeakMod.LOGGER.info(line);
        System.exit(ok ? 0 : 1);
    }
}
