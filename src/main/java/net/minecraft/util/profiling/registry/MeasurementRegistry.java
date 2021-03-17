package net.minecraft.util.profiling.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MeasurementRegistry {
    public static final MeasurementRegistry INSTANCE = new MeasurementRegistry();
    private final WeakHashMap<ProfilerMeasured, Void> measuredInstances = new WeakHashMap<>();

    private MeasurementRegistry() {
    }

    public void add(ProfilerMeasured param0) {
        this.measuredInstances.put(param0, null);
    }

    @OnlyIn(Dist.CLIENT)
    public Map<MeasurementCategory, List<MeasuredMetric>> getMetricsByCategories() {
        return this.measuredInstances
            .keySet()
            .stream()
            .flatMap(param0 -> param0.metrics().stream())
            .collect(Collectors.collectingAndThen(Collectors.groupingBy(MeasuredMetric::getGetCategory), EnumMap::new));
    }
}
