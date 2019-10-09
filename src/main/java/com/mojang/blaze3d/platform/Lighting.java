package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Lighting {
    public static void turnBackOn() {
        RenderSystem.enableLighting();
        RenderSystem.enableColorMaterial();
    }

    public static void turnOff() {
        RenderSystem.disableLighting();
        RenderSystem.disableColorMaterial();
    }

    public static void setupLevel(Matrix4f param0) {
        RenderSystem.setupLevelDiffuseLighting(param0);
    }

    public static void setupGui(Matrix4f param0) {
        RenderSystem.setupGuiDiffuseLighting(param0);
    }
}
