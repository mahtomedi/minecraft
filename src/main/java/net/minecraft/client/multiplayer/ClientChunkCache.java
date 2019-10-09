package net.minecraft.client.multiplayer;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientChunkCache extends ChunkSource {
    private static final Logger LOGGER = LogManager.getLogger();
    private final LevelChunk emptyChunk;
    private final LevelLightEngine lightEngine;
    private volatile ClientChunkCache.Storage storage;
    private final MultiPlayerLevel level;

    public ClientChunkCache(MultiPlayerLevel param0, int param1) {
        this.level = param0;
        this.emptyChunk = new EmptyLevelChunk(param0, new ChunkPos(0, 0));
        this.lightEngine = new LevelLightEngine(this, true, param0.getDimension().isHasSkyLight());
        this.storage = new ClientChunkCache.Storage(calculateStorageRange(param1));
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    private static boolean isValidChunk(@Nullable LevelChunk param0, int param1, int param2) {
        if (param0 == null) {
            return false;
        } else {
            ChunkPos var0 = param0.getPos();
            return var0.x == param1 && var0.z == param2;
        }
    }

    public void drop(int param0, int param1) {
        if (this.storage.inRange(param0, param1)) {
            int var0 = this.storage.getIndex(param0, param1);
            LevelChunk var1 = this.storage.getChunk(var0);
            if (isValidChunk(var1, param0, param1)) {
                this.storage.replace(var0, var1, null);
            }

        }
    }

    @Nullable
    public LevelChunk getChunk(int param0, int param1, ChunkStatus param2, boolean param3) {
        if (this.storage.inRange(param0, param1)) {
            LevelChunk var0 = this.storage.getChunk(this.storage.getIndex(param0, param1));
            if (isValidChunk(var0, param0, param1)) {
                return var0;
            }
        }

        return param3 ? this.emptyChunk : null;
    }

    @Override
    public BlockGetter getLevel() {
        return this.level;
    }

    @Nullable
    public LevelChunk replaceWithPacketData(
        Level param0, int param1, int param2, @Nullable ChunkBiomeContainer param3, FriendlyByteBuf param4, CompoundTag param5, int param6
    ) {
        if (!this.storage.inRange(param1, param2)) {
            LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", param1, param2);
            return null;
        } else {
            int var0 = this.storage.getIndex(param1, param2);
            LevelChunk var1 = this.storage.chunks.get(var0);
            if (!isValidChunk(var1, param1, param2)) {
                if (param3 == null) {
                    LOGGER.warn("Ignoring chunk since we don't have complete data: {}, {}", param1, param2);
                    return null;
                }

                var1 = new LevelChunk(param0, new ChunkPos(param1, param2), param3);
                var1.replaceWithPacketData(param3, param4, param5, param6);
                this.storage.replace(var0, var1);
            } else {
                var1.replaceWithPacketData(param3, param4, param5, param6);
            }

            LevelChunkSection[] var2 = var1.getSections();
            LevelLightEngine var3 = this.getLightEngine();
            var3.enableLightSources(new ChunkPos(param1, param2), true);

            for(int var4 = 0; var4 < var2.length; ++var4) {
                LevelChunkSection var5 = var2[var4];
                var3.updateSectionStatus(SectionPos.of(param1, var4, param2), LevelChunkSection.isEmpty(var5));
            }

            return var1;
        }
    }

    @Override
    public void tick(BooleanSupplier param0) {
    }

    public void updateViewCenter(int param0, int param1) {
        this.storage.viewCenterX = param0;
        this.storage.viewCenterZ = param1;
    }

    public void updateViewRadius(int param0) {
        int var0 = this.storage.chunkRadius;
        int var1 = calculateStorageRange(param0);
        if (var0 != var1) {
            ClientChunkCache.Storage var2 = new ClientChunkCache.Storage(var1);
            var2.viewCenterX = this.storage.viewCenterX;
            var2.viewCenterZ = this.storage.viewCenterZ;

            for(int var3 = 0; var3 < this.storage.chunks.length(); ++var3) {
                LevelChunk var4 = this.storage.chunks.get(var3);
                if (var4 != null) {
                    ChunkPos var5 = var4.getPos();
                    if (var2.inRange(var5.x, var5.z)) {
                        var2.replace(var2.getIndex(var5.x, var5.z), var4);
                    }
                }
            }

            this.storage = var2;
        }

    }

    private static int calculateStorageRange(int param0) {
        return Math.max(2, param0) + 3;
    }

    @Override
    public String gatherStats() {
        return "Client Chunk Cache: " + this.storage.chunks.length() + ", " + this.getLoadedChunksCount();
    }

    public int getLoadedChunksCount() {
        return this.storage.chunkCount;
    }

    @Override
    public void onLightUpdate(LightLayer param0, SectionPos param1) {
        Minecraft.getInstance().levelRenderer.setSectionDirty(param1.x(), param1.y(), param1.z());
    }

    @Override
    public boolean isTickingChunk(BlockPos param0) {
        return this.hasChunk(param0.getX() >> 4, param0.getZ() >> 4);
    }

    @Override
    public boolean isEntityTickingChunk(ChunkPos param0) {
        return this.hasChunk(param0.x, param0.z);
    }

    @Override
    public boolean isEntityTickingChunk(Entity param0) {
        return this.hasChunk(Mth.floor(param0.getX()) >> 4, Mth.floor(param0.getZ()) >> 4);
    }

    @OnlyIn(Dist.CLIENT)
    final class Storage {
        private final AtomicReferenceArray<LevelChunk> chunks;
        private final int chunkRadius;
        private final int viewRange;
        private volatile int viewCenterX;
        private volatile int viewCenterZ;
        private int chunkCount;

        private Storage(int param0) {
            this.chunkRadius = param0;
            this.viewRange = param0 * 2 + 1;
            this.chunks = new AtomicReferenceArray<>(this.viewRange * this.viewRange);
        }

        private int getIndex(int param0, int param1) {
            return Math.floorMod(param1, this.viewRange) * this.viewRange + Math.floorMod(param0, this.viewRange);
        }

        protected void replace(int param0, @Nullable LevelChunk param1) {
            LevelChunk var0 = this.chunks.getAndSet(param0, param1);
            if (var0 != null) {
                --this.chunkCount;
                ClientChunkCache.this.level.unload(var0);
            }

            if (param1 != null) {
                ++this.chunkCount;
            }

        }

        protected LevelChunk replace(int param0, LevelChunk param1, @Nullable LevelChunk param2) {
            if (this.chunks.compareAndSet(param0, param1, param2) && param2 == null) {
                --this.chunkCount;
            }

            ClientChunkCache.this.level.unload(param1);
            return param1;
        }

        private boolean inRange(int param0, int param1) {
            return Math.abs(param0 - this.viewCenterX) <= this.chunkRadius && Math.abs(param1 - this.viewCenterZ) <= this.chunkRadius;
        }

        @Nullable
        protected LevelChunk getChunk(int param0) {
            return this.chunks.get(param0);
        }
    }
}
