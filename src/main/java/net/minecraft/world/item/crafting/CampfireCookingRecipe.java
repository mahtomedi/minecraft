package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class CampfireCookingRecipe extends AbstractCookingRecipe {
    public CampfireCookingRecipe(
        ResourceLocation param0, String param1, CookingBookCategory param2, Ingredient param3, ItemStack param4, float param5, int param6
    ) {
        super(RecipeType.CAMPFIRE_COOKING, param0, param1, param2, param3, param4, param5, param6);
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
