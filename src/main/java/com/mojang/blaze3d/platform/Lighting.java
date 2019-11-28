package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Lighting {
    private static Matrix4f lightPoseForFlatItems = new Matrix4f();
    private static Matrix4f lightPoseFor3DItems = new Matrix4f();

    public static void turnBackOn() {
        RenderSystem.enableLighting();
        RenderSystem.enableColorMaterial();
        RenderSystem.colorMaterial(1032, 5634);
    }

    public static void turnOff() {
        RenderSystem.disableLighting();
        RenderSystem.disableColorMaterial();
    }

    public static void setupLevel(Matrix4f param0) {
        RenderSystem.setupLevelDiffuseLighting(param0);
    }

    public static void setupGui(Matrix4f param0) {
        lightPoseForFlatItems = param0.copy();
        lightPoseFor3DItems = param0.copy();
        lightPoseFor3DItems.multiply(Vector3f.YP.rotationDegrees(62.0F));
        lightPoseFor3DItems.multiply(Vector3f.XP.rotationDegrees(185.5F));
        RenderSystem.setupGuiDiffuseLighting(param0);
    }

    public static void setupForFlatItems() {
        RenderSystem.setupGuiDiffuseLighting(lightPoseForFlatItems);
    }

    public static void setupFor3DItems() {
        RenderSystem.setupGuiDiffuseLighting(lightPoseFor3DItems);
    }
}
