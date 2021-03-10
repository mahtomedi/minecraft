package net.minecraft.server.level.progress;

import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ProcessorChunkProgressListener implements ChunkProgressListener {
    private final ChunkProgressListener delegate;
    private final ProcessorMailbox<Runnable> mailbox;
    private volatile boolean isRunning;

    private ProcessorChunkProgressListener(ChunkProgressListener param0, Executor param1) {
        this.delegate = param0;
        this.mailbox = ProcessorMailbox.create(param1, "progressListener");
    }

    public static ProcessorChunkProgressListener createStarted(ChunkProgressListener param0, Executor param1) {
        ProcessorChunkProgressListener var0 = new ProcessorChunkProgressListener(param0, param1);
        var0.start();
        return var0;
    }

    @Override
    public void updateSpawnPos(ChunkPos param0) {
        if (this.isRunning) {
            this.mailbox.tell(() -> this.delegate.updateSpawnPos(param0));
        }
    }

    @Override
    public void onStatusChange(ChunkPos param0, @Nullable ChunkStatus param1) {
        if (this.isRunning) {
            this.mailbox.tell(() -> this.delegate.onStatusChange(param0, param1));
        }
    }

    @Override
    public void start() {
        if (!this.isRunning) {
            this.isRunning = true;
            this.mailbox.tell(this.delegate::start);
        }
    }

    @Override
    public void stop() {
        if (this.isRunning) {
            this.isRunning = false;
            this.mailbox.tell(this.delegate::stop);
        }
    }
}
