package net.minecraft.client.profiling;

import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ClientMetricsLogger {
    void end();

    void startTick();

    boolean isRecording();

    ProfilerFiller getProfiler();

    void endTick();
}
