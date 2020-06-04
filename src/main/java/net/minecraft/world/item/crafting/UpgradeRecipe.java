package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class UpgradeRecipe implements Recipe<Container> {
    private final Ingredient base;
    private final Ingredient addition;
    private final ItemStack result;
    private final ResourceLocation id;

    public UpgradeRecipe(ResourceLocation param0, Ingredient param1, Ingredient param2, ItemStack param3) {
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
    public ItemStack assemble(Container param0) {
        ItemStack var0 = this.result.copy();
        CompoundTag var1 = param0.getItem(0).getTag();
        if (var1 != null) {
            var0.setTag(var1.copy());
        }

        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 * param1 >= 2;
    }

    @Override
    public ItemStack getResultItem() {
        return this.result;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(Blocks.SMITHING_TABLE);
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
    public RecipeType<?> getType() {
        return RecipeType.SMITHING;
    }

    public static class Serializer implements RecipeSerializer<UpgradeRecipe> {
        public UpgradeRecipe fromJson(ResourceLocation param0, JsonObject param1) {
            Ingredient var0 = Ingredient.fromJson(GsonHelper.getAsJsonObject(param1, "base"));
            Ingredient var1 = Ingredient.fromJson(GsonHelper.getAsJsonObject(param1, "addition"));
            ItemStack var2 = ShapedRecipe.itemFromJson(GsonHelper.getAsJsonObject(param1, "result"));
            return new UpgradeRecipe(param0, var0, var1, var2);
        }

        public UpgradeRecipe fromNetwork(ResourceLocation param0, FriendlyByteBuf param1) {
            Ingredient var0 = Ingredient.fromNetwork(param1);
            Ingredient var1 = Ingredient.fromNetwork(param1);
            ItemStack var2 = param1.readItem();
            return new UpgradeRecipe(param0, var0, var1, var2);
        }

        public void toNetwork(FriendlyByteBuf param0, UpgradeRecipe param1) {
            param1.base.toNetwork(param0);
            param1.addition.toNetwork(param0);
            param0.writeItem(param1.result);
        }
    }
}
