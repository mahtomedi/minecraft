package net.minecraft.util.profiling.metrics;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;

public class MetricSampler {
    private final String name;
    private final MetricCategory category;
    private final DoubleSupplier sampler;
    private final ByteBuf ticks;
    private final ByteBuf values;
    private volatile boolean isRunning;
    @Nullable
    private final Runnable beforeTick;
    @Nullable
    final MetricSampler.ThresholdTest thresholdTest;
    private double currentValue;

    protected MetricSampler(
        String param0, MetricCategory param1, DoubleSupplier param2, @Nullable Runnable param3, @Nullable MetricSampler.ThresholdTest param4
    ) {
        this.name = param0;
        this.category = param1;
        this.beforeTick = param3;
        this.sampler = param2;
        this.thresholdTest = param4;
        this.values = ByteBufAllocator.DEFAULT.buffer();
        this.ticks = ByteBufAllocator.DEFAULT.buffer();
        this.isRunning = true;
    }

    public static MetricSampler create(String param0, MetricCategory param1, DoubleSupplier param2) {
        return new MetricSampler(param0, param1, param2, null, null);
    }

    public static <T> MetricSampler create(String param0, MetricCategory param1, T param2, ToDoubleFunction<T> param3) {
        return builder(param0, param1, param3, param2).build();
    }

    public static <T> MetricSampler.MetricSamplerBuilder<T> builder(String param0, MetricCategory param1, ToDoubleFunction<T> param2, T param3) {
        return new MetricSampler.MetricSamplerBuilder<>(param0, param1, param2, param3);
    }

    public void onStartTick() {
        if (!this.isRunning) {
            throw new IllegalStateException("Not running");
        } else {
            if (this.beforeTick != null) {
                this.beforeTick.run();
            }

        }
    }

    public void onEndTick(int param0) {
        this.verifyRunning();
        this.currentValue = this.sampler.getAsDouble();
        this.values.writeDouble(this.currentValue);
        this.ticks.writeInt(param0);
    }

    public void onFinished() {
        this.verifyRunning();
        this.values.release();
        this.ticks.release();
        this.isRunning = false;
    }

    private void verifyRunning() {
        if (!this.isRunning) {
            throw new IllegalStateException(String.format(Locale.ROOT, "Sampler for metric %s not started!", this.name));
        }
    }

    DoubleSupplier getSampler() {
        return this.sampler;
    }

    public String getName() {
        return this.name;
    }

    public MetricCategory getCategory() {
        return this.category;
    }

    public MetricSampler.SamplerResult result() {
        Int2DoubleMap var0 = new Int2DoubleOpenHashMap();
        int var1 = Integer.MIN_VALUE;

        int var2;
        int var3;
        for(var2 = Integer.MIN_VALUE; this.values.isReadable(8); var2 = var3) {
            var3 = this.ticks.readInt();
            if (var1 == Integer.MIN_VALUE) {
                var1 = var3;
            }

            var0.put(var3, this.values.readDouble());
        }

        return new MetricSampler.SamplerResult(var1, var2, var0);
    }

    public boolean triggersThreshold() {
        return this.thresholdTest != null && this.thresholdTest.test(this.currentValue);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            MetricSampler var0 = (MetricSampler)param0;
            return this.name.equals(var0.name) && this.category.equals(var0.category);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    public static class MetricSamplerBuilder<T> {
        private final String name;
        private final MetricCategory category;
        private final DoubleSupplier sampler;
        private final T context;
        @Nullable
        private Runnable beforeTick;
        @Nullable
        private MetricSampler.ThresholdTest thresholdTest;

        public MetricSamplerBuilder(String param0, MetricCategory param1, ToDoubleFunction<T> param2, T param3) {
            this.name = param0;
            this.category = param1;
            this.sampler = () -> param2.applyAsDouble(param3);
            this.context = param3;
        }

        public MetricSampler.MetricSamplerBuilder<T> withBeforeTick(Consumer<T> param0) {
            this.beforeTick = () -> param0.accept(this.context);
            return this;
        }

        public MetricSampler.MetricSamplerBuilder<T> withThresholdAlert(MetricSampler.ThresholdTest param0) {
            this.thresholdTest = param0;
            return this;
        }

        public MetricSampler build() {
            return new MetricSampler(this.name, this.category, this.sampler, this.beforeTick, this.thresholdTest);
        }
    }

    public static class SamplerResult {
        private final Int2DoubleMap recording;
        private final int firstTick;
        private final int lastTick;

        public SamplerResult(int param0, int param1, Int2DoubleMap param2) {
            this.firstTick = param0;
            this.lastTick = param1;
            this.recording = param2;
        }

        public double valueAtTick(int param0) {
            return this.recording.get(param0);
        }

        public int getFirstTick() {
            return this.firstTick;
        }

        public int getLastTick() {
            return this.lastTick;
        }
    }

    public interface ThresholdTest {
        boolean test(double var1);
    }

    public static class ValueIncreasedByPercentage implements MetricSampler.ThresholdTest {
        private final float percentageIncreaseThreshold;
        private double previousValue = Double.MIN_VALUE;

        public ValueIncreasedByPercentage(float param0) {
            this.percentageIncreaseThreshold = param0;
        }

        @Override
        public boolean test(double param0) {
            boolean var1;
            if (this.previousValue != Double.MIN_VALUE && !(param0 <= this.previousValue)) {
                var1 = (param0 - this.previousValue) / this.previousValue >= (double)this.percentageIncreaseThreshold;
            } else {
                var1 = false;
            }

            this.previousValue = param0;
            return var1;
        }
    }
}
