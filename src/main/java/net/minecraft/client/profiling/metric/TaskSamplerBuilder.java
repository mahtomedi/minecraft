package net.minecraft.client.profiling.metric;

import java.util.function.Supplier;
import net.minecraft.util.profiling.ActiveProfiler;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.registry.Metric;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class TaskSamplerBuilder {
    private final Metric metric;
    private final Supplier<ProfileCollector> profiler;

    public TaskSamplerBuilder(Metric param0, Supplier<ProfileCollector> param1) {
        this.metric = param0;
        this.profiler = param1;
    }

    public TaskSamplerBuilder(String param0, Supplier<ProfileCollector> param1) {
        this(new Metric(param0), param1);
    }

    public MetricSampler forPath(String... param0) {
        if (param0.length == 0) {
            throw new IllegalArgumentException("Expected at least one path node, got no values");
        } else {
            String var0 = StringUtils.join((Object[])param0, '\u001e');
            return MetricSampler.create(this.metric, () -> {
                ActiveProfiler.PathEntry var0x = this.profiler.get().getEntry(var0);
                return var0x == null ? -1.0 : (double)var0x.getDuration() / 1000000.0;
            });
        }
    }
}
