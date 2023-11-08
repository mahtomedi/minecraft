package net.minecraft.data.recipes;

import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;

public class SpecialRecipeBuilder {
    private final Function<CraftingBookCategory, Recipe<?>> factory;

    public SpecialRecipeBuilder(Function<CraftingBookCategory, Recipe<?>> param0) {
        this.factory = param0;
    }

    public static SpecialRecipeBuilder special(Function<CraftingBookCategory, Recipe<?>> param0) {
        return new SpecialRecipeBuilder(param0);
    }

    public void save(RecipeOutput param0, String param1) {
        this.save(param0, new ResourceLocation(param1));
    }

    public void save(RecipeOutput param0, ResourceLocation param1) {
        param0.accept(param1, this.factory.apply(CraftingBookCategory.MISC), null);
    }
}
