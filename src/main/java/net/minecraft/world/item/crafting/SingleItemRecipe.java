package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public abstract class SingleItemRecipe implements Recipe<Container> {
    protected final Ingredient ingredient;
    protected final ItemStack result;
    private final RecipeType<?> type;
    private final RecipeSerializer<?> serializer;
    protected final String group;

    public SingleItemRecipe(RecipeType<?> param0, RecipeSerializer<?> param1, String param2, Ingredient param3, ItemStack param4) {
        this.type = param0;
        this.serializer = param1;
        this.group = param2;
        this.ingredient = param3;
        this.result = param4;
    }

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializer;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess param0) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> var0 = NonNullList.create();
        var0.add(this.ingredient);
        return var0;
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return true;
    }

    @Override
    public ItemStack assemble(Container param0, RegistryAccess param1) {
        return this.result.copy();
    }

    public interface Factory<T extends SingleItemRecipe> {
        T create(String var1, Ingredient var2, ItemStack var3);
    }

    public static class Serializer<T extends SingleItemRecipe> implements RecipeSerializer<T> {
        final SingleItemRecipe.Factory<T> factory;
        private final Codec<T> codec;

        protected Serializer(SingleItemRecipe.Factory<T> param0) {
            this.factory = param0;
            this.codec = RecordCodecBuilder.create(
                param1 -> param1.group(
                            ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(param0x -> param0x.group),
                            Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(param0x -> param0x.ingredient),
                            ItemStack.RESULT_CODEC.forGetter(param0x -> param0x.result)
                        )
                        .apply(param1, param0::create)
            );
        }

        @Override
        public Codec<T> codec() {
            return this.codec;
        }

        public T fromNetwork(FriendlyByteBuf param0) {
            String var0 = param0.readUtf();
            Ingredient var1 = Ingredient.fromNetwork(param0);
            ItemStack var2 = param0.readItem();
            return this.factory.create(var0, var1, var2);
        }

        public void toNetwork(FriendlyByteBuf param0, T param1) {
            param0.writeUtf(param1.group);
            param1.ingredient.toNetwork(param0);
            param0.writeItem(param1.result);
        }
    }
}
