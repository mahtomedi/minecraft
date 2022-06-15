package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
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

    public static void draw(PoseStack param0, int param1, int param2, int param3) {
        draw(param0, param1, param2, param3, true, false);
    }

    public static void draw(PoseStack param0, int param1, int param2, int param3, boolean param4, boolean param5) {
        int var0 = 8 + (param5 ? 8 : 0);
        int var1 = 8 * (param5 ? -1 : 1);
        GuiComponent.blit(param0, param1, param2, param3, param3, 8.0F, (float)var0, 8, var1, 64, 64);
        if (param4) {
            drawHat(param0, param1, param2, param3, param5);
        }

    }

    private static void drawHat(PoseStack param0, int param1, int param2, int param3, boolean param4) {
        int var0 = 8 + (param4 ? 8 : 0);
        int var1 = 8 * (param4 ? -1 : 1);
        RenderSystem.enableBlend();
        GuiComponent.blit(param0, param1, param2, param3, param3, 40.0F, (float)var0, 8, var1, 64, 64);
        RenderSystem.disableBlend();
    }
}
