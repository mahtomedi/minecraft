package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public record FileIOStat(Duration duration, @Nullable String path, long bytes) {
    public static FileIOStat.Summary summary(Duration param0, List<FileIOStat> param1) {
        long var0 = param1.stream().mapToLong(param0x -> param0x.bytes).sum();
        return new FileIOStat.Summary(
            var0,
            (double)var0 / (double)param0.getSeconds(),
            (long)param1.size(),
            (double)param1.size() / (double)param0.getSeconds(),
            param1.stream().map(FileIOStat::duration).reduce(Duration.ZERO, Duration::plus),
            param1.stream()
                .filter(param0x -> param0x.path != null)
                .collect(Collectors.groupingBy(param0x -> param0x.path, Collectors.summingLong(param0x -> param0x.bytes)))
                .entrySet()
                .stream()
                .sorted(Entry.<String, Long>comparingByValue().reversed())
                .map(param0x -> Pair.of(param0x.getKey(), param0x.getValue()))
                .limit(10L)
                .toList()
        );
    }

    public static record Summary(
        long totalBytes,
        double bytesPerSecond,
        long counts,
        double countsPerSecond,
        Duration timeSpentInIO,
        List<Pair<String, Long>> topTenContributorsByTotalBytes
    ) {
    }
}
