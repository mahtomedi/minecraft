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

    public ProcessorChunkProgressListener(ChunkProgressListener param0, Executor param1) {
        this.delegate = param0;
        this.mailbox = ProcessorMailbox.create(param1, "progressListener");
    }

    @Override
    public void updateSpawnPos(ChunkPos param0) {
        this.mailbox.tell(() -> this.delegate.updateSpawnPos(param0));
    }

    @Override
    public void onStatusChange(ChunkPos param0, @Nullable ChunkStatus param1) {
        this.mailbox.tell(() -> this.delegate.onStatusChange(param0, param1));
    }

    @Override
    public void stop() {
        this.mailbox.tell(this.delegate::stop);
    }
}
