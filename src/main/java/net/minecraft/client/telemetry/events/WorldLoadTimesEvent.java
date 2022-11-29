package net.minecraft.client.telemetry.events;

import java.time.Duration;
import javax.annotation.Nullable;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldLoadTimesEvent {
    private final boolean newWorld;
    @Nullable
    private final Duration worldLoadDuration;

    public WorldLoadTimesEvent(boolean param0, @Nullable Duration param1) {
        this.worldLoadDuration = param1;
        this.newWorld = param0;
    }

    public void send(TelemetryEventSender param0) {
        if (this.worldLoadDuration != null) {
            param0.send(TelemetryEventType.WORLD_LOAD_TIMES, param0x -> {
                param0x.put(TelemetryProperty.WORLD_LOAD_TIME_MS, (int)this.worldLoadDuration.toMillis());
                param0x.put(TelemetryProperty.NEW_WORLD, this.newWorld);
            });
        }

    }
}
