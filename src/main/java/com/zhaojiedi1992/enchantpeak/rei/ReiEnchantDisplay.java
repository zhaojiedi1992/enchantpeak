package com.zhaojiedi1992.enchantpeak.rei;

import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REI Display：对应一条 ItemEnchantRecord 的展示数据
 */
public class ReiEnchantDisplay implements Display {

    private final ItemEnchantRecord record;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;

    public ReiEnchantDisplay(ItemEnchantRecord record) {
        this.record = record;

        // inputs: 基础物品
        this.inputs = List.of(
                EntryIngredient.of(EntryStacks.of(new ItemStack(record.item())))
        );

        // outputs: 每个流派附魔后的物品
        List<EntryIngredient> outs = new ArrayList<>();
        for (EnchantGroup group : record.groups()) {
            ItemStack enchanted = group.applyTo(new ItemStack(record.item()));
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
    public me.shedaniel.rei.api.common.category.CategoryIdentifier<?> getCategoryIdentifier() {
        return ReiEnchantCategory.CATEGORY_ID;
    }

    public ItemEnchantRecord getRecord() {
        return record;
    }
}
