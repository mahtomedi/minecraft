package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class SimpleCraftingRecipeSerializer<T extends CraftingRecipe> implements RecipeSerializer<T> {
    private final SimpleCraftingRecipeSerializer.Factory<T> constructor;

    public SimpleCraftingRecipeSerializer(SimpleCraftingRecipeSerializer.Factory<T> param0) {
        this.constructor = param0;
    }

    public T fromJson(ResourceLocation param0, JsonObject param1) {
        CraftingBookCategory var0 = (CraftingBookCategory)Objects.requireNonNullElse(
            CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(param1, "category", null)), CraftingBookCategory.MISC
        );
        return this.constructor.create(param0, var0);
    }

    public T fromNetwork(ResourceLocation param0, FriendlyByteBuf param1) {
        CraftingBookCategory var0 = param1.readEnum(CraftingBookCategory.class);
        return this.constructor.create(param0, var0);
    }

    public void toNetwork(FriendlyByteBuf param0, T param1) {
        param0.writeEnum(param1.category());
    }

    @FunctionalInterface
    public interface Factory<T extends CraftingRecipe> {
        T create(ResourceLocation var1, CraftingBookCategory var2);
    }
}
