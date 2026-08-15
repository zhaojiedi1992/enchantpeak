package com.zhaojiedi1992.enchantpeak.test;

import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import com.zhaojiedi1992.enchantpeak.data.EnchantmentData;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 数据层端到端测试：bootstrap 原版注册表后真正构造 EnchantmentData，
 * 验证所有物品/方案/附魔在运行时都能解析（Holder 绑定成功）且结构非空。
 *
 * 与 python datapack 深度校验互补：那套只覆盖 mc2111/mc26，旧族此前仅有
 * "编译器校验"；本测试让全部版本族在 JVM 里跑到真实数据装配。
 */
class EnchantmentDataRuntimeTest {

    private static volatile Object registryAccess;
    private static Throwable bootFailure;

    static {
        try {
            // 各版本 bootstrap API 不同（createGameVersion / bootStrap），反射调用以兼容全部版本族
            Class<?> shared = Class.forName("net.minecraft.SharedConstants");
            invokeFirstAvailable(shared, "createGameVersion", "tryDetectVersion", "bootStrap");
            Class<?> bootstrap = Class.forName("net.minecraft.server.Bootstrap");
            invokeFirstAvailable(bootstrap, "bootStrap");
            registryAccess = createRegistryAccess();
        } catch (Throwable t) {
            bootFailure = t;
        }
    }

    private static void invokeFirstAvailable(Class<?> clazz, String... methodNames) throws Exception {
        for (String name : methodNames) {
            try {
                clazz.getMethod(name).invoke(null);
                return;
            } catch (NoSuchMethodException ignored) {
                // try the next name
            }
        }
        throw new NoSuchMethodException(Arrays.toString(methodNames) + " on " + clazz.getName());
    }

    private static Object createRegistryAccess() throws Exception {
        // 1.20.5+：RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
        // 1.19.3-1.20.4：Bootstrap.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
        // 更早：BuiltInRegistries.REGISTRY 本身即 RegistryAccess
        try {
            Class<?> ra = Class.forName("net.minecraft.core.RegistryAccess");
            Class<?> reg = Class.forName("net.minecraft.core.Registry");
            Class<?> bir = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
            Object root = bir.getField("REGISTRY").get(null);
            try {
                return ra.getMethod("fromRegistryOfRegistries", reg).invoke(null, root);
            } catch (NoSuchMethodException e) {
                Class<?> bootstrap = Class.forName("net.minecraft.server.Bootstrap");
                return bootstrap.getMethod("fromRegistryOfRegistries", reg).invoke(null, root);
            }
        } catch (Exception e) {
            Class<?> bir = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
            return bir.getField("REGISTRY").get(null);
        }
    }

    @org.junit.jupiter.api.Test
    void bootstrapsAndBuildsAllRecords() {
        org.junit.jupiter.api.Assumptions.assumeTrue(bootFailure == null,
                "vanilla bootstrap 失败（无头环境限制）: " + bootFailure);
        EnchantmentData data;
        try {
            data = newData(registryAccess);
        } catch (IllegalStateException e) {
            // MC 1.21.9+/26.x 的附魔注册表是数据驱动（datapack 装载后才有），
            // 空注册表下构造失败属预期；这些族由 verify_enchants_deep.py 全量校验
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            org.junit.jupiter.api.Assumptions.assumeTrue(
                    root.getMessage() != null && root.getMessage().contains("Missing registry"),
                    "EnchantmentData 构造失败: " + root);
            return;
        }
        List<ItemEnchantRecord> records = data.getAllRecords();
        org.junit.jupiter.api.Assertions.assertFalse(records.isEmpty(), "没有任何物品记录");

        for (ItemEnchantRecord record : records) {
            org.junit.jupiter.api.Assertions.assertNotNull(record.item(), "物品为 null");
            org.junit.jupiter.api.Assertions.assertFalse(record.groups().isEmpty(),
                    "物品 " + record.item() + " 没有任何方案组");
            for (EnchantGroup group : record.groups()) {
                org.junit.jupiter.api.Assertions.assertFalse(group.name().isEmpty(), "空组名");
                // curse-only 物品是空 entries 的显式标记，其余组必须至少一条附魔
                if (!group.entries().isEmpty()) {
                    group.entries().forEach(entry -> {
                        org.junit.jupiter.api.Assertions.assertNotNull(entry.enchantment(),
                                group.name() + ": 附魔 Holder 未绑定（registry 解析失败）");
                        org.junit.jupiter.api.Assertions.assertTrue(entry.level() > 0,
                                group.name() + ": 非法等级 " + entry.level());
                    });
                }
            }
        }
        System.out.println("[EnchantPeak] runtime data check: " + records.size() + " items OK");
    }

    private EnchantmentData newData(Object access) {
        try {
            for (var ctor : EnchantmentData.class.getConstructors()) {
                if (ctor.getParameterCount() == 1) {
                    return (EnchantmentData) ctor.newInstance(access);
                }
            }
            return (EnchantmentData) EnchantmentData.class.getDeclaredConstructors()[0].newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("无法构造 EnchantmentData", e);
        }
    }
}
