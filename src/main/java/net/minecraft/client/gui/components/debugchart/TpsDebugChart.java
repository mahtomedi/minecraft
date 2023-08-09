package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.SampleLogger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TpsDebugChart extends AbstractDebugChart {
    private static final int RED = -65536;
    private static final int YELLOW = -256;
    private static final int GREEN = -16711936;
    private static final int CHART_TOP_VALUE = 50;

    public TpsDebugChart(Font param0, SampleLogger param1) {
        super(param0, param1);
    }

    @Override
    protected void renderAdditionalLinesAndLabels(GuiGraphics param0, int param1, int param2, int param3) {
        this.drawStringWithShade(param0, "20 TPS", param1 + 1, param3 - 60 + 1);
    }

    @Override
    protected String toDisplayString(double param0) {
        return String.format(Locale.ROOT, "%d ms", (int)Math.round(toMilliseconds(param0)));
    }

    @Override
    protected int getSampleHeight(double param0) {
        return (int)Math.round(toMilliseconds(param0) * 60.0 / 50.0);
    }

    @Override
    protected int getSampleColor(long param0) {
        return this.getSampleColor(toMilliseconds((double)param0), 0.0, -16711936, 25.0, -256, 50.0, -65536);
    }

    private static double toMilliseconds(double param0) {
        return param0 / 1000000.0;
    }
}
