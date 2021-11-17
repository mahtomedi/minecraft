package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.Hash.Strategy;
import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public record ScheduledTick<T>(T type, BlockPos pos, long triggerTick, TickPriority priority, long subTickOrder) {
    public static final Comparator<ScheduledTick<?>> DRAIN_ORDER = (param0, param1) -> {
        int var0 = Long.compare(param0.triggerTick, param1.triggerTick);
        if (var0 != 0) {
            return var0;
        } else {
            var0 = param0.priority.compareTo(param1.priority);
            return var0 != 0 ? var0 : Long.compare(param0.subTickOrder, param1.subTickOrder);
        }
    };
    public static final Comparator<ScheduledTick<?>> INTRA_TICK_DRAIN_ORDER = (param0, param1) -> {
        int var0 = param0.priority.compareTo(param1.priority);
        return var0 != 0 ? var0 : Long.compare(param0.subTickOrder, param1.subTickOrder);
    };
    public static final Strategy<ScheduledTick<?>> UNIQUE_TICK_HASH = new Strategy<ScheduledTick<?>>() {
        public int hashCode(ScheduledTick<?> param0) {
            return 31 * param0.pos().hashCode() + param0.type().hashCode();
        }

        public boolean equals(@Nullable ScheduledTick<?> param0, @Nullable ScheduledTick<?> param1) {
            if (param0 == param1) {
                return true;
            } else if (param0 != null && param1 != null) {
                return param0.type() == param1.type() && param0.pos().equals(param1.pos());
            } else {
                return false;
            }
        }
    };

    public ScheduledTick(T param0, BlockPos param1, long param2, long param3) {
        this(param0, param1, param2, TickPriority.NORMAL, param3);
    }

    public ScheduledTick(T param0, BlockPos param1, long param2, TickPriority param3, long param4) {
        param1 = param1.immutable();
        this.type = param0;
        this.pos = param1;
        this.triggerTick = param2;
        this.priority = param3;
        this.subTickOrder = param4;
    }

    public static <T> ScheduledTick<T> probe(T param0, BlockPos param1) {
        return new ScheduledTick<>(param0, param1, 0L, TickPriority.NORMAL, 0L);
    }
}
