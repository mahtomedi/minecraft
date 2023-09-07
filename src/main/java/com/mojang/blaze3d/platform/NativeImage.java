package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.util.FastColor;
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
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public final class NativeImage implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<StandardOpenOption> OPEN_OPTIONS = EnumSet.of(
        StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
    );
    private final NativeImage.Format format;
    private final int width;
    private final int height;
    private final boolean useStbFree;
    private long pixels;
    private final long size;

    public NativeImage(int param0, int param1, boolean param2) {
        this(NativeImage.Format.RGBA, param0, param1, param2);
    }

    public NativeImage(NativeImage.Format param0, int param1, int param2, boolean param3) {
        if (param1 > 0 && param2 > 0) {
            this.format = param0;
            this.width = param1;
            this.height = param2;
            this.size = (long)param1 * (long)param2 * (long)param0.components();
            this.useStbFree = false;
            if (param3) {
                this.pixels = MemoryUtil.nmemCalloc(1L, this.size);
            } else {
                this.pixels = MemoryUtil.nmemAlloc(this.size);
            }

            if (this.pixels == 0L) {
                throw new IllegalStateException("Unable to allocate texture of size " + param1 + "x" + param2 + " (" + param0.components() + " channels)");
            }
        } else {
            throw new IllegalArgumentException("Invalid texture size: " + param1 + "x" + param2);
        }
    }

    private NativeImage(NativeImage.Format param0, int param1, int param2, boolean param3, long param4) {
        if (param1 > 0 && param2 > 0) {
            this.format = param0;
            this.width = param1;
            this.height = param2;
            this.useStbFree = param3;
            this.pixels = param4;
            this.size = (long)param1 * (long)param2 * (long)param0.components();
        } else {
            throw new IllegalArgumentException("Invalid texture size: " + param1 + "x" + param2);
        }
    }

    @Override
    public String toString() {
        return "NativeImage[" + this.format + " " + this.width + "x" + this.height + "@" + this.pixels + (this.useStbFree ? "S" : "N") + "]";
    }

    private boolean isOutsideBounds(int param0, int param1) {
        return param0 < 0 || param0 >= this.width || param1 < 0 || param1 >= this.height;
    }

    public static NativeImage read(InputStream param0) throws IOException {
        return read(NativeImage.Format.RGBA, param0);
    }

    public static NativeImage read(@Nullable NativeImage.Format param0, InputStream param1) throws IOException {
        ByteBuffer var0 = null;

        NativeImage var3;
        try {
            var0 = TextureUtil.readResource(param1);
            var0.rewind();
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

    public static NativeImage read(byte[] param0) throws IOException {
        NativeImage var3;
        try (MemoryStack var0 = MemoryStack.stackPush()) {
            ByteBuffer var1 = var0.malloc(param0.length);
            var1.put(param0);
            var1.rewind();
            var3 = read(var1);
        }

        return var3;
    }

    public static NativeImage read(@Nullable NativeImage.Format param0, ByteBuffer param1) throws IOException {
        if (param0 != null && !param0.supportedByStb()) {
            throw new UnsupportedOperationException("Don't know how to read format " + param0);
        } else if (MemoryUtil.memAddress(param1) == 0L) {
            throw new IllegalArgumentException("Invalid buffer");
        } else {
            NativeImage var7;
            try (MemoryStack var0 = MemoryStack.stackPush()) {
                IntBuffer var1 = var0.mallocInt(1);
                IntBuffer var2 = var0.mallocInt(1);
                IntBuffer var3 = var0.mallocInt(1);
                ByteBuffer var4 = STBImage.stbi_load_from_memory(param1, var1, var2, var3, param0 == null ? 0 : param0.components);
                if (var4 == null) {
                    throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
                }

                var7 = new NativeImage(
                    param0 == null ? NativeImage.Format.getStbFormat(var3.get(0)) : param0, var1.get(0), var2.get(0), true, MemoryUtil.memAddress(var4)
                );
            }

            return var7;
        }
    }

    private static void setFilter(boolean param0, boolean param1) {
        RenderSystem.assertOnRenderThreadOrInit();
        if (param0) {
            GlStateManager._texParameter(3553, 10241, param1 ? 9987 : 9729);
            GlStateManager._texParameter(3553, 10240, 9729);
        } else {
            GlStateManager._texParameter(3553, 10241, param1 ? 9986 : 9728);
            GlStateManager._texParameter(3553, 10240, 9728);
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
            throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelRGBA only works on RGBA images; have %s", this.format));
        } else if (this.isOutsideBounds(param0, param1)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", param0, param1, this.width, this.height));
        } else {
            this.checkAllocated();
            long var0 = ((long)param0 + (long)param1 * (long)this.width) * 4L;
            return MemoryUtil.memGetInt(this.pixels + var0);
        }
    }

    public void setPixelRGBA(int param0, int param1, int param2) {
        if (this.format != NativeImage.Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "setPixelRGBA only works on RGBA images; have %s", this.format));
        } else if (this.isOutsideBounds(param0, param1)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", param0, param1, this.width, this.height));
        } else {
            this.checkAllocated();
            long var0 = ((long)param0 + (long)param1 * (long)this.width) * 4L;
            MemoryUtil.memPutInt(this.pixels + var0, param2);
        }
    }

    public NativeImage mappedCopy(IntUnaryOperator param0) {
        if (this.format != NativeImage.Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "function application only works on RGBA images; have %s", this.format));
        } else {
            this.checkAllocated();
            NativeImage var0 = new NativeImage(this.width, this.height, false);
            int var1 = this.width * this.height;
            IntBuffer var2 = MemoryUtil.memIntBuffer(this.pixels, var1);
            IntBuffer var3 = MemoryUtil.memIntBuffer(var0.pixels, var1);

            for(int var4 = 0; var4 < var1; ++var4) {
                var3.put(var4, param0.applyAsInt(var2.get(var4)));
            }

            return var0;
        }
    }

    public void applyToAllPixels(IntUnaryOperator param0) {
        if (this.format != NativeImage.Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "function application only works on RGBA images; have %s", this.format));
        } else {
            this.checkAllocated();
            int var0 = this.width * this.height;
            IntBuffer var1 = MemoryUtil.memIntBuffer(this.pixels, var0);

            for(int var2 = 0; var2 < var0; ++var2) {
                var1.put(var2, param0.applyAsInt(var1.get(var2)));
            }

        }
    }

    public int[] getPixelsRGBA() {
        if (this.format != NativeImage.Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "getPixelsRGBA only works on RGBA images; have %s", this.format));
        } else {
            this.checkAllocated();
            int[] var0 = new int[this.width * this.height];
            MemoryUtil.memIntBuffer(this.pixels, this.width * this.height).get(var0);
            return var0;
        }
    }

    public void setPixelLuminance(int param0, int param1, byte param2) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasLuminance()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "setPixelLuminance only works on image with luminance; have %s", this.format));
        } else if (this.isOutsideBounds(param0, param1)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", param0, param1, this.width, this.height));
        } else {
            this.checkAllocated();
            long var0 = ((long)param0 + (long)param1 * (long)this.width) * (long)this.format.components() + (long)(this.format.luminanceOffset() / 8);
            MemoryUtil.memPutByte(this.pixels + var0, param2);
        }
    }

    public byte getRedOrLuminance(int param0, int param1) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasLuminanceOrRed()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "no red or luminance in %s", this.format));
        } else if (this.isOutsideBounds(param0, param1)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", param0, param1, this.width, this.height));
        } else {
            int var0 = (param0 + param1 * this.width) * this.format.components() + this.format.luminanceOrRedOffset() / 8;
            return MemoryUtil.memGetByte(this.pixels + (long)var0);
        }
    }

    public byte getGreenOrLuminance(int param0, int param1) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasLuminanceOrGreen()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "no green or luminance in %s", this.format));
        } else if (this.isOutsideBounds(param0, param1)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", param0, param1, this.width, this.height));
        } else {
            int var0 = (param0 + param1 * this.width) * this.format.components() + this.format.luminanceOrGreenOffset() / 8;
            return MemoryUtil.memGetByte(this.pixels + (long)var0);
        }
    }

    public byte getBlueOrLuminance(int param0, int param1) {
        RenderSystem.assertOnRenderThread();
        if (!this.format.hasLuminanceOrBlue()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "no blue or luminance in %s", this.format));
        } else if (this.isOutsideBounds(param0, param1)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", param0, param1, this.width, this.height));
        } else {
            int var0 = (param0 + param1 * this.width) * this.format.components() + this.format.luminanceOrBlueOffset() / 8;
            return MemoryUtil.memGetByte(this.pixels + (long)var0);
        }
    }

    public byte getLuminanceOrAlpha(int param0, int param1) {
        if (!this.format.hasLuminanceOrAlpha()) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "no luminance or alpha in %s", this.format));
        } else if (this.isOutsideBounds(param0, param1)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "(%s, %s) outside of image bounds (%s, %s)", param0, param1, this.width, this.height));
        } else {
            int var0 = (param0 + param1 * this.width) * this.format.components() + this.format.luminanceOrAlphaOffset() / 8;
            return MemoryUtil.memGetByte(this.pixels + (long)var0);
        }
    }

    public void blendPixel(int param0, int param1, int param2) {
        if (this.format != NativeImage.Format.RGBA) {
            throw new UnsupportedOperationException("Can only call blendPixel with RGBA format");
        } else {
            int var0 = this.getPixelRGBA(param0, param1);
            float var1 = (float)FastColor.ABGR32.alpha(param2) / 255.0F;
            float var2 = (float)FastColor.ABGR32.blue(param2) / 255.0F;
            float var3 = (float)FastColor.ABGR32.green(param2) / 255.0F;
            float var4 = (float)FastColor.ABGR32.red(param2) / 255.0F;
            float var5 = (float)FastColor.ABGR32.alpha(var0) / 255.0F;
            float var6 = (float)FastColor.ABGR32.blue(var0) / 255.0F;
            float var7 = (float)FastColor.ABGR32.green(var0) / 255.0F;
            float var8 = (float)FastColor.ABGR32.red(var0) / 255.0F;
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
            this.setPixelRGBA(param0, param1, FastColor.ABGR32.color(var15, var16, var17, var18));
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
                    var0[var2 + var1 * this.getWidth()] = FastColor.ARGB32.color(
                        FastColor.ABGR32.alpha(var3), FastColor.ABGR32.red(var3), FastColor.ABGR32.green(var3), FastColor.ABGR32.blue(var3)
                    );
                }
            }

            return var0;
        }
    }

    public void upload(int param0, int param1, int param2, boolean param3) {
        this.upload(param0, param1, param2, 0, 0, this.width, this.height, false, param3);
    }

    public void upload(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, boolean param8) {
        this.upload(param0, param1, param2, param3, param4, param5, param6, false, false, param7, param8);
    }

    public void upload(
        int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, boolean param8, boolean param9, boolean param10
    ) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> this._upload(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10));
        } else {
            this._upload(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9, param10);
        }

    }

    private void _upload(
        int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, boolean param8, boolean param9, boolean param10
    ) {
        try {
            RenderSystem.assertOnRenderThreadOrInit();
            this.checkAllocated();
            setFilter(param7, param9);
            if (param5 == this.getWidth()) {
                GlStateManager._pixelStore(3314, 0);
            } else {
                GlStateManager._pixelStore(3314, this.getWidth());
            }

            GlStateManager._pixelStore(3316, param3);
            GlStateManager._pixelStore(3315, param4);
            this.format.setUnpackPixelStoreState();
            GlStateManager._texSubImage2D(3553, param0, param1, param2, param5, param6, this.format.glFormat(), 5121, this.pixels);
            if (param8) {
                GlStateManager._texParameter(3553, 10242, 33071);
                GlStateManager._texParameter(3553, 10243, 33071);
            }
        } finally {
            if (param10) {
                this.close();
            }

        }

    }

    public void downloadTexture(int param0, boolean param1) {
        RenderSystem.assertOnRenderThread();
        this.checkAllocated();
        this.format.setPackPixelStoreState();
        GlStateManager._getTexImage(3553, param0, this.format.glFormat(), 5121, this.pixels);
        if (param1 && this.format.hasAlpha()) {
            for(int var0 = 0; var0 < this.getHeight(); ++var0) {
                for(int var1 = 0; var1 < this.getWidth(); ++var1) {
                    this.setPixelRGBA(var1, var0, this.getPixelRGBA(var1, var0) | 255 << this.format.alphaOffset());
                }
            }
        }

    }

    public void downloadDepthBuffer(float param0) {
        RenderSystem.assertOnRenderThread();
        if (this.format.components() != 1) {
            throw new IllegalStateException("Depth buffer must be stored in NativeImage with 1 component.");
        } else {
            this.checkAllocated();
            this.format.setPackPixelStoreState();
            GlStateManager._readPixels(0, 0, this.width, this.height, 6402, 5121, this.pixels);
        }
    }

    public void drawPixels() {
        RenderSystem.assertOnRenderThread();
        this.format.setUnpackPixelStoreState();
        GlStateManager._glDrawPixels(this.width, this.height, this.format.glFormat(), 5121, this.pixels);
    }

    public void writeToFile(File param0) throws IOException {
        this.writeToFile(param0.toPath());
    }

    public void copyFromFont(
        STBTTFontinfo param0, int param1, int param2, int param3, float param4, float param5, float param6, float param7, int param8, int param9
    ) {
        if (param8 < 0 || param8 + param2 > this.getWidth() || param9 < 0 || param9 + param3 > this.getHeight()) {
            throw new IllegalArgumentException(
                String.format(
                    Locale.ROOT, "Out of bounds: start: (%s, %s) (size: %sx%s); size: %sx%s", param8, param9, param2, param3, this.getWidth(), this.getHeight()
                )
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
                if (!this.writeToChannel(var0)) {
                    throw new IOException("Could not write image to the PNG file \"" + param0.toAbsolutePath() + "\": " + STBImage.stbi_failure_reason());
                }
            }

        }
    }

    public byte[] asByteArray() throws IOException {
        byte[] var3;
        try (
            ByteArrayOutputStream var0 = new ByteArrayOutputStream();
            WritableByteChannel var1 = Channels.newChannel(var0);
        ) {
            if (!this.writeToChannel(var1)) {
                throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
            }

            var3 = var0.toByteArray();
        }

        return var3;
    }

    private boolean writeToChannel(WritableByteChannel param0) throws IOException {
        NativeImage.WriteCallback var0 = new NativeImage.WriteCallback(param0);

        boolean var4;
        try {
            int var1 = Math.min(this.getHeight(), Integer.MAX_VALUE / this.getWidth() / this.format.components());
            if (var1 < this.getHeight()) {
                LOGGER.warn("Dropping image height from {} to {} to fit the size into 32-bit signed int", this.getHeight(), var1);
            }

            if (STBImageWrite.nstbi_write_png_to_func(var0.address(), 0L, this.getWidth(), var1, this.format.components(), this.pixels, 0) != 0) {
                var0.throwIfException();
                return true;
            }

            var4 = false;
        } finally {
            var0.free();
        }

        return var4;
    }

    public void copyFrom(NativeImage param0) {
        if (param0.format() != this.format) {
            throw new UnsupportedOperationException("Image formats don't match.");
        } else {
            int var0 = this.format.components();
            this.checkAllocated();
            param0.checkAllocated();
            if (this.width == param0.width) {
                MemoryUtil.memCopy(param0.pixels, this.pixels, Math.min(this.size, param0.size));
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
        this.copyRect(this, param0, param1, param0 + param2, param1 + param3, param4, param5, param6, param7);
    }

    public void copyRect(NativeImage param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, boolean param8) {
        for(int var0 = 0; var0 < param6; ++var0) {
            for(int var1 = 0; var1 < param5; ++var1) {
                int var2 = param7 ? param5 - 1 - var1 : var1;
                int var3 = param8 ? param6 - 1 - var0 : var0;
                int var4 = this.getPixelRGBA(param1 + var1, param2 + var0);
                param0.setPixelRGBA(param3 + var2, param4 + var3, var4);
            }
        }

    }

    public void flipY() {
        this.checkAllocated();
        int var0 = this.format.components();
        int var1 = this.getWidth() * var0;
        long var2 = MemoryUtil.nmemAlloc((long)var1);

        try {
            for(int var3 = 0; var3 < this.getHeight() / 2; ++var3) {
                int var4 = var3 * this.getWidth() * var0;
                int var5 = (this.getHeight() - 1 - var3) * this.getWidth() * var0;
                MemoryUtil.memCopy(this.pixels + (long)var4, var2, (long)var1);
                MemoryUtil.memCopy(this.pixels + (long)var5, this.pixels + (long)var4, (long)var1);
                MemoryUtil.memCopy(var2, this.pixels + (long)var5, (long)var1);
            }
        } finally {
            MemoryUtil.nmemFree(var2);
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

    @OnlyIn(Dist.CLIENT)
    public static enum Format {
        RGBA(4, 6408, true, true, true, false, true, 0, 8, 16, 255, 24, true),
        RGB(3, 6407, true, true, true, false, false, 0, 8, 16, 255, 255, true),
        LUMINANCE_ALPHA(2, 33319, false, false, false, true, true, 255, 255, 255, 0, 8, true),
        LUMINANCE(1, 6403, false, false, false, true, false, 0, 0, 0, 0, 255, true);

        final int components;
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
            RenderSystem.assertOnRenderThread();
            GlStateManager._pixelStore(3333, this.components());
        }

        public void setUnpackPixelStoreState() {
            RenderSystem.assertOnRenderThreadOrInit();
            GlStateManager._pixelStore(3317, this.components());
        }

        public int glFormat() {
            return this.glFormat;
        }

        public boolean hasRed() {
            return this.hasRed;
        }

        public boolean hasGreen() {
            return this.hasGreen;
        }

        public boolean hasBlue() {
            return this.hasBlue;
        }

        public boolean hasLuminance() {
            return this.hasLuminance;
        }

        public boolean hasAlpha() {
            return this.hasAlpha;
        }

        public int redOffset() {
            return this.redOffset;
        }

        public int greenOffset() {
            return this.greenOffset;
        }

        public int blueOffset() {
            return this.blueOffset;
        }

        public int luminanceOffset() {
            return this.luminanceOffset;
        }

        public int alphaOffset() {
            return this.alphaOffset;
        }

        public boolean hasLuminanceOrRed() {
            return this.hasLuminance || this.hasRed;
        }

        public boolean hasLuminanceOrGreen() {
            return this.hasLuminance || this.hasGreen;
        }

        public boolean hasLuminanceOrBlue() {
            return this.hasLuminance || this.hasBlue;
        }

        public boolean hasLuminanceOrAlpha() {
            return this.hasLuminance || this.hasAlpha;
        }

        public int luminanceOrRedOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.redOffset;
        }

        public int luminanceOrGreenOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.greenOffset;
        }

        public int luminanceOrBlueOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.blueOffset;
        }

        public int luminanceOrAlphaOffset() {
            return this.hasLuminance ? this.luminanceOffset : this.alphaOffset;
        }

        public boolean supportedByStb() {
            return this.supportedByStb;
        }

        static NativeImage.Format getStbFormat(int param0) {
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
        RG(33319),
        RED(6403);

        private final int glFormat;

        private InternalGlFormat(int param0) {
            this.glFormat = param0;
        }

        public int glFormat() {
            return this.glFormat;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class WriteCallback extends STBIWriteCallback {
        private final WritableByteChannel output;
        @Nullable
        private IOException exception;

        WriteCallback(WritableByteChannel param0) {
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
