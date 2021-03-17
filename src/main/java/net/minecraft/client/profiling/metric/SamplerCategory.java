package net.minecraft.client.profiling.metric;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SamplerCategory {
    private final String name;
    private final MetricSampler[] metricSamplers;

    public SamplerCategory(String param0, MetricSampler... param1) {
        this.name = param0;
        this.metricSamplers = param1;
    }

    public SamplerCategory(String param0, List<MetricSampler> param1) {
        this.name = param0;
        this.metricSamplers = param1.toArray(new MetricSampler[0]);
    }

    public void onEndTick() {
        for(MetricSampler var0 : this.metricSamplers) {
            var0.onEndTick();
        }

    }

    public void onStartTick() {
        for(MetricSampler var0 : this.metricSamplers) {
            var0.onStartTick();
        }

    }

    public void onFinished() {
        for(MetricSampler var0 : this.metricSamplers) {
            var0.onFinished();
        }

    }

    public String getName() {
        return this.name;
    }

    public List<MetricSampler> getMetricSamplers() {
        return ImmutableList.copyOf(this.metricSamplers);
    }
}
