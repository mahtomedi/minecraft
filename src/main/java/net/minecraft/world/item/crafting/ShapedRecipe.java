package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.NotImplementedException;

public class ShapedRecipe implements CraftingRecipe {
    final int width;
    final int height;
    final NonNullList<Ingredient> recipeItems;
    final ItemStack result;
    final String group;
    final CraftingBookCategory category;
    final boolean showNotification;

    public ShapedRecipe(String param0, CraftingBookCategory param1, int param2, int param3, NonNullList<Ingredient> param4, ItemStack param5, boolean param6) {
        this.group = param0;
        this.category = param1;
        this.width = param2;
        this.height = param3;
        this.recipeItems = param4;
        this.result = param5;
        this.showNotification = param6;
    }

    public ShapedRecipe(String param0, CraftingBookCategory param1, int param2, int param3, NonNullList<Ingredient> param4, ItemStack param5) {
        this(param0, param1, param2, param3, param4, param5, true);
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
    public ItemStack getResultItem(RegistryAccess param0) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return this.recipeItems;
    }

    @Override
    public boolean showNotification() {
        return this.showNotification;
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

    public ItemStack assemble(CraftingContainer param0, RegistryAccess param1) {
        return this.getResultItem(param1).copy();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @VisibleForTesting
    static String[] shrink(List<String> param0) {
        int var0 = Integer.MAX_VALUE;
        int var1 = 0;
        int var2 = 0;
        int var3 = 0;

        for(int var4 = 0; var4 < param0.size(); ++var4) {
            String var5 = param0.get(var4);
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

        if (param0.size() == var3) {
            return new String[0];
        } else {
            String[] var7 = new String[param0.size() - var3 - var2];

            for(int var8 = 0; var8 < var7.length; ++var8) {
                var7[var8] = param0.get(var8 + var2).substring(var0, var1 + 1);
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

    public static class Serializer implements RecipeSerializer<ShapedRecipe> {
        static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().flatXmap(param0 -> {
            if (param0.size() > 3) {
                return DataResult.error(() -> "Invalid pattern: too many rows, 3 is maximum");
            } else if (param0.isEmpty()) {
                return DataResult.error(() -> "Invalid pattern: empty pattern not allowed");
            } else {
                int var0 = param0.get(0).length();

                for(String var1 : param0) {
                    if (var1.length() > 3) {
                        return DataResult.error(() -> "Invalid pattern: too many columns, 3 is maximum");
                    }

                    if (var0 != var1.length()) {
                        return DataResult.error(() -> "Invalid pattern: each row must be the same width");
                    }
                }

                return DataResult.success(param0);
            }
        }, DataResult::success);
        static final Codec<String> SINGLE_CHARACTER_STRING_CODEC = Codec.STRING.flatXmap(param0 -> {
            if (param0.length() != 1) {
                return DataResult.error(() -> "Invalid key entry: '" + param0 + "' is an invalid symbol (must be 1 character only).");
            } else {
                return " ".equals(param0) ? DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.") : DataResult.success(param0);
            }
        }, DataResult::success);
        private static final Codec<ShapedRecipe> CODEC = ShapedRecipe.Serializer.RawShapedRecipe.CODEC.flatXmap(param0 -> {
            String[] var0 = ShapedRecipe.shrink(param0.pattern);
            int var1 = var0[0].length();
            int var2 = var0.length;
            NonNullList<Ingredient> var3 = NonNullList.withSize(var1 * var2, Ingredient.EMPTY);
            Set<String> var4 = Sets.newHashSet(param0.key.keySet());

            for(int var5 = 0; var5 < var0.length; ++var5) {
                String var6 = var0[var5];

                for(int var7 = 0; var7 < var6.length(); ++var7) {
                    String var8 = var6.substring(var7, var7 + 1);
                    Ingredient var9 = var8.equals(" ") ? Ingredient.EMPTY : param0.key.get(var8);
                    if (var9 == null) {
                        return DataResult.error(() -> "Pattern references symbol '" + var8 + "' but it's not defined in the key");
                    }

                    var4.remove(var8);
                    var3.set(var7 + var1 * var5, var9);
                }
            }

            if (!var4.isEmpty()) {
                return DataResult.error(() -> "Key defines symbols that aren't used in pattern: " + var4);
            } else {
                ShapedRecipe var10 = new ShapedRecipe(param0.group, param0.category, var1, var2, var3, param0.result, param0.showNotification);
                return DataResult.success(var10);
            }
        }, param0 -> {
            throw new NotImplementedException("Serializing ShapedRecipe is not implemented yet.");
        });

        @Override
        public Codec<ShapedRecipe> codec() {
            return CODEC;
        }

        public ShapedRecipe fromNetwork(FriendlyByteBuf param0) {
            int var0 = param0.readVarInt();
            int var1 = param0.readVarInt();
            String var2 = param0.readUtf();
            CraftingBookCategory var3 = param0.readEnum(CraftingBookCategory.class);
            NonNullList<Ingredient> var4 = NonNullList.withSize(var0 * var1, Ingredient.EMPTY);

            for(int var5 = 0; var5 < var4.size(); ++var5) {
                var4.set(var5, Ingredient.fromNetwork(param0));
            }

            ItemStack var6 = param0.readItem();
            boolean var7 = param0.readBoolean();
            return new ShapedRecipe(var2, var3, var0, var1, var4, var6, var7);
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
            param0.writeBoolean(param1.showNotification);
        }

        static record RawShapedRecipe(
            String group, CraftingBookCategory category, Map<String, Ingredient> key, List<String> pattern, ItemStack result, boolean showNotification
        ) {
            public static final Codec<ShapedRecipe.Serializer.RawShapedRecipe> CODEC = RecordCodecBuilder.create(
                param0 -> param0.group(
                            ExtraCodecs.strictOptionalField(Codec.STRING, "group", "").forGetter(param0x -> param0x.group),
                            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(param0x -> param0x.category),
                            ExtraCodecs.strictUnboundedMap(ShapedRecipe.Serializer.SINGLE_CHARACTER_STRING_CODEC, Ingredient.CODEC_NONEMPTY)
                                .fieldOf("key")
                                .forGetter(param0x -> param0x.key),
                            ShapedRecipe.Serializer.PATTERN_CODEC.fieldOf("pattern").forGetter(param0x -> param0x.pattern),
                            CraftingRecipeCodecs.ITEMSTACK_OBJECT_CODEC.fieldOf("result").forGetter(param0x -> param0x.result),
                            ExtraCodecs.strictOptionalField(Codec.BOOL, "show_notification", true).forGetter(param0x -> param0x.showNotification)
                        )
                        .apply(param0, ShapedRecipe.Serializer.RawShapedRecipe::new)
            );
        }
    }
}
