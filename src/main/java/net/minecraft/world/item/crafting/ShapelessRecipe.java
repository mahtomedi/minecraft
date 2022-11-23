package net.minecraft.world.item.crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ShapelessRecipe implements CraftingRecipe {
    private final ResourceLocation id;
    final String group;
    final CraftingBookCategory category;
    final ItemStack result;
    final NonNullList<Ingredient> ingredients;

    public ShapelessRecipe(ResourceLocation param0, String param1, CraftingBookCategory param2, ItemStack param3, NonNullList<Ingredient> param4) {
        this.id = param0;
        this.group = param1;
        this.category = param2;
        this.result = param3;
        this.ingredients = param4;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPELESS_RECIPE;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public CraftingBookCategory category() {
        return this.category;
    }

    @Override
    public ItemStack getResultItem() {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        StackedContents var0 = new StackedContents();
        int var1 = 0;

        for(int var2 = 0; var2 < param0.getContainerSize(); ++var2) {
            ItemStack var3 = param0.getItem(var2);
            if (!var3.isEmpty()) {
                ++var1;
                var0.accountStack(var3, 1);
            }
        }

        return var1 == this.ingredients.size() && var0.canCraft(this, null);
    }

    public ItemStack assemble(CraftingContainer param0) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 * param1 >= this.ingredients.size();
    }

    public static class Serializer implements RecipeSerializer<ShapelessRecipe> {
        public ShapelessRecipe fromJson(ResourceLocation param0, JsonObject param1) {
            String var0 = GsonHelper.getAsString(param1, "group", "");
            CraftingBookCategory var1 = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(param1, "category", null), CraftingBookCategory.MISC);
            NonNullList<Ingredient> var2 = itemsFromJson(GsonHelper.getAsJsonArray(param1, "ingredients"));
            if (var2.isEmpty()) {
                throw new JsonParseException("No ingredients for shapeless recipe");
            } else if (var2.size() > 9) {
                throw new JsonParseException("Too many ingredients for shapeless recipe");
            } else {
                ItemStack var3 = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(param1, "result"));
                return new ShapelessRecipe(param0, var0, var1, var3, var2);
            }
        }

        private static NonNullList<Ingredient> itemsFromJson(JsonArray param0) {
            NonNullList<Ingredient> var0 = NonNullList.create();

            for(int var1 = 0; var1 < param0.size(); ++var1) {
                Ingredient var2 = Ingredient.fromJson(param0.get(var1));
                if (!var2.isEmpty()) {
                    var0.add(var2);
                }
            }

            return var0;
        }

        public ShapelessRecipe fromNetwork(ResourceLocation param0, FriendlyByteBuf param1) {
            String var0 = param1.readUtf();
            CraftingBookCategory var1 = param1.readEnum(CraftingBookCategory.class);
            int var2 = param1.readVarInt();
            NonNullList<Ingredient> var3 = NonNullList.withSize(var2, Ingredient.EMPTY);

            for(int var4 = 0; var4 < var3.size(); ++var4) {
                var3.set(var4, Ingredient.fromNetwork(param1));
            }

            ItemStack var5 = param1.readItem();
            return new ShapelessRecipe(param0, var0, var1, var5, var3);
        }

        public void toNetwork(FriendlyByteBuf param0, ShapelessRecipe param1) {
            param0.writeUtf(param1.group);
            param0.writeEnum(param1.category);
            param0.writeVarInt(param1.ingredients.size());

            for(Ingredient var0 : param1.ingredients) {
                var0.toNetwork(param0);
            }

            param0.writeItem(param1.result);
        }
    }
}
