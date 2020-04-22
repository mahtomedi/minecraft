package net.minecraft.client.server;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.Crypt;
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
    private final Minecraft minecraft;
    private boolean paused;
    private int publishedPort = -1;
    private LanServerPinger lanPinger;
    private UUID uuid;

    public IntegratedServer(
        Minecraft param0,
        LevelStorageSource.LevelStorageAccess param1,
        WorldData param2,
        MinecraftSessionService param3,
        GameProfileRepository param4,
        GameProfileCache param5,
        ChunkProgressListenerFactory param6
    ) {
        super(param1, param2, param0.getProxy(), param0.getFixerUpper(), new Commands(false), param3, param4, param5, param6);
        this.setSingleplayerName(param0.getUser().getName());
        this.setDemo(param0.isDemo());
        this.setMaxBuildHeight(256);
        this.setPlayerList(new IntegratedPlayerList(this, this.playerDataStorage));
        this.minecraft = param0;
    }

    @Override
    public boolean initServer() {
        LOGGER.info("Starting integrated minecraft server version " + SharedConstants.getCurrentVersion().getName());
        this.setUsesAuthentication(true);
        this.setPvpAllowed(true);
        this.setFlightAllowed(true);
        LOGGER.info("Generating keypair");
        this.setKeyPair(Crypt.generateKeyPair());
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

        if (!this.paused) {
            super.tickServer(param0);
            int var2 = Math.max(2, this.minecraft.options.renderDistance + -1);
            if (var2 != this.getPlayerList().getViewDistance()) {
                LOGGER.info("Changing view distance to {}, from {}", var2, this.getPlayerList().getViewDistance());
                this.getPlayerList().setViewDistance(var2);
            }

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
    public boolean isEpollEnabled() {
        return false;
    }

    @Override
    public void onServerCrash(CrashReport param0) {
        this.minecraft.delayCrash(param0);
    }

    @Override
    public CrashReport fillReport(CrashReport param0) {
        param0 = super.fillReport(param0);
        param0.getSystemDetails().setDetail("Type", "Integrated Server (map_client.txt)");
        param0.getSystemDetails()
            .setDetail("Is Modded", () -> this.getModdedStatus().orElse("Probably not. Jar signature remains and both client + server brands are untouched."));
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
    public boolean publishServer(GameType param0, boolean param1, int param2) {
        try {
            this.getConnection().startTcpServerListener(null, param2);
            LOGGER.info("Started serving on {}", param2);
            this.publishedPort = param2;
            this.lanPinger = new LanServerPinger(this.getMotd(), param2 + "");
            this.lanPinger.start();
            this.getPlayerList().setOverrideGameMode(param0);
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
        this.getPlayerList().setOverrideGameMode(param0);
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
}
