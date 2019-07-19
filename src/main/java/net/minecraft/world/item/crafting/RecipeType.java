package net.minecraft.world.item.crafting;

import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;

public interface RecipeType<T extends Recipe<?>> {
    RecipeType<CraftingRecipe> CRAFTING = register("crafting");
    RecipeType<SmeltingRecipe> SMELTING = register("smelting");
    RecipeType<BlastingRecipe> BLASTING = register("blasting");
    RecipeType<SmokingRecipe> SMOKING = register("smoking");
    RecipeType<CampfireCookingRecipe> CAMPFIRE_COOKING = register("campfire_cooking");
    RecipeType<StonecutterRecipe> STONECUTTING = register("stonecutting");

    static <T extends Recipe<?>> RecipeType<T> register(final String param0) {
        return Registry.register(Registry.RECIPE_TYPE, new ResourceLocation(param0), new RecipeType<T>() {
            @Override
            public String toString() {
                return param0;
            }
        });
    }

    default <C extends Container> Optional<T> tryMatch(Recipe<C> param0, Level param1, C param2) {
        return param0.matches(param2, param1) ? Optional.of((T)param0) : Optional.empty();
    }
}
