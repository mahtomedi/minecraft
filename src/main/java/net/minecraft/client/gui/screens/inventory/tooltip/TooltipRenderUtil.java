package net.minecraft.client.gui.screens.inventory.tooltip;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TooltipRenderUtil {
    public static final int MOUSE_OFFSET = 12;
    private static final int PADDING = 3;
    public static final int PADDING_LEFT = 3;
    public static final int PADDING_RIGHT = 3;
    public static final int PADDING_TOP = 3;
    public static final int PADDING_BOTTOM = 3;
    private static final int BACKGROUND_COLOR = -267386864;
    private static final int BORDER_COLOR_TOP = 1347420415;
    private static final int BORDER_COLOR_BOTTOM = 1344798847;

    public static void renderTooltipBackground(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5) {
        int var0 = param1 - 3;
        int var1 = param2 - 3;
        int var2 = param3 + 3 + 3;
        int var3 = param4 + 3 + 3;
        renderHorizontalLine(param0, var0, var1 - 1, var2, param5, -267386864);
        renderHorizontalLine(param0, var0, var1 + var3, var2, param5, -267386864);
        renderRectangle(param0, var0, var1, var2, var3, param5, -267386864);
        renderVerticalLine(param0, var0 - 1, var1, var3, param5, -267386864);
        renderVerticalLine(param0, var0 + var2, var1, var3, param5, -267386864);
        renderFrameGradient(param0, var0, var1 + 1, var2, var3, param5, 1347420415, 1344798847);
    }

    private static void renderFrameGradient(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7) {
        renderVerticalLineGradient(param0, param1, param2, param4 - 2, param5, param6, param7);
        renderVerticalLineGradient(param0, param1 + param3 - 1, param2, param4 - 2, param5, param6, param7);
        renderHorizontalLine(param0, param1, param2 - 1, param3, param5, param6);
        renderHorizontalLine(param0, param1, param2 - 1 + param4 - 1, param3, param5, param7);
    }

    private static void renderVerticalLine(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5) {
        param0.fill(param1, param2, param1 + 1, param2 + param3, param4, param5);
    }

    private static void renderVerticalLineGradient(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        param0.fillGradient(param1, param2, param1 + 1, param2 + param3, param4, param5, param6);
    }

    private static void renderHorizontalLine(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5) {
        param0.fill(param1, param2, param1 + param3, param2 + 1, param4, param5);
    }

    private static void renderRectangle(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        param0.fill(param1, param2, param1 + param3, param2 + param4, param5, param6);
    }
}
