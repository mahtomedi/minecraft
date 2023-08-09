package net.minecraft.network;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.SampleLogger;

public class BandwidthDebugMonitor {
    private final AtomicInteger bytesReceived = new AtomicInteger();
    private final SampleLogger bandwidthLogger;

    public BandwidthDebugMonitor(SampleLogger param0) {
        this.bandwidthLogger = param0;
    }

    public void onReceive(int param0) {
        this.bytesReceived.getAndAdd(param0);
    }

    public void tick() {
        this.bandwidthLogger.logSample((long)this.bytesReceived.getAndSet(0));
    }
}
