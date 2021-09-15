package net.minecraft.world.level;

import java.util.Comparator;
import net.minecraft.core.BlockPos;

public class TickNextTickData<T> {
    private static long counter;
    private final T type;
    public final BlockPos pos;
    public final long triggerTick;
    public final TickPriority priority;
    private final long c;

    public TickNextTickData(BlockPos param0, T param1) {
        this(param0, param1, 0L, TickPriority.NORMAL);
    }

    public TickNextTickData(BlockPos param0, T param1, long param2, TickPriority param3) {
        this.c = (long)(counter++);
        this.pos = param0.immutable();
        this.type = param1;
        this.triggerTick = param2;
        this.priority = param3;
    }

    @Override
    public boolean equals(Object param0) {
        if (param0 instanceof TickNextTickData var0 && this.pos.equals(var0.pos) && this.type == var0.type) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.pos.hashCode();
    }

    public static <T> Comparator<TickNextTickData<T>> createTimeComparator() {
        return Comparator.<TickNextTickData<T>>comparingLong(param0 -> param0.triggerTick)
            .thenComparing(param0 -> param0.priority)
            .thenComparingLong(param0 -> param0.c);
    }

    @Override
    public String toString() {
        return this.type + ": " + this.pos + ", " + this.triggerTick + ", " + this.priority + ", " + this.c;
    }

    public T getType() {
        return this.type;
    }
}
