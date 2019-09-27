package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
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

    public static void setupLevel() {
        RenderSystem.setupLevelDiffuseLighting();
    }

    public static void setupGui() {
        RenderSystem.setupGuiDiffuseLighting();
    }
}
