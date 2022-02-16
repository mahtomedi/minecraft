package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.Hash.Strategy;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;

public record SavedTick<T>(T type, BlockPos pos, int delay, TickPriority priority) {
    private static final String TAG_ID = "i";
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";
    private static final String TAG_DELAY = "t";
    private static final String TAG_PRIORITY = "p";
    public static final Strategy<SavedTick<?>> UNIQUE_TICK_HASH = new Strategy<SavedTick<?>>() {
        public int hashCode(SavedTick<?> param0) {
            return 31 * param0.pos().hashCode() + param0.type().hashCode();
        }

        public boolean equals(@Nullable SavedTick<?> param0, @Nullable SavedTick<?> param1) {
            if (param0 == param1) {
                return true;
            } else if (param0 != null && param1 != null) {
                return param0.type() == param1.type() && param0.pos().equals(param1.pos());
            } else {
                return false;
            }
        }
    };

    public static <T> void loadTickList(ListTag param0, Function<String, Optional<T>> param1, ChunkPos param2, Consumer<SavedTick<T>> param3) {
        long var0 = param2.toLong();

        for(int var1 = 0; var1 < param0.size(); ++var1) {
            CompoundTag var2 = param0.getCompound(var1);
            loadTick(var2, param1).ifPresent(param2x -> {
                if (ChunkPos.asLong(param2x.pos()) == var0) {
                    param3.accept(param2x);
                }

            });
        }

    }

    public static <T> Optional<SavedTick<T>> loadTick(CompoundTag param0, Function<String, Optional<T>> param1) {
        return param1.apply(param0.getString("i")).map(param1x -> {
            BlockPos var0x = new BlockPos(param0.getInt("x"), param0.getInt("y"), param0.getInt("z"));
            return new SavedTick<>(param1x, var0x, param0.getInt("t"), TickPriority.byValue(param0.getInt("p")));
        });
    }

    private static CompoundTag saveTick(String param0, BlockPos param1, int param2, TickPriority param3) {
        CompoundTag var0 = new CompoundTag();
        var0.putString("i", param0);
        var0.putInt("x", param1.getX());
        var0.putInt("y", param1.getY());
        var0.putInt("z", param1.getZ());
        var0.putInt("t", param2);
        var0.putInt("p", param3.getValue());
        return var0;
    }

    public static <T> CompoundTag saveTick(ScheduledTick<T> param0, Function<T, String> param1, long param2) {
        return saveTick(param1.apply(param0.type()), param0.pos(), (int)(param0.triggerTick() - param2), param0.priority());
    }

    public CompoundTag save(Function<T, String> param0) {
        return saveTick(param0.apply(this.type), this.pos, this.delay, this.priority);
    }

    public ScheduledTick<T> unpack(long param0, long param1) {
        return new ScheduledTick<>(this.type, this.pos, param0 + (long)this.delay, this.priority, param1);
    }

    public static <T> SavedTick<T> probe(T param0, BlockPos param1) {
        return new SavedTick<>(param0, param1, 0, TickPriority.NORMAL);
    }
}
