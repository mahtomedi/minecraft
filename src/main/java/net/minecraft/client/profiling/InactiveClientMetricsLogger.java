package net.minecraft.client.profiling;

import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InactiveClientMetricsLogger implements ClientMetricsLogger {
    public static final ClientMetricsLogger INSTANCE = new InactiveClientMetricsLogger();

    @Override
    public void end() {
    }

    @Override
    public void startTick() {
    }

    @Override
    public boolean isRecording() {
        return false;
    }

    @Override
    public ProfilerFiller getProfiler() {
        return InactiveProfiler.INSTANCE;
    }

    @Override
    public void endTick() {
    }
}
