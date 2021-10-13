package net.minecraft.client;

import com.mojang.authlib.minecraft.TelemetryEvent;
import com.mojang.authlib.minecraft.TelemetryPropertyContainer;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.authlib.minecraft.UserApiService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.WorldVersion;
import net.minecraft.util.TelemetryConstants;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientTelemetryManager {
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger(1);
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor(param0 -> {
        Thread var0 = new Thread(param0);
        var0.setName("Telemetry-Sender-#" + THREAD_COUNT.getAndIncrement());
        return var0;
    });
    private final Minecraft minecraft;
    private final TelemetrySession telemetrySession;
    private boolean worldLoadEventSent;
    @Nullable
    private ClientTelemetryManager.PlayerInfo playerInfo;
    @Nullable
    private String serverBrand;

    public ClientTelemetryManager(Minecraft param0, UserApiService param1, Optional<String> param2, Optional<String> param3, UUID param4) {
        this.minecraft = param0;
        if (!SharedConstants.IS_RUNNING_IN_IDE) {
            this.telemetrySession = param1.newTelemetrySession(EXECUTOR);
            TelemetryPropertyContainer var0 = this.telemetrySession.globalProperties();
            addOptionalProperty("UserId", param2, var0);
            addOptionalProperty("ClientId", param3, var0);
            var0.addProperty("deviceSessionId", param4.toString());
            var0.addProperty("WorldSessionId", UUID.randomUUID().toString());
            this.telemetrySession
                .eventSetupFunction(param0x -> param0x.addProperty("eventTimestampUtc", TelemetryConstants.TIMESTAMP_FORMATTER.format(Instant.now())));
        } else {
            this.telemetrySession = TelemetrySession.DISABLED;
        }

    }

    private static void addOptionalProperty(String param0, Optional<String> param1, TelemetryPropertyContainer param2) {
        param1.ifPresentOrElse(param2x -> param2.addProperty(param0, param2x), () -> param2.addNullProperty(param0));
    }

    public void onPlayerInfoReceived(GameType param0, boolean param1) {
        this.playerInfo = new ClientTelemetryManager.PlayerInfo(param0, param1);
        if (this.serverBrand != null) {
            this.sendWorldLoadEvent(this.playerInfo);
        }

    }

    public void onServerBrandReceived(String param0) {
        this.serverBrand = param0;
        if (this.playerInfo != null) {
            this.sendWorldLoadEvent(this.playerInfo);
        }

    }

    private void sendWorldLoadEvent(ClientTelemetryManager.PlayerInfo param0) {
        if (!this.worldLoadEventSent) {
            this.worldLoadEventSent = true;
            if (this.telemetrySession.isEnabled()) {
                TelemetryEvent var0 = this.telemetrySession.createNewEvent("WorldLoaded");
                WorldVersion var1 = SharedConstants.getCurrentVersion();
                var0.addProperty("build_display_name", var1.getId());
                var0.addProperty("clientModded", Minecraft.checkModStatus().shouldReportAsModified());
                if (this.serverBrand != null) {
                    var0.addProperty("serverModded", !this.serverBrand.equals("vanilla"));
                } else {
                    var0.addNullProperty("serverModded");
                }

                var0.addProperty("server_type", this.getServerType());
                var0.addProperty("BuildPlat", Util.getPlatform().telemetryName());
                var0.addProperty("Plat", System.getProperty("os.name"));
                var0.addProperty("javaVersion", System.getProperty("java.version"));
                var0.addProperty("PlayerGameMode", param0.getGameModeId());
                var0.send();
            }
        }
    }

    private String getServerType() {
        if (this.minecraft.isConnectedToRealms()) {
            return "realm";
        } else {
            return this.minecraft.hasSingleplayerServer() ? "local" : "server";
        }
    }

    public void onDisconnect() {
        if (this.playerInfo != null) {
            this.sendWorldLoadEvent(this.playerInfo);
        }

    }

    @OnlyIn(Dist.CLIENT)
    static record PlayerInfo(GameType gameType, boolean hardcore) {
        public int getGameModeId() {
            if (this.hardcore && this.gameType == GameType.SURVIVAL) {
                return 99;
            } else {
                return switch(this.gameType) {
                    case SURVIVAL -> 0;
                    case CREATIVE -> 1;
                    case ADVENTURE -> 2;
                    case SPECTATOR -> 6;
                };
            }
        }
    }
}
