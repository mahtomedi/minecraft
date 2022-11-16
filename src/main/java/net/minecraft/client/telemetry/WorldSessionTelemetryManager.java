package net.minecraft.client.telemetry;

import java.time.Duration;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.telemetry.events.PerformanceMetricsEvent;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraft.client.telemetry.events.WorldLoadTimesEvent;
import net.minecraft.client.telemetry.events.WorldUnloadEvent;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldSessionTelemetryManager {
    private final UUID worldSessionId = UUID.randomUUID();
    private final TelemetryEventSender eventSender;
    private final WorldLoadEvent worldLoadEvent;
    private final WorldUnloadEvent worldUnloadEvent = new WorldUnloadEvent();
    private final PerformanceMetricsEvent performanceMetricsEvent;
    private final WorldLoadTimesEvent worldLoadTimesEvent;

    public WorldSessionTelemetryManager(TelemetryEventSender param0, boolean param1, @Nullable Duration param2) {
        this.worldLoadEvent = new WorldLoadEvent(this::worldSessionStart);
        this.performanceMetricsEvent = new PerformanceMetricsEvent(param0);
        this.worldLoadTimesEvent = new WorldLoadTimesEvent(param1, param2);
        this.eventSender = param0.decorate(param0x -> {
            this.worldLoadEvent.addProperties(param0x);
            param0x.put(TelemetryProperty.WORLD_SESSION_ID, this.worldSessionId);
        });
    }

    public void tick() {
        this.performanceMetricsEvent.tick();
    }

    public void onPlayerInfoReceived(GameType param0, boolean param1) {
        this.worldLoadEvent.setGameMode(param0, param1);
        if (this.worldLoadEvent.getServerBrand() != null) {
            this.worldLoadEvent.send(this.eventSender);
        }

    }

    public void onServerBrandReceived(String param0) {
        this.worldLoadEvent.setServerBrand(param0);
        this.worldLoadEvent.send(this.eventSender);
    }

    public void setTime(long param0) {
        this.worldUnloadEvent.setTime(param0);
    }

    public void worldSessionStart() {
        this.worldLoadTimesEvent.send(this.eventSender);
        this.worldUnloadEvent.loadedWorld();
        this.performanceMetricsEvent.start();
    }

    public void onDisconnect() {
        this.worldLoadEvent.send(this.eventSender);
        this.performanceMetricsEvent.stop();
        this.worldUnloadEvent.send(this.eventSender);
    }
}
