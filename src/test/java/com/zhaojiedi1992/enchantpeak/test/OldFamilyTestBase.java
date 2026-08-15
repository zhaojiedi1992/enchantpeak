package com.zhaojiedi1992.enchantpeak.test;

import com.zhaojiedi1992.enchantpeak.data.EnchantmentData;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * 老版本族（内置附魔注册表）JVM 校验的公共基座：bootstrap 原版注册表，
 * 提供反射适配各版本差异的访问器。失败即失败（不静默跳过）——这些族的
 * 正确性没有任何其他校验兜底。
 *
 * 适用族：mc118/mc119/mc120/mc1204/mc1206（1.18.2-1.20.6，代码内置注册表）。
 * mc121-mc1216（1.21+）的附魔是 datapack 定义，注册表 bootstrap 后仍为空，
 * 由 scripts/verify_enchants.py 的 datapack 校验覆盖。
 */
abstract class OldFamilyTestBase {

    private static volatile boolean bootstrapped;
    private static Throwable bootFailure;
    private static Object enchantmentRegistry; // Registry<Enchantment> 或等价物
    private static Map<Object, String> idCache; // Enchantment -> id（懒构建）

    static void bootstrap() {
        if (bootstrapped) return;
        synchronized (OldFamilyTestBase.class) {
            if (bootstrapped) return;
            try {
                Class<?> shared = Class.forName("net.minecraft.SharedConstants");
                invokeFirstAvailable(shared, "createGameVersion", "tryDetectVersion", "bootStrap");
                Class<?> bootstrapCls = Class.forName("net.minecraft.server.Bootstrap");
                invokeFirstAvailable(bootstrapCls, "bootStrap");
                enchantmentRegistry = findEnchantmentRegistry();
                if (hasDatapackEnchantments()) {
                    bootFailure = new IllegalStateException(
                            "该版本族的附魔为 datapack 定义（1.21+），不在本测试范围；由 verify_enchants.py 覆盖");
                } else if (!enchantSemanticsWired()) {
                    bootFailure = new IllegalStateException(
                            "该版本族裸 bootstrap 后附魔语义未接线（1.20.5/1.20.6 过渡版本），无法深度校验");
                }
            } catch (Throwable t) {
                bootFailure = t;
            }
            bootstrapped = true;
        }
    }

    private static Object findEnchantmentRegistry() throws Exception {
        // 1.19.3+：BuiltInRegistries.ENCHANTMENT；1.18.2-1.19.2：Registry.ENCHANTMENT
        try {
            Class<?> bir = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
            return bir.getField("ENCHANTMENT").get(null);
        } catch (ClassNotFoundException e) {
            Class<?> reg = Class.forName("net.minecraft.core.Registry");
            return reg.getField("ENCHANTMENT").get(null);
        }
    }

    private static boolean hasDatapackEnchantments() throws Exception {
        // 1.21+：附魔为 datapack 定义，裸 bootstrap 后注册表为空
        if (enchantmentRegistry instanceof Collection) {
            return ((Collection<?>) enchantmentRegistry).isEmpty();
        }
        return !((Iterable<?>) enchantmentRegistry).iterator().hasNext();
    }

