package net.minecraft.world.level;

import java.util.Comparator;
import net.minecraft.core.BlockPos;

public class TickNextTickData<T> {
    private static long counter;
    private final T type;
    public final BlockPos pos;
    public final long delay;
    public final TickPriority priority;
    private final long c;

    public TickNextTickData(BlockPos param0, T param1) {
        this(param0, param1, 0L, TickPriority.NORMAL);
    }

    public TickNextTickData(BlockPos param0, T param1, long param2, TickPriority param3) {
        this.c = (long)(counter++);
        this.pos = param0.immutable();
        this.type = param1;
        this.delay = param2;
        this.priority = param3;
    }

    @Override
    public boolean equals(Object param0) {
        if (!(param0 instanceof TickNextTickData)) {
            return false;
        } else {
            TickNextTickData<?> var0 = (TickNextTickData)param0;
            return this.pos.equals(var0.pos) && this.type == var0.type;
        }
    }

    @Override
    public int hashCode() {
        return this.pos.hashCode();
    }

    public static <T> Comparator<TickNextTickData<T>> createTimeComparator() {
        return (param0, param1) -> {
            int var0 = Long.compare(param0.delay, param1.delay);
            if (var0 != 0) {
                return var0;
            } else {
                var0 = param0.priority.compareTo(param1.priority);
                return var0 != 0 ? var0 : Long.compare(param0.c, param1.c);
            }
        };
    }

    @Override
    public String toString() {
        return this.type + ": " + this.pos + ", " + this.delay + ", " + this.priority + ", " + this.c;
    }

    public T getType() {
        return this.type;
    }
}
