package net.minecraft.world.level;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

public class ChunkTickList<T> implements TickList<T> {
    private final Set<TickNextTickData<T>> ticks;
    private final Function<T, ResourceLocation> toId;

    public ChunkTickList(Function<T, ResourceLocation> param0, List<TickNextTickData<T>> param1) {
        this(param0, Sets.newHashSet(param1));
    }

    private ChunkTickList(Function<T, ResourceLocation> param0, Set<TickNextTickData<T>> param1) {
        this.ticks = param1;
        this.toId = param0;
    }

    @Override
    public boolean hasScheduledTick(BlockPos param0, T param1) {
        return false;
    }

    @Override
    public void scheduleTick(BlockPos param0, T param1, int param2, TickPriority param3) {
        this.ticks.add(new TickNextTickData<>(param0, param1, (long)param2, param3));
    }

    @Override
    public boolean willTickThisTick(BlockPos param0, T param1) {
        return false;
    }

    @Override
    public void addAll(Stream<TickNextTickData<T>> param0) {
        param0.forEach(this.ticks::add);
    }

    public Stream<TickNextTickData<T>> ticks() {
        return this.ticks.stream();
    }

    public ListTag save(long param0) {
        return ServerTickList.saveTickList(this.toId, this.ticks, param0);
    }

    public static <T> ChunkTickList<T> create(ListTag param0, Function<T, ResourceLocation> param1, Function<ResourceLocation, T> param2) {
        Set<TickNextTickData<T>> var0 = Sets.newHashSet();

        for(int var1 = 0; var1 < param0.size(); ++var1) {
            CompoundTag var2 = param0.getCompound(var1);
            T var3 = param2.apply(new ResourceLocation(var2.getString("i")));
            if (var3 != null) {
                var0.add(
                    new TickNextTickData<>(
                        new BlockPos(var2.getInt("x"), var2.getInt("y"), var2.getInt("z")),
                        var3,
                        (long)var2.getInt("t"),
                        TickPriority.byValue(var2.getInt("p"))
                    )
                );
            }
        }

        return new ChunkTickList<>(param1, var0);
    }
}
