package net.minecraft.world.ticks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;

public class LevelChunkTicks<T> implements SerializableTickContainer<T>, TickContainerAccess<T> {
    private final Queue<ScheduledTick<T>> tickQueue = new PriorityQueue<>(ScheduledTick.DRAIN_ORDER);
    @Nullable
    private List<SavedTick<T>> pendingTicks;
    private final Set<ScheduledTick<?>> ticksPerPosition = new ObjectOpenCustomHashSet<>(ScheduledTick.UNIQUE_TICK_HASH);
    @Nullable
    private BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> onTickAdded;

    public LevelChunkTicks() {
    }

    public LevelChunkTicks(List<SavedTick<T>> param0) {
        this.pendingTicks = param0;

        for(SavedTick<T> var0 : param0) {
            this.ticksPerPosition.add(ScheduledTick.probe(var0.type(), var0.pos()));
        }

    }

    public void setOnTickAdded(@Nullable BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> param0) {
        this.onTickAdded = param0;
    }

    @Nullable
    public ScheduledTick<T> peek() {
        return this.tickQueue.peek();
    }

    @Nullable
    public ScheduledTick<T> poll() {
        ScheduledTick<T> var0 = this.tickQueue.poll();
        if (var0 != null) {
            this.ticksPerPosition.remove(var0);
        }

        return var0;
    }

    @Override
    public void schedule(ScheduledTick<T> param0) {
        if (this.ticksPerPosition.add(param0)) {
            this.scheduleUnchecked(param0);
        }

    }

    private void scheduleUnchecked(ScheduledTick<T> param0) {
        this.tickQueue.add(param0);
        if (this.onTickAdded != null) {
            this.onTickAdded.accept(this, param0);
        }

    }

    @Override
    public boolean hasScheduledTick(BlockPos param0, T param1) {
        return this.ticksPerPosition.contains(ScheduledTick.probe(param1, param0));
    }

    public void removeIf(Predicate<ScheduledTick<T>> param0) {
        Iterator<ScheduledTick<T>> var0 = this.tickQueue.iterator();

        while(var0.hasNext()) {
            ScheduledTick<T> var1 = var0.next();
            if (param0.test(var1)) {
                var0.remove();
                this.ticksPerPosition.remove(var1);
            }
        }

    }

    public Stream<ScheduledTick<T>> getAll() {
        return this.tickQueue.stream();
    }

    @Override
    public int count() {
        return this.tickQueue.size() + (this.pendingTicks != null ? this.pendingTicks.size() : 0);
    }

    public ListTag save(long param0, Function<T, String> param1) {
        ListTag var0 = new ListTag();
        if (this.pendingTicks != null) {
            for(SavedTick<T> var1 : this.pendingTicks) {
                var0.add(var1.save(param1));
            }
        }

        for(ScheduledTick<T> var2 : this.tickQueue) {
            var0.add(SavedTick.saveTick(var2, param1, param0));
        }

        return var0;
    }

    public void unpack(long param0) {
        if (this.pendingTicks != null) {
            int var0 = -this.pendingTicks.size();

            for(SavedTick<T> var1 : this.pendingTicks) {
                this.scheduleUnchecked(var1.unpack(param0, (long)(var0++)));
            }
        }

        this.pendingTicks = null;
    }

    public static <T> LevelChunkTicks<T> load(ListTag param0, Function<String, Optional<T>> param1, ChunkPos param2) {
        Builder<SavedTick<T>> var0 = ImmutableList.builder();
        SavedTick.loadTickList(param0, param1, param2, var0::add);
        return new LevelChunkTicks<>(var0.build());
    }
}
