package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IOWorker implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Thread thread;
    private final AtomicBoolean shutdownRequested = new AtomicBoolean();
    private final Queue<Runnable> inbox = Queues.newConcurrentLinkedQueue();
    private final RegionFileStorage storage;
    private final Map<ChunkPos, IOWorker.PendingStore> pendingWrites = Maps.newLinkedHashMap();
    private boolean running = true;
    private CompletableFuture<Void> shutdownListener = new CompletableFuture<>();

    IOWorker(RegionFileStorage param0, String param1) {
        this.storage = param0;
        this.thread = new Thread(this::loop);
        this.thread.setName(param1 + " IO worker");
        this.thread.start();
    }

    public CompletableFuture<Void> store(ChunkPos param0, CompoundTag param1) {
        return this.submitTask(param2 -> () -> {
                IOWorker.PendingStore var0 = this.pendingWrites.computeIfAbsent(param0, param0x -> new IOWorker.PendingStore());
                var0.data = param1;
                var0.result.whenComplete((param1x, param2x) -> {
                    if (param2x != null) {
                        param2.completeExceptionally(param2x);
                    } else {
                        param2.complete(null);
                    }

                });
            });
    }

    @Nullable
    public CompoundTag load(ChunkPos param0) throws IOException {
        CompletableFuture<CompoundTag> var0 = this.submitTask(param1 -> () -> {
                IOWorker.PendingStore var0x = this.pendingWrites.get(param0);
                if (var0x != null) {
                    param1.complete(var0x.data);
                } else {
                    try {
                        CompoundTag var2x = this.storage.read(param0);
                        param1.complete(var2x);
                    } catch (Exception var5) {
                        LOGGER.warn("Failed to read chunk {}", param0, var5);
                        param1.completeExceptionally(var5);
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

    private CompletableFuture<Void> shutdown() {
        return this.submitTask(param0 -> () -> {
                this.running = false;
                this.shutdownListener = param0;
            });
    }

    public CompletableFuture<Void> synchronize() {
        return this.submitTask(
            param0 -> () -> {
                    CompletableFuture<?> var0 = CompletableFuture.allOf(
                        this.pendingWrites.values().stream().map(param0x -> param0x.result).toArray(param0x -> new CompletableFuture[param0x])
                    );
                    var0.whenComplete((param1, param2) -> {
                        try {
                            this.storage.flush();
                            param0.complete(null);
                        } catch (Exception var5) {
                            LOGGER.warn("Failed to synchronized chunks", (Throwable)var5);
                            param0.completeExceptionally(var5);
                        }
    
                    });
                }
        );
    }

    private <T> CompletableFuture<T> submitTask(Function<CompletableFuture<T>, Runnable> param0) {
        CompletableFuture<T> var0 = new CompletableFuture<>();
        this.inbox.add(param0.apply(var0));
        LockSupport.unpark(this.thread);
        return var0;
    }

    private void waitForQueueNonEmpty() {
        LockSupport.park("waiting for tasks");
    }

    private void loop() {
        try {
            while(this.running) {
                boolean var0 = this.processInbox();
                boolean var1 = this.storePendingChunk();
                if (!var0 && !var1) {
                    this.waitForQueueNonEmpty();
                }
            }

            this.processInbox();
            this.storeRemainingPendingChunks();
        } finally {
            this.closeStorage();
        }

    }

    private boolean storePendingChunk() {
        Iterator<Entry<ChunkPos, IOWorker.PendingStore>> var0 = this.pendingWrites.entrySet().iterator();
        if (!var0.hasNext()) {
            return false;
        } else {
            Entry<ChunkPos, IOWorker.PendingStore> var1 = var0.next();
            var0.remove();
            this.runStore(var1.getKey(), var1.getValue());
            return true;
        }
    }

    private void storeRemainingPendingChunks() {
        this.pendingWrites.forEach(this::runStore);
        this.pendingWrites.clear();
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

    private void closeStorage() {
        try {
            this.storage.close();
            this.shutdownListener.complete(null);
        } catch (Exception var2) {
            LOGGER.error("Failed to close storage", (Throwable)var2);
            this.shutdownListener.completeExceptionally(var2);
        }

    }

    private boolean processInbox() {
        boolean var0 = false;

        Runnable var1;
        while((var1 = this.inbox.poll()) != null) {
            var0 = true;
            var1.run();
        }

        return var0;
    }

    @Override
    public void close() throws IOException {
        if (this.shutdownRequested.compareAndSet(false, true)) {
            try {
                this.shutdown().join();
            } catch (CompletionException var2) {
                if (var2.getCause() instanceof IOException) {
                    throw (IOException)var2.getCause();
                } else {
                    throw var2;
                }
            }
        }
    }

    static class PendingStore {
        private CompoundTag data;
        private final CompletableFuture<Void> result = new CompletableFuture<>();

        private PendingStore() {
        }
    }
}
