package net.minecraft.util.profiling.jfr.stats;

import java.time.Duration;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;

public record ChunkGenStat(Duration duration, ChunkPos chunkPos, ColumnPos worldPos, ChunkStatus status, boolean success, String level) implements TimedStat {
    public static ChunkGenStat from(RecordedEvent param0) {
        return new ChunkGenStat(
            param0.getDuration(),
            new ChunkPos(param0.getInt("chunkPosX"), param0.getInt("chunkPosX")),
            new ColumnPos(param0.getInt("worldPosX"), param0.getInt("worldPosZ")),
            ChunkStatus.byName(param0.getString("status")),
            param0.getBoolean("success"),
            param0.getString("level")
        );
    }
}
