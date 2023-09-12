package net.minecraft.world.item.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class StonecutterRecipe extends SingleItemRecipe {
    public StonecutterRecipe(String param0, Ingredient param1, ItemStack param2) {
        super(RecipeType.STONECUTTING, RecipeSerializer.STONECUTTER, param0, param1, param2);
    }

    @Override
    public boolean matches(Container param0, Level param1) {
        return this.ingredient.test(param0.getItem(0));
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.STONECUTTER);
    }
}
