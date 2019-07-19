package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ChunkRenderWorker implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ChunkRenderDispatcher dispatcher;
    private final ChunkBufferBuilderPack fixedBuffers;
    private boolean running = true;

    public ChunkRenderWorker(ChunkRenderDispatcher param0) {
        this(param0, null);
    }

    public ChunkRenderWorker(ChunkRenderDispatcher param0, @Nullable ChunkBufferBuilderPack param1) {
        this.dispatcher = param0;
        this.fixedBuffers = param1;
    }

    @Override
    public void run() {
        while(this.running) {
            try {
                this.doTask(this.dispatcher.takeChunk());
            } catch (InterruptedException var3) {
                LOGGER.debug("Stopping chunk worker due to interrupt");
                return;
            } catch (Throwable var4) {
                CrashReport var2 = CrashReport.forThrowable(var4, "Batching chunks");
                Minecraft.getInstance().delayCrash(Minecraft.getInstance().fillReport(var2));
                return;
            }
        }

    }

    void doTask(final ChunkCompileTask param0) throws InterruptedException {
        param0.getStatusLock().lock();

        try {
            if (!checkState(param0, ChunkCompileTask.Status.PENDING)) {
                return;
            }

            if (!param0.getChunk().hasAllNeighbors()) {
                param0.cancel();
                return;
            }

            param0.setStatus(ChunkCompileTask.Status.COMPILING);
        } finally {
            param0.getStatusLock().unlock();
        }

        final ChunkBufferBuilderPack var0 = this.takeBuffers();
        param0.getStatusLock().lock();

        try {
            if (!checkState(param0, ChunkCompileTask.Status.COMPILING)) {
                this.releaseBuffers(var0);
                return;
            }
        } finally {
            param0.getStatusLock().unlock();
        }

        param0.setBuilders(var0);
        Vec3 var1 = this.dispatcher.getCameraPosition();
        float var2 = (float)var1.x;
        float var3 = (float)var1.y;
        float var4 = (float)var1.z;
        ChunkCompileTask.Type var5x = param0.getType();
        if (var5x == ChunkCompileTask.Type.REBUILD_CHUNK) {
            param0.getChunk().compile(var2, var3, var4, param0);
        } else if (var5x == ChunkCompileTask.Type.RESORT_TRANSPARENCY) {
            param0.getChunk().rebuildTransparent(var2, var3, var4, param0);
        }

        param0.getStatusLock().lock();

        try {
            if (!checkState(param0, ChunkCompileTask.Status.COMPILING)) {
                this.releaseBuffers(var0);
                return;
            }

            param0.setStatus(ChunkCompileTask.Status.UPLOADING);
        } finally {
            param0.getStatusLock().unlock();
        }

        final CompiledChunk var6 = param0.getCompiledChunk();
        ArrayList var7 = Lists.newArrayList();
        if (var5x == ChunkCompileTask.Type.REBUILD_CHUNK) {
            for(BlockLayer var8 : BlockLayer.values()) {
                if (var6.hasLayer(var8)) {
                    var7.add(this.dispatcher.uploadChunkLayer(var8, param0.getBuilders().builder(var8), param0.getChunk(), var6, param0.getDistAtCreation()));
                }
            }
        } else if (var5x == ChunkCompileTask.Type.RESORT_TRANSPARENCY) {
            var7.add(
                this.dispatcher
                    .uploadChunkLayer(
                        BlockLayer.TRANSLUCENT, param0.getBuilders().builder(BlockLayer.TRANSLUCENT), param0.getChunk(), var6, param0.getDistAtCreation()
                    )
            );
        }

        ListenableFuture<List<Void>> var9 = Futures.allAsList(var7);
        param0.addCancelListener(() -> var9.cancel(false));
        Futures.addCallback(var9, new FutureCallback<List<Void>>() {
            public void onSuccess(@Nullable List<Void> param0x) {
                ChunkRenderWorker.this.releaseBuffers(var0);
                param0.getStatusLock().lock();

                label32: {
                    try {
                        if (ChunkRenderWorker.checkState(param0, ChunkCompileTask.Status.UPLOADING)) {
                            param0.setStatus(ChunkCompileTask.Status.DONE);
                            break label32;
                        }
                    } finally {
                        param0.getStatusLock().unlock();
                    }

                    return;
                }

                param0.getChunk().setCompiledChunk(var6);
            }

            @Override
            public void onFailure(Throwable param0x) {
                ChunkRenderWorker.this.releaseBuffers(var0);
                if (!(param0 instanceof CancellationException) && !(param0 instanceof InterruptedException)) {
                    Minecraft.getInstance().delayCrash(CrashReport.forThrowable(param0, "Rendering chunk"));
                }

            }
        });
    }

    private static boolean checkState(ChunkCompileTask param0, ChunkCompileTask.Status param1) {
        if (param0.getStatus() != param1) {
            if (!param0.wasCancelled()) {
                LOGGER.warn("Chunk render task was {} when I expected it to be {}; ignoring task", param0.getStatus(), param1);
            }

            return false;
        } else {
            return true;
        }
    }

    private ChunkBufferBuilderPack takeBuffers() throws InterruptedException {
        return this.fixedBuffers != null ? this.fixedBuffers : this.dispatcher.takeChunkBufferBuilder();
    }

    private void releaseBuffers(ChunkBufferBuilderPack param0) {
        if (param0 != this.fixedBuffers) {
            this.dispatcher.releaseChunkBufferBuilder(param0);
        }

    }

    public void stop() {
        this.running = false;
    }
}
