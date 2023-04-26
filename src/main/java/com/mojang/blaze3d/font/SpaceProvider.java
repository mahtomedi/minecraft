package com.mojang.blaze3d.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMaps;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Arrays;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.providers.GlyphProviderBuilder;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpaceProvider implements GlyphProvider {
    private final Int2ObjectMap<GlyphInfo.SpaceGlyphInfo> glyphs;

    public SpaceProvider(Int2FloatMap param0) {
        this.glyphs = new Int2ObjectOpenHashMap<>(param0.size());
        Int2FloatMaps.fastForEach(param0, param0x -> {
            float var0 = param0x.getFloatValue();
            this.glyphs.put(param0x.getIntKey(), () -> var0);
        });
    }

    @Nullable
    @Override
    public GlyphInfo getGlyph(int param0) {
        return this.glyphs.get(param0);
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return IntSets.unmodifiable(this.glyphs.keySet());
    }

    public static GlyphProviderBuilder builderFromJson(JsonObject param0) {
        Int2FloatMap var0 = new Int2FloatOpenHashMap();
        JsonObject var1 = GsonHelper.getAsJsonObject(param0, "advances");

        for(Entry<String, JsonElement> var2 : var1.entrySet()) {
            int[] var3 = var2.getKey().codePoints().toArray();
            if (var3.length != 1) {
                throw new JsonParseException("Expected single codepoint, got " + Arrays.toString(var3));
            }

            float var4 = GsonHelper.convertToFloat(var2.getValue(), "advance");
            var0.put(var3[0], var4);
        }

        GlyphProviderBuilder.Loader var5 = param1 -> new SpaceProvider(var0);
        return () -> Either.left(var5);
    }
}
