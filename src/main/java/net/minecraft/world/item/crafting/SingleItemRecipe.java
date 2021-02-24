package net.minecraft.world.item.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class SingleItemRecipe implements Recipe<Container> {
    protected final Ingredient ingredient;
    protected final ItemStack result;
    private final RecipeType<?> type;
    private final RecipeSerializer<?> serializer;
    protected final ResourceLocation id;
    protected final String group;

    public SingleItemRecipe(RecipeType<?> param0, RecipeSerializer<?> param1, ResourceLocation param2, String param3, Ingredient param4, ItemStack param5) {
        this.type = param0;
        this.serializer = param1;
        this.id = param2;
        this.group = param3;
        this.ingredient = param4;
        this.result = param5;
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
    public ResourceLocation getId() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public ItemStack getResultItem() {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> var0 = NonNullList.create();
        var0.add(this.ingredient);
        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return true;
    }

    @Override
    public ItemStack assemble(Container param0) {
        return this.result.copy();
    }

    public static class Serializer<T extends SingleItemRecipe> implements RecipeSerializer<T> {
        final SingleItemRecipe.Serializer.SingleItemMaker<T> factory;

        protected Serializer(SingleItemRecipe.Serializer.SingleItemMaker<T> param0) {
            this.factory = param0;
        }

        public T fromJson(ResourceLocation param0, JsonObject param1) {
            String var0 = GsonHelper.getAsString(param1, "group", "");
            Ingredient var1;
            if (GsonHelper.isArrayNode(param1, "ingredient")) {
                var1 = Ingredient.fromJson(GsonHelper.getAsJsonArray(param1, "ingredient"));
            } else {
                var1 = Ingredient.fromJson(GsonHelper.getAsJsonObject(param1, "ingredient"));
            }

            String var3 = GsonHelper.getAsString(param1, "result");
            int var4 = GsonHelper.getAsInt(param1, "count");
            ItemStack var5 = new ItemStack(Registry.ITEM.get(new ResourceLocation(var3)), var4);
            return this.factory.create(param0, var0, var1, var5);
        }

        public T fromNetwork(ResourceLocation param0, FriendlyByteBuf param1) {
            String var0 = param1.readUtf();
            Ingredient var1 = Ingredient.fromNetwork(param1);
            ItemStack var2 = param1.readItem();
            return this.factory.create(param0, var0, var1, var2);
        }

        public void toNetwork(FriendlyByteBuf param0, T param1) {
            param0.writeUtf(param1.group);
            param1.ingredient.toNetwork(param0);
            param0.writeItem(param1.result);
        }

        interface SingleItemMaker<T extends SingleItemRecipe> {
            T create(ResourceLocation var1, String var2, Ingredient var3, ItemStack var4);
        }
    }
}
