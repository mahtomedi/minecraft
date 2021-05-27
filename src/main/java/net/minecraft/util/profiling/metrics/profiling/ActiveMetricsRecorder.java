package net.minecraft.util.profiling.metrics.profiling;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsSamplerProvider;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.profiling.metrics.storage.RecordedDeviation;

public class ActiveMetricsRecorder implements MetricsRecorder {
    public static final int PROFILING_MAX_DURATION_SECONDS = 10;
    @Nullable
    private static Consumer<Path> globalOnReportFinished = null;
    private final Map<MetricSampler, List<RecordedDeviation>> deviationsBySampler = new Object2ObjectOpenHashMap<>();
    private final ContinuousProfiler taskProfiler;
    private final Executor ioExecutor;
    private final MetricsPersister metricsPersister;
    private final Consumer<ProfileResults> onProfilingEnd;
    private final Consumer<Path> onReportFinished;
    private final MetricsSamplerProvider metricsSamplerProvider;
    private final LongSupplier wallTimeSource;
    private final long deadlineNano;
    private int currentTick;
    private ProfileCollector singleTickProfiler;
    private volatile boolean killSwitch;
    private Set<MetricSampler> thisTickSamplers = ImmutableSet.of();

    private ActiveMetricsRecorder(
        MetricsSamplerProvider param0, LongSupplier param1, Executor param2, MetricsPersister param3, Consumer<ProfileResults> param4, Consumer<Path> param5
    ) {
        this.metricsSamplerProvider = param0;
        this.wallTimeSource = param1;
        this.taskProfiler = new ContinuousProfiler(param1, () -> this.currentTick);
        this.ioExecutor = param2;
        this.metricsPersister = param3;
        this.onProfilingEnd = param4;
        this.onReportFinished = globalOnReportFinished == null ? param5 : param5.andThen(globalOnReportFinished);
        this.deadlineNano = param1.getAsLong() + TimeUnit.NANOSECONDS.convert(10L, TimeUnit.SECONDS);
        this.singleTickProfiler = new ActiveProfiler(this.wallTimeSource, () -> this.currentTick, false);
        this.taskProfiler.enable();
    }

    public static ActiveMetricsRecorder createStarted(
        MetricsSamplerProvider param0, LongSupplier param1, Executor param2, MetricsPersister param3, Consumer<ProfileResults> param4, Consumer<Path> param5
    ) {
        return new ActiveMetricsRecorder(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public synchronized void end() {
        if (this.isRecording()) {
            this.killSwitch = true;
        }
    }

    @Override
    public void startTick() {
        this.verifyStarted();
        this.thisTickSamplers = this.metricsSamplerProvider.samplers(() -> this.singleTickProfiler);

        for(MetricSampler var0 : this.thisTickSamplers) {
            var0.onStartTick();
        }

        ++this.currentTick;
    }

    @Override
    public void endTick() {
        this.verifyStarted();
        if (this.currentTick != 0) {
            for(MetricSampler var0 : this.thisTickSamplers) {
                var0.onEndTick(this.currentTick);
                if (var0.triggersThreshold()) {
                    RecordedDeviation var1 = new RecordedDeviation(Instant.now(), this.currentTick, this.singleTickProfiler.getResults());
                    this.deviationsBySampler.computeIfAbsent(var0, param0 -> Lists.newArrayList()).add(var1);
                }
            }

            if (!this.killSwitch && this.wallTimeSource.getAsLong() <= this.deadlineNano) {
                this.singleTickProfiler = new ActiveProfiler(this.wallTimeSource, () -> this.currentTick, false);
            } else {
                this.killSwitch = false;
                this.singleTickProfiler = InactiveProfiler.INSTANCE;
                ProfileResults var2 = this.taskProfiler.getResults();
                this.onProfilingEnd.accept(var2);
                this.scheduleSaveResults(var2);
            }
        }
    }

    @Override
    public boolean isRecording() {
        return this.taskProfiler.isEnabled();
    }

    @Override
    public ProfilerFiller getProfiler() {
        return ProfilerFiller.tee(this.taskProfiler.getFiller(), this.singleTickProfiler);
    }

    private void verifyStarted() {
        if (!this.isRecording()) {
            throw new IllegalStateException("Not started!");
        }
    }

    private void scheduleSaveResults(ProfileResults param0) {
        HashSet<MetricSampler> var0 = new HashSet<>(this.thisTickSamplers);
        this.ioExecutor.execute(() -> {
            Path var0x = this.metricsPersister.saveReports(var0, this.deviationsBySampler, param0);

            for(MetricSampler var1x : var0) {
                var1x.onFinished();
            }

            this.deviationsBySampler.clear();
            this.taskProfiler.disable();
            this.onReportFinished.accept(var0x);
        });
    }

    public static void registerGlobalCompletionCallback(Consumer<Path> param0) {
        globalOnReportFinished = param0;
    }
}
