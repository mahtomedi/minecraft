package net.minecraft.data.recipes;

import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class SpecialRecipeBuilder extends CraftingRecipeBuilder {
    final RecipeSerializer<?> serializer;

    public SpecialRecipeBuilder(RecipeSerializer<?> param0) {
        this.serializer = param0;
    }

    public static SpecialRecipeBuilder special(RecipeSerializer<? extends CraftingRecipe> param0) {
        return new SpecialRecipeBuilder(param0);
    }

    public void save(RecipeOutput param0, String param1) {
        this.save(param0, new ResourceLocation(param1));
    }

    public void save(RecipeOutput param0, final ResourceLocation param1) {
        param0.accept(new CraftingRecipeBuilder.CraftingResult(CraftingBookCategory.MISC) {
            @Override
            public RecipeSerializer<?> type() {
                return SpecialRecipeBuilder.this.serializer;
            }

            @Override
            public ResourceLocation id() {
                return param1;
            }

            @Nullable
            @Override
            public AdvancementHolder advancement() {
                return null;
            }
        });
    }
}
