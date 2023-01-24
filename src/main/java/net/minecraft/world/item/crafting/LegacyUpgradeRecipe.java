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

@Deprecated(
    forRemoval = true
)
public class LegacyUpgradeRecipe implements SmithingRecipe {
    final Ingredient base;
    final Ingredient addition;
    final ItemStack result;
    private final ResourceLocation id;

    public LegacyUpgradeRecipe(ResourceLocation param0, Ingredient param1, Ingredient param2, ItemStack param3) {
        this.id = param0;
        this.base = param1;
        this.addition = param2;
        this.result = param3;
    }

    @Override
    public boolean matches(Container param0, Level param1) {
        return this.base.test(param0.getItem(0)) && this.addition.test(param0.getItem(1));
    }

    @Override
    public ItemStack assemble(Container param0, RegistryAccess param1) {
        ItemStack var0 = this.result.copy();
        CompoundTag var1 = param0.getItem(0).getTag();
        if (var1 != null) {
            var0.setTag(var1.copy());
        }

        return var0;
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 * param1 >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess param0) {
        return this.result;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack param0) {
        return false;
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
        return RecipeSerializer.SMITHING;
    }

    @Override
    public boolean isIncomplete() {
        return Stream.of(this.base, this.addition).anyMatch(param0 -> param0.getItems().length == 0);
    }

    public static class Serializer implements RecipeSerializer<LegacyUpgradeRecipe> {
        public LegacyUpgradeRecipe fromJson(ResourceLocation param0, JsonObject param1) {
            Ingredient var0 = Ingredient.fromJson(GsonHelper.getAsJsonObject(param1, "base"));
            Ingredient var1 = Ingredient.fromJson(GsonHelper.getAsJsonObject(param1, "addition"));
            ItemStack var2 = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(param1, "result"));
            return new LegacyUpgradeRecipe(param0, var0, var1, var2);
        }

        public LegacyUpgradeRecipe fromNetwork(ResourceLocation param0, FriendlyByteBuf param1) {
            Ingredient var0 = Ingredient.fromNetwork(param1);
            Ingredient var1 = Ingredient.fromNetwork(param1);
            ItemStack var2 = param1.readItem();
            return new LegacyUpgradeRecipe(param0, var0, var1, var2);
        }

        public void toNetwork(FriendlyByteBuf param0, LegacyUpgradeRecipe param1) {
            param1.base.toNetwork(param0);
            param1.addition.toNetwork(param0);
            param0.writeItem(param1.result);
        }
    }
}
