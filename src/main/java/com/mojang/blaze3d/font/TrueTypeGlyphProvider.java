package com.mojang.blaze3d.font;

import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

@OnlyIn(Dist.CLIENT)
public class TrueTypeGlyphProvider implements GlyphProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final STBTTFontinfo font;
    private final float oversample;
    private final CharSet skip = new CharArraySet();
    private final float shiftX;
    private final float shiftY;
    private final float pointScale;
    private final float ascent;

    public TrueTypeGlyphProvider(STBTTFontinfo param0, float param1, float param2, float param3, float param4, String param5) {
        this.font = param0;
        this.oversample = param2;
        param5.chars().forEach(param0x -> this.skip.add((char)(param0x & 65535)));
        this.shiftX = param3 * param2;
        this.shiftY = param4 * param2;
        this.pointScale = STBTruetype.stbtt_ScaleForPixelHeight(param0, param1 * param2);

        try (MemoryStack var0 = MemoryStack.stackPush()) {
            IntBuffer var1 = var0.mallocInt(1);
            IntBuffer var2 = var0.mallocInt(1);
            IntBuffer var3 = var0.mallocInt(1);
            STBTruetype.stbtt_GetFontVMetrics(param0, var1, var2, var3);
            this.ascent = (float)var1.get(0) * this.pointScale;
        }

    }

    @Nullable
    public TrueTypeGlyphProvider.Glyph getGlyph(char param0) {
        if (this.skip.contains(param0)) {
            return null;
        } else {
            Object var6;
            try (MemoryStack var0 = MemoryStack.stackPush()) {
                IntBuffer var1 = var0.mallocInt(1);
                IntBuffer var2 = var0.mallocInt(1);
                IntBuffer var3 = var0.mallocInt(1);
                IntBuffer var4 = var0.mallocInt(1);
                int var5 = STBTruetype.stbtt_FindGlyphIndex(this.font, param0);
                if (var5 != 0) {
                    STBTruetype.stbtt_GetGlyphBitmapBoxSubpixel(
                        this.font, var5, this.pointScale, this.pointScale, this.shiftX, this.shiftY, var1, var2, var3, var4
                    );
                    int var6 = var3.get(0) - var1.get(0);
                    int var7 = var4.get(0) - var2.get(0);
                    if (var6 != 0 && var7 != 0) {
                        IntBuffer var8 = var0.mallocInt(1);
                        IntBuffer var9 = var0.mallocInt(1);
                        STBTruetype.stbtt_GetGlyphHMetrics(this.font, var5, var8, var9);
                        return new TrueTypeGlyphProvider.Glyph(
                            var1.get(0),
                            var3.get(0),
                            -var2.get(0),
                            -var4.get(0),
                            (float)var8.get(0) * this.pointScale,
                            (float)var9.get(0) * this.pointScale,
                            var5
                        );
                    }

                    return null;
                }

                var6 = null;
            }

            return (TrueTypeGlyphProvider.Glyph)var6;
        }
    }

    public static STBTTFontinfo getStbttFontinfo(ByteBuffer param0) throws IOException {
        STBTTFontinfo var0 = STBTTFontinfo.create();
        if (!STBTruetype.stbtt_InitFont(var0, param0)) {
            throw new IOException("Invalid ttf");
        } else {
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class Glyph implements RawGlyph {
        private final int width;
        private final int height;
        private final float bearingX;
        private final float bearingY;
        private final float advance;
        private final int index;

        private Glyph(int param0, int param1, int param2, int param3, float param4, float param5, int param6) {
            this.width = param1 - param0;
            this.height = param2 - param3;
            this.advance = param4 / TrueTypeGlyphProvider.this.oversample;
            this.bearingX = (param5 + (float)param0 + TrueTypeGlyphProvider.this.shiftX) / TrueTypeGlyphProvider.this.oversample;
            this.bearingY = (TrueTypeGlyphProvider.this.ascent - (float)param2 + TrueTypeGlyphProvider.this.shiftY) / TrueTypeGlyphProvider.this.oversample;
            this.index = param6;
        }

        @Override
        public int getPixelWidth() {
            return this.width;
        }

        @Override
        public int getPixelHeight() {
            return this.height;
        }

        @Override
        public float getOversample() {
            return TrueTypeGlyphProvider.this.oversample;
        }

        @Override
        public float getAdvance() {
            return this.advance;
        }

        @Override
        public float getBearingX() {
            return this.bearingX;
        }

        @Override
        public float getBearingY() {
            return this.bearingY;
        }

        @Override
        public void upload(int param0, int param1) {
            NativeImage var0 = new NativeImage(NativeImage.Format.LUMINANCE, this.width, this.height, false);
            var0.copyFromFont(
                TrueTypeGlyphProvider.this.font,
                this.index,
                this.width,
                this.height,
                TrueTypeGlyphProvider.this.pointScale,
                TrueTypeGlyphProvider.this.pointScale,
                TrueTypeGlyphProvider.this.shiftX,
                TrueTypeGlyphProvider.this.shiftY,
                0,
                0
            );
            var0.upload(0, param0, param1, 0, 0, this.width, this.height, false, true);
        }

        @Override
        public boolean isColored() {
            return false;
        }
    }
}
