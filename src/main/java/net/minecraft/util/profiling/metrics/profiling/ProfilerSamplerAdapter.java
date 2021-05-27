package net.minecraft.util.profiling.metrics.profiling;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;

public class ProfilerSamplerAdapter {
    private final Set<String> previouslyFoundSamplerNames = new ObjectOpenHashSet<>();

    public Set<MetricSampler> newSamplersFoundInProfiler(Supplier<ProfileCollector> param0) {
        Set<MetricSampler> var0 = param0.get()
            .getChartedPaths()
            .stream()
            .filter(param0x -> !this.previouslyFoundSamplerNames.contains(param0x.getLeft()))
            .map(param1 -> samplerForProfilingPath(param0, param1.getLeft(), param1.getRight()))
            .collect(Collectors.toSet());

        for(MetricSampler var1 : var0) {
            this.previouslyFoundSamplerNames.add(var1.getName());
        }

        return var0;
    }

    private static MetricSampler samplerForProfilingPath(Supplier<ProfileCollector> param0, String param1, MetricCategory param2) {
        return MetricSampler.create(param1, param2, () -> {
            ActiveProfiler.PathEntry var0x = param0.get().getEntry(param1);
            return var0x == null ? 0.0 : (double)var0x.getMaxDuration() / (double)TimeUtil.NANOSECONDS_PER_MILLISECOND;
        });
    }
}
