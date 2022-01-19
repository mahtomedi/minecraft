package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.level.ChunkPos;

public class ChunkTaskPriorityQueue<T> {
    public static final int PRIORITY_LEVEL_COUNT = ChunkMap.MAX_CHUNK_DISTANCE + 2;
    private final List<Long2ObjectLinkedOpenHashMap<List<Optional<T>>>> taskQueue = IntStream.range(0, PRIORITY_LEVEL_COUNT)
        .mapToObj(param0x -> new Long2ObjectLinkedOpenHashMap())
        .collect(Collectors.toList());
    private volatile int firstQueue = PRIORITY_LEVEL_COUNT;
    private final String name;
    private final LongSet acquired = new LongOpenHashSet();
    private final int maxTasks;

    public ChunkTaskPriorityQueue(String param0, int param1) {
        this.name = param0;
        this.maxTasks = param1;
    }

    protected void resortChunkTasks(int param0, ChunkPos param1, int param2) {
        if (param0 < PRIORITY_LEVEL_COUNT) {
            Long2ObjectLinkedOpenHashMap<List<Optional<T>>> var0 = this.taskQueue.get(param0);
            List<Optional<T>> var1 = var0.remove(param1.toLong());
            if (param0 == this.firstQueue) {
                while(this.hasWork() && this.taskQueue.get(this.firstQueue).isEmpty()) {
                    ++this.firstQueue;
                }
            }

            if (var1 != null && !var1.isEmpty()) {
                this.taskQueue.get(param2).computeIfAbsent(param1.toLong(), param0x -> Lists.newArrayList()).addAll(var1);
                this.firstQueue = Math.min(this.firstQueue, param2);
            }

        }
    }

    protected void submit(Optional<T> param0, long param1, int param2) {
        this.taskQueue.get(param2).computeIfAbsent(param1, param0x -> Lists.newArrayList()).add(param0);
        this.firstQueue = Math.min(this.firstQueue, param2);
    }

    protected void release(long param0, boolean param1) {
        for(Long2ObjectLinkedOpenHashMap<List<Optional<T>>> var0 : this.taskQueue) {
            List<Optional<T>> var1 = var0.get(param0);
            if (var1 != null) {
                if (param1) {
                    var1.clear();
                } else {
                    var1.removeIf(param0x -> !param0x.isPresent());
                }

                if (var1.isEmpty()) {
                    var0.remove(param0);
                }
            }
        }

        while(this.hasWork() && this.taskQueue.get(this.firstQueue).isEmpty()) {
            ++this.firstQueue;
        }

        this.acquired.remove(param0);
    }

    private Runnable acquire(long param0) {
        return () -> this.acquired.add(param0);
    }

    @Nullable
    public Stream<Either<T, Runnable>> pop() {
        if (this.acquired.size() >= this.maxTasks) {
            return null;
        } else if (!this.hasWork()) {
            return null;
        } else {
            int var0 = this.firstQueue;
            Long2ObjectLinkedOpenHashMap<List<Optional<T>>> var1 = this.taskQueue.get(var0);
            long var2 = var1.firstLongKey();
            List<Optional<T>> var3 = var1.removeFirst();

            while(this.hasWork() && this.taskQueue.get(this.firstQueue).isEmpty()) {
                ++this.firstQueue;
            }

            return var3.stream().map(param1 -> param1.map(Either::left).orElseGet(() -> Either.right(this.acquire(var2))));
        }
    }

    public boolean hasWork() {
        return this.firstQueue < PRIORITY_LEVEL_COUNT;
    }

    @Override
    public String toString() {
        return this.name + " " + this.firstQueue + "...";
    }

    @VisibleForTesting
    LongSet getAcquired() {
        return new LongOpenHashSet(this.acquired);
    }
}
