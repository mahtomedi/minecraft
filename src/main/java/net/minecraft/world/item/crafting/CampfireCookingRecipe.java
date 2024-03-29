package net.minecraft.world.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class CampfireCookingRecipe extends AbstractCookingRecipe {
    public CampfireCookingRecipe(String param0, CookingBookCategory param1, Ingredient param2, ItemStack param3, float param4, int param5) {
        super(RecipeType.CAMPFIRE_COOKING, param0, param1, param2, param3, param4, param5);
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.CAMPFIRE);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.CAMPFIRE_COOKING_RECIPE;
    }
}
