package net.minecraft.util.profiling.metrics.profiling;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsRegistry;
import net.minecraft.util.profiling.metrics.MetricsSamplerProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class ServerMetricsSamplersProvider implements MetricsSamplerProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Set<MetricSampler> samplers = new ObjectOpenHashSet<>();
    private final ProfilerSamplerAdapter samplerFactory = new ProfilerSamplerAdapter();

    public ServerMetricsSamplersProvider(LongSupplier param0, boolean param1) {
        this.samplers.add(tickTimeSampler(param0));
        if (param1) {
            this.samplers.addAll(runtimeIndependentSamplers());
        }

    }

    public static Set<MetricSampler> runtimeIndependentSamplers() {
        Builder<MetricSampler> var0 = ImmutableSet.builder();

        try {
            ServerMetricsSamplersProvider.CpuStats var1 = new ServerMetricsSamplersProvider.CpuStats();
            IntStream.range(0, var1.nrOfCpus)
                .mapToObj(param1 -> MetricSampler.create("cpu#" + param1, MetricCategory.CPU, () -> var1.loadForCpu(param1)))
                .forEach(var0::add);
        } catch (Throwable var21) {
            LOGGER.warn("Failed to query cpu, no cpu stats will be recorded", var21);
        }

        var0.add(
            MetricSampler.create(
                "heap MiB", MetricCategory.JVM, () -> (double)((float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576.0F)
            )
        );
        var0.addAll(MetricsRegistry.INSTANCE.getRegisteredSamplers());
        return var0.build();
    }

    @Override
    public Set<MetricSampler> samplers(Supplier<ProfileCollector> param0) {
        this.samplers.addAll(this.samplerFactory.newSamplersFoundInProfiler(param0));
        return this.samplers;
    }

    public static MetricSampler tickTimeSampler(final LongSupplier param0) {
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

            long var0x = param0x.elapsed(TimeUnit.NANOSECONDS);
            param0x.reset();
            return (double)var0x;
        };
        MetricSampler.ValueIncreasedByPercentage var2 = new MetricSampler.ValueIncreasedByPercentage(2.0F);
        return MetricSampler.builder("ticktime", MetricCategory.TICK_LOOP, var1, var0).withBeforeTick(Stopwatch::start).withThresholdAlert(var2).build();
    }

    static class CpuStats {
        private final SystemInfo systemInfo = new SystemInfo();
        private final CentralProcessor processor = this.systemInfo.getHardware().getProcessor();
        public final int nrOfCpus = this.processor.getLogicalProcessorCount();
        private long[][] previousCpuLoadTick = this.processor.getProcessorCpuLoadTicks();
        private double[] currentLoad = this.processor.getProcessorCpuLoadBetweenTicks(this.previousCpuLoadTick);
        private long lastPollMs;

        public double loadForCpu(int param0) {
            long var0 = System.currentTimeMillis();
            if (this.lastPollMs == 0L || this.lastPollMs + 501L < var0) {
                this.currentLoad = this.processor.getProcessorCpuLoadBetweenTicks(this.previousCpuLoadTick);
                this.previousCpuLoadTick = this.processor.getProcessorCpuLoadTicks();
                this.lastPollMs = var0;
            }

            return this.currentLoad[param0] * 100.0;
        }
    }
}
