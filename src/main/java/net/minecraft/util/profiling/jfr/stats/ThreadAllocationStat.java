package net.minecraft.util.profiling.jfr.stats;

import com.google.common.base.MoreObjects;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedThread;

public record ThreadAllocationStat(Instant timestamp, String threadName, long totalBytes) {
    private static final String UNKNOWN_THREAD = "unknown";

    public static ThreadAllocationStat from(RecordedEvent param0) {
        RecordedThread var0 = param0.getThread("thread");
        String var1 = var0 == null ? "unknown" : MoreObjects.firstNonNull(var0.getJavaName(), "unknown");
        return new ThreadAllocationStat(param0.getStartTime(), var1, param0.getLong("allocated"));
    }

    public static ThreadAllocationStat.Summary summary(List<ThreadAllocationStat> param0) {
        Map<String, Double> var0 = new TreeMap<>();
        Map<String, List<ThreadAllocationStat>> var1 = param0.stream().collect(Collectors.groupingBy(param0x -> param0x.threadName));
        var1.forEach((param1, param2) -> {
            if (param2.size() >= 2) {
                ThreadAllocationStat var0x = (ThreadAllocationStat)param2.get(0);
                ThreadAllocationStat var1x = (ThreadAllocationStat)param2.get(param2.size() - 1);
                long var2x = Duration.between(var0x.timestamp, var1x.timestamp).getSeconds();
                long var3 = var1x.totalBytes - var0x.totalBytes;
                var0.put(param1, (double)var3 / (double)var2x);
            }
        });
        return new ThreadAllocationStat.Summary(var0);
    }

    public static record Summary(Map<String, Double> allocationsPerSecondByThread) {
    }
}
