package net.minecraft.client.profiling.metric;

import java.util.Date;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class FpsSpikeRecording {
    public final Date timestamp;
    public final int tick;
    public final ProfileResults profilerResultForSpikeFrame;

    public FpsSpikeRecording(Date param0, int param1, ProfileResults param2) {
        this.timestamp = param0;
        this.tick = param1;
        this.profilerResultForSpikeFrame = param2;
    }
}
