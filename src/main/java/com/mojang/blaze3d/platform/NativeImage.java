package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public final class NativeImage implements AutoCloseable {
    private static final Set<StandardOpenOption> OPEN_OPTIONS = EnumSet.of(
        StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
    );
    private final NativeImage.Format format;
    private final int width;
    private final int height;
    private final boolean useStbFree;
    private long pixels;
    private final int size;

    public NativeImage(int param0, int param1, boolean param2) {
        this(NativeImage.Format.RGBA, param0, param1, param2);
    }

    public NativeImage(NativeImage.Format param0, int param1, int param2, boolean param3) {
        this.format = param0;
        this.width = param1;
        this.height = param2;
        this.size = param1 * param2 * param0.components();
        this.useStbFree = false;
        if (param3) {
            this.pixels = MemoryUtil.nmemCalloc(1L, (long)this.size);
        } else {
            this.pixels = MemoryUtil.nmemAlloc((long)this.size);
        }

    }

    private NativeImage(NativeImage.Format param0, int param1, int param2, boolean param3, long param4) {
        this.format = param0;
        this.width = param1;
        this.height = param2;
        this.useStbFree = param3;
        this.pixels = param4;
        this.size = param1 * param2 * param0.components();
    }

    @Override
    public String toString() {
        return "NativeImage[" + this.format + " " + this.width + "x" + this.height + "@" + this.pixels + (this.useStbFree ? "S" : "N") + "]";
    }

    public static NativeImage read(InputStream param0) throws IOException {
        return read(NativeImage.Format.RGBA, param0);
    }

    public static NativeImage read(@Nullable NativeImage.Format param0, InputStream param1) throws IOException {
        ByteBuffer var0 = null;

        NativeImage var3;
        try {
            var0 = TextureUtil.readResource(param1);
            ((Buffer)var0).rewind();
            var3 = read(param0, var0);
        } finally {
            MemoryUtil.memFree(var0);
            IOUtils.closeQuietly(param1);
        }

        return var3;
    }

    public static NativeImage read(ByteBuffer param0) throws IOException {
        return read(NativeImage.Format.RGBA, param0);
    }

    public static NativeImage read(@Nullable NativeImage.Format param0, ByteBuffer param1) throws IOException {
        if (param0 != null && !param0.supportedByStb()) {
            throw new UnsupportedOperationException("Don't know how to read format " + param0);
        } else if (MemoryUtil.memAddress(param1) == 0L) {
            throw new IllegalArgumentException("Invalid buffer");
        } else {
            NativeImage var8;
            try (MemoryStack var0 = MemoryStack.stackPush()) {
                IntBuffer var1 = var0.mallocInt(1);
                IntBuffer var2 = var0.mallocInt(1);
                IntBuffer var3 = var0.mallocInt(1);
                ByteBuffer var4 = STBImage.stbi_load_from_memory(param1, var1, var2, var3, param0 == null ? 0 : param0.components);
                if (var4 == null) {
                    throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
                }

                var8 = new NativeImage(
                    param0 == null ? NativeImage.Format.getStbFormat(var3.get(0)) : param0, var1.get(0), var2.get(0), true, MemoryUtil.memAddress(var4)
                );
            }

            return var8;
        }
    }

    private static void setClamp(boolean param0) {
        if (param0) {
            RenderSystem.texParameter(3553, 10242, 10496);
            RenderSystem.texParameter(3553, 10243, 10496);
        } else {
            RenderSystem.texParameter(3553, 10242, 10497);
            RenderSystem.texParameter(3553, 10243, 10497);
        }

    }

    private static void setFilter(boolean param0, boolean param1) {
        if (param0) {
            RenderSystem.texParameter(3553, 10241, param1 ? 9987 : 9729);
            RenderSystem.texParameter(3553, 10240, 9729);
        } else {
            RenderSystem.texParameter(3553, 10241, param1 ? 9986 : 9728);
            RenderSystem.texParameter(3553, 10240, 9728);
        }

    }

    private void checkAllocated() {
        if (this.pixels == 0L) {
            throw new IllegalStateException("Image is not allocated.");
        }
    }

    @Override
    public void close() {
        if (this.pixels != 0L) {
            if (this.useStbFree) {
                STBImage.nstbi_image_free(this.pixels);
            } else {
                MemoryUtil.nmemFree(this.pixels);
            }
        }

        this.pixels = 0L;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public NativeImage.Format format() {
        return this.format;
    }

    public int getPixelRGBA(int param0, int param1) {
        if (this.format != NativeImage.Format.RGBA) {
            throw new IllegalArgumentException(String.format("getPixelRGBA only works on RGBA images; have %s", this.format));
        } else if (param0 <= this.width && param1 <= this.height) {
            this.checkAllocated();
            return MemoryUtil.memIntBuffer(this.pixels, this.size).get(param0 + param1 * this.width);
        } else {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", param0, param1, this.width, this.height));
        }
    }

    public void setPixelRGBA(int param0, int param1, int param2) {
        if (this.format != NativeImage.Format.RGBA) {
            throw new IllegalArgumentException(String.format("getPixelRGBA only works on RGBA images; have %s", this.format));
        } else if (param0 <= this.width && param1 <= this.height) {
            this.checkAllocated();
            MemoryUtil.memIntBuffer(this.pixels, this.size).put(param0 + param1 * this.width, param2);
        } else {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", param0, param1, this.width, this.height));
        }
    }

    public byte getLuminanceOrAlpha(int param0, int param1) {
        if (!this.format.hasLuminanceOrAlpha()) {
            throw new IllegalArgumentException(String.format("no luminance or alpha in %s", this.format));
        } else if (param0 <= this.width && param1 <= this.height) {
            return MemoryUtil.memByteBuffer(this.pixels, this.size)
                .get((param0 + param1 * this.width) * this.format.components() + this.format.luminanceOrAlphaOffset() / 8);
        } else {
            throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", param0, param1, this.width, this.height));
        }
    }

    public void blendPixel(int param0, int param1, int param2) {
        if (this.format != NativeImage.Format.RGBA) {
            throw new UnsupportedOperationException("Can only call blendPixel with RGBA format");
        } else {
            int var0 = this.getPixelRGBA(param0, param1);
            float var1 = (float)(param2 >> 24 & 0xFF) / 255.0F;
            float var2 = (float)(param2 >> 16 & 0xFF) / 255.0F;
            float var3 = (float)(param2 >> 8 & 0xFF) / 255.0F;
            float var4 = (float)(param2 >> 0 & 0xFF) / 255.0F;
            float var5 = (float)(var0 >> 24 & 0xFF) / 255.0F;
            float var6 = (float)(var0 >> 16 & 0xFF) / 255.0F;
            float var7 = (float)(var0 >> 8 & 0xFF) / 255.0F;
            float var8 = (float)(var0 >> 0 & 0xFF) / 255.0F;
            float var10 = 1.0F - var1;
            float var11 = var1 * var1 + var5 * var10;
            float var12 = var2 * var1 + var6 * var10;
            float var13 = var3 * var1 + var7 * var10;
            float var14 = var4 * var1 + var8 * var10;
            if (var11 > 1.0F) {
                var11 = 1.0F;
            }

            if (var12 > 1.0F) {
                var12 = 1.0F;
            }

            if (var13 > 1.0F) {
                var13 = 1.0F;
            }

            if (var14 > 1.0F) {
                var14 = 1.0F;
            }

            int var15 = (int)(var11 * 255.0F);
            int var16 = (int)(var12 * 255.0F);
            int var17 = (int)(var13 * 255.0F);
            int var18 = (int)(var14 * 255.0F);
            this.setPixelRGBA(param0, param1, var15 << 24 | var16 << 16 | var17 << 8 | var18 << 0);
        }
    }

    @Deprecated
    public int[] makePixelArray() {
        if (this.format != NativeImage.Format.RGBA) {
            throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
        } else {
            this.checkAllocated();
            int[] var0 = new int[this.getWidth() * this.getHeight()];

            for(int var1 = 0; var1 < this.getHeight(); ++var1) {
                for(int var2 = 0; var2 < this.getWidth(); ++var2) {
                    int var3 = this.getPixelRGBA(var2, var1);
                    int var4 = var3 >> 24 & 0xFF;
                    int var5 = var3 >> 16 & 0xFF;
                    int var6 = var3 >> 8 & 0xFF;
                    int var7 = var3 >> 0 & 0xFF;
                    int var8 = var4 << 24 | var7 << 16 | var6 << 8 | var5;
                    var0[var2 + var1 * this.getWidth()] = var8;
                }
            }

            return var0;
        }
    }

    public void upload(int param0, int param1, int param2, boolean param3) {
        this.upload(param0, param1, param2, 0, 0, this.width, this.height, param3);
    }

    public void upload(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7) {
        this.upload(param0, param1, param2, param3, param4, param5, param6, false, false, param7);
    }

    public void upload(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, boolean param8, boolean param9) {
        this.checkAllocated();
        setFilter(param7, param9);
        setClamp(param8);
        if (param5 == this.getWidth()) {
            RenderSystem.pixelStore(3314, 0);
        } else {
            RenderSystem.pixelStore(3314, this.getWidth());
        }

        RenderSystem.pixelStore(3316, param3);
        RenderSystem.pixelStore(3315, param4);
        this.format.setUnpackPixelStoreState();
        RenderSystem.texSubImage2D(3553, param0, param1, param2, param5, param6, this.format.glFormat(), 5121, this.pixels);
    }

    public void downloadTexture(int param0, boolean param1) {
        this.checkAllocated();
        this.format.setPackPixelStoreState();
        RenderSystem.getTexImage(3553, param0, this.format.glFormat(), 5121, this.pixels);
        if (param1 && this.format.hasAlpha()) {
            for(int var0 = 0; var0 < this.getHeight(); ++var0) {
                for(int var1 = 0; var1 < this.getWidth(); ++var1) {
                    this.setPixelRGBA(var1, var0, this.getPixelRGBA(var1, var0) | 255 << this.format.alphaOffset());
                }
            }
        }

    }

    public void writeToFile(File param0) throws IOException {
        this.writeToFile(param0.toPath());
    }

    public void copyFromFont(
        STBTTFontinfo param0, int param1, int param2, int param3, float param4, float param5, float param6, float param7, int param8, int param9
    ) {
        if (param8 < 0 || param8 + param2 > this.getWidth() || param9 < 0 || param9 + param3 > this.getHeight()) {
            throw new IllegalArgumentException(
                String.format("Out of bounds: start: (%s, %s) (size: %sx%s); size: %sx%s", param8, param9, param2, param3, this.getWidth(), this.getHeight())
            );
        } else if (this.format.components() != 1) {
            throw new IllegalArgumentException("Can only write fonts into 1-component images.");
        } else {
            STBTruetype.nstbtt_MakeGlyphBitmapSubpixel(
                param0.address(),
                this.pixels + (long)param8 + (long)(param9 * this.getWidth()),
                param2,
                param3,
                this.getWidth(),
                param4,
                param5,
                param6,
                param7,
                param1
            );
        }
    }

    public void writeToFile(Path param0) throws IOException {
        if (!this.format.supportedByStb()) {
            throw new UnsupportedOperationException("Don't know how to write format " + this.format);
        } else {
            this.checkAllocated();

            try (WritableByteChannel var0 = Files.newByteChannel(param0, OPEN_OPTIONS)) {
                NativeImage.WriteCallback var1 = new NativeImage.WriteCallback(var0);

                try {
                    if (!STBImageWrite.stbi_write_png_to_func(
                        var1, 0L, this.getWidth(), this.getHeight(), this.format.components(), MemoryUtil.memByteBuffer(this.pixels, this.size), 0
                    )) {
                        throw new IOException("Could not write image to the PNG file \"" + param0.toAbsolutePath() + "\": " + STBImage.stbi_failure_reason());
                    }
                } finally {
                    var1.free();
                }

                var1.throwIfException();
            }

        }
    }

    public void copyFrom(NativeImage param0) {
        if (param0.format() != this.format) {
            throw new UnsupportedOperationException("Image formats don't match.");
        } else {
            int var0 = this.format.components();
            this.checkAllocated();
            param0.checkAllocated();
            if (this.width == param0.width) {
                MemoryUtil.memCopy(param0.pixels, this.pixels, (long)Math.min(this.size, param0.size));
            } else {
                int var1 = Math.min(this.getWidth(), param0.getWidth());
                int var2 = Math.min(this.getHeight(), param0.getHeight());

                for(int var3 = 0; var3 < var2; ++var3) {
                    int var4 = var3 * param0.getWidth() * var0;
                    int var5 = var3 * this.getWidth() * var0;
                    MemoryUtil.memCopy(param0.pixels + (long)var4, this.pixels + (long)var5, (long)var1);
                }
            }

        }
    }

    public void fillRect(int param0, int param1, int param2, int param3, int param4) {
        for(int var0 = param1; var0 < param1 + param3; ++var0) {
            for(int var1 = param0; var1 < param0 + param2; ++var1) {
                this.setPixelRGBA(var1, var0, param4);
            }
        }

    }

    public void copyRect(int param0, int param1, int param2, int param3, int param4, int param5, boolean param6, boolean param7) {
        for(int var0 = 0; var0 < param5; ++var0) {
            for(int var1 = 0; var1 < param4; ++var1) {
                int var2 = param6 ? param4 - 1 - var1 : var1;
                int var3 = param7 ? param5 - 1 - var0 : var0;
                int var4 = this.getPixelRGBA(param0 + var1, param1 + var0);
                this.setPixelRGBA(param0 + param2 + var2, param1 + param3 + var3, var4);
            }
        }

    }

    public void flipY() {
        this.checkAllocated();

        try (MemoryStack var0 = MemoryStack.stackPush()) {
            int var1 = this.format.components();
            int var2 = this.getWidth() * var1;
            long var3 = var0.nmalloc(var2);

            for(int var4 = 0; var4 < this.getHeight() / 2; ++var4) {
                int var5 = var4 * this.getWidth() * var1;
                int var6 = (this.getHeight() - 1 - var4) * this.getWidth() * var1;
                MemoryUtil.memCopy(this.pixels + (long)var5, var3, (long)var2);
                MemoryUtil.memCopy(this.pixels + (long)var6, this.pixels + (long)var5, (long)var2);
                MemoryUtil.memCopy(var3, this.pixels + (long)var6, (long)var2);
            }
        }

    }

    public void resizeSubRectTo(int param0, int param1, int param2, int param3, NativeImage param4) {
        this.checkAllocated();
        if (param4.format() != this.format) {
            throw new UnsupportedOperationException("resizeSubRectTo only works for images of the same format.");
        } else {
            int var0 = this.format.components();
            STBImageResize.nstbir_resize_uint8(
                this.pixels + (long)((param0 + param1 * this.getWidth()) * var0),
                param2,
                param3,
                this.getWidth() * var0,
                param4.pixels,
                param4.getWidth(),
                param4.getHeight(),
                0,
                var0
            );
        }
    }

    public void untrack() {
        DebugMemoryUntracker.untrack(this.pixels);
    }

    public static NativeImage fromBase64(String param0) throws IOException {
        NativeImage var6;
        try (MemoryStack var0 = MemoryStack.stackPush()) {
            ByteBuffer var1 = var0.UTF8(param0.replaceAll("\n", ""), false);
            ByteBuffer var2 = Base64.getDecoder().decode(var1);
            ByteBuffer var3 = var0.malloc(var2.remaining());
            var3.put(var2);
            ((Buffer)var3).rewind();
            var6 = read(var3);
        }

        return var6;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Format {
        RGBA(4, 6408, true, true, true, false, true, 0, 8, 16, 255, 24, true),
        RGB(3, 6407, true, true, true, false, false, 0, 8, 16, 255, 255, true),
        LUMINANCE_ALPHA(2, 6410, false, false, false, true, true, 255, 255, 255, 0, 8, true),
        LUMINANCE(1, 6409, false, false, false, true, false, 0, 0, 0, 0, 255, true);

        private final int components;
        private final int glFormat;
        private final boolean hasRed;
        private final boolean hasGreen;
        private final boolean hasBlue;
        private final boolean hasLuminance;
        private final boolean hasAlpha;
        private final int redOffset;
        private final int greenOffset;
        private final int blueOffset;
        private final int luminanceOffset;
        private final int alphaOffset;
        private final boolean supportedByStb;

        private Format(
            int param0,
            int param1,
            boolean param2,
            boolean param3,
            boolean param4,
            boolean param5,
            boolean param6,
            int param7,
            int param8,
            int param9,
            int param10,
            int param11,
            boolean param12
        ) {
            this.components = param0;
            this.glFormat = param1;
            this.hasRed = param2;
            this.hasGreen = param3;
            this.hasBlue = param4;
            this.hasLuminance = param5;
            this.hasAlpha = param6;
            this.redOffset = param7;
            this.greenOffset = param8;
            this.blueOffset = param9;
            this.luminanceOffset = param10;
            this.alphaOffset = param11;
            this.supportedByStb = param12;
        }

        public int components() {
            return this.components;
        }

        public void setPackPixelStoreState() {
            RenderSystem.pixelStore(3333, this.components());
        }

        public void setUnpackPixelStoreState() {
            RenderSystem.pixelStore(3317, this.components());
        }

        public int glFormat() {
            return this.glFormat;
        }

        public boolean hasAlpha() {
            return this.hasAlpha;
        }

        public int alphaOffset() {
            return this.alphaOffset;
        }

        public boolean hasLuminanceOrAlpha() {
            return this.hasLuminance || this.hasAlpha;
        }

        public int luminanceOrAlphaOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.alphaOffset;
        }

        public boolean supportedByStb() {
            return this.supportedByStb;
        }

        private static NativeImage.Format getStbFormat(int param0) {
            switch(param0) {
                case 1:
                    return LUMINANCE;
                case 2:
                    return LUMINANCE_ALPHA;
                case 3:
                    return RGB;
                case 4:
                default:
                    return RGBA;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum InternalGlFormat {
        RGBA(6408),
        RGB(6407),
        LUMINANCE_ALPHA(6410),
        LUMINANCE(6409),
        INTENSITY(32841);

        private final int glFormat;

        private InternalGlFormat(int param0) {
            this.glFormat = param0;
        }

        int glFormat() {
            return this.glFormat;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class WriteCallback extends STBIWriteCallback {
        private final WritableByteChannel output;
        private IOException exception;

        private WriteCallback(WritableByteChannel param0) {
            this.output = param0;
        }

        @Override
        public void invoke(long param0, long param1, int param2) {
            ByteBuffer var0 = getData(param1, param2);

            try {
                this.output.write(var0);
            } catch (IOException var8) {
                this.exception = var8;
            }

        }

        public void throwIfException() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
        }
    }
}
