package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Either;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IOWorker implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final AtomicBoolean shutdownRequested = new AtomicBoolean();
    private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;
    private final RegionFileStorage storage;
    private final Map<ChunkPos, IOWorker.PendingStore> pendingWrites = Maps.newLinkedHashMap();

    protected IOWorker(File param0, boolean param1, String param2) {
        this.storage = new RegionFileStorage(param0, param1);
        this.mailbox = new ProcessorMailbox<>(new StrictQueue.FixedPriorityQueue(IOWorker.Priority.values().length), Util.ioPool(), "IOWorker-" + param2);
    }

    public CompletableFuture<Void> store(ChunkPos param0, CompoundTag param1) {
        return this.<CompletableFuture<Void>>submitTask(() -> {
            IOWorker.PendingStore var0 = this.pendingWrites.computeIfAbsent(param0, param1x -> new IOWorker.PendingStore(param1));
            var0.data = param1;
            return Either.left(var0.result);
        }).thenCompose(Function.identity());
    }

    @Nullable
    public CompoundTag load(ChunkPos param0) throws IOException {
        CompletableFuture<CompoundTag> var0 = this.submitTask(() -> {
            IOWorker.PendingStore var0x = this.pendingWrites.get(param0);
            if (var0x != null) {
                return Either.left(var0x.data);
            } else {
                try {
                    CompoundTag var2x = this.storage.read(param0);
                    return Either.left(var2x);
                } catch (Exception var4x) {
                    LOGGER.warn("Failed to read chunk {}", param0, var4x);
                    return Either.right(var4x);
                }
            }
        });

        try {
            return var0.join();
        } catch (CompletionException var4) {
            if (var4.getCause() instanceof IOException) {
                throw (IOException)var4.getCause();
            } else {
                throw var4;
            }
        }
    }

    public CompletableFuture<Void> synchronize() {
        CompletableFuture<Void> var0 = this.<CompletableFuture<Void>>submitTask(
                () -> Either.left(
                        CompletableFuture.allOf(
                            this.pendingWrites.values().stream().map(param0 -> param0.result).toArray(param0 -> new CompletableFuture[param0])
                        )
                    )
            )
            .thenCompose(Function.identity());
        return var0.thenCompose(param0 -> this.submitTask(() -> {
                try {
                    this.storage.flush();
                    return Either.left(null);
                } catch (Exception var2) {
                    LOGGER.warn("Failed to synchronized chunks", (Throwable)var2);
                    return Either.right(var2);
                }
            }));
    }

    private <T> CompletableFuture<T> submitTask(Supplier<Either<T, Exception>> param0) {
        return this.mailbox.askEither(param1 -> new StrictQueue.IntRunnable(IOWorker.Priority.HIGH.ordinal(), () -> {
                if (!this.shutdownRequested.get()) {
                    param1.tell(param0.get());
                }

                this.tellStorePending();
            }));
    }

    private void storePendingChunk() {
        Iterator<Entry<ChunkPos, IOWorker.PendingStore>> var0 = this.pendingWrites.entrySet().iterator();
        if (var0.hasNext()) {
            Entry<ChunkPos, IOWorker.PendingStore> var1 = var0.next();
            var0.remove();
            this.runStore(var1.getKey(), var1.getValue());
            this.tellStorePending();
        }
    }

    private void tellStorePending() {
        this.mailbox.tell(new StrictQueue.IntRunnable(IOWorker.Priority.LOW.ordinal(), this::storePendingChunk));
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
            CompletableFuture<Unit> var0 = this.mailbox
                .ask(param0 -> new StrictQueue.IntRunnable(IOWorker.Priority.HIGH.ordinal(), () -> param0.tell(Unit.INSTANCE)));

            try {
                var0.join();
            } catch (CompletionException var4) {
                if (var4.getCause() instanceof IOException) {
                    throw (IOException)var4.getCause();
                }

                throw var4;
            }

            this.mailbox.close();
            this.pendingWrites.forEach(this::runStore);
            this.pendingWrites.clear();

            try {
                this.storage.close();
            } catch (Exception var3) {
                LOGGER.error("Failed to close storage", (Throwable)var3);
            }

        }
    }

    static class PendingStore {
        private CompoundTag data;
        private final CompletableFuture<Void> result = new CompletableFuture<>();

        public PendingStore(CompoundTag param0) {
            this.data = param0;
        }
    }

    static enum Priority {
        HIGH,
        LOW;
    }
}
