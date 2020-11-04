package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityStorage implements EntityPersistentStorage<Entity> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ServerLevel level;
    private final IOWorker worker;
    private final LongSet emptyChunks = new LongOpenHashSet();
    private final Executor mainThreadExecutor;
    protected final DataFixer fixerUpper;

    public EntityStorage(ServerLevel param0, File param1, DataFixer param2, boolean param3, Executor param4) {
        this.level = param0;
        this.fixerUpper = param2;
        this.mainThreadExecutor = param4;
        this.worker = new IOWorker(param1, param3, "entities");
    }

    @Override
    public CompletableFuture<ChunkEntities<Entity>> loadEntities(ChunkPos param0) {
        return this.emptyChunks.contains(param0.toLong())
            ? CompletableFuture.completedFuture(emptyChunk(param0))
            : this.worker.loadAsync(param0).thenApplyAsync(param1 -> {
                if (param1 == null) {
                    this.emptyChunks.add(param0.toLong());
                    return emptyChunk(param0);
                } else {
                    try {
                        ChunkPos var0 = readChunkPos(param1);
                        if (!Objects.equals(param0, var0)) {
                            LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", param0, param0, var0);
                        }
                    } catch (Exception var6) {
                        LOGGER.warn("Failed to parse chunk {} position info", param0, var6);
                    }
    
                    CompoundTag var2 = this.upgradeChunkTag(param1);
                    ListTag var3 = var2.getList("Entities", 10);
                    List<Entity> var4 = EntityType.loadEntitiesRecursive(var3, this.level).collect(ImmutableList.toImmutableList());
                    return new ChunkEntities<>(param0, var4);
                }
            }, this.mainThreadExecutor);
    }

    private static ChunkPos readChunkPos(CompoundTag param0) {
        int[] var0 = param0.getIntArray("Position");
        return new ChunkPos(var0[0], var0[1]);
    }

    private static void writeChunkPos(CompoundTag param0, ChunkPos param1) {
        param0.put("Position", new IntArrayTag(new int[]{param1.x, param1.z}));
    }

    private static ChunkEntities<Entity> emptyChunk(ChunkPos param0) {
        return new ChunkEntities<>(param0, ImmutableList.of());
    }

    @Override
    public void storeEntities(ChunkEntities<Entity> param0) {
        ChunkPos var0 = param0.getPos();
        if (param0.isEmpty()) {
            if (this.emptyChunks.add(var0.toLong())) {
                this.worker.store(var0, null);
            }

        } else {
            ListTag var1 = new ListTag();
            param0.getEntities().forEach(param1 -> {
                CompoundTag var0x = new CompoundTag();
                if (param1.save(var0x)) {
                    var1.add(var0x);
                }

            });
            CompoundTag var2 = new CompoundTag();
            var2.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
            var2.put("Entities", var1);
            writeChunkPos(var2, var0);
            this.worker.store(var0, var2).exceptionally(param1 -> {
                LOGGER.error("Failed to store chunk {}", var0, param1);
                return null;
            });
            this.emptyChunks.remove(var0.toLong());
        }
    }

    @Override
    public void flush() {
        this.worker.synchronize().join();
    }

    private CompoundTag upgradeChunkTag(CompoundTag param0) {
        int var0 = getVersion(param0);
        return NbtUtils.update(this.fixerUpper, DataFixTypes.ENTITY_CHUNK, param0, var0);
    }

    public static int getVersion(CompoundTag param0) {
        return param0.contains("DataVersion", 99) ? param0.getInt("DataVersion") : -1;
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }
}
