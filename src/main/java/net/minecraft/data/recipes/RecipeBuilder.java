package net.minecraft.data.recipes;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public interface RecipeBuilder {
    ResourceLocation ROOT_RECIPE_ADVANCEMENT = new ResourceLocation("recipes/root");

    RecipeBuilder unlockedBy(String var1, CriterionTriggerInstance var2);

    RecipeBuilder group(@Nullable String var1);

    Item getResult();

    void save(Consumer<FinishedRecipe> var1, ResourceLocation var2);

    default void save(Consumer<FinishedRecipe> param0) {
        this.save(param0, getDefaultRecipeId(this.getResult()));
    }

    default void save(Consumer<FinishedRecipe> param0, String param1) {
        ResourceLocation var0 = getDefaultRecipeId(this.getResult());
        ResourceLocation var1 = new ResourceLocation(param1);
        if (var1.equals(var0)) {
            throw new IllegalStateException("Recipe " + param1 + " should remove its 'save' argument as it is equal to default one");
        } else {
            this.save(param0, var1);
        }
    }

    static ResourceLocation getDefaultRecipeId(ItemLike param0) {
        return BuiltInRegistries.ITEM.getKey(param0.asItem());
    }
}
