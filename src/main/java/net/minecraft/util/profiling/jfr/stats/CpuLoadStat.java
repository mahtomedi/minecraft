package net.minecraft.util.profiling.jfr.stats;

import jdk.jfr.consumer.RecordedEvent;

public record CpuLoadStat(double jvm, double userJvm, double system) {
    public static CpuLoadStat from(RecordedEvent param0) {
        return new CpuLoadStat((double)param0.getFloat("jvmSystem"), (double)param0.getFloat("jvmUser"), (double)param0.getFloat("machineTotal"));
    }
}
