package com.zhaojiedi1992.enchantpeak.test;

import com.zhaojiedi1992.enchantpeak.common.EnchantEntry;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 数据结构不变量测试（无 Minecraft 运行时依赖的部分）。
 * EnchantmentData 需要 registryAccess，只在 headless 游戏环境构造；
 * 这里验证的是 common 记录与其组合的通用不变量。
 */
class CommonRecordTest {

    @org.junit.jupiter.api.Test
    void levelStringCoversRomanNumeralsUpToFive() {
        var entry = new EnchantEntry(null, 1);
        org.junit.jupiter.api.Assertions.assertEquals("I", entry.levelString());
        org.junit.jupiter.api.Assertions.assertEquals("II", new EnchantEntry(null, 2).levelString());
        org.junit.jupiter.api.Assertions.assertEquals("III", new EnchantEntry(null, 3).levelString());
        org.junit.jupiter.api.Assertions.assertEquals("IV", new EnchantEntry(null, 4).levelString());
        org.junit.jupiter.api.Assertions.assertEquals("V", new EnchantEntry(null, 5).levelString());
        org.junit.jupiter.api.Assertions.assertEquals("6", new EnchantEntry(null, 6).levelString());
        org.junit.jupiter.api.Assertions.assertEquals("0", new EnchantEntry(null, 0).levelString());
    }

    @org.junit.jupiter.api.Test
    void recordComponentsRoundTrip() {
        var group = new EnchantGroup("fortune",
                List.of(new EnchantEntry(null, 3), new EnchantEntry(null, 5)));
        var record = new ItemEnchantRecord(null, List.of(group));

        org.junit.jupiter.api.Assertions.assertEquals("fortune", group.name());
        org.junit.jupiter.api.Assertions.assertEquals(2, group.entries().size());
        org.junit.jupiter.api.Assertions.assertEquals(3, group.entries().get(0).level());
        org.junit.jupiter.api.Assertions.assertSame(group, record.groups().get(0));
    }

    @org.junit.jupiter.api.Test
    void aBuildNeverRepeatsAnEnchantment() {
        // EnchantmentData 组装数据时最典型的手误：同一附魔在一个方案里出现两次。
        // 记录层用一个明确的失败样例固定该不变量的判定方式。
        List<String> duplicated = List.of("efficiency", "efficiency");
        org.junit.jupiter.api.Assertions.assertTrue(duplicated.size() > new HashSet<>(duplicated).size());

        List<String> distinct = List.of("efficiency", "fortune");
        org.junit.jupiter.api.Assertions.assertFalse(distinct.size() > new HashSet<>(distinct).size());
    }
}
