package net.minecraft.server.level;

import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.TickPriority;

public class WorldGenTickList<T> implements TickList<T> {
    private final Function<BlockPos, TickList<T>> index;

    public WorldGenTickList(Function<BlockPos, TickList<T>> param0) {
        this.index = param0;
    }

    @Override
    public boolean hasScheduledTick(BlockPos param0, T param1) {
        return this.index.apply(param0).hasScheduledTick(param0, param1);
    }

    @Override
    public void scheduleTick(BlockPos param0, T param1, int param2, TickPriority param3) {
        this.index.apply(param0).scheduleTick(param0, param1, param2, param3);
    }

    @Override
    public boolean willTickThisTick(BlockPos param0, T param1) {
        return false;
    }

    @Override
    public void addAll(Stream<TickNextTickData<T>> param0) {
        param0.forEach(param0x -> this.index.apply(param0x.pos).addAll(Stream.of(param0x)));
    }
}
