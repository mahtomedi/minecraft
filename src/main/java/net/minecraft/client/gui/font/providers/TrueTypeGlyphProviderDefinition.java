package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.TrueTypeGlyphProvider;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public record TrueTypeGlyphProviderDefinition(ResourceLocation location, float size, float oversample, TrueTypeGlyphProviderDefinition.Shift shift, String skip)
    implements GlyphProviderDefinition {
    private static final Codec<String> SKIP_LIST_CODEC = ExtraCodecs.withAlternative(Codec.STRING, Codec.STRING.listOf(), param0 -> String.join("", param0));
    public static final MapCodec<TrueTypeGlyphProviderDefinition> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    ResourceLocation.CODEC.fieldOf("file").forGetter(TrueTypeGlyphProviderDefinition::location),
                    Codec.FLOAT.optionalFieldOf("size", Float.valueOf(11.0F)).forGetter(TrueTypeGlyphProviderDefinition::size),
                    Codec.FLOAT.optionalFieldOf("oversample", Float.valueOf(1.0F)).forGetter(TrueTypeGlyphProviderDefinition::oversample),
                    TrueTypeGlyphProviderDefinition.Shift.CODEC
                        .optionalFieldOf("shift", TrueTypeGlyphProviderDefinition.Shift.NONE)
                        .forGetter(TrueTypeGlyphProviderDefinition::shift),
                    SKIP_LIST_CODEC.optionalFieldOf("skip", "").forGetter(TrueTypeGlyphProviderDefinition::skip)
                )
                .apply(param0, TrueTypeGlyphProviderDefinition::new)
    );

    @Override
    public GlyphProviderType type() {
        return GlyphProviderType.TTF;
    }

    @Override
    public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
        return Either.left(this::load);
    }

    private GlyphProvider load(ResourceManager param0) throws IOException {
        STBTTFontinfo var0 = null;
        ByteBuffer var1 = null;

        try {
            TrueTypeGlyphProvider var5;
            try (InputStream var2 = param0.open(this.location.withPrefix("font/"))) {
                var0 = STBTTFontinfo.malloc();
                var1 = TextureUtil.readResource(var2);
                var1.flip();
                if (!STBTruetype.stbtt_InitFont(var0, var1)) {
                    throw new IOException("Invalid ttf");
                }

                var5 = new TrueTypeGlyphProvider(var1, var0, this.size, this.oversample, this.shift.x, this.shift.y, this.skip);
            }

            return var5;
        } catch (Exception var9) {
            if (var0 != null) {
                var0.free();
            }

            MemoryUtil.memFree(var1);
            throw var9;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Shift(float x, float y) {
        public static final TrueTypeGlyphProviderDefinition.Shift NONE = new TrueTypeGlyphProviderDefinition.Shift(0.0F, 0.0F);
        public static final Codec<TrueTypeGlyphProviderDefinition.Shift> CODEC = Codec.FLOAT
            .listOf()
            .comapFlatMap(
                param0 -> Util.fixedSize(param0, 2).map(param0x -> new TrueTypeGlyphProviderDefinition.Shift(param0x.get(0), param0x.get(1))),
                param0 -> List.of(param0.x, param0.y)
            );
    }
}
