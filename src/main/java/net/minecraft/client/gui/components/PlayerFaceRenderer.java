package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerFaceRenderer {
    public static final int SKIN_HEAD_U = 8;
    public static final int SKIN_HEAD_V = 8;
    public static final int SKIN_HEAD_WIDTH = 8;
    public static final int SKIN_HEAD_HEIGHT = 8;
    public static final int SKIN_HAT_U = 40;
    public static final int SKIN_HAT_V = 8;
    public static final int SKIN_HAT_WIDTH = 8;
    public static final int SKIN_HAT_HEIGHT = 8;
    public static final int SKIN_TEX_WIDTH = 64;
    public static final int SKIN_TEX_HEIGHT = 64;

    public static void draw(GuiGraphics param0, PlayerSkin param1, int param2, int param3, int param4) {
        draw(param0, param1.texture(), param2, param3, param4);
    }

    public static void draw(GuiGraphics param0, ResourceLocation param1, int param2, int param3, int param4) {
        draw(param0, param1, param2, param3, param4, true, false);
    }

    public static void draw(GuiGraphics param0, ResourceLocation param1, int param2, int param3, int param4, boolean param5, boolean param6) {
        int var0 = 8 + (param6 ? 8 : 0);
        int var1 = 8 * (param6 ? -1 : 1);
        param0.blit(param1, param2, param3, param4, param4, 8.0F, (float)var0, 8, var1, 64, 64);
        if (param5) {
            drawHat(param0, param1, param2, param3, param4, param6);
        }

    }

    private static void drawHat(GuiGraphics param0, ResourceLocation param1, int param2, int param3, int param4, boolean param5) {
        int var0 = 8 + (param5 ? 8 : 0);
        int var1 = 8 * (param5 ? -1 : 1);
        RenderSystem.enableBlend();
        param0.blit(param1, param2, param3, param4, param4, 40.0F, (float)var0, 8, var1, 64, 64);
        RenderSystem.disableBlend();
    }
}
