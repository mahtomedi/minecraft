package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class BlastingRecipe extends AbstractCookingRecipe {
    public BlastingRecipe(String param0, CookingBookCategory param1, Ingredient param2, ItemStack param3, float param4, int param5) {
        super(RecipeType.BLASTING, param0, param1, param2, param3, param4, param5);
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.BLAST_FURNACE);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.BLASTING_RECIPE;
    }
}
