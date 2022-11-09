package net.minecraft.data.recipes;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public interface FinishedRecipe {
    void serializeRecipeData(JsonObject var1);

    default JsonObject serializeRecipe() {
        JsonObject var0 = new JsonObject();
        var0.addProperty("type", BuiltInRegistries.RECIPE_SERIALIZER.getKey(this.getType()).toString());
        this.serializeRecipeData(var0);
        return var0;
    }

    ResourceLocation getId();

    RecipeSerializer<?> getType();

    @Nullable
    JsonObject serializeAdvancement();

    @Nullable
    ResourceLocation getAdvancementId();
}
