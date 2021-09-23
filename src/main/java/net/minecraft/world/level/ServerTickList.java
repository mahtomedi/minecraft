package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class ServerTickList<T> implements TickList<T> {
    public static final int MAX_TICK_BLOCKS_PER_TICK = 65536;
    protected final Predicate<T> ignore;
    private final Function<T, ResourceLocation> toId;
    private final Set<TickNextTickData<T>> tickNextTickSet = Sets.newHashSet();
    private final Set<TickNextTickData<T>> tickNextTickList = Sets.newTreeSet(TickNextTickData.createTimeComparator());
    private final ServerLevel level;
    private final Queue<TickNextTickData<T>> currentlyTicking = Queues.newArrayDeque();
    private final List<TickNextTickData<T>> alreadyTicked = Lists.newArrayList();
    private final Consumer<TickNextTickData<T>> ticker;

    public ServerTickList(ServerLevel param0, Predicate<T> param1, Function<T, ResourceLocation> param2, Consumer<TickNextTickData<T>> param3) {
        this.ignore = param1;
        this.toId = param2;
        this.level = param0;
        this.ticker = param3;
    }

    public void tick() {
        int var0 = this.tickNextTickList.size();
        if (var0 != this.tickNextTickSet.size()) {
            throw new IllegalStateException("TickNextTick list out of synch");
        } else {
            if (var0 > 65536) {
                var0 = 65536;
            }

            Iterator<TickNextTickData<T>> var1 = this.tickNextTickList.iterator();
            this.level.getProfiler().push("cleaning");

            while(var0 > 0 && var1.hasNext()) {
                TickNextTickData<T> var2 = var1.next();
                if (var2.triggerTick > this.level.getGameTime()) {
                    break;
                }

                if (this.level.isPositionTickingWithEntitiesLoaded(var2.pos)) {
                    var1.remove();
                    this.tickNextTickSet.remove(var2);
                    this.currentlyTicking.add(var2);
                    --var0;
                }
            }

            this.level.getProfiler().popPush("ticking");

            TickNextTickData<T> var3;
            while((var3 = this.currentlyTicking.poll()) != null) {
                this.alreadyTicked.add(var3);

                try {
                    this.ticker.accept(var3);
                } catch (Throwable var7) {
                    CrashReport var5 = CrashReport.forThrowable(var7, "Exception while ticking");
                    CrashReportCategory var6 = var5.addCategory("Block being ticked");
                    CrashReportCategory.populateBlockDetails(var6, this.level, var3.pos, null);
                    throw new ReportedException(var5);
                }
            }

            this.level.getProfiler().pop();
            this.alreadyTicked.clear();
            this.currentlyTicking.clear();
        }
    }

    @Override
    public boolean willTickThisTick(BlockPos param0, T param1) {
        return this.currentlyTicking.contains(new TickNextTickData(param0, param1));
    }

    public List<TickNextTickData<T>> fetchTicksInChunk(ChunkPos param0, boolean param1, boolean param2) {
        int var0 = param0.getMinBlockX() - 2;
        int var1 = var0 + 16 + 2;
        int var2 = param0.getMinBlockZ() - 2;
        int var3 = var2 + 16 + 2;
        return this.fetchTicksInArea(new BoundingBox(var0, this.level.getMinBuildHeight(), var2, var1, this.level.getMaxBuildHeight(), var3), param1, param2);
    }

    public List<TickNextTickData<T>> fetchTicksInArea(BoundingBox param0, boolean param1, boolean param2) {
        List<TickNextTickData<T>> var0 = this.fetchTicksInArea(null, this.tickNextTickList, param0, param1);
        if (param1 && var0 != null) {
            this.tickNextTickSet.removeAll(var0);
        }

        var0 = this.fetchTicksInArea(var0, this.currentlyTicking, param0, param1);
        if (!param2) {
            var0 = this.fetchTicksInArea(var0, this.alreadyTicked, param0, param1);
        }

        return var0 == null ? Collections.emptyList() : var0;
    }

    @Nullable
    private List<TickNextTickData<T>> fetchTicksInArea(
        @Nullable List<TickNextTickData<T>> param0, Collection<TickNextTickData<T>> param1, BoundingBox param2, boolean param3
    ) {
        Iterator<TickNextTickData<T>> var0 = param1.iterator();

        while(var0.hasNext()) {
            TickNextTickData<T> var1 = var0.next();
            BlockPos var2 = var1.pos;
            if (var2.getX() >= param2.minX() && var2.getX() < param2.maxX() && var2.getZ() >= param2.minZ() && var2.getZ() < param2.maxZ()) {
                if (param3) {
                    var0.remove();
                }

                if (param0 == null) {
                    param0 = Lists.newArrayList();
                }

                param0.add(var1);
            }
        }

        return param0;
    }

    public void copy(BoundingBox param0, BlockPos param1) {
        for(TickNextTickData<T> var1 : this.fetchTicksInArea(param0, false, false)) {
            if (param0.isInside(var1.pos)) {
                BlockPos var2 = var1.pos.offset(param1);
                T var3 = var1.getType();
                this.addTickData(new TickNextTickData<>(var2, var3, var1.triggerTick, var1.priority));
            }
        }

    }

    public ListTag save(ChunkPos param0) {
        List<TickNextTickData<T>> var0 = this.fetchTicksInChunk(param0, false, true);
        return saveTickList(this.toId, var0, this.level.getGameTime());
    }

    private static <T> ListTag saveTickList(Function<T, ResourceLocation> param0, Iterable<TickNextTickData<T>> param1, long param2) {
        ListTag var0 = new ListTag();

        for(TickNextTickData<T> var1 : param1) {
            CompoundTag var2 = new CompoundTag();
            var2.putString("i", param0.apply(var1.getType()).toString());
            var2.putInt("x", var1.pos.getX());
            var2.putInt("y", var1.pos.getY());
            var2.putInt("z", var1.pos.getZ());
            var2.putInt("t", (int)(var1.triggerTick - param2));
            var2.putInt("p", var1.priority.getValue());
            var0.add(var2);
        }

        return var0;
    }

    @Override
    public boolean hasScheduledTick(BlockPos param0, T param1) {
        return this.tickNextTickSet.contains(new TickNextTickData(param0, param1));
    }

    @Override
    public void scheduleTick(BlockPos param0, T param1, int param2, TickPriority param3) {
        if (!this.ignore.test(param1)) {
            this.addTickData(new TickNextTickData<>(param0, param1, (long)param2 + this.level.getGameTime(), param3));
        }

    }

    private void addTickData(TickNextTickData<T> param0) {
        if (!this.tickNextTickSet.contains(param0)) {
            this.tickNextTickSet.add(param0);
            this.tickNextTickList.add(param0);
        }

    }

    @Override
    public int size() {
        return this.tickNextTickSet.size();
    }
}
