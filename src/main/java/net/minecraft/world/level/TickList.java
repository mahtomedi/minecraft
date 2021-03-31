package net.minecraft.world.level;

import net.minecraft.core.BlockPos;

public interface TickList<T> {
    boolean hasScheduledTick(BlockPos var1, T var2);

    default void scheduleTick(BlockPos param0, T param1, int param2) {
        this.scheduleTick(param0, param1, param2, TickPriority.NORMAL);
    }

    void scheduleTick(BlockPos var1, T var2, int var3, TickPriority var4);

    boolean willTickThisTick(BlockPos var1, T var2);

    int size();
}
