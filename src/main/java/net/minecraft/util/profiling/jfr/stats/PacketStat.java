package net.minecraft.util.profiling.jfr.stats;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;

public record PacketStat(Instant timestamp, String packetName, int bytes) implements TimeStamped {
    public static PacketStat from(RecordedEvent param0) {
        return new PacketStat(param0.getStartTime(), param0.getString("packetName"), param0.getInt("bytes"));
    }

    public static PacketStat.Summary summary(Duration param0, List<PacketStat> param1) {
        IntSummaryStatistics var0 = param1.stream().mapToInt(param0x -> param0x.bytes).summaryStatistics();
        long var1 = (long)param1.size();
        long var2 = var0.getSum();
        List<Pair<String, Long>> var3 = param1.stream()
            .collect(Collectors.groupingBy(param0x -> param0x.packetName, Collectors.summingLong(param0x -> (long)param0x.bytes)))
            .entrySet()
            .stream()
            .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(5L)
            .map(param0x -> Pair.of(param0x.getKey(), param0x.getValue()))
            .toList();
        return new PacketStat.Summary(var1, var2, var3, param0);
    }

    @Override
    public Instant getTimestamp() {
        return this.timestamp;
    }

    public static record Summary(long totalCount, long totalSize, List<Pair<String, Long>> largestSizeContributors, Duration recordingDuration) {
        public double countsPerSecond() {
            return (double)this.totalCount / (double)this.recordingDuration.getSeconds();
        }

        public double sizePerSecond() {
            return (double)this.totalSize / (double)this.recordingDuration.getSeconds();
        }
    }
}
