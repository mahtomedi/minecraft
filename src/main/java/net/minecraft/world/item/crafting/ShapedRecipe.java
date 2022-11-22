package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ShapedRecipe implements CraftingRecipe {
    final int width;
    final int height;
    final NonNullList<Ingredient> recipeItems;
    final ItemStack result;
    private final ResourceLocation id;
    final String group;
    final CraftingBookCategory category;

    public ShapedRecipe(
        ResourceLocation param0, String param1, CraftingBookCategory param2, int param3, int param4, NonNullList<Ingredient> param5, ItemStack param6
    ) {
        this.id = param0;
        this.group = param1;
        this.category = param2;
        this.width = param3;
        this.height = param4;
        this.recipeItems = param5;
        this.result = param6;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.SHAPED_RECIPE;
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
        return this.recipeItems;
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 >= this.width && param1 >= this.height;
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        for(int var0 = 0; var0 <= param0.getWidth() - this.width; ++var0) {
            for(int var1 = 0; var1 <= param0.getHeight() - this.height; ++var1) {
                if (this.matches(param0, var0, var1, true)) {
                    return true;
                }

                if (this.matches(param0, var0, var1, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matches(CraftingContainer param0, int param1, int param2, boolean param3) {
        for(int var0 = 0; var0 < param0.getWidth(); ++var0) {
            for(int var1 = 0; var1 < param0.getHeight(); ++var1) {
                int var2 = var0 - param1;
                int var3 = var1 - param2;
                Ingredient var4 = Ingredient.EMPTY;
                if (var2 >= 0 && var3 >= 0 && var2 < this.width && var3 < this.height) {
                    if (param3) {
                        var4 = this.recipeItems.get(this.width - var2 - 1 + var3 * this.width);
                    } else {
                        var4 = this.recipeItems.get(var2 + var3 * this.width);
                    }
                }

                if (!var4.test(param0.getItem(var0 + var1 * param0.getWidth()))) {
                    return false;
                }
            }
        }

        return true;
    }

    public ItemStack assemble(CraftingContainer param0) {
        return this.getResultItem().copy();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    static NonNullList<Ingredient> dissolvePattern(String[] param0, Map<String, Ingredient> param1, int param2, int param3) {
        NonNullList<Ingredient> var0 = NonNullList.withSize(param2 * param3, Ingredient.EMPTY);
        Set<String> var1 = Sets.newHashSet(param1.keySet());
        var1.remove(" ");

        for(int var2 = 0; var2 < param0.length; ++var2) {
            for(int var3 = 0; var3 < param0[var2].length(); ++var3) {
                String var4 = param0[var2].substring(var3, var3 + 1);
                Ingredient var5 = param1.get(var4);
                if (var5 == null) {
                    throw new JsonSyntaxException("Pattern references symbol '" + var4 + "' but it's not defined in the key");
                }

                var1.remove(var4);
                var0.set(var3 + param2 * var2, var5);
            }
        }

        if (!var1.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + var1);
        } else {
            return var0;
        }
    }

    @VisibleForTesting
    static String[] shrink(String... param0) {
        int var0 = Integer.MAX_VALUE;
        int var1 = 0;
        int var2 = 0;
        int var3 = 0;

        for(int var4 = 0; var4 < param0.length; ++var4) {
            String var5 = param0[var4];
            var0 = Math.min(var0, firstNonSpace(var5));
            int var6 = lastNonSpace(var5);
            var1 = Math.max(var1, var6);
            if (var6 < 0) {
                if (var2 == var4) {
                    ++var2;
                }

                ++var3;
            } else {
                var3 = 0;
            }
        }

        if (param0.length == var3) {
            return new String[0];
        } else {
            String[] var7 = new String[param0.length - var3 - var2];

            for(int var8 = 0; var8 < var7.length; ++var8) {
                var7[var8] = param0[var8 + var2].substring(var0, var1 + 1);
            }

            return var7;
        }
    }

    @Override
    public boolean isIncomplete() {
        NonNullList<Ingredient> var0 = this.getIngredients();
        return var0.isEmpty() || var0.stream().filter(param0 -> !param0.isEmpty()).anyMatch(param0 -> param0.getItems().length == 0);
    }

    private static int firstNonSpace(String param0) {
        int var0 = 0;

        while(var0 < param0.length() && param0.charAt(var0) == ' ') {
            ++var0;
        }

        return var0;
    }

    private static int lastNonSpace(String param0) {
        int var0 = param0.length() - 1;

        while(var0 >= 0 && param0.charAt(var0) == ' ') {
            --var0;
        }

        return var0;
    }

    static String[] patternFromJson(JsonArray param0) {
        String[] var0 = new String[param0.size()];
        if (var0.length > 3) {
            throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
        } else if (var0.length == 0) {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        } else {
            for(int var1 = 0; var1 < var0.length; ++var1) {
                String var2 = GsonHelper.convertToString(param0.get(var1), "pattern[" + var1 + "]");
                if (var2.length() > 3) {
                    throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
                }

                if (var1 > 0 && var0[0].length() != var2.length()) {
                    throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
                }

                var0[var1] = var2;
            }

            return var0;
        }
    }

    static Map<String, Ingredient> keyFromJson(JsonObject param0) {
        Map<String, Ingredient> var0 = Maps.newHashMap();

        for(Entry<String, JsonElement> var1 : param0.entrySet()) {
            if (var1.getKey().length() != 1) {
                throw new JsonSyntaxException("Invalid key entry: '" + (String)var1.getKey() + "' is an invalid symbol (must be 1 character only).");
            }

            if (" ".equals(var1.getKey())) {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }

            var0.put(var1.getKey(), Ingredient.fromJson(var1.getValue()));
        }

        var0.put(" ", Ingredient.EMPTY);
        return var0;
    }

    public static ItemStack itemStackFromJson(JsonObject param0) {
        Item var0 = itemFromJson(param0);
        if (param0.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
        } else {
            int var1 = GsonHelper.getAsInt(param0, "count", 1);
            if (var1 < 1) {
                throw new JsonSyntaxException("Invalid output count: " + var1);
            } else {
                return new ItemStack(var0, var1);
            }
        }
    }

    public static Item itemFromJson(JsonObject param0) {
        String var0 = GsonHelper.getAsString(param0, "item");
        Item var1 = BuiltInRegistries.ITEM.getOptional(new ResourceLocation(var0)).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + var0 + "'"));
        if (var1 == Items.AIR) {
            throw new JsonSyntaxException("Invalid item: " + var0);
        } else {
            return var1;
        }
    }

    public static class Serializer implements RecipeSerializer<ShapedRecipe> {
        public ShapedRecipe fromJson(ResourceLocation param0, JsonObject param1) {
            String var0 = GsonHelper.getAsString(param1, "group", "");
            CraftingBookCategory var1 = (CraftingBookCategory)Objects.requireNonNullElse(
                CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(param1, "category", null)), CraftingBookCategory.MISC
            );
            Map<String, Ingredient> var2 = ShapedRecipe.keyFromJson(GsonHelper.getAsJsonObject(param1, "key"));
            String[] var3 = ShapedRecipe.shrink(ShapedRecipe.patternFromJson(GsonHelper.getAsJsonArray(param1, "pattern")));
            int var4 = var3[0].length();
            int var5 = var3.length;
            NonNullList<Ingredient> var6 = ShapedRecipe.dissolvePattern(var3, var2, var4, var5);
            ItemStack var7 = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(param1, "result"));
            return new ShapedRecipe(param0, var0, var1, var4, var5, var6, var7);
        }

        public ShapedRecipe fromNetwork(ResourceLocation param0, FriendlyByteBuf param1) {
            int var0 = param1.readVarInt();
            int var1 = param1.readVarInt();
            String var2 = param1.readUtf();
            CraftingBookCategory var3 = param1.readEnum(CraftingBookCategory.class);
            NonNullList<Ingredient> var4 = NonNullList.withSize(var0 * var1, Ingredient.EMPTY);

            for(int var5 = 0; var5 < var4.size(); ++var5) {
                var4.set(var5, Ingredient.fromNetwork(param1));
            }

            ItemStack var6 = param1.readItem();
            return new ShapedRecipe(param0, var2, var3, var0, var1, var4, var6);
        }

        public void toNetwork(FriendlyByteBuf param0, ShapedRecipe param1) {
            param0.writeVarInt(param1.width);
            param0.writeVarInt(param1.height);
            param0.writeUtf(param1.group);
            param0.writeEnum(param1.category);

            for(Ingredient var0 : param1.recipeItems) {
                var0.toNetwork(param0);
            }

            param0.writeItem(param1.result);
        }
    }
}
