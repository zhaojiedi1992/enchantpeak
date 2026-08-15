package com.zhaojiedi1992.enchantpeak.test;

import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import com.zhaojiedi1992.enchantpeak.data.EnchantmentData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 老版本族（内置注册表，1.18.2-1.21.8）的深度数据校验：
 * 在 JVM 里 bootstrap 原版注册表后，用真实 Enchantment 对象执行与
 * verify_enchants_deep.py（datapack 级）等价的 5 项检查：
 *
 * 1. 等级最大：每条附魔等级 == 官方 getMaxLevel()
 * 2. 适用性：附魔 canEnchant(item)
 * 3. 无冲突：方案内任意两条附魔 isCompatibleWith
 * 4. 方案顶配：方案外不存在仍可加入且兼容的官方附魔
 * 5. 组合完整：方案集合 == 该物品全部极大兼容附魔组合（按 2^13 枚举）
 *
 * 26.x/1.21.9+（数据驱动族）不在此测试范围——它们的注册表需要完整
 * datapack 装载，由 verify_enchants_deep.py 覆盖。
 */
class EnchantmentDataDeepTest extends OldFamilyTestBase {

    @org.junit.jupiter.api.Test
    void enchantmentDataMatchesVanillaSemantics() {
        // 范围外的族（1.21+ datapack 定义、1.20.5/1.20.6 语义未接线）明确跳过；
        // 范围内的族 bootstrap 失败则硬失败——它们没有其他校验兜底
        org.junit.jupiter.api.Assumptions.assumeTrue(isApplicable(), skipReason());
        List<String> errors = new ArrayList<>();
        EnchantmentData data = newData();

        // 官方注册表里的全部附魔（排除诅咒：binding_curse / vanishing_curse）
        List<net.minecraft.world.item.enchantment.Enchantment> official = new ArrayList<>();
        for (var entry : enchantmentRegistry()) {
            var ench = entry instanceof net.minecraft.core.Holder
                    ? (net.minecraft.world.item.enchantment.Enchantment) ((net.minecraft.core.Holder<?>) entry).value()
                    : (net.minecraft.world.item.enchantment.Enchantment) entry;
            String id = idOf(ench);
            if (!id.equals("binding_curse") && !id.equals("vanishing_curse")) {
                official.add(ench);
            }
        }

        for (ItemEnchantRecord record : data.getAllRecords()) {
            net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(record.item());
            for (EnchantGroup group : record.groups()) {
                if (group.entries().isEmpty()) continue; // curse-only 标记
                Set<net.minecraft.world.item.enchantment.Enchantment> configured = new HashSet<>();
                for (var entry : group.entries()) {
                    var ench = unwrap(entry.enchantment());
                    configured.add(ench);
                    String label = idOf(ench) + "@" + entry.level();
                    // 1+2. 等级与适用性
                    if (entry.level() != maxLevelOf(ench)) {
                        errors.add(record.item() + "/" + group.name() + ": " + label
                                + " 等级未达 getMaxLevel=" + maxLevelOf(ench));
                    }
                    if (!canEnchant(ench, stack)) {
                        errors.add(record.item() + "/" + group.name() + ": " + idOf(ench)
                                + " 不适用于该物品");
                    }
                }
                // 3. 方案内互不冲突
                var asList = new ArrayList<>(configured);
                for (int i = 0; i < asList.size(); i++) {
                    for (int j = i + 1; j < asList.size(); j++) {
                        if (!compatibleWith(asList.get(i), asList.get(j))) {
                            errors.add(record.item() + "/" + group.name() + ": "
                                    + idOf(asList.get(i)) + " 与 " + idOf(asList.get(j)) + " 互斥");
                        }
                    }
                }
                // 4. 顶配：没有仍可加入的兼容附魔
                for (var ench : official) {
                    if (configured.contains(ench) || !canEnchant(ench, stack)) continue;
                    boolean compatible = true;
                    for (var existing : configured) {
                        if (!compatibleWith(ench, existing)) { compatible = false; break; }
                    }
                    if (compatible) {
                        errors.add(record.item() + "/" + group.name() + ": 未顶配，可再加 "
                                + idOf(ench));
                    }
                }
            }
            // 5. 组合完整：方案数 == 极大兼容组合数
            checkCompleteEnumeration(record, official, errors);
        }
        org.junit.jupiter.api.Assertions.assertTrue(errors.isEmpty(),
                String.join("\n", errors));
    }

    /**
     * 枚举该物品全部官方兼容附魔的极大子集，与 mod 的方案集合比对。
     * 与 python 版 maximal_compatible_sets 同构：2^13 穷举（物品候选附魔最多约 13 个）。
     */
    private void checkCompleteEnumeration(ItemEnchantRecord record,
            List<net.minecraft.world.item.enchantment.Enchantment> official, List<String> errors) {
        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(record.item());
        List<net.minecraft.world.item.enchantment.Enchantment> candidates = new ArrayList<>();
        for (var ench : official) {
            if (canEnchant(ench, stack)) candidates.add(ench);
        }
        if (candidates.isEmpty()) return; // curse-only 物品

        Set<Set<net.minecraft.world.item.enchantment.Enchantment>> compatible = new HashSet<>();
        for (int mask = 0; mask < (1 << candidates.size()); mask++) {
            Set<net.minecraft.world.item.enchantment.Enchantment> subset = new HashSet<>();
            for (int i = 0; i < candidates.size(); i++) {
                if ((mask & (1 << i)) != 0) subset.add(candidates.get(i));
            }
            boolean ok = true;
            var arr = new ArrayList<>(subset);
            outer:
            for (int i = 0; i < arr.size(); i++) {
                for (int j = i + 1; j < arr.size(); j++) {
                    if (!compatibleWith(arr.get(i), arr.get(j))) { ok = false; break outer; }
                }
            }
            if (ok) compatible.add(subset);
        }
        Set<Set<net.minecraft.world.item.enchantment.Enchantment>> maximal = new HashSet<>();
        for (var set : compatible) {
            boolean hasSuperset = compatible.stream().anyMatch(o -> set.size() < o.size() && o.containsAll(set));
            if (!hasSuperset) maximal.add(set);
        }

        Set<Set<net.minecraft.world.item.enchantment.Enchantment>> actual = new HashSet<>();
        for (EnchantGroup group : record.groups()) {
            if (group.entries().isEmpty()) continue;
            Set<net.minecraft.world.item.enchantment.Enchantment> s = new HashSet<>();
            group.entries().forEach(e -> s.add(unwrap(e.enchantment())));
            actual.add(s);
        }
        if (!actual.equals(maximal)) {
            errors.add(record.item() + ": 方案集合与极大兼容组合不匹配\n  mod=" + ids(actual)
                    + "\n  官方=" + ids(maximal));
        }
    }

    private String ids(Set<Set<net.minecraft.world.item.enchantment.Enchantment>> sets) {
        StringBuilder sb = new StringBuilder();
        for (var s : sets) {
            List<String> ids = new ArrayList<>();
            s.forEach(e -> ids.add(idOf(e)));
            ids.sort(null);
            sb.append("[").append(String.join("+", ids)).append("] ");
        }
        return sb.toString().trim();
    }
}
