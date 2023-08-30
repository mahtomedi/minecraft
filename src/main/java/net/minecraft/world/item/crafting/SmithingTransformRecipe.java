package net.minecraft.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SmithingTransformRecipe implements SmithingRecipe {
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;
    final ItemStack result;

    public SmithingTransformRecipe(Ingredient param0, Ingredient param1, Ingredient param2, ItemStack param3) {
        this.template = param0;
        this.base = param1;
        this.addition = param2;
        this.result = param3;
    }

    @Override
    public boolean matches(Container param0, Level param1) {
        return this.template.test(param0.getItem(0)) && this.base.test(param0.getItem(1)) && this.addition.test(param0.getItem(2));
    }

    @Override
    public ItemStack assemble(Container param0, RegistryAccess param1) {
        ItemStack var0 = this.result.copy();
        CompoundTag var1 = param0.getItem(1).getTag();
        if (var1 != null) {
            var0.setTag(var1.copy());
        }

        return var0;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess param0) {
        return this.result;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack param0) {
        return this.template.test(param0);
    }

    @Override
    public boolean isBaseIngredient(ItemStack param0) {
        return this.base.test(param0);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack param0) {
        return this.addition.test(param0);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SMITHING_TRANSFORM;
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::isEmpty);
    }

    public static class Serializer implements RecipeSerializer<SmithingTransformRecipe> {
        private static final Codec<SmithingTransformRecipe> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Ingredient.CODEC.fieldOf("template").forGetter(param0x -> param0x.template),
                        Ingredient.CODEC.fieldOf("base").forGetter(param0x -> param0x.base),
                        Ingredient.CODEC.fieldOf("addition").forGetter(param0x -> param0x.addition),
                        CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter(param0x -> param0x.result)
                    )
                    .apply(param0, SmithingTransformRecipe::new)
        );

        @Override
        public Codec<SmithingTransformRecipe> codec() {
            return CODEC;
        }

        public SmithingTransformRecipe fromNetwork(FriendlyByteBuf param0) {
            Ingredient var0 = Ingredient.fromNetwork(param0);
            Ingredient var1 = Ingredient.fromNetwork(param0);
            Ingredient var2 = Ingredient.fromNetwork(param0);
            ItemStack var3 = param0.readItem();
            return new SmithingTransformRecipe(var0, var1, var2, var3);
        }

        public void toNetwork(FriendlyByteBuf param0, SmithingTransformRecipe param1) {
            param1.template.toNetwork(param0);
            param1.base.toNetwork(param0);
            param1.addition.toNetwork(param0);
            param0.writeItem(param1.result);
        }
    }
}
