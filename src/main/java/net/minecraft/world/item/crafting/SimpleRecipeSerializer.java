package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SimpleRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    private final Function<ResourceLocation, T> constructor;

    public SimpleRecipeSerializer(Function<ResourceLocation, T> param0) {
        this.constructor = param0;
    }

    @Override
    public T fromJson(ResourceLocation param0, JsonObject param1) {
        return this.constructor.apply(param0);
    }

    @Override
    public T fromNetwork(ResourceLocation param0, FriendlyByteBuf param1) {
        return this.constructor.apply(param0);
    }

    @Override
    public void toNetwork(FriendlyByteBuf param0, T param1) {
    }
}
