package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.util.SampleLogger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BandwidthDebugChart extends AbstractDebugChart {
    private static final int MIN_COLOR = -16711681;
    private static final int MID_COLOR = -6250241;
    private static final int MAX_COLOR = -65536;
    private static final int KILOBYTE = 1024;
    private static final int MEGABYTE = 1048576;
    private static final int CHART_TOP_VALUE = 1048576;

    public BandwidthDebugChart(Font param0, SampleLogger param1) {
        super(param0, param1);
    }

    @Override
    protected void renderAdditionalLinesAndLabels(GuiGraphics param0, int param1, int param2, int param3) {
        this.drawLabeledLineAtValue(param0, param1, param2, param3, 64);
        this.drawLabeledLineAtValue(param0, param1, param2, param3, 1024);
        this.drawLabeledLineAtValue(param0, param1, param2, param3, 16384);
        this.drawStringWithShade(param0, this.toDisplayString(1048576.0), param1 + 1, param3 - this.getSampleHeight(1048576.0) + 1);
    }

    private void drawLabeledLineAtValue(GuiGraphics param0, int param1, int param2, int param3, int param4) {
        this.drawLineWithLabel(param0, param1, param2, param3 - getSampleHeightInternal((double)param4), toDisplayStringInternal((double)param4));
    }

    private void drawLineWithLabel(GuiGraphics param0, int param1, int param2, int param3, String param4) {
        this.drawStringWithShade(param0, param4, param1 + 1, param3 + 1);
        param0.hLine(RenderType.guiOverlay(), param1, param1 + param2 - 1, param3, -1);
    }

    @Override
    protected String toDisplayString(double param0) {
        return toDisplayStringInternal(toBytesPerSecond(param0));
    }

    private static String toDisplayStringInternal(double param0) {
        if (param0 >= 1048576.0) {
            return String.format(Locale.ROOT, "%.1f MiB/s", param0 / 1048576.0);
        } else {
            return param0 >= 1024.0 ? String.format(Locale.ROOT, "%.1f KiB/s", param0 / 1024.0) : String.format(Locale.ROOT, "%d B/s", Mth.floor(param0));
        }
    }

    @Override
    protected int getSampleHeight(double param0) {
        return getSampleHeightInternal(toBytesPerSecond(param0));
    }

    private static int getSampleHeightInternal(double param0) {
        return (int)Math.round(Math.log(param0 + 1.0) * 60.0 / Math.log(1048576.0));
    }

    @Override
    protected int getSampleColor(long param0) {
        return this.getSampleColor(toBytesPerSecond((double)param0), 0.0, -16711681, 8192.0, -6250241, 1.048576E7, -65536);
    }

    private static double toBytesPerSecond(double param0) {
        return param0 * 20.0;
    }
}
