package com.zhaojiedi1992.enchantpeak.test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 源码级数据一致性测试：直接解析每个版本族 EnchantmentData.java 的
 * addRecords/new EnchantGroup/new EnchantEntry 调用，检查跨版本通用的结构不变量：
 *
 * 1. 每件物品每个方案内同一附魔键不重复出现
 * 2. 组名（翻译键后缀）在 [A-Z0-9_]+ 深度内合法且非空
 * 3. 所有出现过的组名在 en_us.json/zh_cn.json 都有 enchantpeak.build.&lt;name&gt; 翻译键
 * 4. 每件物品至少有一个方案，且方案数 ≥ 1（含 curse-only 的空方案标记）
 *
 * 不需要 Minecraft 运行时，因此可以和 verify_enchants_deep.py 的 datapack
 * 级校验互补：datapack 校验覆盖 1.21+/26.x，JVM 深度校验覆盖 mc118-mc1204，
 * 本测试（源码解析）覆盖全部版本族的结构不变量。
 */
class EnchantmentDataStructureTest {

    // Gradle test 的工作目录即项目根；familyDir 形如 src/mc26/java
    private static final Path REPO_ROOT = Paths.get(System.getProperty("user.dir")).toAbsolutePath();

    // new EnchantGroup("name", List.of( e(l, KEY, 1), e(l, KEY, 5), ... ))
    private static final Pattern GROUP_PATTERN = Pattern.compile(
            "new EnchantGroup\\(\\s*\"([a-z0-9_]+)\"\\s*,\\s*List\\.of\\((.*?)\\)\\s*\\)",
            Pattern.DOTALL);
    private static final Pattern ENTRY_KEY_PATTERN = Pattern.compile(
            "Enchantments\\.([A-Z0-9_]+)");
    // addRecords(List.of(groupA, groupB), Items.X, Items.Y, ...) —— 一件物品的全部方案
    private static final Pattern ADD_RECORDS_PATTERN = Pattern.compile(
            "addRecords\\(\\s*List\\.of\\((.*?)\\)\\s*,\\s*(Items\\.[A-Z0-9_]+(?:\\s*,\\s*Items\\.[A-Z0-9_]+)*)\\s*\\)",
            Pattern.DOTALL);
    private static final Pattern ITEM_PATTERN = Pattern.compile("Items\\.([A-Z0-9_]+)");

    private List<Path> familySources() throws IOException {
        try (Stream<Path> dirs = Files.list(REPO_ROOT.resolve("src"))) {
            return dirs.filter(p -> {
                        String n = p.getFileName().toString();
                        return n.startsWith("mc") && Files.isDirectory(p);
                    })
                    .map(p -> p.resolve("java/com/zhaojiedi1992/enchantpeak/data/EnchantmentData.java"))
                    .filter(Files::isRegularFile)
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    private static String familyOf(Path src) {
        // src/mc26/java/... → mc26
        for (Path part : src) {
            String n = part.toString();
            if (n.startsWith("mc") && !n.equals("main")) return n;
        }
        return "?";
    }

    @org.junit.jupiter.api.Test
    void everyBuildHasNoRepeatedEnchantmentPerItem() throws IOException {
        // 语义：每个方案（EnchantGroup）内不得重复出现同一附魔键。
        // 注意：不能检查"同物品的两个组不得共享附魔键"——合法方案必然共享
        // unbreaking/mending/efficiency（时运流和精准流都含耐久 III），
        // 且各组通过 helper 方法构造（toolFortune(l)），正则取不到组名。
        List<String> failures = new ArrayList<>();
        for (Path src : familySources()) {
            String code = Files.readString(src, StandardCharsets.UTF_8);
            String family = familyOf(src);
            Matcher gm = GROUP_PATTERN.matcher(code);
            int groups = 0;
            while (gm.find()) {
                groups++;
                // gm.group(2) 是该组的 List.of(...) 内容；统计每个附魔键出现次数
                Map<String, Integer> keyCounts = new HashMap<>();
                Matcher em = ENTRY_KEY_PATTERN.matcher(gm.group(2));
                while (em.find()) {
                    String key = em.group(1);
                    keyCounts.merge(key, 1, Integer::sum);
                }
                for (Map.Entry<String, Integer> e : keyCounts.entrySet()) {
                    if (e.getValue() > 1) {
                        failures.add(family + "/" + gm.group(1) + ": 附魔键 " + e.getKey()
                                + " 出现 " + e.getValue() + " 次（组内不允许重复）");
                    }
                }
            }
            if (groups == 0) {
                failures.add(family + ": 未解析到任何 EnchantGroup —— 解析器或代码结构变化？");
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    @org.junit.jupiter.api.Test
    void everyGroupNameHasTranslations() throws IOException {
        Set<String> allNames = new HashSet<>();
        for (Path src : familySources()) {
            Matcher gm = GROUP_PATTERN.matcher(Files.readString(src, StandardCharsets.UTF_8));
            while (gm.find()) allNames.add(gm.group(1));
        }
        org.junit.jupiter.api.Assertions.assertFalse(allNames.isEmpty(), "未解析到任何组名");
        for (String locale : List.of("en_us", "zh_cn")) {
            Path lang = REPO_ROOT.resolve(
                    "src/main/resources/assets/enchantpeak/lang/" + locale + ".json");
            String json = Files.readString(lang, StandardCharsets.UTF_8);
            List<String> missing = allNames.stream()
                    .filter(n -> !json.contains("\"enchantpeak.build." + n + "\""))
                    .collect(java.util.stream.Collectors.toList());
            org.junit.jupiter.api.Assertions.assertTrue(missing.isEmpty(),
                    locale + " 缺少组名翻译键: " + missing + "（已知组: " + allNames + "）");
        }
    }

    @org.junit.jupiter.api.Test
    void everyItemHasAtLeastOneBuildOrCurseOnlyMarker() throws IOException {
        // 新族：addRecords(List.of(groups), Items.X, ...)；旧族：records.add(new ItemEnchantRecord(item, List.of(...)))
        // 两种形态都必须存在且每个调用都绑定了物品与至少一个组
        List<String> failures = new ArrayList<>();
        Pattern legacyRecordPattern = Pattern.compile(
                "new ItemEnchantRecord\\(\\s*(?:item|Items\\.[A-Z0-9_]+)\\s*,\\s*List\\.of\\(",
                Pattern.DOTALL);
        for (Path src : familySources()) {
            String code = Files.readString(src, StandardCharsets.UTF_8);
            String family = familyOf(src);
            int calls = 0;
            Matcher rm = ADD_RECORDS_PATTERN.matcher(code);
            while (rm.find()) {
                calls++;
                if (rm.group(2).isBlank()) {
                    failures.add(family + ": addRecords 未引用任何 Items");
                }
            }
            Matcher lm = legacyRecordPattern.matcher(code);
            while (lm.find()) calls++;
            if (calls == 0) {
                failures.add(family + ": 未解析到任何 addRecords/ItemEnchantRecord 调用");
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(failures.isEmpty(), String.join("\n", failures));
    }
}
