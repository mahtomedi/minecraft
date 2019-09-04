package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class TextureUtil {
    private static final Logger LOGGER = LogManager.getLogger();

    public static int generateTextureId() {
        return RenderSystem.genTexture();
    }

    public static void releaseTextureId(int param0) {
        RenderSystem.deleteTexture(param0);
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
        bind(param1);
        if (param2 >= 0) {
            RenderSystem.texParameter(3553, 33085, param2);
            RenderSystem.texParameter(3553, 33082, 0);
            RenderSystem.texParameter(3553, 33083, param2);
            RenderSystem.texParameter(3553, 34049, 0.0F);
        }

        for(int var0 = 0; var0 <= param2; ++var0) {
            RenderSystem.texImage2D(3553, var0, param0.glFormat(), param3 >> var0, param4 >> var0, 0, 6408, 5121, null);
        }

    }

    private static void bind(int param0) {
        RenderSystem.bindTexture(param0);
    }

    public static ByteBuffer readResource(InputStream param0) throws IOException {
        ByteBuffer var2;
        if (param0 instanceof FileInputStream) {
            FileInputStream var0 = (FileInputStream)param0;
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

    public static String readResourceAsString(InputStream param0) {
        ByteBuffer var0 = null;

        try {
            var0 = readResource(param0);
            int var1 = var0.position();
            ((Buffer)var0).rewind();
            return MemoryUtil.memASCII(var0, var1);
        } catch (IOException var7) {
        } finally {
            if (var0 != null) {
                MemoryUtil.memFree(var0);
            }

        }

        return null;
    }

    public static void initTexture(IntBuffer param0, int param1, int param2) {
        GL11.glPixelStorei(3312, 0);
        GL11.glPixelStorei(3313, 0);
        GL11.glPixelStorei(3314, 0);
        GL11.glPixelStorei(3315, 0);
        GL11.glPixelStorei(3316, 0);
        GL11.glPixelStorei(3317, 4);
        GL11.glTexImage2D(3553, 0, 6408, param1, param2, 0, 32993, 33639, param0);
        GL11.glTexParameteri(3553, 10242, 10497);
        GL11.glTexParameteri(3553, 10243, 10497);
        GL11.glTexParameteri(3553, 10240, 9728);
        GL11.glTexParameteri(3553, 10241, 9729);
    }
}
