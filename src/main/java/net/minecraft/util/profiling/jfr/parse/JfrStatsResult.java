package net.minecraft.util.profiling.jfr.parse;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.jfr.serialize.JfrResultJsonSerializer;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.NetworkPacketSummary;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;
import net.minecraft.util.profiling.jfr.stats.TimedStatSummary;
import net.minecraft.world.level.chunk.ChunkStatus;

public record JfrStatsResult(
    Instant recordingStarted,
    Instant recordingEnded,
    Duration recordingDuration,
    @Nullable Duration worldCreationDuration,
    List<TickTimeStat> tickTimes,
    List<CpuLoadStat> cpuLoadStats,
    GcHeapStat.Summary heapSummary,
    ThreadAllocationStat.Summary threadAllocationSummary,
    NetworkPacketSummary receivedPacketsSummary,
    NetworkPacketSummary sentPacketsSummary,
    FileIOStat.Summary fileWrites,
    FileIOStat.Summary fileReads,
    List<ChunkGenStat> chunkGenStats
) {
    public List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> chunkGenSummary() {
        Map<ChunkStatus, List<ChunkGenStat>> var0 = this.chunkGenStats.stream().collect(Collectors.groupingBy(ChunkGenStat::status));
        return var0.entrySet()
            .stream()
            .map(param0 -> Pair.of(param0.getKey(), TimedStatSummary.summary(param0.getValue())))
            .sorted(Comparator.<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>, Duration>comparing(param0 -> param0.getSecond().totalDuration()).reversed())
            .toList();
    }

    public String asJson() {
        return new JfrResultJsonSerializer().format(this);
    }
}
