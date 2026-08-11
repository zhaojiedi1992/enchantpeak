package com.zhaojiedi1992.enchantpeak.rei;

import com.zhaojiedi1992.enchantpeak.EnchantPeakMod;
import com.zhaojiedi1992.enchantpeak.common.EnchantEntry;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import com.zhaojiedi1992.enchantpeak.data.EnchantmentData;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;

/**
 * REI 插件入口
 * 参考 REI 官方 DefaultClientPlugin（26.2 分支）实现模式
 *
 * 三层展示策略：
 * 1. registerEntries  —— 可搜索条目（附魔后物品，带中英双语 lore 供搜索）
 * 2. registerDisplays —— DefaultInformationDisplay（点击物品 → Information 标签）
 * 3. registerDisplays —— 自定义 Category Display（可视化展示）
 *
 * 搜索能力（REI 26.2 默认 tooltipSearch = ALWAYS）：
 * - 物品名（TextArgumentType）：钻石镐、zuanshijiangao（拼音）
 * - Tooltip/Lore（TooltipArgumentType）：时运、shiyun（拼音）、Fortune（英文）
 */
public class ReiEnchantPlugin implements REIClientPlugin {

    /** 附魔 ID → 中文名 映射（用于双语搜索） */
    private static final java.util.Map<String, String> ENCH_CN_NAMES = new java.util.HashMap<>();
    static {
        ENCH_CN_NAMES.put("efficiency", "效率");
        ENCH_CN_NAMES.put("fortune", "时运");
        ENCH_CN_NAMES.put("silk_touch", "精准采集");
        ENCH_CN_NAMES.put("unbreaking", "耐久");
        ENCH_CN_NAMES.put("mending", "修补");
        ENCH_CN_NAMES.put("sharpness", "锋利");
        ENCH_CN_NAMES.put("knockback", "击退");
        ENCH_CN_NAMES.put("fire_aspect", "火焰附加");
        ENCH_CN_NAMES.put("looting", "抢夺");
        ENCH_CN_NAMES.put("sweeping_edge", "横扫之刃");
        ENCH_CN_NAMES.put("power", "力量");
        ENCH_CN_NAMES.put("punch", "冲击");
        ENCH_CN_NAMES.put("flame", "火矢");
        ENCH_CN_NAMES.put("infinity", "无限");
        ENCH_CN_NAMES.put("piercing", "穿透");
        ENCH_CN_NAMES.put("quick_charge", "快速装填");
        ENCH_CN_NAMES.put("multishot", "多重射击");
        ENCH_CN_NAMES.put("loyalty", "忠诚");
        ENCH_CN_NAMES.put("impaling", "穿刺");
        ENCH_CN_NAMES.put("riptide", "激流");
        ENCH_CN_NAMES.put("luck_of_the_sea", "海之眷顾");
        ENCH_CN_NAMES.put("lure", "引饵");
        ENCH_CN_NAMES.put("protection", "保护");
        ENCH_CN_NAMES.put("respiration", "水下呼吸");
        ENCH_CN_NAMES.put("aqua_affinity", "水下速掘");
        ENCH_CN_NAMES.put("thorns", "荆棘");
        ENCH_CN_NAMES.put("feather_falling", "摔落保护");
        ENCH_CN_NAMES.put("soul_speed", "灵魂疾行");
        ENCH_CN_NAMES.put("depth_strider", "深海探索者");
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        try {
            registry.add(new ReiEnchantCategory());
            EnchantPeakMod.LOGGER.info("[EnchantPeak] REI category registered");
        } catch (Throwable e) {
            EnchantPeakMod.LOGGER.error("[EnchantPeak] Failed to register REI category", e);
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        try {
            // BasicDisplay.registryAccess() 委托到 REI Internals，未初始化时抛 AssertionError（非 Exception）
            // 官方 DefaultClientPlugin 在 registerDisplays 阶段用它是安全的（Internals 此时已就绪）
            EnchantmentData data = new EnchantmentData(BasicDisplay.registryAccess());
            int infoCount = 0;
            int displayCount = 0;

            for (ItemEnchantRecord record : data.getAllRecords()) {
                // 1. DefaultInformationDisplay：点击物品后 Information 标签显示附魔方案
                for (EnchantGroup group : record.groups()) {
                    DefaultInformationDisplay info = DefaultInformationDisplay.createFromEntries(
                            EntryIngredients.of(record.item()),
                            Component.literal("§6▶ " + group.name())
                    );
                    for (EnchantEntry entry : group.enchants()) {
                        String enchName = getEnchantmentDisplayName(entry.enchantment());
                        info.line(Component.literal("§7" + enchName + " " + entry.levelString()));
                    }
                    registry.add(info);
                    infoCount++;
                }

                // 2. 自定义 Category Display：可视化展示所有流派
                registry.add(new ReiEnchantDisplay(record));
                displayCount++;
            }

            EnchantPeakMod.LOGGER.info("[EnchantPeak] REI displays registered: {} info, {} custom", infoCount, displayCount);
        } catch (Throwable e) {
            EnchantPeakMod.LOGGER.error("[EnchantPeak] Failed to register REI displays", e);
        }
    }

    @Override
    public void registerEntries(EntryRegistry registry) {
        try {
            // 官方 DefaultClientPlugin.registerEntries() 从不调用 BasicDisplay.registryAccess()，
            // 只在 registerDisplays 阶段才用。这里 catch Throwable 兜住潜在的 AssertionError，
            // 避免注册表未就绪时崩游戏（参考 REI Internals.throwNotSetup()）
            EnchantmentData data = new EnchantmentData(BasicDisplay.registryAccess());
            int count = 0;

            for (ItemEnchantRecord record : data.getAllRecords()) {
                for (EnchantGroup group : record.groups()) {
                    ItemStack base = new ItemStack(record.item());
                    ItemStack enchanted = group.applyTo(base);

                    // 设置可搜索的自定义名称
                    String itemName = record.item().getName(base).getString();
                    String displayName = "★ " + itemName + "（" + group.name() + "）";
                    enchanted.set(DataComponents.CUSTOM_NAME, Component.literal(displayName));

                    // 构建 lore（中英双语附魔名 + 等级），供 tooltip 搜索
                    // REI 默认 tooltipSearch = ALWAYS，lore 中的文本会被索引
                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.literal("§e附魔方案 / Enchantments:"));
                    for (EnchantEntry entry : group.enchants()) {
                        String cnName = getEnchantmentCNName(entry.enchantment());
                        String enName = getEnchantmentEnName(entry.enchantment());
                        lore.add(Component.literal("§7" + cnName + " " + entry.levelString()
                                + " §8(" + enName + " " + entry.levelString() + ")"));
                    }
                    enchanted.set(DataComponents.LORE, new ItemLore(lore));

                    EntryStack<?> entryStack = EntryStacks.of(enchanted);
                    registry.addEntry(entryStack);
                    count++;
                }
            }

            EnchantPeakMod.LOGGER.info("[EnchantPeak] REI entries added: {}", count);
        } catch (Throwable e) {
            EnchantPeakMod.LOGGER.error("[EnchantPeak] Failed to register REI entries", e);
        }
    }

