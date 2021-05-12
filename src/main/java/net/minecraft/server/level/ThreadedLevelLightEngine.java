package net.minecraft.server.level;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadedLevelLightEngine extends LevelLightEngine implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ProcessorMailbox<Runnable> taskMailbox;
    private final ObjectList<Pair<ThreadedLevelLightEngine.TaskType, Runnable>> lightTasks = new ObjectArrayList<>();
    private final ChunkMap chunkMap;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> sorterMailbox;
    private volatile int taskPerBatch = 5;
    private final AtomicBoolean scheduled = new AtomicBoolean();

    public ThreadedLevelLightEngine(
        LightChunkGetter param0,
        ChunkMap param1,
        boolean param2,
        ProcessorMailbox<Runnable> param3,
        ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> param4
    ) {
        super(param0, true, param2);
        this.chunkMap = param1;
        this.sorterMailbox = param4;
        this.taskMailbox = param3;
    }

    @Override
    public void close() {
    }

    @Override
    public int runUpdates(int param0, boolean param1, boolean param2) {
        throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Ran authomatically on a different thread!"));
    }

    @Override
    public void onBlockEmissionIncrease(BlockPos param0, int param1) {
        throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Ran authomatically on a different thread!"));
    }

    @Override
    public void checkBlock(BlockPos param0) {
        BlockPos var0 = param0.immutable();
        this.addTask(
            SectionPos.blockToSectionCoord(param0.getX()),
            SectionPos.blockToSectionCoord(param0.getZ()),
            ThreadedLevelLightEngine.TaskType.POST_UPDATE,
            Util.name(() -> super.checkBlock(var0), () -> "checkBlock " + var0)
        );
    }

    protected void updateChunkStatus(ChunkPos param0) {
        this.addTask(param0.x, param0.z, () -> 0, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
            super.retainData(param0, false);
            super.enableLightSources(param0, false);

            for(int var1x = this.getMinLightSection(); var1x < this.getMaxLightSection(); ++var1x) {
                super.queueSectionData(LightLayer.BLOCK, SectionPos.of(param0, var1x), null, true);
                super.queueSectionData(LightLayer.SKY, SectionPos.of(param0, var1x), null, true);
            }

            for(int var1 = this.levelHeightAccessor.getMinSection(); var1 < this.levelHeightAccessor.getMaxSection(); ++var1) {
                super.updateSectionStatus(SectionPos.of(param0, var1), true);
            }

        }, () -> "updateChunkStatus " + param0 + " true"));
    }

    @Override
    public void updateSectionStatus(SectionPos param0, boolean param1) {
        this.addTask(
            param0.x(),
            param0.z(),
            () -> 0,
            ThreadedLevelLightEngine.TaskType.PRE_UPDATE,
            Util.name(() -> super.updateSectionStatus(param0, param1), () -> "updateSectionStatus " + param0 + " " + param1)
        );
    }

    @Override
    public void enableLightSources(ChunkPos param0, boolean param1) {
        this.addTask(
            param0.x,
            param0.z,
            ThreadedLevelLightEngine.TaskType.PRE_UPDATE,
            Util.name(() -> super.enableLightSources(param0, param1), () -> "enableLight " + param0 + " " + param1)
        );
    }

    @Override
    public void queueSectionData(LightLayer param0, SectionPos param1, @Nullable DataLayer param2, boolean param3) {
        this.addTask(
            param1.x(),
            param1.z(),
            () -> 0,
            ThreadedLevelLightEngine.TaskType.PRE_UPDATE,
            Util.name(() -> super.queueSectionData(param0, param1, param2, param3), () -> "queueData " + param1)
        );
    }

    private void addTask(int param0, int param1, ThreadedLevelLightEngine.TaskType param2, Runnable param3) {
        this.addTask(param0, param1, this.chunkMap.getChunkQueueLevel(ChunkPos.asLong(param0, param1)), param2, param3);
    }

    private void addTask(int param0, int param1, IntSupplier param2, ThreadedLevelLightEngine.TaskType param3, Runnable param4) {
        this.sorterMailbox.tell(ChunkTaskPriorityQueueSorter.message(() -> {
            this.lightTasks.add(Pair.of(param3, param4));
            if (this.lightTasks.size() >= this.taskPerBatch) {
                this.runUpdate();
            }

        }, ChunkPos.asLong(param0, param1), param2));
    }

    @Override
    public void retainData(ChunkPos param0, boolean param1) {
        this.addTask(
            param0.x,
            param0.z,
            () -> 0,
            ThreadedLevelLightEngine.TaskType.PRE_UPDATE,
            Util.name(() -> super.retainData(param0, param1), () -> "retainData " + param0)
        );
    }

    public CompletableFuture<ChunkAccess> lightChunk(ChunkAccess param0, boolean param1) {
        ChunkPos var0 = param0.getPos();
        param0.setLightCorrect(false);
        this.addTask(var0.x, var0.z, ThreadedLevelLightEngine.TaskType.PRE_UPDATE, Util.name(() -> {
            LevelChunkSection[] var0x = param0.getSections();

            for(int var1x = 0; var1x < param0.getSectionsCount(); ++var1x) {
                LevelChunkSection var2x = var0x[var1x];
                if (!LevelChunkSection.isEmpty(var2x)) {
                    int var3x = this.levelHeightAccessor.getSectionYFromSectionIndex(var1x);
                    super.updateSectionStatus(SectionPos.of(var0, var3x), false);
                }
            }

            super.enableLightSources(var0, true);
            if (!param1) {
                param0.getLights().forEach(param1x -> super.onBlockEmissionIncrease(param1x, param0.getLightEmission(param1x)));
            }

            this.chunkMap.releaseLightTicket(var0);
        }, () -> "lightChunk " + var0 + " " + param1));
        return CompletableFuture.supplyAsync(() -> {
            param0.setLightCorrect(true);
            super.retainData(var0, false);
            return param0;
        }, param1x -> this.addTask(var0.x, var0.z, ThreadedLevelLightEngine.TaskType.POST_UPDATE, param1x));
    }

    public void tryScheduleUpdate() {
        if ((!this.lightTasks.isEmpty() || super.hasLightWork()) && this.scheduled.compareAndSet(false, true)) {
            this.taskMailbox.tell(() -> {
                this.runUpdate();
                this.scheduled.set(false);
            });
        }

    }

    private void runUpdate() {
        int var0 = Math.min(this.lightTasks.size(), this.taskPerBatch);
        ObjectListIterator<Pair<ThreadedLevelLightEngine.TaskType, Runnable>> var1 = this.lightTasks.iterator();

        int var2;
        for(var2 = 0; var1.hasNext() && var2 < var0; ++var2) {
            Pair<ThreadedLevelLightEngine.TaskType, Runnable> var3 = var1.next();
            if (var3.getFirst() == ThreadedLevelLightEngine.TaskType.PRE_UPDATE) {
                var3.getSecond().run();
            }
        }

        var1.back(var2);
        super.runUpdates(Integer.MAX_VALUE, true, true);

        for(int var5 = 0; var1.hasNext() && var5 < var0; ++var5) {
            Pair<ThreadedLevelLightEngine.TaskType, Runnable> var4 = var1.next();
            if (var4.getFirst() == ThreadedLevelLightEngine.TaskType.POST_UPDATE) {
                var4.getSecond().run();
            }

            var1.remove();
        }

    }

    public void setTaskPerBatch(int param0) {
        this.taskPerBatch = param0;
    }

    static enum TaskType {
        PRE_UPDATE,
        POST_UPDATE;
    }
}