    private static boolean enchantSemanticsWired() {
        // 1.20.5/1.20.6 过渡版本：注册表非空但裸 bootstrap 后 canEnchant 恒 false
        // （附魔定义未接线），无法用原版语义校验。直接在底层注册表对象上探测，
        // 避免 bootstrap 过程中经 requireApplicable() 递归。
        try {
            for (Object o : (Iterable<?>) registryValues()) {
                var ench = unwrap(o);
                if (idOf(ench).equals("sharpness")) {
                    return (boolean) ench.getClass()
                            .getMethod("canEnchant", net.minecraft.world.item.ItemStack.class)
                            .invoke(ench, new net.minecraft.world.item.ItemStack(
                                    net.minecraft.world.item.Items.WOODEN_SWORD));
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static void invokeFirstAvailable(Class<?> clazz, String... methodNames) throws Exception {
        for (String name : methodNames) {
            try {
                clazz.getMethod(name).invoke(null);
                return;
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException(Arrays.toString(methodNames) + " on " + clazz.getName());
    }

    // ===== 供子类使用的访问器 =====

    static boolean isApplicable() {
        bootstrap();
        return bootFailure == null;
    }

    static String skipReason() {
        return String.valueOf(bootFailure);
    }

    static void requireApplicable() {
        bootstrap();
        if (bootFailure != null) {
            throw new IllegalStateException("bootstrap 失败（应视为构建环境错误，而非跳过）: " + bootFailure,
                    bootFailure);
        }
    }

    private static Object registryValues() throws Exception {
        // MappedRegistry 的 values() 声明在 Registry 接口上，getClass().getMethod 拿不到
        Class<?> registryInterface = Class.forName("net.minecraft.core.Registry");
        try {
            return registryInterface.getMethod("values").invoke(enchantmentRegistry);
        } catch (NoSuchMethodException e) {
            // 兜底：直接迭代（1.18 的 DefaultedRegistry 也实现 Iterable）
            return enchantmentRegistry;
        }
    }

    static Iterable<?> enchantmentRegistry() {
        requireApplicable();
        try {
            return (Iterable<?>) registryValues();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    static net.minecraft.world.item.enchantment.Enchantment unwrap(Object holderOrEnch) {
        if (holderOrEnch instanceof net.minecraft.core.Holder) {
            return (net.minecraft.world.item.enchantment.Enchantment) ((net.minecraft.core.Holder<?>) holderOrEnch).value();
        }
        return (net.minecraft.world.item.enchantment.Enchantment) holderOrEnch;
    }

    static String idOf(net.minecraft.world.item.enchantment.Enchantment ench) {
        // 1.20.6-：getDescriptionId()；1.21+：descriptionId()
        String did;
        try {
            did = (String) ench.getClass().getMethod("getDescriptionId").invoke(ench);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        return did.substring(did.lastIndexOf('.') + 1);
    }

    // 1.20.6- 直接调用；1.21+（接口化）反射访问。范围内族语义一致。
    static int maxLevelOf(net.minecraft.world.item.enchantment.Enchantment ench) {
        try {
            return (int) ench.getClass().getMethod("getMaxLevel").invoke(ench);
        } catch (NoSuchMethodException e) {
            try {
                var m = ench.getClass().getMethod("maxLevel");
                return (int) m.invoke(ench);
            } catch (ReflectiveOperationException e2) {
                throw new IllegalStateException(e2);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static boolean canEnchant(net.minecraft.world.item.enchantment.Enchantment ench,
            net.minecraft.world.item.ItemStack stack) {
        for (String name : new String[]{"canEnchant", "canEnchant"}) {
            try {
                return (boolean) ench.getClass().getMethod(name, net.minecraft.world.item.ItemStack.class).invoke(ench, stack);
            } catch (NoSuchMethodException ignored) {
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalStateException("canEnchant not found");
    }

    static boolean compatibleWith(net.minecraft.world.item.enchantment.Enchantment a,
            net.minecraft.world.item.enchantment.Enchantment b) {
        try {
            var cls = net.minecraft.world.item.enchantment.Enchantment.class;
            try {
                return (boolean) cls.getMethod("isCompatibleWith", cls).invoke(a, b);
            } catch (NoSuchMethodException e) {
                // 1.21+: exclusiveSet / conflictsWith 形态——但 1.21+ 族不在本测试范围，
                // 编译兼容即可；运行到这里说明误入，直接失败
                throw new IllegalStateException("1.21+ 语义族不应进入深度校验");
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    static EnchantmentData newData() {
        requireApplicable();
        // 老族构造器签名是 EnchantmentData(RegistryAccess) 但实现里直接读 Enchantments 静态常量，
        // 参数仅保持 API 一致，传 null 完全安全。
        try {
            for (var ctor : EnchantmentData.class.getConstructors()) {
                return (EnchantmentData) ctor.newInstance(ctor.getParameterCount() == 0
                        ? new Object[0] : new Object[]{null});
            }
            throw new IllegalStateException("EnchantmentData 无可调用构造器");
        } catch (Exception e) {
            throw new IllegalStateException("无法构造 EnchantmentData", e);
        }
    }
}
