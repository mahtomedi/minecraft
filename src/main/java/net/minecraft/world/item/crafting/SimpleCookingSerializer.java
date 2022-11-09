package net.minecraft.world.item.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class SimpleCookingSerializer<T extends AbstractCookingRecipe> implements RecipeSerializer<T> {
    private final int defaultCookingTime;
    private final SimpleCookingSerializer.CookieBaker<T> factory;

    public SimpleCookingSerializer(SimpleCookingSerializer.CookieBaker<T> param0, int param1) {
        this.defaultCookingTime = param1;
        this.factory = param0;
    }

    public T fromJson(ResourceLocation param0, JsonObject param1) {
        String var0 = GsonHelper.getAsString(param1, "group", "");
        CookingBookCategory var1 = Objects.requireNonNullElse(
            CookingBookCategory.CODEC.byName(GsonHelper.getAsString(param1, "category", null)), CookingBookCategory.MISC
        );
        JsonElement var2 = (JsonElement)(GsonHelper.isArrayNode(param1, "ingredient")
            ? GsonHelper.getAsJsonArray(param1, "ingredient")
            : GsonHelper.getAsJsonObject(param1, "ingredient"));
        Ingredient var3 = Ingredient.fromJson(var2);
        String var4 = GsonHelper.getAsString(param1, "result");
        ResourceLocation var5 = new ResourceLocation(var4);
        ItemStack var6 = new ItemStack(
            BuiltInRegistries.ITEM.getOptional(var5).orElseThrow(() -> new IllegalStateException("Item: " + var4 + " does not exist"))
        );
        float var7 = GsonHelper.getAsFloat(param1, "experience", 0.0F);
        int var8 = GsonHelper.getAsInt(param1, "cookingtime", this.defaultCookingTime);
        return this.factory.create(param0, var0, var1, var3, var6, var7, var8);
    }

    public T fromNetwork(ResourceLocation param0, FriendlyByteBuf param1) {
        String var0 = param1.readUtf();
        CookingBookCategory var1 = param1.readEnum(CookingBookCategory.class);
        Ingredient var2 = Ingredient.fromNetwork(param1);
        ItemStack var3 = param1.readItem();
        float var4 = param1.readFloat();
        int var5 = param1.readVarInt();
        return this.factory.create(param0, var0, var1, var2, var3, var4, var5);
    }

    public void toNetwork(FriendlyByteBuf param0, T param1) {
        param0.writeUtf(param1.group);
        param0.writeEnum(param1.category());
        param1.ingredient.toNetwork(param0);
        param0.writeItem(param1.result);
        param0.writeFloat(param1.experience);
        param0.writeVarInt(param1.cookingTime);
    }

    interface CookieBaker<T extends AbstractCookingRecipe> {
        T create(ResourceLocation var1, String var2, CookingBookCategory var3, Ingredient var4, ItemStack var5, float var6, int var7);
    }
}
