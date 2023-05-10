package net.minecraft.world.level.entity;

import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.ChunkPos;

@FunctionalInterface
public interface ChunkStatusUpdateListener {
    void onChunkStatusChange(ChunkPos var1, FullChunkStatus var2);
}
