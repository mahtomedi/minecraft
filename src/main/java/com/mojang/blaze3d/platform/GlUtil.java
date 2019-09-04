package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GlUtil {
    public static String getVendor() {
        return RenderSystem.getString(7936);
    }

    public static String getCpuInfo() {
        return GLX._getCpuInfo();
    }

    public static String getRenderer() {
        return RenderSystem.getString(7937);
    }

    public static String getOpenGLVersion() {
        return RenderSystem.getString(7938);
    }
}
