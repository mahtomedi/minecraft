package net.minecraft.world.level.chunk;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;

public abstract class ChunkSource implements AutoCloseable, LightChunkGetter {
    @Nullable
    public LevelChunk getChunk(int param0, int param1, boolean param2) {
        return (LevelChunk)this.getChunk(param0, param1, ChunkStatus.FULL, param2);
    }

    @Nullable
    public LevelChunk getChunkNow(int param0, int param1) {
        return this.getChunk(param0, param1, false);
    }

    @Nullable
    @Override
    public LightChunk getChunkForLighting(int param0, int param1) {
        return this.getChunk(param0, param1, ChunkStatus.EMPTY, false);
    }

    public boolean hasChunk(int param0, int param1) {
        return this.getChunk(param0, param1, ChunkStatus.FULL, false) != null;
    }

    @Nullable
    public abstract ChunkAccess getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

    public abstract void tick(BooleanSupplier var1, boolean var2);

    public abstract String gatherStats();

    public abstract int getLoadedChunksCount();

    @Override
    public void close() throws IOException {
    }

    public abstract LevelLightEngine getLightEngine();

    public void setSpawnSettings(boolean param0, boolean param1) {
    }

    public void updateChunkForced(ChunkPos param0, boolean param1) {
    }
}
