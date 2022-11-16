package net.minecraft.client.telemetry.events;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class PerformanceMetricsEvent extends AggregatedTelemetryEvent {
    private static final long DEDICATED_MEMORY_KB = toKilobytes(Runtime.getRuntime().maxMemory());
    private final LongList fpsSamples = new LongArrayList();
    private final LongList frameTimeSamples = new LongArrayList();
    private final LongList usedMemorySamples = new LongArrayList();
    private final TelemetryEventSender eventSender;

    public PerformanceMetricsEvent(TelemetryEventSender param0) {
        this.eventSender = param0;
    }

    @Override
    public void tick() {
        if (Minecraft.getInstance().telemetryOptInExtra()) {
            super.tick();
        }

    }

    private void resetValues() {
        this.fpsSamples.clear();
        this.frameTimeSamples.clear();
        this.usedMemorySamples.clear();
    }

    @Override
    public void takeSample() {
        this.fpsSamples.add((long)Minecraft.getInstance().getFps());
        this.takeUsedMemorySample();
        this.frameTimeSamples.add(Minecraft.getInstance().getFrameTimeNs());
    }

    private void takeUsedMemorySample() {
        long var0 = Runtime.getRuntime().totalMemory();
        long var1 = Runtime.getRuntime().freeMemory();
        long var2 = var0 - var1;
        this.usedMemorySamples.add(toKilobytes(var2));
    }

    @Override
    public void sendEvent() {
        this.send(this.eventSender);
    }

    @Override
    public void send(TelemetryEventSender param0) {
        param0.send(TelemetryEventType.PERFORMANCE_METRICS, param0x -> {
            param0x.put(TelemetryProperty.FRAME_RATE_SAMPLES, new LongArrayList(this.fpsSamples));
            param0x.put(TelemetryProperty.RENDER_TIME_SAMPLES, new LongArrayList(this.frameTimeSamples));
            param0x.put(TelemetryProperty.USED_MEMORY_SAMPLES, new LongArrayList(this.usedMemorySamples));
            param0x.put(TelemetryProperty.NUMBER_OF_SAMPLES, this.getSampleCount());
            param0x.put(TelemetryProperty.RENDER_DISTANCE, Minecraft.getInstance().options.getEffectiveRenderDistance());
            param0x.put(TelemetryProperty.DEDICATED_MEMORY_KB, (int)DEDICATED_MEMORY_KB);
        });
        this.resetValues();
    }

    private static long toKilobytes(long param0) {
        return param0 / 1000L;
    }
}
