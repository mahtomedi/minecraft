package net.minecraft.client.server;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.stats.Stats;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Snooper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class IntegratedServer extends MinecraftServer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int MIN_SIM_DISTANCE = 2;
    private final Minecraft minecraft;
    private boolean paused;
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
        RegistryAccess.RegistryHolder param2,
        LevelStorageSource.LevelStorageAccess param3,
        PackRepository param4,
        ServerResources param5,
        WorldData param6,
        MinecraftSessionService param7,
        GameProfileRepository param8,
        GameProfileCache param9,
        ChunkProgressListenerFactory param10
    ) {
        super(param0, param2, param3, param6, param4, param1.getProxy(), param1.getFixerUpper(), param5, param7, param8, param9, param10);
        this.setSingleplayerName(param1.getUser().getName());
        this.setDemo(param1.isDemo());
        this.setPlayerList(new IntegratedPlayerList(this, this.registryHolder, this.playerDataStorage));
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
        this.setMotd(this.getSingleplayerName() + " - " + this.getWorldData().getLevelName());
        return true;
    }

    @Override
    public void tickServer(BooleanSupplier param0) {
        boolean var0 = this.paused;
        this.paused = Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().isPaused();
        ProfilerFiller var1 = this.getProfiler();
        if (!var0 && this.paused) {
            var1.push("autoSave");
            LOGGER.info("Saving and pausing game...");
            this.getPlayerList().saveAll();
            this.saveAllChunks(false, false, false);
            var1.pop();
        }

        if (this.paused) {
            this.tickPaused();
        } else {
            super.tickServer(param0);
            int var2 = Math.max(2, this.minecraft.options.renderDistance);
            if (var2 != this.getPlayerList().getViewDistance()) {
                LOGGER.info("Changing view distance to {}, from {}", var2, this.getPlayerList().getViewDistance());
                this.getPlayerList().setViewDistance(var2);
            }

            int var3 = Math.max(2, this.minecraft.options.simulationDistance);
            if (var3 != this.previousSimulationDistance) {
                LOGGER.info("Changing simulation distance to {}, from {}", var3, this.previousSimulationDistance);
                this.getPlayerList().setSimulationDistance(var3);
                this.previousSimulationDistance = var3;
            }

        }
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
        this.minecraft.delayCrash(param0);
    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport param0) {
        param0.setDetail("Type", "Integrated Server (map_client.txt)");
        param0.setDetail("Is Modded", () -> this.getModdedStatus().orElse("Probably not. Jar signature remains and both client + server brands are untouched."));
        return param0;
    }

    @Override
    public Optional<String> getModdedStatus() {
        String var0 = ClientBrandRetriever.getClientModName();
        if (!var0.equals("vanilla")) {
            return Optional.of("Definitely; Client brand changed to '" + var0 + "'");
        } else {
            var0 = this.getServerModName();
            if (!"vanilla".equals(var0)) {
                return Optional.of("Definitely; Server brand changed to '" + var0 + "'");
            } else {
                return Minecraft.class.getSigners() == null ? Optional.of("Very likely; Jar signature invalidated") : Optional.empty();
            }
        }
    }

    @Override
    public void populateSnooper(Snooper param0) {
        super.populateSnooper(param0);
        param0.setDynamicData("snooper_partner", this.minecraft.getSnooper().getToken());
    }

    @Override
    public boolean isSnooperEnabled() {
        return Minecraft.getInstance().isSnooperEnabled();
    }

    @Override
    public boolean publishServer(@Nullable GameType param0, boolean param1, int param2) {
        try {
            this.minecraft.prepareForMultiplayer();
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
        return param0.getName().equalsIgnoreCase(this.getSingleplayerName());
    }

    @Override
    public int getScaledTrackingDistance(int param0) {
        return (int)(this.minecraft.options.entityDistanceScaling * (float)param0);
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
