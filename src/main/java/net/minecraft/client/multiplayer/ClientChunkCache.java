package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientChunkCache extends ChunkSource {
    static final Logger LOGGER = LogUtils.getLogger();
    private final LevelChunk emptyChunk;
    private final LevelLightEngine lightEngine;
    volatile ClientChunkCache.Storage storage;
    final ClientLevel level;

    public ClientChunkCache(ClientLevel param0, int param1) {
        this.level = param0;
        this.emptyChunk = new EmptyLevelChunk(
            param0, new ChunkPos(0, 0), param0.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS)
        );
        this.lightEngine = new LevelLightEngine(this, true, param0.dimensionType().hasSkyLight());
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

    public void drop(ChunkPos param0) {
        if (this.storage.inRange(param0.x, param0.z)) {
            int var0 = this.storage.getIndex(param0.x, param0.z);
            LevelChunk var1 = this.storage.getChunk(var0);
            if (isValidChunk(var1, param0.x, param0.z)) {
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

    public void replaceBiomes(int param0, int param1, FriendlyByteBuf param2) {
        if (!this.storage.inRange(param0, param1)) {
            LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", param0, param1);
        } else {
            int var0 = this.storage.getIndex(param0, param1);
            LevelChunk var1 = this.storage.chunks.get(var0);
            if (!isValidChunk(var1, param0, param1)) {
                LOGGER.warn("Ignoring chunk since it's not present: {}, {}", param0, param1);
            } else {
                var1.replaceBiomes(param2);
            }

        }
    }

    @Nullable
    public LevelChunk replaceWithPacketData(
        int param0, int param1, FriendlyByteBuf param2, CompoundTag param3, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> param4
    ) {
        if (!this.storage.inRange(param0, param1)) {
            LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", param0, param1);
            return null;
        } else {
            int var0 = this.storage.getIndex(param0, param1);
            LevelChunk var1 = this.storage.chunks.get(var0);
            ChunkPos var2 = new ChunkPos(param0, param1);
            if (!isValidChunk(var1, param0, param1)) {
                var1 = new LevelChunk(this.level, var2);
                var1.replaceWithPacketData(param2, param3, param4);
                this.storage.replace(var0, var1);
            } else {
                var1.replaceWithPacketData(param2, param3, param4);
            }

            this.level.onChunkLoaded(var2);
            return var1;
        }
    }

    @Override
    public void tick(BooleanSupplier param0, boolean param1) {
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
        return this.storage.chunks.length() + ", " + this.getLoadedChunksCount();
    }

    @Override
    public int getLoadedChunksCount() {
        return this.storage.chunkCount;
    }

    @Override
    public void onLightUpdate(LightLayer param0, SectionPos param1) {
        Minecraft.getInstance().levelRenderer.setSectionDirty(param1.x(), param1.y(), param1.z());
    }

    @OnlyIn(Dist.CLIENT)
    final class Storage {
        final AtomicReferenceArray<LevelChunk> chunks;
        final int chunkRadius;
        private final int viewRange;
        volatile int viewCenterX;
        volatile int viewCenterZ;
        int chunkCount;

        Storage(int param0) {
            this.chunkRadius = param0;
            this.viewRange = param0 * 2 + 1;
            this.chunks = new AtomicReferenceArray<>(this.viewRange * this.viewRange);
        }

        int getIndex(int param0, int param1) {
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

        boolean inRange(int param0, int param1) {
            return Math.abs(param0 - this.viewCenterX) <= this.chunkRadius && Math.abs(param1 - this.viewCenterZ) <= this.chunkRadius;
        }

        @Nullable
        protected LevelChunk getChunk(int param0) {
            return this.chunks.get(param0);
        }

        private void dumpChunks(String param0) {
            try (FileOutputStream var0 = new FileOutputStream(param0)) {
                int var1 = ClientChunkCache.this.storage.chunkRadius;

                for(int var2 = this.viewCenterZ - var1; var2 <= this.viewCenterZ + var1; ++var2) {
                    for(int var3 = this.viewCenterX - var1; var3 <= this.viewCenterX + var1; ++var3) {
                        LevelChunk var4 = ClientChunkCache.this.storage.chunks.get(ClientChunkCache.this.storage.getIndex(var3, var2));
                        if (var4 != null) {
                            ChunkPos var5 = var4.getPos();
                            var0.write((var5.x + "\t" + var5.z + "\t" + var4.isEmpty() + "\n").getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }
            } catch (IOException var10) {
                ClientChunkCache.LOGGER.error("Failed to dump chunks to file {}", param0, var10);
            }

        }
    }
}
