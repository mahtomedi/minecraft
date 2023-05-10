package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BitmapProvider implements GlyphProvider {
    static final Logger LOGGER = LogUtils.getLogger();
    private final NativeImage image;
    private final CodepointMap<BitmapProvider.Glyph> glyphs;

    BitmapProvider(NativeImage param0, CodepointMap<BitmapProvider.Glyph> param1) {
        this.image = param0;
        this.glyphs = param1;
    }

    @Override
    public void close() {
        this.image.close();
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
    public static record Definition(ResourceLocation file, int height, int ascent, int[][] codepointGrid) implements GlyphProviderDefinition {
        private static final Codec<int[][]> CODEPOINT_GRID_CODEC = ExtraCodecs.validate(Codec.STRING.listOf().xmap(param0 -> {
            int var0 = param0.size();
            int[][] var1 = new int[var0][];

            for(int var2 = 0; var2 < var0; ++var2) {
                var1[var2] = param0.get(var2).codePoints().toArray();
            }

            return var1;
        }, param0 -> {
            List<String> var0 = new ArrayList<>(param0.length);

            for(int[] var1 : param0) {
                var0.add(new String(var1, 0, var1.length));
            }

            return var0;
        }), BitmapProvider.Definition::validateDimensions);
        public static final MapCodec<BitmapProvider.Definition> CODEC = ExtraCodecs.validate(
            RecordCodecBuilder.mapCodec(
                param0 -> param0.group(
                            ResourceLocation.CODEC.fieldOf("file").forGetter(BitmapProvider.Definition::file),
                            Codec.INT.optionalFieldOf("height", Integer.valueOf(8)).forGetter(BitmapProvider.Definition::height),
                            Codec.INT.fieldOf("ascent").forGetter(BitmapProvider.Definition::ascent),
                            CODEPOINT_GRID_CODEC.fieldOf("chars").forGetter(BitmapProvider.Definition::codepointGrid)
                        )
                        .apply(param0, BitmapProvider.Definition::new)
            ),
            BitmapProvider.Definition::validate
        );

        private static DataResult<int[][]> validateDimensions(int[][] param0) {
            int var0 = param0.length;
            if (var0 == 0) {
                return DataResult.error(() -> "Expected to find data in codepoint grid");
            } else {
                int[] var1 = param0[0];
                int var2 = var1.length;
                if (var2 == 0) {
                    return DataResult.error(() -> "Expected to find data in codepoint grid");
                } else {
                    for(int var3 = 1; var3 < var0; ++var3) {
                        int[] var4 = param0[var3];
                        if (var4.length != var2) {
                            return DataResult.error(
                                () -> "Lines in codepoint grid have to be the same length (found: "
                                        + var4.length
                                        + " codepoints, expected: "
                                        + var2
                                        + "), pad with \\u0000"
                            );
                        }
                    }

                    return DataResult.success(param0);
                }
            }
        }

        private static DataResult<BitmapProvider.Definition> validate(BitmapProvider.Definition param0) {
            return param0.ascent > param0.height
                ? DataResult.error(() -> "Ascent " + param0.ascent + " higher than height " + param0.height)
                : DataResult.success(param0);
        }

        @Override
        public GlyphProviderType type() {
            return GlyphProviderType.BITMAP;
        }

        @Override
        public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
            return Either.left(this::load);
        }

        private GlyphProvider load(ResourceManager param0) throws IOException {
            ResourceLocation var0 = this.file.withPrefix("textures/");

            BitmapProvider var22;
            try (InputStream var1 = param0.open(var0)) {
                NativeImage var2 = NativeImage.read(NativeImage.Format.RGBA, var1);
                int var3 = var2.getWidth();
                int var4 = var2.getHeight();
                int var5 = var3 / this.codepointGrid[0].length;
                int var6 = var4 / this.codepointGrid.length;
                float var7 = (float)this.height / (float)var6;
                CodepointMap<BitmapProvider.Glyph> var8 = new CodepointMap<>(
                    param0x -> new BitmapProvider.Glyph[param0x], param0x -> new BitmapProvider.Glyph[param0x][]
                );

                for(int var9 = 0; var9 < this.codepointGrid.length; ++var9) {
                    int var10 = 0;

                    for(int var11 : this.codepointGrid[var9]) {
                        int var12 = var10++;
                        if (var11 != 0) {
                            int var13 = this.getActualGlyphWidth(var2, var5, var6, var12, var9);
                            BitmapProvider.Glyph var14 = var8.put(
                                var11,
                                new BitmapProvider.Glyph(
                                    var7, var2, var12 * var5, var9 * var6, var5, var6, (int)(0.5 + (double)((float)var13 * var7)) + 1, this.ascent
                                )
                            );
                            if (var14 != null) {
                                BitmapProvider.LOGGER.warn("Codepoint '{}' declared multiple times in {}", Integer.toHexString(var11), var0);
                            }
                        }
                    }
                }

                var22 = new BitmapProvider(var2, var8);
            }

            return var22;
        }

        private int getActualGlyphWidth(NativeImage param0, int param1, int param2, int param3, int param4) {
            int var0;
            for(var0 = param1 - 1; var0 >= 0; --var0) {
                int var1 = param3 * param1 + var0;

                for(int var2 = 0; var2 < param2; ++var2) {
                    int var3 = param4 * param2 + var2;
                    if (param0.getLuminanceOrAlpha(var1, var3) != 0) {
                        return var0 + 1;
                    }
                }
            }

            return var0 + 1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record Glyph(float scale, NativeImage image, int offsetX, int offsetY, int width, int height, int advance, int ascent) implements GlyphInfo {
        @Override
        public float getAdvance() {
            return (float)this.advance;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> param0) {
            return param0.apply(new SheetGlyphInfo() {
                @Override
                public float getOversample() {
                    return 1.0F / Glyph.this.scale;
                }

                @Override
                public int getPixelWidth() {
                    return Glyph.this.width;
                }

                @Override
                public int getPixelHeight() {
                    return Glyph.this.height;
                }

                @Override
                public float getBearingY() {
                    return SheetGlyphInfo.super.getBearingY() + 7.0F - (float)Glyph.this.ascent;
                }

                @Override
                public void upload(int param0, int param1) {
                    Glyph.this.image.upload(0, param0, param1, Glyph.this.offsetX, Glyph.this.offsetY, Glyph.this.width, Glyph.this.height, false, false);
                }

                @Override
                public boolean isColored() {
                    return Glyph.this.image.format().components() > 1;
                }
            });
        }
    }
}
