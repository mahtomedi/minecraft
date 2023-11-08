package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class SimpleCookingSerializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T> {
    private final AbstractCookingRecipe.Factory<T> factory;
    private final Codec<T> codec;

    public SimpleCookingSerializer(AbstractCookingRecipe.Factory<T> param0, int param1) {
        this.factory = param0;
        this.codec = RecordCodecBuilder.create(
            param2 -> param2.group(
                        ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(param0x -> param0x.group),
                        CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter(param0x -> param0x.category),
                        Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(param0x -> param0x.ingredient),
                        BuiltInRegistries.ITEM.byNameCodec().xmap(ItemStack::new, ItemStack::getItem).fieldOf("result").forGetter(param0x -> param0x.result),
                        Codec.FLOAT.fieldOf("experience").orElse(0.0F).forGetter(param0x -> param0x.experience),
                        Codec.INT.fieldOf("cookingtime").orElse(param1).forGetter(param0x -> param0x.cookingTime)
                    )
                    .apply(param2, param0::create)
        );
    }

    @Override
    public Codec<T> codec() {
        return this.codec;
    }

    public T fromNetwork(FriendlyByteBuf param0) {
        String var0 = param0.readUtf();
        CookingBookCategory var1 = param0.readEnum(CookingBookCategory.class);
        Ingredient var2 = Ingredient.fromNetwork(param0);
        ItemStack var3 = param0.readItem();
        float var4 = param0.readFloat();
        int var5 = param0.readVarInt();
        return this.factory.create(var0, var1, var2, var3, var4, var5);
    }

    public void toNetwork(FriendlyByteBuf param0, T param1) {
        param0.writeUtf(param1.group);
        param0.writeEnum(param1.category());
        param1.ingredient.toNetwork(param0);
        param0.writeItem(param1.result);
        param0.writeFloat(param1.experience);
        param0.writeVarInt(param1.cookingTime);
    }

    public AbstractCookingRecipe create(String param0, CookingBookCategory param1, Ingredient param2, ItemStack param3, float param4, int param5) {
        return this.factory.create(param0, param1, param2, param3, param4, param5);
    }
}
