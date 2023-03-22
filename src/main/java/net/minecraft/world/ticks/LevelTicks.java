package net.minecraft.world.ticks;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class LevelTicks<T> implements LevelTickAccess<T> {
    private static final Comparator<LevelChunkTicks<?>> CONTAINER_DRAIN_ORDER = (param0, param1) -> ScheduledTick.INTRA_TICK_DRAIN_ORDER
            .compare(param0.peek(), param1.peek());
    private final LongPredicate tickCheck;
    private final Supplier<ProfilerFiller> profiler;
    private final Long2ObjectMap<LevelChunkTicks<T>> allContainers = new Long2ObjectOpenHashMap<>();
    private final Long2LongMap nextTickForContainer = Util.make(new Long2LongOpenHashMap(), param0x -> param0x.defaultReturnValue(Long.MAX_VALUE));
    private final Queue<LevelChunkTicks<T>> containersToTick = new PriorityQueue<>(CONTAINER_DRAIN_ORDER);
    private final Queue<ScheduledTick<T>> toRunThisTick = new ArrayDeque<>();
    private final List<ScheduledTick<T>> alreadyRunThisTick = new ArrayList<>();
    private final Set<ScheduledTick<?>> toRunThisTickSet = new ObjectOpenCustomHashSet<>(ScheduledTick.UNIQUE_TICK_HASH);
    private final BiConsumer<LevelChunkTicks<T>, ScheduledTick<T>> chunkScheduleUpdater = (param0x, param1x) -> {
        if (param1x.equals(param0x.peek())) {
            this.updateContainerScheduling(param1x);
        }

    };

    public LevelTicks(LongPredicate param0, Supplier<ProfilerFiller> param1) {
        this.tickCheck = param0;
        this.profiler = param1;
    }

    public void addContainer(ChunkPos param0, LevelChunkTicks<T> param1) {
        long var0 = param0.toLong();
        this.allContainers.put(var0, param1);
        ScheduledTick<T> var1 = param1.peek();
        if (var1 != null) {
            this.nextTickForContainer.put(var0, var1.triggerTick());
        }

        param1.setOnTickAdded(this.chunkScheduleUpdater);
    }

    public void removeContainer(ChunkPos param0) {
        long var0 = param0.toLong();
        LevelChunkTicks<T> var1 = this.allContainers.remove(var0);
        this.nextTickForContainer.remove(var0);
        if (var1 != null) {
            var1.setOnTickAdded(null);
        }

    }

    @Override
    public void schedule(ScheduledTick<T> param0) {
        long var0 = ChunkPos.asLong(param0.pos());
        LevelChunkTicks<T> var1 = this.allContainers.get(var0);
        if (var1 == null) {
            Util.pauseInIde(new IllegalStateException("Trying to schedule tick in not loaded position " + param0.pos()));
        } else {
            var1.schedule(param0);
        }
    }

    public void tick(long param0, int param1, BiConsumer<BlockPos, T> param2) {
        ProfilerFiller var0 = this.profiler.get();
        var0.push("collect");
        this.collectTicks(param0, param1, var0);
        var0.popPush("run");
        var0.incrementCounter("ticksToRun", this.toRunThisTick.size());
        this.runCollectedTicks(param2);
        var0.popPush("cleanup");
        this.cleanupAfterTick();
        var0.pop();
    }

    private void collectTicks(long param0, int param1, ProfilerFiller param2) {
        this.sortContainersToTick(param0);
        param2.incrementCounter("containersToTick", this.containersToTick.size());
        this.drainContainers(param0, param1);
        this.rescheduleLeftoverContainers();
    }

    private void sortContainersToTick(long param0) {
        ObjectIterator<Entry> var0 = Long2LongMaps.fastIterator(this.nextTickForContainer);

        while(var0.hasNext()) {
            Entry var1 = var0.next();
            long var2 = var1.getLongKey();
            long var3 = var1.getLongValue();
            if (var3 <= param0) {
                LevelChunkTicks<T> var4 = this.allContainers.get(var2);
                if (var4 == null) {
                    var0.remove();
                } else {
                    ScheduledTick<T> var5 = var4.peek();
                    if (var5 == null) {
                        var0.remove();
                    } else if (var5.triggerTick() > param0) {
                        var1.setValue(var5.triggerTick());
                    } else if (this.tickCheck.test(var2)) {
                        var0.remove();
                        this.containersToTick.add(var4);
                    }
                }
            }
        }

    }

    private void drainContainers(long param0, int param1) {
        LevelChunkTicks<T> var0;
        while(this.canScheduleMoreTicks(param1) && (var0 = this.containersToTick.poll()) != null) {
            ScheduledTick<T> var1 = var0.poll();
            this.scheduleForThisTick(var1);
            this.drainFromCurrentContainer(this.containersToTick, var0, param0, param1);
            ScheduledTick<T> var2 = var0.peek();
            if (var2 != null) {
                if (var2.triggerTick() <= param0 && this.canScheduleMoreTicks(param1)) {
                    this.containersToTick.add(var0);
                } else {
                    this.updateContainerScheduling(var2);
                }
            }
        }

    }

    private void rescheduleLeftoverContainers() {
        for(LevelChunkTicks<T> var0 : this.containersToTick) {
            this.updateContainerScheduling(var0.peek());
        }

    }

    private void updateContainerScheduling(ScheduledTick<T> param0) {
        this.nextTickForContainer.put(ChunkPos.asLong(param0.pos()), param0.triggerTick());
    }

    private void drainFromCurrentContainer(Queue<LevelChunkTicks<T>> param0, LevelChunkTicks<T> param1, long param2, int param3) {
        if (this.canScheduleMoreTicks(param3)) {
            LevelChunkTicks<T> var0 = param0.peek();
            ScheduledTick<T> var1 = var0 != null ? var0.peek() : null;

            while(this.canScheduleMoreTicks(param3)) {
                ScheduledTick<T> var2 = param1.peek();
                if (var2 == null || var2.triggerTick() > param2 || var1 != null && ScheduledTick.INTRA_TICK_DRAIN_ORDER.compare(var2, var1) > 0) {
                    break;
                }

                param1.poll();
                this.scheduleForThisTick(var2);
            }

        }
    }

    private void scheduleForThisTick(ScheduledTick<T> param0) {
        this.toRunThisTick.add(param0);
    }

    private boolean canScheduleMoreTicks(int param0) {
        return this.toRunThisTick.size() < param0;
    }

    private void runCollectedTicks(BiConsumer<BlockPos, T> param0) {
        while(!this.toRunThisTick.isEmpty()) {
            ScheduledTick<T> var0 = this.toRunThisTick.poll();
            if (!this.toRunThisTickSet.isEmpty()) {
                this.toRunThisTickSet.remove(var0);
            }

            this.alreadyRunThisTick.add(var0);
            param0.accept(var0.pos(), var0.type());
        }

    }

    private void cleanupAfterTick() {
        this.toRunThisTick.clear();
        this.containersToTick.clear();
        this.alreadyRunThisTick.clear();
        this.toRunThisTickSet.clear();
    }

    @Override
    public boolean hasScheduledTick(BlockPos param0, T param1) {
        LevelChunkTicks<T> var0 = this.allContainers.get(ChunkPos.asLong(param0));
        return var0 != null && var0.hasScheduledTick(param0, param1);
    }

    @Override
    public boolean willTickThisTick(BlockPos param0, T param1) {
        this.calculateTickSetIfNeeded();
        return this.toRunThisTickSet.contains(ScheduledTick.probe(param1, param0));
    }

    private void calculateTickSetIfNeeded() {
        if (this.toRunThisTickSet.isEmpty() && !this.toRunThisTick.isEmpty()) {
            this.toRunThisTickSet.addAll(this.toRunThisTick);
        }

    }

    private void forContainersInArea(BoundingBox param0, LevelTicks.PosAndContainerConsumer<T> param1) {
        int var0 = SectionPos.posToSectionCoord((double)param0.minX());
        int var1 = SectionPos.posToSectionCoord((double)param0.minZ());
        int var2 = SectionPos.posToSectionCoord((double)param0.maxX());
        int var3 = SectionPos.posToSectionCoord((double)param0.maxZ());

        for(int var4 = var0; var4 <= var2; ++var4) {
            for(int var5 = var1; var5 <= var3; ++var5) {
                long var6 = ChunkPos.asLong(var4, var5);
                LevelChunkTicks<T> var7 = this.allContainers.get(var6);
                if (var7 != null) {
                    param1.accept(var6, var7);
                }
            }
        }

    }

    public void clearArea(BoundingBox param0) {
        Predicate<ScheduledTick<T>> var0 = param1 -> param0.isInside(param1.pos());
        this.forContainersInArea(param0, (param1, param2) -> {
            ScheduledTick<T> var0x = param2.peek();
            param2.removeIf(var0);
            ScheduledTick<T> var1x = param2.peek();
            if (var1x != var0x) {
                if (var1x != null) {
                    this.updateContainerScheduling(var1x);
                } else {
                    this.nextTickForContainer.remove(param1);
                }
            }

        });
        this.alreadyRunThisTick.removeIf(var0);
        this.toRunThisTick.removeIf(var0);
    }

    public void copyArea(BoundingBox param0, Vec3i param1) {
        this.copyAreaFrom(this, param0, param1);
    }

    public void copyAreaFrom(LevelTicks<T> param0, BoundingBox param1, Vec3i param2) {
        List<ScheduledTick<T>> var0 = new ArrayList<>();
        Predicate<ScheduledTick<T>> var1 = param1x -> param1.isInside(param1x.pos());
        param0.alreadyRunThisTick.stream().filter(var1).forEach(var0::add);
        param0.toRunThisTick.stream().filter(var1).forEach(var0::add);
        param0.forContainersInArea(param1, (param2x, param3) -> param3.getAll().filter(var1).forEach(var0::add));
        LongSummaryStatistics var2 = var0.stream().mapToLong(ScheduledTick::subTickOrder).summaryStatistics();
        long var3 = var2.getMin();
        long var4 = var2.getMax();
        var0.forEach(
            param3 -> this.schedule(
                    new ScheduledTick<>(
                        param3.type(), param3.pos().offset(param2), param3.triggerTick(), param3.priority(), param3.subTickOrder() - var3 + var4 + 1L
                    )
                )
        );
    }

    @Override
    public int count() {
        return this.allContainers.values().stream().mapToInt(TickAccess::count).sum();
    }

    @FunctionalInterface
    interface PosAndContainerConsumer<T> {
        void accept(long var1, LevelChunkTicks<T> var3);
    }
}
