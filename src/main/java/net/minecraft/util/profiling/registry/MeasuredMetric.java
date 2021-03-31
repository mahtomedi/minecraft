package net.minecraft.util.profiling.registry;

import java.util.function.DoubleSupplier;

public class MeasuredMetric {
    private final Metric metric;
    private final DoubleSupplier currentValue;
    private final MeasurementCategory getCategory;

    public MeasuredMetric(Metric param0, DoubleSupplier param1, MeasurementCategory param2) {
        this.metric = param0;
        this.currentValue = param1;
        this.getCategory = param2;
    }

    public Metric getMetric() {
        return this.metric;
    }

    public DoubleSupplier getCurrentValue() {
        return this.currentValue;
    }

    public MeasurementCategory getGetCategory() {
        return this.getCategory;
    }
}
