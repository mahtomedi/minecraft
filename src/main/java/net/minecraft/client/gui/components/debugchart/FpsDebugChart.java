package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.SampleLogger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FpsDebugChart extends AbstractDebugChart {
    private static final int RED = -65536;
    private static final int YELLOW = -256;
    private static final int GREEN = -16711936;
    private static final int CHART_TOP_FPS = 30;
    private static final double CHART_TOP_VALUE = 33.333333333333336;

    public FpsDebugChart(Font param0, SampleLogger param1) {
        super(param0, param1);
    }

    @Override
    protected void renderAdditionalLinesAndLabels(GuiGraphics param0, int param1, int param2, int param3) {
        this.drawStringWithShade(param0, "30 FPS", param1 + 1, param3 - 60 + 1);
        this.drawStringWithShade(param0, "60 FPS", param1 + 1, param3 - 30 + 1);
        param0.hLine(RenderType.guiOverlay(), param1, param1 + param2 - 1, param3 - 30, -1);
        int var0 = Minecraft.getInstance().options.framerateLimit().get();
        if (var0 > 0 && var0 <= 250) {
            param0.hLine(RenderType.guiOverlay(), param1, param1 + param2 - 1, param3 - this.getSampleHeight(1.0E9 / (double)var0) - 1, -16711681);
        }

    }

    @Override
    protected String toDisplayString(double param0) {
        return String.format(Locale.ROOT, "%d ms", (int)Math.round(toMilliseconds(param0)));
    }

    @Override
    protected int getSampleHeight(double param0) {
        return (int)Math.round(toMilliseconds(param0) * 60.0 / 33.333333333333336);
    }

    @Override
    protected int getSampleColor(long param0) {
        return this.getSampleColor(toMilliseconds((double)param0), 0.0, -16711936, 28.0, -256, 56.0, -65536);
    }

    private static double toMilliseconds(double param0) {
        return param0 / 1000000.0;
    }
}
