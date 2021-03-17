package net.minecraft.client.profiling.metric;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.profiling.registry.Metric;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MetricSampler {
    private final Metric metric;
    private final DoubleSupplier sampler;
    private final ByteBuf values;
    private volatile boolean isRunning;
    @Nullable
    private final Runnable beforeTick;
    @Nullable
    private final MetricSampler.ThresholdAlerter thresholdAlerter;

    private <T> MetricSampler(Metric param0, DoubleSupplier param1, @Nullable Runnable param2, @Nullable MetricSampler.ThresholdAlerter param3) {
        this.metric = param0;
        this.beforeTick = param2;
        this.sampler = param1;
        this.thresholdAlerter = param3;
        this.values = new FriendlyByteBuf(Unpooled.directBuffer());
        this.isRunning = true;
    }

    public static MetricSampler create(Metric param0, DoubleSupplier param1) {
        return new MetricSampler(param0, param1, null, null);
    }

    public static MetricSampler create(String param0, DoubleSupplier param1) {
        return create(new Metric(param0), param1);
    }

    public static <T> MetricSampler create(String param0, T param1, ToDoubleFunction<T> param2) {
        return builder(param0, param2, param1).build();
    }

    public static <T> MetricSampler.MetricSamplerBuilder<T> builder(String param0, ToDoubleFunction<T> param1, T param2) {
        return new MetricSampler.MetricSamplerBuilder<>(new Metric(param0), param1, param2);
    }

    public int numberOfValues() {
        return this.values.readableBytes() / 8;
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

    public void onEndTick() {
        this.verifyRunning();
        double var0 = this.sampler.getAsDouble();
        this.values.writeDouble(var0);
        if (this.thresholdAlerter != null) {
            this.thresholdAlerter.test(var0);
        }

    }

    public void onFinished() {
        this.verifyRunning();
        this.values.release();
        this.isRunning = false;
    }

    private void verifyRunning() {
        if (!this.isRunning) {
            throw new IllegalStateException(String.format("Sampler for metric %s not started!", this.metric.getName()));
        }
    }

    public Metric getMetric() {
        return this.metric;
    }

    public boolean hasMoreValues() {
        return this.values.isReadable(8);
    }

    public double readNextValue() {
        return this.values.readDouble();
    }

    @OnlyIn(Dist.CLIENT)
    public static class MetricSamplerBuilder<T> {
        private final Metric metric;
        private final DoubleSupplier sampler;
        private final T context;
        @Nullable
        private Runnable beforeTick = null;
        @Nullable
        private MetricSampler.ThresholdAlerter thresholdAlerter;

        public MetricSamplerBuilder(Metric param0, ToDoubleFunction<T> param1, T param2) {
            this.metric = param0;
            this.sampler = () -> param1.applyAsDouble(param2);
            this.context = param2;
        }

        public MetricSampler.MetricSamplerBuilder<T> withBeforeTick(Consumer<T> param0) {
            this.beforeTick = () -> param0.accept(this.context);
            return this;
        }

        public MetricSampler.MetricSamplerBuilder<T> withThresholdAlert(MetricSampler.ThresholdAlerter param0) {
            this.thresholdAlerter = param0;
            return this;
        }

        public MetricSampler build() {
            return new MetricSampler(this.metric, this.sampler, this.beforeTick, this.thresholdAlerter);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface ThresholdAlerter {
        void test(double var1);
    }

    @OnlyIn(Dist.CLIENT)
    public static class ValueIncreased implements MetricSampler.ThresholdAlerter {
        private final float percentageIncreaseThreshold;
        private final DoubleConsumer action;
        private double previousValue = Double.MIN_VALUE;

        public ValueIncreased(float param0, DoubleConsumer param1) {
            this.percentageIncreaseThreshold = param0;
            this.action = param1;
        }

        @Override
        public void test(double param0) {
            boolean var0 = this.previousValue != Double.MIN_VALUE
                && param0 > this.previousValue
                && (param0 - this.previousValue) / this.previousValue >= (double)this.percentageIncreaseThreshold;
            if (var0) {
                this.action.accept(param0);
            }

            this.previousValue = param0;
        }
    }
}
