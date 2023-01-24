package net.minecraft.world.item.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public interface SmithingRecipe extends Recipe<Container> {
    @Override
    default RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    @Override
    default boolean canCraftInDimensions(int param0, int param1) {
        return param0 >= 3 && param1 >= 1;
    }

    @Override
    default ItemStack getToastSymbol() {
        return new ItemStack(Blocks.SMITHING_TABLE);
    }

    boolean isTemplateIngredient(ItemStack var1);

    boolean isBaseIngredient(ItemStack var1);

    boolean isAdditionIngredient(ItemStack var1);
}
