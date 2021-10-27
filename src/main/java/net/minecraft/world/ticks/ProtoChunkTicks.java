package net.minecraft.world.ticks;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

public class ProtoChunkTicks<T> implements SerializableTickContainer<T>, TickContainerAccess<T> {
    private final List<SavedTick<T>> ticks = Lists.newArrayList();
    private final Set<SavedTick<?>> ticksPerPosition = new ObjectOpenCustomHashSet<>(SavedTick.UNIQUE_TICK_HASH);

    @Override
    public void schedule(ScheduledTick<T> param0) {
        SavedTick<T> var0 = new SavedTick<>(param0.type(), param0.pos(), 0, param0.priority());
        this.schedule(var0);
    }

    private void schedule(SavedTick<T> param0) {
        if (this.ticksPerPosition.add(param0)) {
            this.ticks.add(param0);
        }

    }

    @Override
    public boolean hasScheduledTick(BlockPos param0, T param1) {
        return this.ticksPerPosition.contains(SavedTick.probe(param1, param0));
    }

    @Override
    public int count() {
        return this.ticks.size();
    }

    @Override
    public Tag save(long param0, Function<T, String> param1) {
        ListTag var0 = new ListTag();

        for(SavedTick<T> var1 : this.ticks) {
            var0.add(var1.save(param1));
        }

        return var0;
    }

    public List<SavedTick<T>> scheduledTicks() {
        return List.copyOf(this.ticks);
    }

    public static <T> ProtoChunkTicks<T> load(ListTag param0, Function<String, Optional<T>> param1, ChunkPos param2) {
        ProtoChunkTicks<T> var0 = new ProtoChunkTicks<>();
        SavedTick.loadTickList(param0, param1, param2, var0::schedule);
        return var0;
    }
}
