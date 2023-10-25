package net.minecraft.client.server;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.stats.Stats;
import net.minecraft.util.ModCheck;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class IntegratedServer extends MinecraftServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MIN_SIM_DISTANCE = 2;
    private final Minecraft minecraft;
    private boolean paused = true;
    private int publishedPort = -1;
    @Nullable
    private GameType publishedGameType;
    @Nullable
    private LanServerPinger lanPinger;
    @Nullable
    private UUID uuid;
    private int previousSimulationDistance = 0;

    public IntegratedServer(
        Thread param0,
        Minecraft param1,
        LevelStorageSource.LevelStorageAccess param2,
        PackRepository param3,
        WorldStem param4,
        Services param5,
        ChunkProgressListenerFactory param6
    ) {
        super(param0, param2, param3, param4, param1.getProxy(), param1.getFixerUpper(), param5, param6);
        this.setSingleplayerProfile(param1.getGameProfile());
        this.setDemo(param1.isDemo());
        this.setPlayerList(new IntegratedPlayerList(this, this.registries(), this.playerDataStorage));
        this.minecraft = param1;
    }

    @Override
    public boolean initServer() {
        LOGGER.info("Starting integrated minecraft server version {}", SharedConstants.getCurrentVersion().getName());
        this.setUsesAuthentication(true);
        this.setPvpAllowed(true);
        this.setFlightAllowed(true);
        this.initializeKeyPair();
        this.loadLevel();
        GameProfile var0 = this.getSingleplayerProfile();
        String var1 = this.getWorldData().getLevelName();
        this.setMotd(var0 != null ? var0.getName() + " - " + var1 : var1);
        return true;
    }

    @Override
    public boolean isPaused() {
        return this.paused;
    }

    @Override
    public void tickServer(BooleanSupplier param0) {
        boolean var0 = this.paused;
        this.paused = Minecraft.getInstance().isPaused();
        ProfilerFiller var1 = this.getProfiler();
        if (!var0 && this.paused) {
            var1.push("autoSave");
            LOGGER.info("Saving and pausing game...");
            this.saveEverything(false, false, false);
            var1.pop();
        }

        boolean var2 = Minecraft.getInstance().getConnection() != null;
        if (var2 && this.paused) {
            this.tickPaused();
        } else {
            if (var0 && !this.paused) {
                this.forceTimeSynchronization();
            }

            super.tickServer(param0);
            int var3 = Math.max(2, this.minecraft.options.renderDistance().get());
            if (var3 != this.getPlayerList().getViewDistance()) {
                LOGGER.info("Changing view distance to {}, from {}", var3, this.getPlayerList().getViewDistance());
                this.getPlayerList().setViewDistance(var3);
            }

            int var4 = Math.max(2, this.minecraft.options.simulationDistance().get());
            if (var4 != this.previousSimulationDistance) {
                LOGGER.info("Changing simulation distance to {}, from {}", var4, this.previousSimulationDistance);
                this.getPlayerList().setSimulationDistance(var4);
                this.previousSimulationDistance = var4;
            }

        }
    }

    @Override
    public void logTickTime(long param0) {
        this.minecraft.getDebugOverlay().logTickDuration(param0);
    }

    private void tickPaused() {
        for(ServerPlayer var0 : this.getPlayerList().getPlayers()) {
            var0.awardStat(Stats.TOTAL_WORLD_TIME);
        }

    }

    @Override
    public boolean shouldRconBroadcast() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return true;
    }

    @Override
    public File getServerDirectory() {
        return this.minecraft.gameDirectory;
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public int getRateLimitPacketsPerSecond() {
        return 0;
    }

    @Override
    public boolean isEpollEnabled() {
        return false;
    }

    @Override
    public void onServerCrash(CrashReport param0) {
        this.minecraft.delayCrashRaw(param0);
    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport param0) {
        param0.setDetail("Type", "Integrated Server (map_client.txt)");
        param0.setDetail("Is Modded", () -> this.getModdedStatus().fullDescription());
        param0.setDetail("Launched Version", this.minecraft::getLaunchedVersion);
        return param0;
    }

    @Override
    public ModCheck getModdedStatus() {
        return Minecraft.checkModStatus().merge(super.getModdedStatus());
    }

    @Override
    public boolean publishServer(@Nullable GameType param0, boolean param1, int param2) {
        try {
            this.minecraft.prepareForMultiplayer();
            this.minecraft.getProfileKeyPairManager().prepareKeyPair().thenAcceptAsync(param0x -> param0x.ifPresent(param0xx -> {
                    ClientPacketListener var0x = this.minecraft.getConnection();
                    if (var0x != null) {
                        var0x.setKeyPair(param0xx);
                    }

                }), this.minecraft);
            this.getConnection().startTcpServerListener(null, param2);
            LOGGER.info("Started serving on {}", param2);
            this.publishedPort = param2;
            this.lanPinger = new LanServerPinger(this.getMotd(), param2 + "");
            this.lanPinger.start();
            this.publishedGameType = param0;
            this.getPlayerList().setAllowCheatsForAllPlayers(param1);
            int var0 = this.getProfilePermissions(this.minecraft.player.getGameProfile());
            this.minecraft.player.setPermissionLevel(var0);

            for(ServerPlayer var1 : this.getPlayerList().getPlayers()) {
                this.getCommands().sendCommands(var1);
            }

            return true;
        } catch (IOException var7) {
            return false;
        }
    }

    @Override
    public void stopServer() {
        super.stopServer();
        if (this.lanPinger != null) {
            this.lanPinger.interrupt();
            this.lanPinger = null;
        }

    }

    @Override
    public void halt(boolean param0) {
        this.executeBlocking(() -> {
            for(ServerPlayer var1x : Lists.newArrayList(this.getPlayerList().getPlayers())) {
                if (!var1x.getUUID().equals(this.uuid)) {
                    this.getPlayerList().remove(var1x);
                }
            }

        });
        super.halt(param0);
        if (this.lanPinger != null) {
            this.lanPinger.interrupt();
            this.lanPinger = null;
        }

    }

    @Override
    public boolean isPublished() {
        return this.publishedPort > -1;
    }

    @Override
    public int getPort() {
        return this.publishedPort;
    }

    @Override
    public void setDefaultGameType(GameType param0) {
        super.setDefaultGameType(param0);
        this.publishedGameType = null;
    }

    @Override
    public boolean isCommandBlockEnabled() {
        return true;
    }

    @Override
    public int getOperatorUserPermissionLevel() {
        return 2;
    }

    @Override
    public int getFunctionCompilationLevel() {
        return 2;
    }

    public void setUUID(UUID param0) {
        this.uuid = param0;
    }

    @Override
    public boolean isSingleplayerOwner(GameProfile param0) {
        return this.getSingleplayerProfile() != null && param0.getName().equalsIgnoreCase(this.getSingleplayerProfile().getName());
    }

    @Override
    public int getScaledTrackingDistance(int param0) {
        return (int)(this.minecraft.options.entityDistanceScaling().get() * (double)param0);
    }

    @Override
    public boolean forceSynchronousWrites() {
        return this.minecraft.options.syncWrites;
    }

    @Nullable
    @Override
    public GameType getForcedGameType() {
        return this.isPublished() ? MoreObjects.firstNonNull(this.publishedGameType, this.worldData.getGameType()) : null;
    }
}
