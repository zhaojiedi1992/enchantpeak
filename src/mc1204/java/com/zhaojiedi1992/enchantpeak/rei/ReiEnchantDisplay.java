package com.zhaojiedi1992.enchantpeak.rei;

import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import com.zhaojiedi1992.enchantpeak.compat.EnchantStacks;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 自定义 Display：展示一个物品的所有流派附魔方案
 */
public class ReiEnchantDisplay implements Display {

    private final ItemEnchantRecord record;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;

    public ReiEnchantDisplay(ItemEnchantRecord record) {
        this.record = record;

        // 输入：基础物品
        List<EntryIngredient> ins = new ArrayList<>();
        ins.add(EntryIngredient.of(EntryStacks.of(new ItemStack(record.item()))));
        this.inputs = ins;

        // 输出：每个流派附魔后的物品
        List<EntryIngredient> outs = new ArrayList<>();
        for (EnchantGroup group : record.groups()) {
            ItemStack enchanted = new ItemStack(record.item());
            EnchantStacks.applyTo(enchanted, group);
            outs.add(EntryIngredient.of(EntryStacks.of(enchanted)));
        }
        this.outputs = outs;
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiEnchantCategory.CATEGORY_ID;
    }

    @Override
    public Optional<ResourceLocation> getDisplayLocation() {
        return Optional.empty();
    }

    public ItemEnchantRecord getRecord() {
        return record;
    }
}
