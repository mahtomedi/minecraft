package com.mojang.blaze3d.font;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Function;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class TrueTypeGlyphProvider implements GlyphProvider {
    private final ByteBuffer fontMemory;
    final STBTTFontinfo font;
    final float oversample;
    private final IntSet skip = new IntArraySet();
    final float shiftX;
    final float shiftY;
    final float pointScale;
    final float ascent;

    public TrueTypeGlyphProvider(ByteBuffer param0, STBTTFontinfo param1, float param2, float param3, float param4, float param5, String param6) {
        this.fontMemory = param0;
        this.font = param1;
        this.oversample = param3;
        param6.codePoints().forEach(this.skip::add);
        this.shiftX = param4 * param3;
        this.shiftY = param5 * param3;
        this.pointScale = STBTruetype.stbtt_ScaleForPixelHeight(param1, param2 * param3);

        try (MemoryStack var0 = MemoryStack.stackPush()) {
            IntBuffer var1 = var0.mallocInt(1);
            IntBuffer var2 = var0.mallocInt(1);
            IntBuffer var3 = var0.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(param1, var1, var2, var3);
            this.ascent = (float)var1.get(0) * this.pointScale;
        }

    }

    @Nullable
    @Override
    public GlyphInfo getGlyph(int param0) {
        if (this.skip.contains(param0)) {
            return null;
        } else {
            GlyphInfo.SpaceGlyphInfo var13;
            try (MemoryStack var0 = MemoryStack.stackPush()) {
                int var1 = STBTruetype.stbtt_FindGlyphIndex(this.font, param0);
                if (var1 == 0) {
                    return null;
                }

                IntBuffer var2 = var0.mallocInt(1);
                IntBuffer var3 = var0.mallocInt(1);
                IntBuffer var4 = var0.mallocInt(1);
                IntBuffer var5 = var0.mallocInt(1);
                IntBuffer var6 = var0.mallocInt(1);
                IntBuffer var7 = var0.mallocInt(1);
                STBTruetype.stbtt_GetGlyphHMetrics(this.font, var1, var6, var7);
                STBTruetype.stbtt_GetGlyphBitmapBoxSubpixel(this.font, var1, this.pointScale, this.pointScale, this.shiftX, this.shiftY, var2, var3, var4, var5);
                float var8 = (float)var6.get(0) * this.pointScale;
                int var9 = var4.get(0) - var2.get(0);
                int var10 = var5.get(0) - var3.get(0);
                if (var9 > 0 && var10 > 0) {
                    return new TrueTypeGlyphProvider.Glyph(
                        var2.get(0), var4.get(0), -var3.get(0), -var5.get(0), var8, (float)var7.get(0) * this.pointScale, var1
                    );
                }

                var13 = () -> var8 / this.oversample;
            }

            return var13;
        }
    }

    @Override
    public void close() {
        this.font.free();
        MemoryUtil.memFree(this.fontMemory);
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return IntStream.range(0, 65535).filter(param0 -> !this.skip.contains(param0)).collect(IntOpenHashSet::new, IntCollection::add, IntCollection::addAll);
    }

    @OnlyIn(Dist.CLIENT)
    class Glyph implements GlyphInfo {
        final int width;
        final int height;
        final float bearingX;
        final float bearingY;
        private final float advance;
        final int index;

        Glyph(int param0, int param1, int param2, int param3, float param4, float param5, int param6) {
            this.width = param1 - param0;
            this.height = param2 - param3;
            this.advance = param4 / TrueTypeGlyphProvider.this.oversample;
            this.bearingX = (param5 + (float)param0 + TrueTypeGlyphProvider.this.shiftX) / TrueTypeGlyphProvider.this.oversample;
            this.bearingY = (TrueTypeGlyphProvider.this.ascent - (float)param2 + TrueTypeGlyphProvider.this.shiftY) / TrueTypeGlyphProvider.this.oversample;
            this.index = param6;
        }

        @Override
        public float getAdvance() {
            return this.advance;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> param0) {
            return param0.apply(
                new SheetGlyphInfo() {
                    @Override
                    public int getPixelWidth() {
                        return Glyph.this.width;
                    }
    
                    @Override
                    public int getPixelHeight() {
                        return Glyph.this.height;
                    }
    
                    @Override
                    public float getOversample() {
                        return TrueTypeGlyphProvider.this.oversample;
                    }
    
                    @Override
                    public float getBearingX() {
                        return Glyph.this.bearingX;
                    }
    
                    @Override
                    public float getBearingY() {
                        return Glyph.this.bearingY;
                    }
    
                    @Override
                    public void upload(int param0, int param1) {
                        NativeImage var0 = new NativeImage(NativeImage.Format.LUMINANCE, Glyph.this.width, Glyph.this.height, false);
                        var0.copyFromFont(
                            TrueTypeGlyphProvider.this.font,
                            Glyph.this.index,
                            Glyph.this.width,
                            Glyph.this.height,
                            TrueTypeGlyphProvider.this.pointScale,
                            TrueTypeGlyphProvider.this.pointScale,
                            TrueTypeGlyphProvider.this.shiftX,
                            TrueTypeGlyphProvider.this.shiftY,
                            0,
                            0
                        );
                        var0.upload(0, param0, param1, 0, 0, Glyph.this.width, Glyph.this.height, false, true);
                    }
    
                    @Override
                    public boolean isColored() {
                        return false;
                    }
                }
            );
        }
    }
}
