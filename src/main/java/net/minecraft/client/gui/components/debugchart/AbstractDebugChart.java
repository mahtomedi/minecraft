package net.minecraft.client.gui.components.debugchart;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.SampleLogger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractDebugChart {
    protected static final int COLOR_GREY = 14737632;
    protected static final int CHART_HEIGHT = 60;
    protected static final int LINE_WIDTH = 1;
    protected final Font font;
    protected final SampleLogger logger;

    protected AbstractDebugChart(Font param0, SampleLogger param1) {
        this.font = param0;
        this.logger = param1;
    }

    public int getWidth(int param0) {
        return Math.min(this.logger.capacity() + 2, param0);
    }

    public void drawChart(GuiGraphics param0, int param1, int param2) {
        int var0 = param0.guiHeight();
        param0.fill(RenderType.guiOverlay(), param1, var0 - 60, param1 + param2, var0, -1873784752);
        long var1 = 0L;
        long var2 = 2147483647L;
        long var3 = -2147483648L;
        int var4 = Math.max(0, this.logger.capacity() - (param2 - 2));
        int var5 = this.logger.size() - var4;

        for(int var6 = 0; var6 < var5; ++var6) {
            int var7 = param1 + var6 + 1;
            long var8 = this.logger.get(var4 + var6);
            var2 = Math.min(var2, var8);
            var3 = Math.max(var3, var8);
            var1 += var8;
            int var9 = this.getSampleHeight((double)var8);
            int var10 = this.getSampleColor(var8);
            param0.fill(RenderType.guiOverlay(), var7, var0 - var9, var7 + 1, var0, var10);
        }

        param0.hLine(RenderType.guiOverlay(), param1, param1 + param2 - 1, var0 - 60, -1);
        param0.hLine(RenderType.guiOverlay(), param1, param1 + param2 - 1, var0 - 1, -1);
        param0.vLine(RenderType.guiOverlay(), param1, var0 - 60, var0, -1);
        param0.vLine(RenderType.guiOverlay(), param1 + param2 - 1, var0 - 60, var0, -1);
        if (var5 > 0) {
            String var11 = this.toDisplayString((double)var2) + " min";
            String var12 = this.toDisplayString((double)var1 / (double)var5) + " avg";
            String var13 = this.toDisplayString((double)var3) + " max";
            param0.drawString(this.font, var11, param1 + 2, var0 - 60 - 9, 14737632);
            param0.drawCenteredString(this.font, var12, param1 + param2 / 2, var0 - 60 - 9, 14737632);
            param0.drawString(this.font, var13, param1 + param2 - this.font.width(var13) - 2, var0 - 60 - 9, 14737632);
        }

        this.renderAdditionalLinesAndLabels(param0, param1, param2, var0);
    }

    protected void renderAdditionalLinesAndLabels(GuiGraphics param0, int param1, int param2, int param3) {
    }

    protected void drawStringWithShade(GuiGraphics param0, String param1, int param2, int param3) {
        param0.fill(RenderType.guiOverlay(), param2, param3, param2 + this.font.width(param1) + 1, param3 + 9, -1873784752);
        param0.drawString(this.font, param1, param2 + 1, param3 + 1, 14737632, false);
    }

    protected abstract String toDisplayString(double var1);

    protected abstract int getSampleHeight(double var1);

    protected abstract int getSampleColor(long var1);

    protected int getSampleColor(double param0, double param1, int param2, double param3, int param4, double param5, int param6) {
        param0 = Mth.clamp(param0, param1, param5);
        return param0 < param3
            ? FastColor.ARGB32.lerp((float)(param0 / (param3 - param1)), param2, param4)
            : FastColor.ARGB32.lerp((float)((param0 - param3) / (param5 - param3)), param4, param6);
    }
}
