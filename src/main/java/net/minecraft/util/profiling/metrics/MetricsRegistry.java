package net.minecraft.util.profiling.metrics;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class MetricsRegistry {
    public static final MetricsRegistry INSTANCE = new MetricsRegistry();
    private final WeakHashMap<ProfilerMeasured, Void> measuredInstances = new WeakHashMap<>();

    private MetricsRegistry() {
    }

    public void add(ProfilerMeasured param0) {
        this.measuredInstances.put(param0, null);
    }

    public List<MetricSampler> getRegisteredSamplers() {
        Map<String, List<MetricSampler>> var0 = this.measuredInstances
            .keySet()
            .stream()
            .flatMap(param0 -> param0.profiledMetrics().stream())
            .collect(Collectors.groupingBy(MetricSampler::getName));
        return aggregateDuplicates(var0);
    }

    private static List<MetricSampler> aggregateDuplicates(Map<String, List<MetricSampler>> param0) {
        return param0.entrySet().stream().map(param0x -> {
            String var0x = param0x.getKey();
            List<MetricSampler> var1 = param0x.getValue();
            return (MetricSampler)(var1.size() > 1 ? new MetricsRegistry.AggregatedMetricSampler(var0x, var1) : var1.get(0));
        }).collect(Collectors.toList());
    }

    static class AggregatedMetricSampler extends MetricSampler {
        private final List<MetricSampler> delegates;

        AggregatedMetricSampler(String param0, List<MetricSampler> param1) {
            super(param0, param1.get(0).getCategory(), () -> averageValueFromDelegates(param1), () -> beforeTick(param1), thresholdTest(param1));
            this.delegates = param1;
        }

        private static MetricSampler.ThresholdTest thresholdTest(List<MetricSampler> param0) {
            return param1 -> param0.stream().anyMatch(param1x -> param1x.thresholdTest != null ? param1x.thresholdTest.test(param1) : false);
        }

        private static void beforeTick(List<MetricSampler> param0) {
            for(MetricSampler var0 : param0) {
                var0.onStartTick();
            }

        }

        private static double averageValueFromDelegates(List<MetricSampler> param0) {
            double var0 = 0.0;

            for(MetricSampler var1 : param0) {
                var0 += var1.getSampler().getAsDouble();
            }

            return var0 / (double)param0.size();
        }

        @Override
        public boolean equals(@Nullable Object param0) {
            if (this == param0) {
                return true;
            } else if (param0 == null || this.getClass() != param0.getClass()) {
                return false;
            } else if (!super.equals(param0)) {
                return false;
            } else {
                MetricsRegistry.AggregatedMetricSampler var0 = (MetricsRegistry.AggregatedMetricSampler)param0;
                return this.delegates.equals(var0.delegates);
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), this.delegates);
        }
    }
}
