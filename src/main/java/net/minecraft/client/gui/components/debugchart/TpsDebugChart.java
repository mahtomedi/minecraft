package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.SampleLogger;
import net.minecraft.util.TimeUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TpsDebugChart extends AbstractDebugChart {
    private static final int RED = -65536;
    private static final int YELLOW = -256;
    private static final int GREEN = -16711936;
    private final Supplier<Float> msptSupplier;

    public TpsDebugChart(Font param0, SampleLogger param1, Supplier<Float> param2) {
        super(param0, param1);
        this.msptSupplier = param2;
    }

    @Override
    protected void renderAdditionalLinesAndLabels(GuiGraphics param0, int param1, int param2, int param3) {
        float var0 = (float)TimeUtil.MILLISECONDS_PER_SECOND / this.msptSupplier.get();
        this.drawStringWithShade(param0, String.format("%.1f TPS", var0), param1 + 1, param3 - 60 + 1);
    }

    @Override
    protected String toDisplayString(double param0) {
        return String.format(Locale.ROOT, "%d ms", (int)Math.round(toMilliseconds(param0)));
    }

    @Override
    protected int getSampleHeight(double param0) {
        return (int)Math.round(toMilliseconds(param0) * 60.0 / (double)this.msptSupplier.get().floatValue());
    }

    @Override
    protected int getSampleColor(long param0) {
        float var0 = this.msptSupplier.get();
        return this.getSampleColor(toMilliseconds((double)param0), 0.0, -16711936, (double)var0 / 2.0, -256, (double)var0, -65536);
    }

    private static double toMilliseconds(double param0) {
        return param0 / 1000000.0;
    }
}
