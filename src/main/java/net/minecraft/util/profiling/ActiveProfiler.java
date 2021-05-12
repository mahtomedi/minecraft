package net.minecraft.util.profiling;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActiveProfiler implements ProfileCollector {
    private static final long WARNING_TIME_NANOS = Duration.ofMillis(100L).toNanos();
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<String> paths = Lists.newArrayList();
    private final LongList startTimes = new LongArrayList();
    private final Map<String, ActiveProfiler.PathEntry> entries = Maps.newHashMap();
    private final IntSupplier getTickTime;
    private final LongSupplier getRealTime;
    private final long startTimeNano;
    private final int startTimeTicks;
    private String path = "";
    private boolean started;
    @Nullable
    private ActiveProfiler.PathEntry currentEntry;
    private final boolean warn;

    public ActiveProfiler(LongSupplier param0, IntSupplier param1, boolean param2) {
        this.startTimeNano = param0.getAsLong();
        this.getRealTime = param0;
        this.startTimeTicks = param1.getAsInt();
        this.getTickTime = param1;
        this.warn = param2;
    }

    @Override
    public void startTick() {
        if (this.started) {
            LOGGER.error("Profiler tick already started - missing endTick()?");
        } else {
            this.started = true;
            this.path = "";
            this.paths.clear();
            this.push("root");
        }
    }

    @Override
    public void endTick() {
        if (!this.started) {
            LOGGER.error("Profiler tick already ended - missing startTick()?");
        } else {
            this.pop();
            this.started = false;
            if (!this.path.isEmpty()) {
                LOGGER.error(
                    "Profiler tick ended before path was fully popped (remainder: '{}'). Mismatched push/pop?", () -> ProfileResults.demanglePath(this.path)
                );
            }

        }
    }

    @Override
    public void push(String param0) {
        if (!this.started) {
            LOGGER.error("Cannot push '{}' to profiler if profiler tick hasn't started - missing startTick()?", param0);
        } else {
            if (!this.path.isEmpty()) {
                this.path = this.path + "\u001e";
            }

            this.path = this.path + param0;
            this.paths.add(this.path);
            this.startTimes.add(Util.getNanos());
            this.currentEntry = null;
        }
    }

    @Override
    public void push(Supplier<String> param0) {
        this.push(param0.get());
    }

    @Override
    public void pop() {
        if (!this.started) {
            LOGGER.error("Cannot pop from profiler if profiler tick hasn't started - missing startTick()?");
        } else if (this.startTimes.isEmpty()) {
            LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
        } else {
            long var0 = Util.getNanos();
            long var1 = this.startTimes.removeLong(this.startTimes.size() - 1);
            this.paths.remove(this.paths.size() - 1);
            long var2 = var0 - var1;
            ActiveProfiler.PathEntry var3 = this.getCurrentEntry();
            var3.duration += var2;
            ++var3.count;
            if (this.warn && var2 > WARNING_TIME_NANOS) {
                LOGGER.warn("Something's taking too long! '{}' took aprox {} ms", () -> ProfileResults.demanglePath(this.path), () -> (double)var2 / 1000000.0);
            }

            this.path = this.paths.isEmpty() ? "" : this.paths.get(this.paths.size() - 1);
            this.currentEntry = null;
        }
    }

    @Override
    public void popPush(String param0) {
        this.pop();
        this.push(param0);
    }

    @Override
    public void popPush(Supplier<String> param0) {
        this.pop();
        this.push(param0);
    }

    private ActiveProfiler.PathEntry getCurrentEntry() {
        if (this.currentEntry == null) {
            this.currentEntry = this.entries.computeIfAbsent(this.path, param0 -> new ActiveProfiler.PathEntry());
        }

        return this.currentEntry;
    }

    @Override
    public void incrementCounter(String param0) {
        this.getCurrentEntry().counters.addTo(param0, 1L);
    }

    @Override
    public void incrementCounter(Supplier<String> param0) {
        this.getCurrentEntry().counters.addTo(param0.get(), 1L);
    }

    @Override
    public ProfileResults getResults() {
        return new FilledProfileResults(this.entries, this.startTimeNano, this.startTimeTicks, this.getRealTime.getAsLong(), this.getTickTime.getAsInt());
    }

    @Nullable
    @Override
    public ActiveProfiler.PathEntry getEntry(String param0) {
        return this.entries.get(param0);
    }

    public static class PathEntry implements ProfilerPathEntry {
        long duration;
        long count;
        final Object2LongOpenHashMap<String> counters = new Object2LongOpenHashMap<>();

        @Override
        public long getDuration() {
            return this.duration;
        }

        @Override
        public long getCount() {
            return this.count;
        }

        @Override
        public Object2LongMap<String> getCounters() {
            return Object2LongMaps.unmodifiable(this.counters);
        }
    }
}
