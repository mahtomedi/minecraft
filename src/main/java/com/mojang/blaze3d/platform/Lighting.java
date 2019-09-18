package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Lighting {
    public static void turnOff() {
        RenderSystem.disableDiffuseLighting();
    }

    public static void turnOn() {
        RenderSystem.enableUsualDiffuseLighting();
    }

    public static void turnOnGui() {
        RenderSystem.enableGuiDiffuseLighting();
    }
}
