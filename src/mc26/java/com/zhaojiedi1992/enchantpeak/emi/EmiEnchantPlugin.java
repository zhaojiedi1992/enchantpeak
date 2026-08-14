package com.zhaojiedi1992.enchantpeak.emi;

import com.zhaojiedi1992.enchantpeak.EnchantPeakMod;
import com.zhaojiedi1992.enchantpeak.common.EnchantGroup;
import com.zhaojiedi1992.enchantpeak.common.ItemEnchantRecord;
import com.zhaojiedi1992.enchantpeak.compat.EnchantStacks;
import com.zhaojiedi1992.enchantpeak.data.EnchantmentData;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EmiEnchantPlugin implements EmiPlugin {

    private static final ResourceLocation CATEGORY_ID =
            ResourceLocation.fromNamespaceAndPath("enchantpeak", "enchantments");

    private static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            CATEGORY_ID,
            EmiStack.of(Items.ENCHANTED_BOOK),
            EmiTexture.EMPTY_ARROW
    ) {
        @Override
        public Component getName() {
            return Component.translatable("enchantpeak.emi.category.title");
        }
    };

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(CATEGORY);

        var registryAccess = Minecraft.getInstance().level.registryAccess();
        List<ItemEnchantRecord> records = EnchantmentData.getAllRecords(registryAccess);

        for (ItemEnchantRecord record : records) {
            for (EnchantGroup group : record.groups()) {
                registry.addRecipe(new EnchantRecipe(record.item(), group));
            }
        }

        EnchantPeakMod.LOGGER.info("[EnchantPeak] EMI recipes registered: {} items", records.size());
    }

    private static class EnchantRecipe implements EmiRecipe {
        private final EmiIngredient input;
        private final EmiStack output;
        private final List<Component> lines;
        private final Component groupName;

        public EnchantRecipe(net.minecraft.world.item.Item item, EnchantGroup group) {
            this.input = EmiStack.of(item);

            ItemStack stack = new ItemStack(item);
            EnchantStacks.applyTo(stack, group);
            this.output = EmiStack.of(stack);

            this.lines = EnchantStacks.enchantmentLines(group);
            this.groupName = EnchantStacks.displayName(group);
        }

        @Override
        public EmiRecipeCategory getCategory() {
            return CATEGORY;
        }

        @Override
        public @Nullable ResourceLocation getId() {
            return null;
        }

        @Override
        public List<EmiIngredient> getInputs() {
            return List.of(input);
        }

        @Override
        public List<EmiStack> getOutputs() {
            return List.of(output);
        }

        @Override
        public int getDisplayWidth() {
            return 125;
        }

        @Override
        public int getDisplayHeight() {
            return 18 + lines.size() * 10;
        }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            widgets.addSlot(input, 0, 0);
            widgets.addSlot(output, 54, 0).recipeContext(this);

            int y = 18;
            for (Component line : lines) {
                widgets.addText(line, 0, y, 0xFFFFFF, false);
                y += 10;
            }
        }
    }
}
