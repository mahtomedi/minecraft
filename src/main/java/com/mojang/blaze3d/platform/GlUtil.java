package com.mojang.blaze3d.platform;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GlUtil {
    public static String getVendor() {
        return "GLU.getVendor";
    }

    public static String getCpuInfo() {
        return GLX._getCpuInfo();
    }

    public static String getRenderer() {
        return "GLU.getRenderer";
    }

    public static String getOpenGLVersion() {
        return "GLU.getOpenGLVersion";
    }
}
