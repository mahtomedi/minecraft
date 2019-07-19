package net.minecraft.world.level.chunk;

import java.io.IOException;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    public BlockGetter getChunkForLighting(int param0, int param1) {
        return this.getChunk(param0, param1, ChunkStatus.EMPTY, false);
    }

    public boolean hasChunk(int param0, int param1) {
        return this.getChunk(param0, param1, ChunkStatus.FULL, false) != null;
    }

    @Nullable
    public abstract ChunkAccess getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

    @OnlyIn(Dist.CLIENT)
    public abstract void tick(BooleanSupplier var1);

    public abstract String gatherStats();

    public abstract ChunkGenerator<?> getGenerator();

    @Override
    public void close() throws IOException {
    }

    public abstract LevelLightEngine getLightEngine();

    public void setSpawnSettings(boolean param0, boolean param1) {
    }

    public void updateChunkForced(ChunkPos param0, boolean param1) {
    }

    public boolean isEntityTickingChunk(Entity param0) {
        return true;
    }

    public boolean isEntityTickingChunk(ChunkPos param0) {
        return true;
    }

    public boolean isTickingChunk(BlockPos param0) {
        return true;
    }
}
