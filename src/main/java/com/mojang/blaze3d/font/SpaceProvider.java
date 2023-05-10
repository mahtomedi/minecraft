package com.mojang.blaze3d.font;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpaceProvider implements GlyphProvider {
    private final Int2ObjectMap<GlyphInfo.SpaceGlyphInfo> glyphs;

    public SpaceProvider(Map<Integer, Float> param0) {
        this.glyphs = new Int2ObjectOpenHashMap<>(param0.size());
        param0.forEach((param0x, param1) -> this.glyphs.put(param0x.intValue(), () -> param1));
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

    @OnlyIn(Dist.CLIENT)
    public static record Definition(Map<Integer, Float> advances) implements GlyphProviderDefinition {
        public static final MapCodec<SpaceProvider.Definition> CODEC = RecordCodecBuilder.mapCodec(
            param0 -> param0.group(Codec.unboundedMap(ExtraCodecs.CODEPOINT, Codec.FLOAT).fieldOf("advances").forGetter(SpaceProvider.Definition::advances))
                    .apply(param0, SpaceProvider.Definition::new)
        );

        @Override
        public GlyphProviderType type() {
            return GlyphProviderType.SPACE;
        }

        @Override
        public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
            GlyphProviderDefinition.Loader var0 = param0 -> new SpaceProvider(this.advances);
            return Either.left(var0);
        }
    }
}
