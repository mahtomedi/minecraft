package net.minecraft.world.item.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
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
        JsonElement var1 = (JsonElement)(GsonHelper.isArrayNode(param1, "ingredient")
            ? GsonHelper.getAsJsonArray(param1, "ingredient")
            : GsonHelper.getAsJsonObject(param1, "ingredient"));
        Ingredient var2 = Ingredient.fromJson(var1);
        String var3 = GsonHelper.getAsString(param1, "result");
        ResourceLocation var4 = new ResourceLocation(var3);
        ItemStack var5 = new ItemStack(Registry.ITEM.getOptional(var4).orElseThrow(() -> new IllegalStateException("Item: " + var3 + " does not exist")));
        float var6 = GsonHelper.getAsFloat(param1, "experience", 0.0F);
        int var7 = GsonHelper.getAsInt(param1, "cookingtime", this.defaultCookingTime);
        return this.factory.create(param0, var0, var2, var5, var6, var7);
    }

    public T fromNetwork(ResourceLocation param0, FriendlyByteBuf param1) {
        String var0 = param1.readUtf(32767);
        Ingredient var1 = Ingredient.fromNetwork(param1);
        ItemStack var2 = param1.readItem();
        float var3 = param1.readFloat();
        int var4 = param1.readVarInt();
        return this.factory.create(param0, var0, var1, var2, var3, var4);
    }

    public void toNetwork(FriendlyByteBuf param0, T param1) {
        param0.writeUtf(param1.group);
        param1.ingredient.toNetwork(param0);
        param0.writeItem(param1.result);
        param0.writeFloat(param1.experience);
        param0.writeVarInt(param1.cookingTime);
    }

    interface CookieBaker<T extends AbstractCookingRecipe> {
        T create(ResourceLocation var1, String var2, Ingredient var3, ItemStack var4, float var5, int var6);
    }
}
