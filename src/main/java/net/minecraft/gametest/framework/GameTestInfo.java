package net.minecraft.gametest.framework;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class GameTestInfo {
    private final TestFunction testFunction;
    @Nullable
    private BlockPos structureBlockPos;
    private final ServerLevel level;
    private final Collection<GameTestListener> listeners = Lists.newArrayList();
    private final int timeoutTicks;
    private final Collection<GameTestSequence> sequences = Lists.newCopyOnWriteArrayList();
    private final Object2LongMap<Runnable> runAtTickTimeMap = new Object2LongOpenHashMap<>();
    private long startTick;
    private long tickCount;
    private boolean started;
    private final Stopwatch timer = Stopwatch.createUnstarted();
    private boolean done;
    private final Rotation rotation;
    @Nullable
    private Throwable error;
    @Nullable
    private StructureBlockEntity structureBlockEntity;

    public GameTestInfo(TestFunction param0, Rotation param1, ServerLevel param2) {
        this.testFunction = param0;
        this.level = param2;
        this.timeoutTicks = param0.getMaxTicks();
        this.rotation = param0.getRotation().getRotated(param1);
    }

    void setStructureBlockPos(BlockPos param0) {
        this.structureBlockPos = param0;
    }

    void startExecution() {
        this.startTick = this.level.getGameTime() + 1L + this.testFunction.getSetupTicks();
        this.timer.start();
    }

    public void tick() {
        if (!this.isDone()) {
            this.tickCount = this.level.getGameTime() - this.startTick;
            if (this.tickCount >= 0L) {
                if (this.tickCount == 0L) {
                    this.startTest();
                }

                ObjectIterator<Entry<Runnable>> var0 = this.runAtTickTimeMap.object2LongEntrySet().iterator();

                while(var0.hasNext()) {
                    Entry<Runnable> var1 = var0.next();
                    if (var1.getLongValue() <= this.tickCount) {
                        try {
                            var1.getKey().run();
                        } catch (Exception var4) {
                            this.fail(var4);
                        }

                        var0.remove();
                    }
                }

                if (this.tickCount > (long)this.timeoutTicks) {
                    if (this.sequences.isEmpty()) {
                        this.fail(new GameTestTimeoutException("Didn't succeed or fail within " + this.testFunction.getMaxTicks() + " ticks"));
                    } else {
                        this.sequences.forEach(param0 -> param0.tickAndFailIfNotComplete(this.tickCount));
                        if (this.error == null) {
                            this.fail(new GameTestTimeoutException("No sequences finished"));
                        }
                    }
                } else {
                    this.sequences.forEach(param0 -> param0.tickAndContinue(this.tickCount));
                }

            }
        }
    }

    private void startTest() {
        if (this.started) {
            throw new IllegalStateException("Test already started");
        } else {
            this.started = true;

            try {
                this.testFunction.run(new GameTestHelper(this));
            } catch (Exception var2) {
                this.fail(var2);
            }

        }
    }

    public String getTestName() {
        return this.testFunction.getTestName();
    }

    public BlockPos getStructureBlockPos() {
        return this.structureBlockPos;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public boolean hasSucceeded() {
        return this.done && this.error == null;
    }

    public boolean hasFailed() {
        return this.error != null;
    }

    public boolean hasStarted() {
        return this.started;
    }

    public boolean isDone() {
        return this.done;
    }

    private void finish() {
        if (!this.done) {
            this.done = true;
            this.timer.stop();
        }

    }

    public void fail(Throwable param0) {
        this.finish();
        this.error = param0;
        this.listeners.forEach(param0x -> param0x.testFailed(this));
    }

    @Nullable
    public Throwable getError() {
        return this.error;
    }

    @Override
    public String toString() {
        return this.getTestName();
    }

    public void addListener(GameTestListener param0) {
        this.listeners.add(param0);
    }

    public void spawnStructure(BlockPos param0, int param1) {
        this.structureBlockEntity = StructureUtils.spawnStructure(this.getStructureName(), param0, this.getRotation(), param1, this.level, false);
        this.structureBlockPos = this.structureBlockEntity.getBlockPos();
        this.structureBlockEntity.setStructureName(this.getTestName());
        StructureUtils.addCommandBlockAndButtonToStartTest(this.structureBlockPos, new BlockPos(1, 0, -1), this.getRotation(), this.level);
        this.listeners.forEach(param0x -> param0x.testStructureLoaded(this));
    }

    public void clearStructure() {
        if (this.structureBlockEntity == null) {
            throw new IllegalStateException("Expected structure to be initialized, but it was null");
        } else {
            BoundingBox var0 = StructureUtils.getStructureBoundingBox(this.structureBlockEntity);
            StructureUtils.clearSpaceForStructure(var0, this.structureBlockPos.getY(), this.level);
        }
    }

    public boolean isRequired() {
        return this.testFunction.isRequired();
    }

    public boolean isOptional() {
        return !this.testFunction.isRequired();
    }

    public String getStructureName() {
        return this.testFunction.getStructureName();
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public TestFunction getTestFunction() {
        return this.testFunction;
    }

    public boolean isFlaky() {
        return this.testFunction.isFlaky();
    }

    public int maxAttempts() {
        return this.testFunction.getMaxAttempts();
    }

    public int requiredSuccesses() {
        return this.testFunction.getRequiredSuccesses();
    }
}
