package net.minecraft.util.profiling.jfr.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import net.minecraft.Util;
import net.minecraft.util.profiling.jfr.Percentiles;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.PacketStat;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;
import net.minecraft.util.profiling.jfr.stats.TimedStatSummary;
import net.minecraft.world.level.chunk.ChunkStatus;

public class JfrResultJsonSerializer {
    private static final String BYTES_PER_SECOND = "bytesPerSecond";
    private static final String COUNT = "count";
    private static final String DURATION_NANOS_TOTAL = "durationNanosTotal";
    private static final String TOTAL_BYTES = "totalBytes";
    private static final String COUNT_PER_SECOND = "countPerSecond";
    final Gson gson = new GsonBuilder().setPrettyPrinting().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT).create();

    public String format(JfrStatsResult param0) {
        JsonObject var0 = new JsonObject();
        var0.addProperty("startedEpoch", param0.recordingStarted().toEpochMilli());
        var0.addProperty("endedEpoch", param0.recordingEnded().toEpochMilli());
        var0.addProperty("durationMs", param0.recordingDuration().toMillis());
        Duration var1 = param0.worldCreationDuration();
        if (var1 != null) {
            var0.addProperty("worldGenDurationMs", var1.toMillis());
        }

        var0.add("heap", this.heap(param0.heapSummary()));
        var0.add("cpuPercent", this.cpu(param0.cpuLoadStats()));
        var0.add("network", this.network(param0));
        var0.add("fileIO", this.fileIO(param0));
        var0.add("serverTick", this.serverTicks(param0.tickTimes()));
        var0.add("threadAllocation", this.threadAllocations(param0.threadAllocationSummary()));
        var0.add("chunkGen", this.chunkGen(param0.chunkGenSummary()));
        return this.gson.toJson((JsonElement)var0);
    }

    private JsonElement heap(GcHeapStat.Summary param0) {
        JsonObject var0 = new JsonObject();
        var0.addProperty("allocationRateBytesPerSecond", param0.allocationRateBytesPerSecond());
        var0.addProperty("gcCount", param0.totalGCs());
        var0.addProperty("gcOverHeadPercent", param0.gcOverHead());
        var0.addProperty("gcTotalDurationMs", param0.gcTotalDuration().toMillis());
        return var0;
    }

    private JsonElement chunkGen(List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> param0) {
        JsonObject var0 = new JsonObject();
        var0.addProperty("durationNanosTotal", param0.stream().mapToDouble(param0x -> (double)param0x.getSecond().totalDuration().toNanos()).sum());
        JsonArray var1 = Util.make(new JsonArray(), param1 -> var0.add("status", param1));

        for(Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>> var2 : param0) {
            TimedStatSummary<ChunkGenStat> var3 = var2.getSecond();
            JsonObject var4 = Util.make(new JsonObject(), var1::add);
            var4.addProperty("state", var2.getFirst().getName());
            var4.addProperty("count", var3.count());
            var4.addProperty("durationNanosTotal", var3.totalDuration().toNanos());
            var4.addProperty("durationNanosAvg", var3.totalDuration().toNanos() / (long)var3.count());
            JsonObject var5 = Util.make(new JsonObject(), param1 -> var4.add("durationNanosPercentiles", param1));
            var3.percentilesNanos().forEach((param1, param2) -> var5.addProperty("p" + param1, param2));
            Function<ChunkGenStat, JsonElement> var6 = param0x -> {
                JsonObject var0x = new JsonObject();
                var0x.addProperty("durationNanos", param0x.duration().toNanos());
                var0x.addProperty("level", param0x.level());
                var0x.addProperty("chunkPosX", param0x.chunkPos().x);
                var0x.addProperty("chunkPosZ", param0x.chunkPos().z);
                var0x.addProperty("worldPosX", param0x.worldPos().x);
                var0x.addProperty("worldPosZ", param0x.worldPos().z);
                return var0x;
            };
            var4.add("fastest", var6.apply(var3.fastest()));
            var4.add("slowest", var6.apply(var3.slowest()));
            var4.add("secondSlowest", (JsonElement)(var3.secondSlowest() != null ? var6.apply(var3.secondSlowest()) : JsonNull.INSTANCE));
        }

        return var0;
    }

    private JsonElement threadAllocations(ThreadAllocationStat.Summary param0) {
        JsonArray var0 = new JsonArray();
        param0.allocationsPerSecondByThread().forEach((param1, param2) -> var0.add(Util.make(new JsonObject(), param2x -> {
                param2x.addProperty("thread", param1);
                param2x.addProperty("bytesPerSecond", param2);
            })));
        return var0;
    }

    private JsonElement serverTicks(List<TickTimeStat> param0) {
        if (param0.isEmpty()) {
            return JsonNull.INSTANCE;
        } else {
            JsonObject var0 = new JsonObject();
            DoubleSummaryStatistics var1 = param0.stream().mapToDouble(TickTimeStat::currentAverage).summaryStatistics();
            var0.addProperty("minMs", var1.getMin());
            var0.addProperty("averageMs", var1.getAverage());
            var0.addProperty("maxMs", var1.getMax());
            Percentiles.evaluate(param0.stream().mapToDouble(TickTimeStat::currentAverage).toArray())
                .forEach((param1, param2) -> var0.addProperty("p" + param1, param2));
            return var0;
        }
    }

    private JsonElement fileIO(JfrStatsResult param0) {
        JsonObject var0 = new JsonObject();
        var0.add("write", this.fileIoSummary(param0.fileWrites()));
        var0.add("read", this.fileIoSummary(param0.fileReads()));
        return var0;
    }

    private JsonElement fileIoSummary(FileIOStat.Summary param0) {
        JsonObject var0 = new JsonObject();
        var0.addProperty("totalBytes", param0.totalBytes());
        var0.addProperty("count", param0.counts());
        var0.addProperty("bytesPerSecond", param0.bytesPerSecond());
        var0.addProperty("countPerSecond", param0.countsPerSecond());
        JsonArray var1 = new JsonArray();
        var0.add("topContributors", var1);
        param0.topTenContributorsByTotalBytes().forEach(param1 -> {
            JsonObject var0x = new JsonObject();
            var1.add(var0x);
            var0x.addProperty("path", param1.getFirst());
            var0x.addProperty("totalBytes", param1.getSecond());
        });
        return var0;
    }

    private JsonElement network(JfrStatsResult param0) {
        JsonObject var0 = new JsonObject();
        var0.add("sent", this.packets(param0.sentPackets()));
        var0.add("received", this.packets(param0.receivedPackets()));
        return var0;
    }

    private JsonElement packets(PacketStat.Summary param0) {
        JsonObject var0 = new JsonObject();
        var0.addProperty("totalBytes", param0.totalSize());
        var0.addProperty("count", param0.totalCount());
        var0.addProperty("bytesPerSecond", param0.sizePerSecond());
        var0.addProperty("countPerSecond", param0.countsPerSecond());
        JsonArray var1 = new JsonArray();
        var0.add("topContributors", var1);
        param0.largestSizeContributors().stream().limit(10L).forEach(param1 -> {
            JsonObject var0x = new JsonObject();
            var1.add(var0x);
            var0x.addProperty("packetName", param1.getFirst());
            var0x.addProperty("totalBytes", param1.getSecond());
        });
        return var0;
    }

    private JsonElement cpu(List<CpuLoadStat> param0) {
        JsonObject var0 = new JsonObject();
        BiFunction<List<CpuLoadStat>, ToDoubleFunction<CpuLoadStat>, JsonObject> var1 = (param0x, param1) -> {
            JsonObject var0x = new JsonObject();
            DoubleSummaryStatistics var1x = param0x.stream().mapToDouble(param1).summaryStatistics();
            var0x.addProperty("min", var1x.getMin());
            var0x.addProperty("average", var1x.getAverage());
            var0x.addProperty("max", var1x.getMax());
            return var0x;
        };
        var0.add("jvm", var1.apply(param0, CpuLoadStat::jvm));
        var0.add("userJvm", var1.apply(param0, CpuLoadStat::userJvm));
        var0.add("system", var1.apply(param0, CpuLoadStat::system));
        return var0;
    }
}
