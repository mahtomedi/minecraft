package net.minecraft.world.ticks;

import java.util.function.Function;
import net.minecraft.core.BlockPos;

public class WorldGenTickAccess<T> implements LevelTickAccess<T> {
    private final Function<BlockPos, TickContainerAccess<T>> containerGetter;

    public WorldGenTickAccess(Function<BlockPos, TickContainerAccess<T>> param0) {
        this.containerGetter = param0;
    }

    @Override
    public boolean hasScheduledTick(BlockPos param0, T param1) {
        return this.containerGetter.apply(param0).hasScheduledTick(param0, param1);
    }

    @Override
    public void schedule(ScheduledTick<T> param0) {
        this.containerGetter.apply(param0.pos()).schedule(param0);
    }

    @Override
    public boolean willTickThisTick(BlockPos param0, T param1) {
        return false;
    }

    @Override
    public int count() {
        return 0;
    }
}
