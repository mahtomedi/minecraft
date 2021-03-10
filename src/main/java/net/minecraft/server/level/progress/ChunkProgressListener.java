package net.minecraft.server.level.progress;

import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ChunkProgressListener {
    void updateSpawnPos(ChunkPos var1);

    void onStatusChange(ChunkPos var1, @Nullable ChunkStatus var2);

    @OnlyIn(Dist.CLIENT)
    void start();

    void stop();
}
