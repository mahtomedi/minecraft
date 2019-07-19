package net.minecraft.world.item.crafting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SmokingRecipe extends AbstractCookingRecipe {
    public SmokingRecipe(ResourceLocation param0, String param1, Ingredient param2, ItemStack param3, float param4, int param5) {
        super(RecipeType.SMOKING, param0, param1, param2, param3, param4, param5);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.SMOKER);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMOKING_RECIPE;
    }
}
