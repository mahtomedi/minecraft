package net.minecraft.server.level.progress;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StoringChunkProgressListener implements ChunkProgressListener {
    private final LoggerChunkProgressListener delegate;
    private final Long2ObjectOpenHashMap<ChunkStatus> statuses;
    private ChunkPos spawnPos = new ChunkPos(0, 0);
    private final int fullDiameter;
    private final int radius;
    private final int diameter;
    private boolean started;

    public StoringChunkProgressListener(int param0) {
        this.delegate = new LoggerChunkProgressListener(param0);
        this.fullDiameter = param0 * 2 + 1;
        this.radius = param0 + ChunkStatus.maxDistance();
        this.diameter = this.radius * 2 + 1;
        this.statuses = new Long2ObjectOpenHashMap<>();
    }

    @Override
    public void updateSpawnPos(ChunkPos param0) {
        if (this.started) {
            this.delegate.updateSpawnPos(param0);
            this.spawnPos = param0;
        }
    }

    @Override
    public void onStatusChange(ChunkPos param0, @Nullable ChunkStatus param1) {
        if (this.started) {
            this.delegate.onStatusChange(param0, param1);
            if (param1 == null) {
                this.statuses.remove(param0.toLong());
            } else {
                this.statuses.put(param0.toLong(), param1);
            }

        }
    }

    public void start() {
        this.started = true;
        this.statuses.clear();
    }

    @Override
    public void stop() {
        this.started = false;
        this.delegate.stop();
    }

    public int getFullDiameter() {
        return this.fullDiameter;
    }

    public int getDiameter() {
        return this.diameter;
    }

    public int getProgress() {
        return this.delegate.getProgress();
    }

    @Nullable
    public ChunkStatus getStatus(int param0, int param1) {
        return this.statuses.get(ChunkPos.asLong(param0 + this.spawnPos.x - this.radius, param1 + this.spawnPos.z - this.radius));
    }
}