    /** 获取附魔的展示名称（本地化） */
    private static String getEnchantmentDisplayName(Holder<Enchantment> holder) {
        try {
            return holder.value().description().getString();
        } catch (Exception e) {
            return getEnchantmentEnName(holder);
        }
    }

    /** 获取附魔的中文名称（从静态映射，不依赖游戏语言） */
    private static String getEnchantmentCNName(Holder<Enchantment> holder) {
        String enchId = getEnchantmentId(holder);
        String cnName = ENCH_CN_NAMES.get(enchId);
        return cnName != null ? cnName : getEnchantmentDisplayName(holder);
    }

    /** 获取附魔的英文名称（从注册表 ID） */
    private static String getEnchantmentEnName(Holder<Enchantment> holder) {
        String enchId = getEnchantmentId(holder);
        // 将 snake_case 转为 Title Case（如 fortune → Fortune）
        if (enchId.isEmpty()) return "Unknown";
        StringBuilder sb = new StringBuilder();
        for (String word : enchId.split("_")) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    /** 从 Holder 获取附魔的注册表 ID path（如 "fortune"） */
    private static String getEnchantmentId(Holder<Enchantment> holder) {
        return holder.unwrapKey()
                .map(key -> {
                    Identifier id = key.identifier();
                    return id != null ? id.getPath() : "";
                })
                .orElse("");
    }
}
