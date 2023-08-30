package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

public class SimpleCraftingRecipeSerializer<T extends CraftingRecipe> implements RecipeSerializer<T> {
    private final SimpleCraftingRecipeSerializer.Factory<T> constructor;
    private final Codec<T> codec;

    public SimpleCraftingRecipeSerializer(SimpleCraftingRecipeSerializer.Factory<T> param0) {
        this.constructor = param0;
        this.codec = RecordCodecBuilder.create(
            param1 -> param1.group(CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(CraftingRecipe::category))
                    .apply(param1, param0::create)
        );
    }

    @Override
    public Codec<T> codec() {
        return this.codec;
    }

    public T fromNetwork(FriendlyByteBuf param0) {
        CraftingBookCategory var0 = param0.readEnum(CraftingBookCategory.class);
        return this.constructor.create(var0);
    }

    public void toNetwork(FriendlyByteBuf param0, T param1) {
        param0.writeEnum(param1.category());
    }

    @FunctionalInterface
    public interface Factory<T extends CraftingRecipe> {
        T create(CraftingBookCategory var1);
    }
}
