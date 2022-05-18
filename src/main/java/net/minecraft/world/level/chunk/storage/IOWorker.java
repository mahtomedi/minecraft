package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

public class IOWorker implements AutoCloseable, ChunkScanAccess {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final AtomicBoolean shutdownRequested = new AtomicBoolean();
    private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;
    private final RegionFileStorage storage;
    private final Map<ChunkPos, IOWorker.PendingStore> pendingWrites = Maps.newLinkedHashMap();
    private final Long2ObjectLinkedOpenHashMap<CompletableFuture<BitSet>> regionCacheForBlender = new Long2ObjectLinkedOpenHashMap<>();
    private static final int REGION_CACHE_SIZE = 1024;

    protected IOWorker(Path param0, boolean param1, String param2) {
        this.storage = new RegionFileStorage(param0, param1);
        this.mailbox = new ProcessorMailbox<>(new StrictQueue.FixedPriorityQueue(IOWorker.Priority.values().length), Util.ioPool(), "IOWorker-" + param2);
    }

    public boolean isOldChunkAround(ChunkPos param0, int param1) {
        ChunkPos var0 = new ChunkPos(param0.x - param1, param0.z - param1);
        ChunkPos var1 = new ChunkPos(param0.x + param1, param0.z + param1);

        for(int var2 = var0.getRegionX(); var2 <= var1.getRegionX(); ++var2) {
            for(int var3 = var0.getRegionZ(); var3 <= var1.getRegionZ(); ++var3) {
                BitSet var4 = this.getOrCreateOldDataForRegion(var2, var3).join();
                if (!var4.isEmpty()) {
                    ChunkPos var5 = ChunkPos.minFromRegion(var2, var3);
                    int var6 = Math.max(var0.x - var5.x, 0);
                    int var7 = Math.max(var0.z - var5.z, 0);
                    int var8 = Math.min(var1.x - var5.x, 31);
                    int var9 = Math.min(var1.z - var5.z, 31);

                    for(int var10 = var6; var10 <= var8; ++var10) {
                        for(int var11 = var7; var11 <= var9; ++var11) {
                            int var12 = var11 * 32 + var10;
                            if (var4.get(var12)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    private CompletableFuture<BitSet> getOrCreateOldDataForRegion(int param0, int param1) {
        long var0 = ChunkPos.asLong(param0, param1);
        synchronized(this.regionCacheForBlender) {
            CompletableFuture<BitSet> var1 = this.regionCacheForBlender.getAndMoveToFirst(var0);
            if (var1 == null) {
                var1 = this.createOldDataForRegion(param0, param1);
                this.regionCacheForBlender.putAndMoveToFirst(var0, var1);
                if (this.regionCacheForBlender.size() > 1024) {
                    this.regionCacheForBlender.removeLast();
                }
            }

            return var1;
        }
    }

    private CompletableFuture<BitSet> createOldDataForRegion(int param0, int param1) {
        return CompletableFuture.supplyAsync(() -> {
            ChunkPos var0 = ChunkPos.minFromRegion(param0, param1);
            ChunkPos var1x = ChunkPos.maxFromRegion(param0, param1);
            BitSet var2x = new BitSet();
            ChunkPos.rangeClosed(var0, var1x).forEach(param1x -> {
                CollectFields var0x = new CollectFields(new FieldSelector(IntTag.TYPE, "DataVersion"), new FieldSelector(CompoundTag.TYPE, "blending_data"));

                try {
                    this.scanChunk(param1x, var0x).join();
                } catch (Exception var7) {
                    LOGGER.warn("Failed to scan chunk {}", param1x, var7);
                    return;
                }

                Tag var2xx = var0x.getResult();
                if (var2xx instanceof CompoundTag var3x && this.isOldChunk(var3x)) {
                    int var4x = param1x.getRegionLocalZ() * 32 + param1x.getRegionLocalX();
                    var2x.set(var4x);
                }

            });
            return var2x;
        }, Util.backgroundExecutor());
    }

    private boolean isOldChunk(CompoundTag param0) {
        return param0.contains("DataVersion", 99) && param0.getInt("DataVersion") >= 3088 ? param0.contains("blending_data", 10) : true;
    }

    public CompletableFuture<Void> store(ChunkPos param0, @Nullable CompoundTag param1) {
        return this.<CompletableFuture<Void>>submitTask(() -> {
            IOWorker.PendingStore var0 = this.pendingWrites.computeIfAbsent(param0, param1x -> new IOWorker.PendingStore(param1));
            var0.data = param1;
            return Either.left(var0.result);
        }).thenCompose(Function.identity());
    }

    public CompletableFuture<Optional<CompoundTag>> loadAsync(ChunkPos param0) {
        return this.submitTask(() -> {
            IOWorker.PendingStore var0 = this.pendingWrites.get(param0);
            if (var0 != null) {
                return Either.left(Optional.ofNullable(var0.data));
            } else {
                try {
                    CompoundTag var1 = this.storage.read(param0);
                    return Either.left(Optional.ofNullable(var1));
                } catch (Exception var4) {
                    LOGGER.warn("Failed to read chunk {}", param0, var4);
                    return Either.right(var4);
                }
            }
        });
    }

    public CompletableFuture<Void> synchronize(boolean param0) {
        CompletableFuture<Void> var0 = this.<CompletableFuture<Void>>submitTask(
                () -> Either.left(
                        CompletableFuture.allOf(
                            this.pendingWrites.values().stream().map(param0x -> param0x.result).toArray(param0x -> new CompletableFuture[param0x])
                        )
                    )
            )
            .thenCompose(Function.identity());
        return param0 ? var0.thenCompose(param0x -> this.submitTask(() -> {
                try {
                    this.storage.flush();
                    return Either.left(null);
                } catch (Exception var2x) {
                    LOGGER.warn("Failed to synchronize chunks", (Throwable)var2x);
                    return Either.right(var2x);
                }
            })) : var0.thenCompose(param0x -> this.submitTask(() -> Either.left(null)));
    }

    @Override
    public CompletableFuture<Void> scanChunk(ChunkPos param0, StreamTagVisitor param1) {
        return this.submitTask(() -> {
            try {
                IOWorker.PendingStore var1x = this.pendingWrites.get(param0);
                if (var1x != null) {
                    if (var1x.data != null) {
                        var1x.data.acceptAsRoot(param1);
                    }
                } else {
                    this.storage.scanChunk(param0, param1);
                }

                return Either.left(null);
            } catch (Exception var4) {
                LOGGER.warn("Failed to bulk scan chunk {}", param0, var4);
                return Either.right(var4);
            }
        });
    }

    private <T> CompletableFuture<T> submitTask(Supplier<Either<T, Exception>> param0) {
        return this.mailbox.askEither(param1 -> new StrictQueue.IntRunnable(IOWorker.Priority.FOREGROUND.ordinal(), () -> {
                if (!this.shutdownRequested.get()) {
                    param1.tell(param0.get());
                }

                this.tellStorePending();
            }));
    }

    private void storePendingChunk() {
        if (!this.pendingWrites.isEmpty()) {
            Iterator<Entry<ChunkPos, IOWorker.PendingStore>> var0 = this.pendingWrites.entrySet().iterator();
            Entry<ChunkPos, IOWorker.PendingStore> var1 = var0.next();
            var0.remove();
            this.runStore(var1.getKey(), var1.getValue());
            this.tellStorePending();
        }
    }

    private void tellStorePending() {
        this.mailbox.tell(new StrictQueue.IntRunnable(IOWorker.Priority.BACKGROUND.ordinal(), this::storePendingChunk));
    }

    private void runStore(ChunkPos param0, IOWorker.PendingStore param1) {
        try {
            this.storage.write(param0, param1.data);
            param1.result.complete(null);
        } catch (Exception var4) {
            LOGGER.error("Failed to store chunk {}", param0, var4);
            param1.result.completeExceptionally(var4);
        }

    }

    @Override
    public void close() throws IOException {
        if (this.shutdownRequested.compareAndSet(false, true)) {
            this.mailbox.ask(param0 -> new StrictQueue.IntRunnable(IOWorker.Priority.SHUTDOWN.ordinal(), () -> param0.tell(Unit.INSTANCE))).join();
            this.mailbox.close();

            try {
                this.storage.close();
            } catch (Exception var2) {
                LOGGER.error("Failed to close storage", (Throwable)var2);
            }

        }
    }

    static class PendingStore {
        @Nullable
        CompoundTag data;
        final CompletableFuture<Void> result = new CompletableFuture<>();

        public PendingStore(@Nullable CompoundTag param0) {
            this.data = param0;
        }
    }

    static enum Priority {
        FOREGROUND,
        BACKGROUND,
        SHUTDOWN;
    }
}
