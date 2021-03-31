package com.mojang.blaze3d.platform;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class GlUtil {
    public static void populateSnooperWithOpenGL(SnooperAccess param0) {
    }

    public static ByteBuffer allocateMemory(int param0) {
        return MemoryUtil.memAlloc(param0);
    }

    public static void freeMemory(Buffer param0) {
        MemoryUtil.memFree(param0);
    }

    public static String getVendor() {
        return GlStateManager._getString(7936);
    }

    public static String getCpuInfo() {
        return GLX._getCpuInfo();
    }

    public static String getRenderer() {
        return GlStateManager._getString(7937);
    }

    public static String getOpenGLVersion() {
        return GlStateManager._getString(7938);
    }
}
