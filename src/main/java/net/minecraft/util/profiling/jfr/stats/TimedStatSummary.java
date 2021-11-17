package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.jfr.Percentiles;

public record TimedStatSummary<T>(T fastest, T slowest, @Nullable T secondSlowest, int count, Map<Integer, Double> percentilesNanos, Duration totalDuration) {
    public static <T extends TimedStat> TimedStatSummary<T> summary(List<T> param0) {
        if (param0.isEmpty()) {
            throw new IllegalArgumentException("No values");
        } else {
            List<T> var0 = param0.stream().sorted(Comparator.comparing(TimedStat::duration)).toList();
            Duration var1 = var0.stream().map(TimedStat::duration).reduce(Duration::plus).orElse(Duration.ZERO);
            T var2 = var0.get(0);
            T var3 = var0.get(var0.size() - 1);
            T var4 = var0.size() > 1 ? var0.get(var0.size() - 2) : null;
            int var5 = var0.size();
            Map<Integer, Double> var6 = Percentiles.evaluate(var0.stream().mapToLong(param0x -> param0x.duration().toNanos()).toArray());
            return new TimedStatSummary<>(var2, var3, var4, var5, var6, var1);
        }
    }
}
