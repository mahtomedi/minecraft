package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import java.util.stream.Stream;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SmithingTransformRecipe implements SmithingRecipe {
    private final ResourceLocation id;
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;
    final ItemStack result;

    public SmithingTransformRecipe(ResourceLocation param0, Ingredient param1, Ingredient param2, Ingredient param3, ItemStack param4) {
        this.id = param0;
        this.template = param1;
        this.base = param2;
        this.addition = param3;
        this.result = param4;
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
    public ResourceLocation getId() {
        return this.id;
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
        public SmithingTransformRecipe fromJson(ResourceLocation param0, JsonObject param1) {
            Ingredient var0 = Ingredient.fromJson(GsonHelper.getAsJsonObject(param1, "template"));
            Ingredient var1 = Ingredient.fromJson(GsonHelper.getAsJsonObject(param1, "base"));
            Ingredient var2 = Ingredient.fromJson(GsonHelper.getAsJsonObject(param1, "addition"));
            ItemStack var3 = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(param1, "result"));
            return new SmithingTransformRecipe(param0, var0, var1, var2, var3);
        }

        public SmithingTransformRecipe fromNetwork(ResourceLocation param0, FriendlyByteBuf param1) {
            Ingredient var0 = Ingredient.fromNetwork(param1);
            Ingredient var1 = Ingredient.fromNetwork(param1);
            Ingredient var2 = Ingredient.fromNetwork(param1);
            ItemStack var3 = param1.readItem();
            return new SmithingTransformRecipe(param0, var0, var1, var2, var3);
        }

        public void toNetwork(FriendlyByteBuf param0, SmithingTransformRecipe param1) {
            param1.template.toNetwork(param0);
            param1.base.toNetwork(param0);
            param1.addition.toNetwork(param0);
            param0.writeItem(param1.result);
        }
    }
}
