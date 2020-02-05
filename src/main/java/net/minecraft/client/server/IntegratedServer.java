package net.minecraft.client.server;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
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
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.Crypt;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Snooper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class IntegratedServer extends MinecraftServer {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft minecraft;
    private final LevelSettings settings;
    private boolean paused;
    private int publishedPort = -1;
    private LanServerPinger lanPinger;
    private UUID uuid;

    public IntegratedServer(
        Minecraft param0,
        String param1,
        String param2,
        LevelSettings param3,
        YggdrasilAuthenticationService param4,
        MinecraftSessionService param5,
        GameProfileRepository param6,
        GameProfileCache param7,
        ChunkProgressListenerFactory param8
    ) {
        super(
            new File(param0.gameDirectory, "saves"),
            param0.getProxy(),
            param0.getFixerUpper(),
            new Commands(false),
            param4,
            param5,
            param6,
            param7,
            param8,
            param1
        );
        this.setSingleplayerName(param0.getUser().getName());
        this.setLevelName(param2);
        this.setDemo(param0.isDemo());
        this.setBonusChest(param3.hasStartingBonusItems());
        this.setMaxBuildHeight(256);
        this.setPlayerList(new IntegratedPlayerList(this));
        this.minecraft = param0;
        this.settings = this.isDemo() ? MinecraftServer.DEMO_SETTINGS : param3;
    }

    @Override
    public void loadLevel(String param0, String param1, long param2, LevelType param3, JsonElement param4) {
        this.ensureLevelConversion(param0);
        LevelStorage var0 = this.getStorageSource().selectLevel(param0, this);
        this.detectBundledResources(this.getLevelIdName(), var0);
        LevelData var1 = var0.prepareLevel();
        if (var1 == null) {
            var1 = new LevelData(this.settings, param1);
        } else {
            var1.setLevelName(param1);
        }

        var1.setModdedInfo(this.getServerModName(), this.getModdedStatus().isPresent());
        this.loadDataPacks(var0.getFolder(), var1);
        ChunkProgressListener var2 = this.progressListenerFactory.create(11);
        this.createLevels(var0, var1, this.settings, var2);
        if (this.getLevel(DimensionType.OVERWORLD).getLevelData().getDifficulty() == null) {
            this.setDifficulty(this.minecraft.options.difficulty, true);
        }

        this.prepareLevels(var2);
    }

    @Override
    public boolean initServer() throws IOException {
        LOGGER.info("Starting integrated minecraft server version " + SharedConstants.getCurrentVersion().getName());
        this.setUsesAuthentication(true);
        this.setAnimals(true);
        this.setNpcsEnabled(true);
        this.setPvpAllowed(true);
        this.setFlightAllowed(true);
        LOGGER.info("Generating keypair");
        this.setKeyPair(Crypt.generateKeyPair());
        this.loadLevel(this.getLevelIdName(), this.getLevelName(), this.settings.getSeed(), this.settings.getLevelType(), this.settings.getLevelTypeOptions());
        this.setMotd(this.getSingleplayerName() + " - " + this.getLevel(DimensionType.OVERWORLD).getLevelData().getLevelName());
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
    public boolean canGenerateStructures() {
        return false;
    }

    @Override
    public GameType getDefaultGameType() {
        return this.settings.getGameType();
    }

    @Override
    public Difficulty getDefaultDifficulty() {
        return this.minecraft.level.getLevelData().getDifficulty();
    }

    @Override
    public boolean isHardcore() {
        return this.settings.isHardcore();
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
    public void setDefaultGameMode(GameType param0) {
        super.setDefaultGameMode(param0);
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
