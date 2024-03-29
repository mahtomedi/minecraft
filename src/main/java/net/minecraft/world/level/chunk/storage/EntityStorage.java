package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import org.slf4j.Logger;

public class EntityStorage implements EntityPersistentStorage<Entity> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ENTITIES_TAG = "Entities";
    private static final String POSITION_TAG = "Position";
    private final ServerLevel level;
    private final IOWorker worker;
    private final LongSet emptyChunks = new LongOpenHashSet();
    private final ProcessorMailbox<Runnable> entityDeserializerQueue;
    protected final DataFixer fixerUpper;

    public EntityStorage(ServerLevel param0, Path param1, DataFixer param2, boolean param3, Executor param4) {
        this.level = param0;
        this.fixerUpper = param2;
        this.entityDeserializerQueue = ProcessorMailbox.create(param4, "entity-deserializer");
        this.worker = new IOWorker(param1, param3, "entities");
    }

    @Override
    public CompletableFuture<ChunkEntities<Entity>> loadEntities(ChunkPos param0) {
        return this.emptyChunks.contains(param0.toLong())
            ? CompletableFuture.completedFuture(emptyChunk(param0))
            : this.worker.loadAsync(param0).thenApplyAsync(param1 -> {
                if (param1.isEmpty()) {
                    this.emptyChunks.add(param0.toLong());
                    return emptyChunk(param0);
                } else {
                    try {
                        ChunkPos var0 = readChunkPos(param1.get());
                        if (!Objects.equals(param0, var0)) {
                            LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", param0, param0, var0);
                        }
                    } catch (Exception var6) {
                        LOGGER.warn("Failed to parse chunk {} position info", param0, var6);
                    }
    
                    CompoundTag var2 = this.upgradeChunkTag(param1.get());
                    ListTag var3 = var2.getList("Entities", 10);
                    List<Entity> var4 = EntityType.loadEntitiesRecursive(var3, this.level).collect(ImmutableList.toImmutableList());
                    return new ChunkEntities<>(param0, var4);
                }
            }, this.entityDeserializerQueue::tell);
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
            CompoundTag var2 = NbtUtils.addCurrentDataVersion(new CompoundTag());
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
    public void flush(boolean param0) {
        this.worker.synchronize(param0).join();
        this.entityDeserializerQueue.runAll();
    }

    private CompoundTag upgradeChunkTag(CompoundTag param0) {
        int var0 = NbtUtils.getDataVersion(param0, -1);
        return DataFixTypes.ENTITY_CHUNK.updateToCurrentVersion(this.fixerUpper, param0, var0);
    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }
}
