package net.minecraft.client.telemetry.events;

import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface TelemetryEventProducer {
    void send(TelemetryEventSender var1);
}
