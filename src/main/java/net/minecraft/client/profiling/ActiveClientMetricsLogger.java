package net.minecraft.client.profiling;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.profiling.metric.FpsSpikeRecording;
import net.minecraft.client.profiling.metric.MetricSampler;
import net.minecraft.client.profiling.metric.SamplerCategory;
import net.minecraft.client.profiling.metric.TaskSamplerBuilder;
import net.minecraft.client.profiling.storage.MetricsPersister;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.registry.MeasurementRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ActiveClientMetricsLogger implements ClientMetricsLogger {
    @Nullable
    private static Consumer<Path> globalOnReportFinished = null;
    private final List<SamplerCategory> samplerCategories = new ObjectArrayList<>();
    private final ContinuousProfiler taskProfiler;
    private final Executor ioExecutor;
    private final MetricsPersister metricsPersister;
    private final Runnable onFinished;
    private final Consumer<Path> onReportFinished;
    private final LongSupplier wallTimeSource;
    private final List<FpsSpikeRecording> fpsSpikeRecordings = new ObjectArrayList<>();
    private final long deadlineNano;
    private int currentTick;
    private ProfileCollector singleTickProfiler;
    private volatile boolean killSwitch;

    private ActiveClientMetricsLogger(LongSupplier param0, Executor param1, MetricsPersister param2, Runnable param3, Consumer<Path> param4) {
        this.wallTimeSource = param0;
        this.taskProfiler = new ContinuousProfiler(param0, () -> this.currentTick);
        this.ioExecutor = param1;
        this.metricsPersister = param2;
        this.onFinished = param3;
        this.onReportFinished = globalOnReportFinished == null ? param4 : param4.andThen(globalOnReportFinished);
        this.deadlineNano = param0.getAsLong() + TimeUnit.NANOSECONDS.convert(10L, TimeUnit.SECONDS);
        this.addSamplers();
        this.singleTickProfiler = new ActiveProfiler(this.wallTimeSource, () -> this.currentTick, false);
        this.taskProfiler.enable();
    }

    public static ActiveClientMetricsLogger createStarted(LongSupplier param0, Executor param1, MetricsPersister param2, Runnable param3, Consumer<Path> param4) {
        return new ActiveClientMetricsLogger(param0, param1, param2, param3, param4);
    }

    private void addSamplers() {
        this.samplerCategories
            .add(
                new SamplerCategory(
                    "JVM",
                    MetricSampler.create("heap (Mb)", () -> (double)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576.0)
                )
            );
        this.samplerCategories.add(new SamplerCategory("Frame times (ms)", this.frameTimeSampler(this.wallTimeSource)));
        this.samplerCategories
            .add(
                new SamplerCategory(
                    "Task total durations (ms)",
                    this.profilerTaskSampler("gameRendering").forPath("root", "gameRenderer"),
                    this.profilerTaskSampler("updateDisplay").forPath("root", "updateDisplay"),
                    this.profilerTaskSampler("skyRendering").forPath("root", "gameRenderer", "level", "sky")
                )
            );
        LevelRenderer var0 = Minecraft.getInstance().levelRenderer;
        this.samplerCategories
            .add(
                new SamplerCategory(
                    "Rendering chunk dispatching",
                    MetricSampler.create("totalChunks", var0, LevelRenderer::getTotalChunks),
                    MetricSampler.create("renderedChunks", var0, LevelRenderer::countRenderedChunks),
                    MetricSampler.create("lastViewDistance", var0, LevelRenderer::getLastViewDistance)
                )
            );
        ChunkRenderDispatcher var1 = var0.getChunkRenderDispatcher();
        this.samplerCategories
            .add(
                new SamplerCategory(
                    "Rendering chunk stats",
                    MetricSampler.create("toUpload", var1, ChunkRenderDispatcher::getToUpload),
                    MetricSampler.create("freeBufferCount", var1, ChunkRenderDispatcher::getFreeBufferCount),
                    MetricSampler.create("toBatchCount", var1, ChunkRenderDispatcher::getToBatchCount)
                )
            );
        MeasurementRegistry.INSTANCE
            .getMetricsByCategories()
            .forEach(
                (param0, param1) -> {
                    List<MetricSampler> var0x = param1.stream()
                        .map(param0x -> MetricSampler.create(param0x.getMetric(), param0x.getCurrentValue()))
                        .collect(Collectors.toList());
                    this.samplerCategories.add(new SamplerCategory(param0.getName(), var0x));
                }
            );
    }

    private TaskSamplerBuilder profilerTaskSampler(String param0) {
        return new TaskSamplerBuilder(param0, () -> this.singleTickProfiler);
    }

    private MetricSampler frameTimeSampler(final LongSupplier param0) {
        Stopwatch var0 = Stopwatch.createUnstarted(new Ticker() {
            @Override
            public long read() {
                return param0.getAsLong();
            }
        });
        ToDoubleFunction<Stopwatch> var1 = param0x -> {
            if (param0x.isRunning()) {
                param0x.stop();
            }

            long var0x = param0x.elapsed(TimeUnit.MILLISECONDS);
            param0x.reset();
            return (double)var0x;
        };
        MetricSampler.ValueIncreased var2 = new MetricSampler.ValueIncreased(
            0.5F, param0x -> this.fpsSpikeRecordings.add(new FpsSpikeRecording(new Date(), this.currentTick, this.singleTickProfiler.getResults()))
        );
        return MetricSampler.builder("frametime", var1, var0).withBeforeTick(Stopwatch::start).withThresholdAlert(var2).build();
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

        for(SamplerCategory var0 : this.samplerCategories) {
            var0.onStartTick();
        }

        ++this.currentTick;
    }

    @Override
    public void endTick() {
        this.verifyStarted();
        if (this.currentTick != 0) {
            for(SamplerCategory var0 : this.samplerCategories) {
                var0.onEndTick();
            }

            if (!this.killSwitch && this.wallTimeSource.getAsLong() <= this.deadlineNano) {
                this.singleTickProfiler = new ActiveProfiler(this.wallTimeSource, () -> this.currentTick, false);
            } else {
                this.onFinished.run();
                this.killSwitch = false;
                this.singleTickProfiler = InactiveProfiler.INSTANCE;
                this.scheduleSaveResults();
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

    private void scheduleSaveResults() {
        this.ioExecutor.execute(() -> {
            Path var0 = this.metricsPersister.saveReports(this.samplerCategories, this.fpsSpikeRecordings, this.taskProfiler);

            for(SamplerCategory var1 : this.samplerCategories) {
                var1.onFinished();
            }

            this.samplerCategories.clear();
            this.fpsSpikeRecordings.clear();
            this.taskProfiler.disable();
            this.onReportFinished.accept(var0);
        });
    }
}
