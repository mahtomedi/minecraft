package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;

public record GcHeapStat(Instant timestamp, long heapUsed, GcHeapStat.Timing timing) {
    public static GcHeapStat from(RecordedEvent param0) {
        return new GcHeapStat(
            param0.getStartTime(),
            param0.getLong("heapUsed"),
            param0.getString("when").equalsIgnoreCase("before gc") ? GcHeapStat.Timing.BEFORE_GC : GcHeapStat.Timing.AFTER_GC
        );
    }

    public static GcHeapStat.Summary summary(Duration param0, List<GcHeapStat> param1, Duration param2, int param3) {
        return new GcHeapStat.Summary(param0, param2, param3, calculateAllocationRatePerSecond(param1));
    }

    private static double calculateAllocationRatePerSecond(List<GcHeapStat> param0) {
        long var0 = 0L;
        Map<GcHeapStat.Timing, List<GcHeapStat>> var1 = param0.stream().collect(Collectors.groupingBy(param0x -> param0x.timing));
        List<GcHeapStat> var2 = var1.get(GcHeapStat.Timing.BEFORE_GC);
        List<GcHeapStat> var3 = var1.get(GcHeapStat.Timing.AFTER_GC);

        for(int var4 = 1; var4 < var2.size(); ++var4) {
            GcHeapStat var5 = var2.get(var4);
            GcHeapStat var6 = var3.get(var4 - 1);
            var0 += var5.heapUsed - var6.heapUsed;
        }

        Duration var7 = Duration.between(param0.get(1).timestamp, param0.get(param0.size() - 1).timestamp);
        return (double)var0 / (double)var7.getSeconds();
    }

    public static record Summary(Duration duration, Duration gcTotalDuration, int totalGCs, double allocationRateBytesPerSecond) {
        public float gcOverHead() {
            return (float)this.gcTotalDuration.toMillis() / (float)this.duration.toMillis();
        }
    }

    static enum Timing {
        BEFORE_GC,
        AFTER_GC;
    }
}
