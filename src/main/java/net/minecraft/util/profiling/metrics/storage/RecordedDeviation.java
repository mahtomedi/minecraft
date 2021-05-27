package net.minecraft.util.profiling.metrics.storage;

import java.time.Instant;
import net.minecraft.util.profiling.ProfileResults;

public final class RecordedDeviation {
    public final Instant timestamp;
    public final int tick;
    public final ProfileResults profilerResultAtTick;

    public RecordedDeviation(Instant param0, int param1, ProfileResults param2) {
        this.timestamp = param0;
        this.tick = param1;
        this.profilerResultAtTick = param2;
    }
}
