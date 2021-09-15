package net.minecraft.util.profiling.jfr.stats;

import java.time.Instant;
import jdk.jfr.consumer.RecordedEvent;

public record TickTimeStat(Instant timestamp, float currentAverage) {
    public static TickTimeStat from(RecordedEvent param0) {
        return new TickTimeStat(param0.getStartTime(), param0.getFloat("averageTickMs"));
    }
}
