package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkCompileTask implements Comparable<ChunkCompileTask> {
    private final RenderChunk chunk;
    private final ReentrantLock lock = new ReentrantLock();
    private final List<Runnable> cancelListeners = Lists.newArrayList();
    private final ChunkCompileTask.Type type;
    private final double distAtCreation;
    @Nullable
    private RenderChunkRegion region;
    private ChunkBufferBuilderPack builders;
    private CompiledChunk compiledChunk;
    private ChunkCompileTask.Status status = ChunkCompileTask.Status.PENDING;
    private boolean isCancelled;

    public ChunkCompileTask(RenderChunk param0, ChunkCompileTask.Type param1, double param2, @Nullable RenderChunkRegion param3) {
        this.chunk = param0;
        this.type = param1;
        this.distAtCreation = param2;
        this.region = param3;
    }

    public ChunkCompileTask.Status getStatus() {
        return this.status;
    }

    public RenderChunk getChunk() {
        return this.chunk;
    }

    @Nullable
    public RenderChunkRegion takeRegion() {
        RenderChunkRegion var0 = this.region;
        this.region = null;
        return var0;
    }

    public CompiledChunk getCompiledChunk() {
        return this.compiledChunk;
    }

    public void setCompiledChunk(CompiledChunk param0) {
        this.compiledChunk = param0;
    }

    public ChunkBufferBuilderPack getBuilders() {
        return this.builders;
    }

    public void setBuilders(ChunkBufferBuilderPack param0) {
        this.builders = param0;
    }

    public void setStatus(ChunkCompileTask.Status param0) {
        this.lock.lock();

        try {
            this.status = param0;
        } finally {
            this.lock.unlock();
        }

    }

    public void cancel() {
        this.lock.lock();

        try {
            this.region = null;
            if (this.type == ChunkCompileTask.Type.REBUILD_CHUNK && this.status != ChunkCompileTask.Status.DONE) {
                this.chunk.setDirty(false);
            }

            this.isCancelled = true;
            this.status = ChunkCompileTask.Status.DONE;

            for(Runnable var0 : this.cancelListeners) {
                var0.run();
            }
        } finally {
            this.lock.unlock();
        }

    }

    public void addCancelListener(Runnable param0) {
        this.lock.lock();

        try {
            this.cancelListeners.add(param0);
            if (this.isCancelled) {
                param0.run();
            }
        } finally {
            this.lock.unlock();
        }

    }

    public ReentrantLock getStatusLock() {
        return this.lock;
    }

    public ChunkCompileTask.Type getType() {
        return this.type;
    }

    public boolean wasCancelled() {
        return this.isCancelled;
    }

    public int compareTo(ChunkCompileTask param0) {
        return Doubles.compare(this.distAtCreation, param0.distAtCreation);
    }

    public double getDistAtCreation() {
        return this.distAtCreation;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Status {
        PENDING,
        COMPILING,
        UPLOADING,
        DONE;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type {
        REBUILD_CHUNK,
        RESORT_TRANSPARENCY;
    }
}
