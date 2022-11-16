package net.minecraft.client.telemetry.events;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.TelemetryPropertyMap;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldLoadEvent implements TelemetryEventProducer {
    private final WorldLoadEvent.WorldLoadEventCallbacks callbacks;
    private boolean eventSent;
    @Nullable
    private TelemetryProperty.GameMode gameMode = null;
    @Nullable
    private String serverBrand;

    public WorldLoadEvent(WorldLoadEvent.WorldLoadEventCallbacks param0) {
        this.callbacks = param0;
    }

    public void addProperties(TelemetryPropertyMap.Builder param0) {
        if (this.serverBrand != null) {
            param0.put(TelemetryProperty.SERVER_MODDED, !this.serverBrand.equals("vanilla"));
        }

        param0.put(TelemetryProperty.SERVER_TYPE, this.getServerType());
    }

    private TelemetryProperty.ServerType getServerType() {
        if (Minecraft.getInstance().isConnectedToRealms()) {
            return TelemetryProperty.ServerType.REALM;
        } else {
            return Minecraft.getInstance().hasSingleplayerServer() ? TelemetryProperty.ServerType.LOCAL : TelemetryProperty.ServerType.OTHER;
        }
    }

    @Override
    public void send(TelemetryEventSender param0) {
        if (!this.eventSent && this.gameMode != null) {
            this.eventSent = true;
            this.callbacks.onWorldLoadSent();
            param0.send(TelemetryEventType.WORLD_LOADED, param0x -> param0x.put(TelemetryProperty.GAME_MODE, this.gameMode));
        }
    }

    public void setGameMode(GameType param0, boolean param1) {
        this.gameMode = switch(param0) {
            case SURVIVAL -> param1 ? TelemetryProperty.GameMode.HARDCORE : TelemetryProperty.GameMode.SURVIVAL;
            case CREATIVE -> TelemetryProperty.GameMode.CREATIVE;
            case ADVENTURE -> TelemetryProperty.GameMode.ADVENTURE;
            case SPECTATOR -> TelemetryProperty.GameMode.SPECTATOR;
        };
    }

    public void setServerBrand(String param0) {
        this.serverBrand = param0;
    }

    @Nullable
    public String getServerBrand() {
        return this.serverBrand;
    }

    @OnlyIn(Dist.CLIENT)
    public interface WorldLoadEventCallbacks {
        void onWorldLoadSent();
    }
}
