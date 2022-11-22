package net.minecraft.util.profiling.jfr.parse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.NetworkPacketSummary;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;

public class JfrStatsParser {
    private Instant recordingStarted = Instant.EPOCH;
    private Instant recordingEnded = Instant.EPOCH;
    private final List<ChunkGenStat> chunkGenStats = Lists.newArrayList();
    private final List<CpuLoadStat> cpuLoadStat = Lists.newArrayList();
    private final Map<NetworkPacketSummary.PacketIdentification, JfrStatsParser.MutableCountAndSize> receivedPackets = Maps.newHashMap();
    private final Map<NetworkPacketSummary.PacketIdentification, JfrStatsParser.MutableCountAndSize> sentPackets = Maps.newHashMap();
    private final List<FileIOStat> fileWrites = Lists.newArrayList();
    private final List<FileIOStat> fileReads = Lists.newArrayList();
    private int garbageCollections;
    private Duration gcTotalDuration = Duration.ZERO;
    private final List<GcHeapStat> gcHeapStats = Lists.newArrayList();
    private final List<ThreadAllocationStat> threadAllocationStats = Lists.newArrayList();
    private final List<TickTimeStat> tickTimes = Lists.newArrayList();
    @Nullable
    private Duration worldCreationDuration = null;

    private JfrStatsParser(Stream<RecordedEvent> param0) {
        this.capture(param0);
    }

    public static JfrStatsResult parse(Path param0) {
        try {
            JfrStatsResult var4;
            try (final RecordingFile var0 = new RecordingFile(param0)) {
                Iterator<RecordedEvent> var1 = new Iterator<RecordedEvent>() {
                    @Override
                    public boolean hasNext() {
                        return var0.hasMoreEvents();
                    }

                    public RecordedEvent next() {
                        if (!this.hasNext()) {
                            throw new NoSuchElementException();
                        } else {
                            try {
                                return var0.readEvent();
                            } catch (IOException var2) {
                                throw new UncheckedIOException(var2);
                            }
                        }
                    }
                };
                Stream<RecordedEvent> var2 = StreamSupport.stream(Spliterators.spliteratorUnknownSize(var1, 1297), false);
                var4 = new JfrStatsParser(var2).results();
            }

            return var4;
        } catch (IOException var7) {
            throw new UncheckedIOException(var7);
        }
    }

    private JfrStatsResult results() {
        Duration var0 = Duration.between(this.recordingStarted, this.recordingEnded);
        return new JfrStatsResult(
            this.recordingStarted,
            this.recordingEnded,
            var0,
            this.worldCreationDuration,
            this.tickTimes,
            this.cpuLoadStat,
            GcHeapStat.summary(var0, this.gcHeapStats, this.gcTotalDuration, this.garbageCollections),
            ThreadAllocationStat.summary(this.threadAllocationStats),
            collectPacketStats(var0, this.receivedPackets),
            collectPacketStats(var0, this.sentPackets),
            FileIOStat.summary(var0, this.fileWrites),
            FileIOStat.summary(var0, this.fileReads),
            this.chunkGenStats
        );
    }

    private void capture(Stream<RecordedEvent> param0) {
        param0.forEach(param0x -> {
            if (param0x.getEndTime().isAfter(this.recordingEnded) || this.recordingEnded.equals(Instant.EPOCH)) {
                this.recordingEnded = param0x.getEndTime();
            }

            if (param0x.getStartTime().isBefore(this.recordingStarted) || this.recordingStarted.equals(Instant.EPOCH)) {
                this.recordingStarted = param0x.getStartTime();
            }

            String var2 = param0x.getEventType().getName();
            switch(var2) {
                case "minecraft.ChunkGeneration":
                    this.chunkGenStats.add(ChunkGenStat.from(param0x));
                    break;
                case "minecraft.LoadWorld":
                    this.worldCreationDuration = param0x.getDuration();
                    break;
                case "minecraft.ServerTickTime":
                    this.tickTimes.add(TickTimeStat.from(param0x));
                    break;
                case "minecraft.PacketReceived":
                    this.incrementPacket(param0x, param0x.getInt("bytes"), this.receivedPackets);
                    break;
                case "minecraft.PacketSent":
                    this.incrementPacket(param0x, param0x.getInt("bytes"), this.sentPackets);
                    break;
                case "jdk.ThreadAllocationStatistics":
                    this.threadAllocationStats.add(ThreadAllocationStat.from(param0x));
                    break;
                case "jdk.GCHeapSummary":
                    this.gcHeapStats.add(GcHeapStat.from(param0x));
                    break;
                case "jdk.CPULoad":
                    this.cpuLoadStat.add(CpuLoadStat.from(param0x));
                    break;
                case "jdk.FileWrite":
                    this.appendFileIO(param0x, this.fileWrites, "bytesWritten");
                    break;
                case "jdk.FileRead":
                    this.appendFileIO(param0x, this.fileReads, "bytesRead");
                    break;
                case "jdk.GarbageCollection":
                    ++this.garbageCollections;
                    this.gcTotalDuration = this.gcTotalDuration.plus(param0x.getDuration());
            }

        });
    }

    private void incrementPacket(RecordedEvent param0, int param1, Map<NetworkPacketSummary.PacketIdentification, JfrStatsParser.MutableCountAndSize> param2) {
        param2.computeIfAbsent(NetworkPacketSummary.PacketIdentification.from(param0), param0x -> new JfrStatsParser.MutableCountAndSize()).increment(param1);
    }

    private void appendFileIO(RecordedEvent param0, List<FileIOStat> param1, String param2) {
        param1.add(new FileIOStat(param0.getDuration(), param0.getString("path"), param0.getLong(param2)));
    }

    private static NetworkPacketSummary collectPacketStats(
        Duration param0, Map<NetworkPacketSummary.PacketIdentification, JfrStatsParser.MutableCountAndSize> param1
    ) {
        List<Pair<NetworkPacketSummary.PacketIdentification, NetworkPacketSummary.PacketCountAndSize>> var0 = param1.entrySet()
            .stream()
            .map(
                param0x -> Pair.of(
                        (NetworkPacketSummary.PacketIdentification)param0x.getKey(), ((JfrStatsParser.MutableCountAndSize)param0x.getValue()).toCountAndSize()
                    )
            )
            .toList();
        return new NetworkPacketSummary(param0, var0);
    }

    public static final class MutableCountAndSize {
        private long count;
        private long totalSize;

        public void increment(int param0) {
            this.totalSize += (long)param0;
            ++this.count;
        }

        public NetworkPacketSummary.PacketCountAndSize toCountAndSize() {
            return new NetworkPacketSummary.PacketCountAndSize(this.count, this.totalSize);
        }
    }
}
