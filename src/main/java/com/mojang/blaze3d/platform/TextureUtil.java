package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.SharedConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
@DontObfuscate
public class TextureUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MIN_MIPMAP_LEVEL = 0;
    private static final int DEFAULT_IMAGE_BUFFER_SIZE = 8192;

    public static int generateTextureId() {
        RenderSystem.assertOnRenderThreadOrInit();
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            int[] var0 = new int[ThreadLocalRandom.current().nextInt(15) + 1];
            GlStateManager._genTextures(var0);
            int var1 = GlStateManager._genTexture();
            GlStateManager._deleteTextures(var0);
            return var1;
        } else {
            return GlStateManager._genTexture();
        }
    }

    public static void releaseTextureId(int param0) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._deleteTexture(param0);
    }

    public static void prepareImage(int param0, int param1, int param2) {
        prepareImage(NativeImage.InternalGlFormat.RGBA, param0, 0, param1, param2);
    }

    public static void prepareImage(NativeImage.InternalGlFormat param0, int param1, int param2, int param3) {
        prepareImage(param0, param1, 0, param2, param3);
    }

    public static void prepareImage(int param0, int param1, int param2, int param3) {
        prepareImage(NativeImage.InternalGlFormat.RGBA, param0, param1, param2, param3);
    }

    public static void prepareImage(NativeImage.InternalGlFormat param0, int param1, int param2, int param3, int param4) {
        RenderSystem.assertOnRenderThreadOrInit();
        bind(param1);
        if (param2 >= 0) {
            GlStateManager._texParameter(3553, 33085, param2);
            GlStateManager._texParameter(3553, 33082, 0);
            GlStateManager._texParameter(3553, 33083, param2);
            GlStateManager._texParameter(3553, 34049, 0.0F);
        }

        for(int var0 = 0; var0 <= param2; ++var0) {
            GlStateManager._texImage2D(3553, var0, param0.glFormat(), param3 >> var0, param4 >> var0, 0, 6408, 5121, null);
        }

    }

    private static void bind(int param0) {
        RenderSystem.assertOnRenderThreadOrInit();
        GlStateManager._bindTexture(param0);
    }

    public static ByteBuffer readResource(InputStream param0) throws IOException {
        ByteBuffer var2;
        if (param0 instanceof FileInputStream var0) {
            FileChannel var1 = var0.getChannel();
            var2 = MemoryUtil.memAlloc((int)var1.size() + 1);

            while(var1.read(var2) != -1) {
            }
        } else {
            var2 = MemoryUtil.memAlloc(8192);
            ReadableByteChannel var4 = Channels.newChannel(param0);

            while(var4.read(var2) != -1) {
                if (var2.remaining() == 0) {
                    var2 = MemoryUtil.memRealloc(var2, var2.capacity() * 2);
                }
            }
        }

        return var2;
    }

    public static void writeAsPNG(Path param0, String param1, int param2, int param3, int param4, int param5) {
        RenderSystem.assertOnRenderThread();
        bind(param2);

        for(int var0 = 0; var0 <= param3; ++var0) {
            int var1 = param4 >> var0;
            int var2 = param5 >> var0;

            try (NativeImage var3 = new NativeImage(var1, var2, false)) {
                var3.downloadTexture(var0, false);
                Path var4 = param0.resolve(param1 + "_" + var0 + ".png");
                var3.writeToFile(var4);
                LOGGER.debug("Exported png to: {}", var4.toAbsolutePath());
            } catch (IOException var14) {
                LOGGER.debug("Unable to write: ", (Throwable)var14);
            }
        }

    }

    public static void initTexture(IntBuffer param0, int param1, int param2) {
        RenderSystem.assertOnRenderThread();
        GL11.glPixelStorei(3312, 0);
        GL11.glPixelStorei(3313, 0);
        GL11.glPixelStorei(3314, 0);
        GL11.glPixelStorei(3315, 0);
        GL11.glPixelStorei(3316, 0);
        GL11.glPixelStorei(3317, 4);
        GL11.glTexImage2D(3553, 0, 6408, param1, param2, 0, 32993, 33639, param0);
        GL11.glTexParameteri(3553, 10240, 9728);
        GL11.glTexParameteri(3553, 10241, 9729);
    }

    public static Path getDebugTexturePath(Path param0) {
        return param0.resolve("screenshots").resolve("debug");
    }

    public static Path getDebugTexturePath() {
        return getDebugTexturePath(Path.of("."));
    }
}
