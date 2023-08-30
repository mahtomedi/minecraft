package net.minecraft.client.telemetry;

import java.time.Duration;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.telemetry.events.PerformanceMetricsEvent;
import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraft.client.telemetry.events.WorldLoadTimesEvent;
import net.minecraft.client.telemetry.events.WorldUnloadEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
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

    public WorldSessionTelemetryManager(TelemetryEventSender param0, boolean param1, @Nullable Duration param2, @Nullable String param3) {
        this.worldLoadEvent = new WorldLoadEvent(param3);
        this.performanceMetricsEvent = new PerformanceMetricsEvent();
        this.worldLoadTimesEvent = new WorldLoadTimesEvent(param1, param2);
        this.eventSender = param0.decorate(param0x -> {
            this.worldLoadEvent.addProperties(param0x);
            param0x.put(TelemetryProperty.WORLD_SESSION_ID, this.worldSessionId);
        });
    }

    public void tick() {
        this.performanceMetricsEvent.tick(this.eventSender);
    }

    public void onPlayerInfoReceived(GameType param0, boolean param1) {
        this.worldLoadEvent.setGameMode(param0, param1);
        this.worldUnloadEvent.onPlayerInfoReceived();
        this.worldSessionStart();
    }

    public void onServerBrandReceived(String param0) {
        this.worldLoadEvent.setServerBrand(param0);
        this.worldSessionStart();
    }

    public void setTime(long param0) {
        this.worldUnloadEvent.setTime(param0);
    }

    public void worldSessionStart() {
        if (this.worldLoadEvent.send(this.eventSender)) {
            this.worldLoadTimesEvent.send(this.eventSender);
            this.performanceMetricsEvent.start();
        }

    }

    public void onDisconnect() {
        this.worldLoadEvent.send(this.eventSender);
        this.performanceMetricsEvent.stop();
        this.worldUnloadEvent.send(this.eventSender);
    }

    public void onAdvancementDone(Level param0, AdvancementHolder param1) {
        ResourceLocation var0 = param1.id();
        if (param1.value().sendsTelemetryEvent() && "minecraft".equals(var0.getNamespace())) {
            long var1 = param0.getGameTime();
            this.eventSender.send(TelemetryEventType.ADVANCEMENT_MADE, param2 -> {
                param2.put(TelemetryProperty.ADVANCEMENT_ID, var0.toString());
                param2.put(TelemetryProperty.ADVANCEMENT_GAME_TIME, var1);
            });
        }

    }
}
