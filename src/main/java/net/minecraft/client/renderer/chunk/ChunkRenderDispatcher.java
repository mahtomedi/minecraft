package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexBufferUploader;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ChunkRenderDispatcher {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
        .setNameFormat("Chunk Batcher %d")
        .setDaemon(true)
        .setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER))
        .build();
    private final int bufferCount;
    private final List<Thread> threads = Lists.newArrayList();
    private final List<ChunkRenderWorker> workers = Lists.newArrayList();
    private final PriorityBlockingQueue<ChunkCompileTask> chunksToBatch = Queues.newPriorityBlockingQueue();
    private final BlockingQueue<ChunkBufferBuilderPack> availableChunkBuffers;
    private final BufferUploader uploader = new BufferUploader();
    private final VertexBufferUploader vboUploader = new VertexBufferUploader();
    private final Queue<ChunkRenderDispatcher.PendingUpload> pendingUploads = Queues.newPriorityQueue();
    private final ChunkRenderWorker localWorker;
    private Vec3 camera = Vec3.ZERO;

    public ChunkRenderDispatcher(boolean param0) {
        int var0 = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / 10485760 - 1);
        int var1 = Runtime.getRuntime().availableProcessors();
        int var2 = param0 ? var1 : Math.min(var1, 4);
        int var3 = Math.max(1, Math.min(var2 * 2, var0));
        this.localWorker = new ChunkRenderWorker(this, new ChunkBufferBuilderPack());
        List<ChunkBufferBuilderPack> var4 = Lists.newArrayListWithExpectedSize(var3);

        try {
            for(int var5 = 0; var5 < var3; ++var5) {
                var4.add(new ChunkBufferBuilderPack());
            }
        } catch (OutOfMemoryError var111) {
            LOGGER.warn("Allocated only {}/{} buffers", var4.size(), var3);
            int var7 = var4.size() * 2 / 3;

            for(int var8 = 0; var8 < var7; ++var8) {
                var4.remove(var4.size() - 1);
            }

            System.gc();
        }

        this.bufferCount = var4.size();
        this.availableChunkBuffers = Queues.newArrayBlockingQueue(this.bufferCount);
        this.availableChunkBuffers.addAll(var4);
        int var9 = Math.min(var2, this.bufferCount);
        if (var9 > 1) {
            for(int var10 = 0; var10 < var9; ++var10) {
                ChunkRenderWorker var11 = new ChunkRenderWorker(this);
                Thread var12 = THREAD_FACTORY.newThread(var11);
                var12.start();
                this.workers.add(var11);
                this.threads.add(var12);
            }
        }

    }

    public String getStats() {
        return this.threads.isEmpty()
            ? String.format("pC: %03d, single-threaded", this.chunksToBatch.size())
            : String.format("pC: %03d, pU: %02d, aB: %02d", this.chunksToBatch.size(), this.pendingUploads.size(), this.availableChunkBuffers.size());
    }

    public void setCamera(Vec3 param0) {
        this.camera = param0;
    }

    public Vec3 getCameraPosition() {
        return this.camera;
    }

    public boolean uploadAllPendingUploadsUntil(long param0) {
        boolean var0 = false;

        boolean var1;
        do {
            var1 = false;
            if (this.threads.isEmpty()) {
                ChunkCompileTask var2 = this.chunksToBatch.poll();
                if (var2 != null) {
                    try {
                        this.localWorker.doTask(var2);
                        var1 = true;
                    } catch (InterruptedException var9) {
                        LOGGER.warn("Skipped task due to interrupt");
                    }
                }
            }

            int var4 = 0;
            synchronized(this.pendingUploads) {
                while(var4 < 10) {
                    ChunkRenderDispatcher.PendingUpload var5 = this.pendingUploads.poll();
                    if (var5 == null) {
                        break;
                    }

                    if (!var5.future.isDone()) {
                        var5.future.run();
                        var1 = true;
                        var0 = true;
                        ++var4;
                    }
                }
            }
        } while(param0 != 0L && var1 && param0 >= Util.getNanos());

        return var0;
    }

    public boolean rebuildChunkAsync(RenderChunk param0) {
        param0.getTaskLock().lock();

        boolean var4;
        try {
            ChunkCompileTask var0 = param0.createCompileTask();
            var0.addCancelListener(() -> this.chunksToBatch.remove(var0));
            boolean var1 = this.chunksToBatch.offer(var0);
            if (!var1) {
                var0.cancel();
            }

            var4 = var1;
        } finally {
            param0.getTaskLock().unlock();
        }

        return var4;
    }

    public boolean rebuildChunkSync(RenderChunk param0) {
        param0.getTaskLock().lock();

        boolean var3;
        try {
            ChunkCompileTask var0 = param0.createCompileTask();

            try {
                this.localWorker.doTask(var0);
            } catch (InterruptedException var7) {
            }

            var3 = true;
        } finally {
            param0.getTaskLock().unlock();
        }

        return var3;
    }

    public void blockUntilClear() {
        this.clearBatchQueue();
        List<ChunkBufferBuilderPack> var0 = Lists.newArrayList();

        while(var0.size() != this.bufferCount) {
            this.uploadAllPendingUploadsUntil(Long.MAX_VALUE);

            try {
                var0.add(this.takeChunkBufferBuilder());
            } catch (InterruptedException var3) {
            }
        }

        this.availableChunkBuffers.addAll(var0);
    }

    public void releaseChunkBufferBuilder(ChunkBufferBuilderPack param0) {
        this.availableChunkBuffers.add(param0);
    }

    public ChunkBufferBuilderPack takeChunkBufferBuilder() throws InterruptedException {
        return this.availableChunkBuffers.take();
    }

    public ChunkCompileTask takeChunk() throws InterruptedException {
        return this.chunksToBatch.take();
    }

    public boolean resortChunkTransparencyAsync(RenderChunk param0) {
        param0.getTaskLock().lock();

        boolean var3;
        try {
            ChunkCompileTask var0 = param0.createTransparencySortTask();
            if (var0 == null) {
                return true;
            }

            var0.addCancelListener(() -> this.chunksToBatch.remove(var0));
            var3 = this.chunksToBatch.offer(var0);
        } finally {
            param0.getTaskLock().unlock();
        }

        return var3;
    }

    public ListenableFuture<Void> uploadChunkLayer(BlockLayer param0, BufferBuilder param1, RenderChunk param2, CompiledChunk param3, double param4) {
        if (Minecraft.getInstance().isSameThread()) {
            this.uploadChunkLayer(param1, param2.getBuffer(param0.ordinal()));
            param1.offset(0.0, 0.0, 0.0);
            return Futures.immediateFuture(null);
        } else {
            ListenableFutureTask<Void> var0 = ListenableFutureTask.create(() -> this.uploadChunkLayer(param0, param1, param2, param3, param4), null);
            synchronized(this.pendingUploads) {
                this.pendingUploads.add(new ChunkRenderDispatcher.PendingUpload(var0, param4));
                return var0;
            }
        }
    }

    private void uploadChunkLayer(BufferBuilder param0, VertexBuffer param1) {
        this.vboUploader.setBuffer(param1);
        this.vboUploader.end(param0);
    }

    public void clearBatchQueue() {
        while(!this.chunksToBatch.isEmpty()) {
            ChunkCompileTask var0 = this.chunksToBatch.poll();
            if (var0 != null) {
                var0.cancel();
            }
        }

    }

    public boolean isQueueEmpty() {
        return this.chunksToBatch.isEmpty() && this.pendingUploads.isEmpty();
    }

    public void dispose() {
        this.clearBatchQueue();

        for(ChunkRenderWorker var0 : this.workers) {
            var0.stop();
        }

        for(Thread var1 : this.threads) {
            try {
                var1.interrupt();
                var1.join();
            } catch (InterruptedException var4) {
                LOGGER.warn("Interrupted whilst waiting for worker to die", (Throwable)var4);
            }
        }

        this.availableChunkBuffers.clear();
    }

    @OnlyIn(Dist.CLIENT)
    class PendingUpload implements Comparable<ChunkRenderDispatcher.PendingUpload> {
        private final ListenableFutureTask<Void> future;
        private final double dist;

        public PendingUpload(ListenableFutureTask<Void> param0, double param1) {
            this.future = param0;
            this.dist = param1;
        }

        public int compareTo(ChunkRenderDispatcher.PendingUpload param0) {
            return Doubles.compare(this.dist, param0.dist);
        }
    }
}
