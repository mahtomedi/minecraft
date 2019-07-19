package net.minecraft.world.level;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class EmptyTickList<T> implements TickList<T> {
    private static final EmptyTickList<Object> INSTANCE = new EmptyTickList<>();

    public static <T> EmptyTickList<T> empty() {
        return INSTANCE;
    }

    @Override
    public boolean hasScheduledTick(BlockPos param0, T param1) {
        return false;
    }

    @Override
    public void scheduleTick(BlockPos param0, T param1, int param2) {
    }

    @Override
    public void scheduleTick(BlockPos param0, T param1, int param2, TickPriority param3) {
    }

    @Override
    public boolean willTickThisTick(BlockPos param0, T param1) {
        return false;
    }

    @Override
    public void addAll(Stream<TickNextTickData<T>> param0) {
    }
}
