package net.minecraft.client.telemetry.events;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldUnloadEvent {
    private static final int NOT_TRACKING_TIME = -1;
    private Optional<Instant> worldLoadedTime = Optional.empty();
    private long totalTicks;
    private long lastGameTime;

    public void onPlayerInfoReceived() {
        this.lastGameTime = -1L;
        if (this.worldLoadedTime.isEmpty()) {
            this.worldLoadedTime = Optional.of(Instant.now());
        }

    }

    public void setTime(long param0) {
        if (this.lastGameTime != -1L) {
            this.totalTicks += Math.max(0L, param0 - this.lastGameTime);
        }

        this.lastGameTime = param0;
    }

    private int getTimeInSecondsSinceLoad(Instant param0) {
        Duration var0 = Duration.between(param0, Instant.now());
        return (int)var0.toSeconds();
    }

    public void send(TelemetryEventSender param0) {
        this.worldLoadedTime.ifPresent(param1 -> param0.send(TelemetryEventType.WORLD_UNLOADED, param1x -> {
                param1x.put(TelemetryProperty.SECONDS_SINCE_LOAD, this.getTimeInSecondsSinceLoad(param1));
                param1x.put(TelemetryProperty.TICKS_SINCE_LOAD, (int)this.totalTicks);
            }));
    }
}
