package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.inventory.CraftingContainer;

public record ShapedRecipePattern(int width, int height, NonNullList<Ingredient> ingredients, Optional<ShapedRecipePattern.Data> data) {
    private static final int MAX_SIZE = 3;
    public static final MapCodec<ShapedRecipePattern> MAP_CODEC = ShapedRecipePattern.Data.MAP_CODEC
        .flatXmap(
            ShapedRecipePattern::unpack,
            param0 -> param0.data().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Cannot encode unpacked recipe"))
        );

    public static ShapedRecipePattern of(Map<Character, Ingredient> param0, String... param1) {
        return of(param0, List.of(param1));
    }

    public static ShapedRecipePattern of(Map<Character, Ingredient> param0, List<String> param1) {
        ShapedRecipePattern.Data var0 = new ShapedRecipePattern.Data(param0, param1);
        return Util.getOrThrow(unpack(var0), IllegalArgumentException::new);
    }

    private static DataResult<ShapedRecipePattern> unpack(ShapedRecipePattern.Data param0) {
        String[] var0 = shrink(param0.pattern);
        int var1 = var0[0].length();
        int var2 = var0.length;
        NonNullList<Ingredient> var3 = NonNullList.withSize(var1 * var2, Ingredient.EMPTY);
        CharSet var4 = new CharArraySet(param0.key.keySet());

        for(int var5 = 0; var5 < var0.length; ++var5) {
            String var6 = var0[var5];

            for(int var7 = 0; var7 < var6.length(); ++var7) {
                char var8 = var6.charAt(var7);
                Ingredient var9 = var8 == ' ' ? Ingredient.EMPTY : param0.key.get(var8);
                if (var9 == null) {
                    return DataResult.error(() -> "Pattern references symbol '" + var8 + "' but it's not defined in the key");
                }

                var4.remove(var8);
                var3.set(var7 + var1 * var5, var9);
            }
        }

        return !var4.isEmpty()
            ? DataResult.error(() -> "Key defines symbols that aren't used in pattern: " + var4)
            : DataResult.success(new ShapedRecipePattern(var1, var2, var3, Optional.of(param0)));
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

    public boolean matches(CraftingContainer param0) {
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
                        var4 = this.ingredients.get(this.width - var2 - 1 + var3 * this.width);
                    } else {
                        var4 = this.ingredients.get(var2 + var3 * this.width);
                    }
                }

                if (!var4.test(param0.getItem(var0 + var1 * param0.getWidth()))) {
                    return false;
                }
            }
        }

        return true;
    }

    public void toNetwork(FriendlyByteBuf param0) {
        param0.writeVarInt(this.width);
        param0.writeVarInt(this.height);

        for(Ingredient var0 : this.ingredients) {
            var0.toNetwork(param0);
        }

    }

    public static ShapedRecipePattern fromNetwork(FriendlyByteBuf param0) {
        int var0 = param0.readVarInt();
        int var1 = param0.readVarInt();
        NonNullList<Ingredient> var2 = NonNullList.withSize(var0 * var1, Ingredient.EMPTY);
        var2.replaceAll(param1 -> Ingredient.fromNetwork(param0));
        return new ShapedRecipePattern(var0, var1, var2, Optional.empty());
    }

    public static record Data(Map<Character, Ingredient> key, List<String> pattern) {
        private static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().comapFlatMap(param0 -> {
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
        }, Function.identity());
        private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap(param0 -> {
            if (param0.length() != 1) {
                return DataResult.error(() -> "Invalid key entry: '" + param0 + "' is an invalid symbol (must be 1 character only).");
            } else {
                return " ".equals(param0) ? DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.") : DataResult.success(param0.charAt(0));
            }
        }, String::valueOf);
        public static final MapCodec<ShapedRecipePattern.Data> MAP_CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(
                        ExtraCodecs.strictUnboundedMap(SYMBOL_CODEC, Ingredient.CODEC_NONEMPTY).fieldOf("key").forGetter(param0x -> param0x.key),
                        PATTERN_CODEC.fieldOf("pattern").forGetter(param0x -> param0x.pattern)
                    )
                    .apply(param0, ShapedRecipePattern.Data::new)
        );
    }
}
