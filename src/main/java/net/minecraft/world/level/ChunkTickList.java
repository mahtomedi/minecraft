package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

public class ChunkTickList<T> implements TickList<T> {
    private final List<ChunkTickList.ScheduledTick<T>> ticks;
    private final Function<T, ResourceLocation> toId;

    public ChunkTickList(Function<T, ResourceLocation> param0, List<TickNextTickData<T>> param1, long param2) {
        this(
            param0,
            param1.stream()
                .map(param1x -> new ChunkTickList.ScheduledTick(param1x.getType(), param1x.pos, (int)(param1x.triggerTick - param2), param1x.priority))
                .collect(Collectors.toList())
        );
    }

    private ChunkTickList(Function<T, ResourceLocation> param0, List<ChunkTickList.ScheduledTick<T>> param1) {
        this.ticks = param1;
        this.toId = param0;
    }

    @Override
    public boolean hasScheduledTick(BlockPos param0, T param1) {
        return false;
    }

    @Override
    public void scheduleTick(BlockPos param0, T param1, int param2, TickPriority param3) {
        this.ticks.add(new ChunkTickList.ScheduledTick<>(param1, param0, param2, param3));
    }

    @Override
    public boolean willTickThisTick(BlockPos param0, T param1) {
        return false;
    }

    public ListTag save() {
        ListTag var0 = new ListTag();

        for(ChunkTickList.ScheduledTick<T> var1 : this.ticks) {
            CompoundTag var2 = new CompoundTag();
            var2.putString("i", this.toId.apply(var1.type).toString());
            var2.putInt("x", var1.pos.getX());
            var2.putInt("y", var1.pos.getY());
            var2.putInt("z", var1.pos.getZ());
            var2.putInt("t", var1.delay);
            var2.putInt("p", var1.priority.getValue());
            var0.add(var2);
        }

        return var0;
    }

    public static <T> ChunkTickList<T> create(ListTag param0, Function<T, ResourceLocation> param1, Function<ResourceLocation, T> param2) {
        List<ChunkTickList.ScheduledTick<T>> var0 = Lists.newArrayList();

        for(int var1 = 0; var1 < param0.size(); ++var1) {
            CompoundTag var2 = param0.getCompound(var1);
            T var3 = param2.apply(new ResourceLocation(var2.getString("i")));
            if (var3 != null) {
                BlockPos var4 = new BlockPos(var2.getInt("x"), var2.getInt("y"), var2.getInt("z"));
                var0.add(new ChunkTickList.ScheduledTick<>(var3, var4, var2.getInt("t"), TickPriority.byValue(var2.getInt("p"))));
            }
        }

        return new ChunkTickList<>(param1, var0);
    }

    public void copyOut(TickList<T> param0) {
        this.ticks.forEach(param1 -> param0.scheduleTick(param1.pos, param1.type, param1.delay, param1.priority));
    }

    @Override
    public int size() {
        return this.ticks.size();
    }

    static class ScheduledTick<T> {
        final T type;
        public final BlockPos pos;
        public final int delay;
        public final TickPriority priority;

        ScheduledTick(T param0, BlockPos param1, int param2, TickPriority param3) {
            this.type = param0;
            this.pos = param1;
            this.delay = param2;
            this.priority = param3;
        }

        @Override
        public String toString() {
            return this.type + ": " + this.pos + ", " + this.delay + ", " + this.priority;
        }
    }
}
