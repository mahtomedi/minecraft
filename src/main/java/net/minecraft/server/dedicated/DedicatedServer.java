package net.minecraft.server.dedicated;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ConsoleInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.ServerResources;
import net.minecraft.server.gui.MinecraftServerGui;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.network.TextFilterClient;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.server.rcon.RconConsoleSource;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import net.minecraft.server.rcon.thread.RconThread;
import net.minecraft.util.Mth;
import net.minecraft.util.monitoring.jmx.MinecraftServerStatistics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class DedicatedServer extends MinecraftServer implements ServerInterface {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int CONVERSION_RETRY_DELAY_MS = 5000;
    private static final int CONVERSION_RETRIES = 2;
    private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
    private final List<ConsoleInput> consoleInput = Collections.synchronizedList(Lists.newArrayList());
    @Nullable
    private QueryThreadGs4 queryThreadGs4;
    private final RconConsoleSource rconConsoleSource;
    @Nullable
    private RconThread rconThread;
    private final DedicatedServerSettings settings;
    @Nullable
    private MinecraftServerGui gui;
    @Nullable
    private final TextFilterClient textFilterClient;
    @Nullable
    private final Component resourcePackPrompt;

    public DedicatedServer(
        Thread param0,
        RegistryAccess.RegistryHolder param1,
        LevelStorageSource.LevelStorageAccess param2,
        PackRepository param3,
        ServerResources param4,
        WorldData param5,
        DedicatedServerSettings param6,
        DataFixer param7,
        MinecraftSessionService param8,
        GameProfileRepository param9,
        GameProfileCache param10,
        ChunkProgressListenerFactory param11
    ) {
        super(param0, param1, param2, param5, param3, Proxy.NO_PROXY, param7, param4, param8, param9, param10, param11);
        this.settings = param6;
        this.rconConsoleSource = new RconConsoleSource(this);
        this.textFilterClient = TextFilterClient.createFromConfig(param6.getProperties().textFilteringConfig);
        this.resourcePackPrompt = parseResourcePackPrompt(param6);
    }

    @Override
    public boolean initServer() throws IOException {
        Thread var0 = new Thread("Server console handler") {
            @Override
            public void run() {
                BufferedReader var0 = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

                String var1;
                try {
                    while(!DedicatedServer.this.isStopped() && DedicatedServer.this.isRunning() && (var1 = var0.readLine()) != null) {
                        DedicatedServer.this.handleConsoleInput(var1, DedicatedServer.this.createCommandSourceStack());
                    }
                } catch (IOException var4) {
                    DedicatedServer.LOGGER.error("Exception handling console input", (Throwable)var4);
                }

            }
        };
        var0.setDaemon(true);
        var0.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        var0.start();
        LOGGER.info("Starting minecraft server version {}", SharedConstants.getCurrentVersion().getName());
        if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
            LOGGER.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
        }

        LOGGER.info("Loading properties");
        DedicatedServerProperties var1 = this.settings.getProperties();
        if (this.isSingleplayer()) {
            this.setLocalIp("127.0.0.1");
        } else {
            this.setUsesAuthentication(var1.onlineMode);
            this.setPreventProxyConnections(var1.preventProxyConnections);
            this.setLocalIp(var1.serverIp);
        }

        this.setPvpAllowed(var1.pvp);
        this.setFlightAllowed(var1.allowFlight);
        this.setResourcePack(var1.resourcePack, this.getPackHash());
        this.setMotd(var1.motd);
        super.setPlayerIdleTimeout(var1.playerIdleTimeout.get());
        this.setEnforceWhitelist(var1.enforceWhitelist);
        this.worldData.setGameType(var1.gamemode);
        LOGGER.info("Default game type: {}", var1.gamemode);
        InetAddress var2 = null;
        if (!this.getLocalIp().isEmpty()) {
            var2 = InetAddress.getByName(this.getLocalIp());
        }

        if (this.getPort() < 0) {
            this.setPort(var1.serverPort);
        }

        this.initializeKeyPair();
        LOGGER.info("Starting Minecraft server on {}:{}", this.getLocalIp().isEmpty() ? "*" : this.getLocalIp(), this.getPort());

        try {
            this.getConnection().startTcpServerListener(var2, this.getPort());
        } catch (IOException var10) {
            LOGGER.warn("**** FAILED TO BIND TO PORT!");
            LOGGER.warn("The exception was: {}", var10.toString());
            LOGGER.warn("Perhaps a server is already running on that port?");
            return false;
        }

        if (!this.usesAuthentication()) {
            LOGGER.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
            LOGGER.warn("The server will make no attempt to authenticate usernames. Beware.");
            LOGGER.warn(
                "While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose."
            );
            LOGGER.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
        }

        if (this.convertOldUsers()) {
            this.getProfileCache().save();
        }

        if (!OldUsersConverter.serverReadyAfterUserconversion(this)) {
            return false;
        } else {
            this.setPlayerList(new DedicatedPlayerList(this, this.registryHolder, this.playerDataStorage));
            long var4 = Util.getNanos();
            SkullBlockEntity.setup(this.getProfileCache(), this.getSessionService(), this);
            GameProfileCache.setUsesAuthentication(this.usesAuthentication());
            LOGGER.info("Preparing level \"{}\"", this.getLevelIdName());
            this.loadLevel();
            long var5 = Util.getNanos() - var4;
            String var6 = String.format(Locale.ROOT, "%.3fs", (double)var5 / 1.0E9);
            LOGGER.info("Done ({})! For help, type \"help\"", var6);
            if (var1.announcePlayerAchievements != null) {
                this.getGameRules().getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(var1.announcePlayerAchievements, this);
            }

            if (var1.enableQuery) {
                LOGGER.info("Starting GS4 status listener");
                this.queryThreadGs4 = QueryThreadGs4.create(this);
            }

            if (var1.enableRcon) {
                LOGGER.info("Starting remote control listener");
                this.rconThread = RconThread.create(this);
            }

            if (this.getMaxTickLength() > 0L) {
                Thread var7 = new Thread(new ServerWatchdog(this));
                var7.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(LOGGER));
                var7.setName("Server Watchdog");
                var7.setDaemon(true);
                var7.start();
            }

            Items.AIR.fillItemCategory(CreativeModeTab.TAB_SEARCH, NonNullList.create());
            if (var1.enableJmxMonitoring) {
                MinecraftServerStatistics.registerJmxMonitoring(this);
                LOGGER.info("JMX monitoring enabled");
            }

            return true;
        }
    }

    @Override
    public boolean isSpawningAnimals() {
        return this.getProperties().spawnAnimals && super.isSpawningAnimals();
    }

    @Override
    public boolean isSpawningMonsters() {
        return this.settings.getProperties().spawnMonsters && super.isSpawningMonsters();
    }

    @Override
    public boolean areNpcsEnabled() {
        return this.settings.getProperties().spawnNpcs && super.areNpcsEnabled();
    }

    public String getPackHash() {
        DedicatedServerProperties var0 = this.settings.getProperties();
        String var1;
        if (!var0.resourcePackSha1.isEmpty()) {
            var1 = var0.resourcePackSha1;
            if (!Strings.isNullOrEmpty(var0.resourcePackHash)) {
                LOGGER.warn("resource-pack-hash is deprecated and found along side resource-pack-sha1. resource-pack-hash will be ignored.");
            }
        } else if (!Strings.isNullOrEmpty(var0.resourcePackHash)) {
            LOGGER.warn("resource-pack-hash is deprecated. Please use resource-pack-sha1 instead.");
            var1 = var0.resourcePackHash;
        } else {
            var1 = "";
        }

        if (!var1.isEmpty() && !SHA1.matcher(var1).matches()) {
            LOGGER.warn("Invalid sha1 for ressource-pack-sha1");
        }

        if (!var0.resourcePack.isEmpty() && var1.isEmpty()) {
            LOGGER.warn(
                "You specified a resource pack without providing a sha1 hash. Pack will be updated on the client only if you change the name of the pack."
            );
        }

        return var1;
    }

    @Override
    public DedicatedServerProperties getProperties() {
        return this.settings.getProperties();
    }

    @Override
    public void forceDifficulty() {
        this.setDifficulty(this.getProperties().difficulty, true);
    }

    @Override
    public boolean isHardcore() {
        return this.getProperties().hardcore;
    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport param0) {
        param0.setDetail("Is Modded", () -> this.getModdedStatus().fullDescription());
        param0.setDetail("Type", () -> "Dedicated Server (map_server.txt)");
        return param0;
    }

    @Override
    public void dumpServerProperties(Path param0) throws IOException {
        DedicatedServerProperties var0 = this.getProperties();

        try (Writer var1 = Files.newBufferedWriter(param0)) {
            var1.write(String.format("sync-chunk-writes=%s%n", var0.syncChunkWrites));
            var1.write(String.format("gamemode=%s%n", var0.gamemode));
            var1.write(String.format("spawn-monsters=%s%n", var0.spawnMonsters));
            var1.write(String.format("entity-broadcast-range-percentage=%d%n", var0.entityBroadcastRangePercentage));
            var1.write(String.format("max-world-size=%d%n", var0.maxWorldSize));
            var1.write(String.format("spawn-npcs=%s%n", var0.spawnNpcs));
            var1.write(String.format("view-distance=%d%n", var0.viewDistance));
            var1.write(String.format("simulation-distance=%d%n", var0.simulationDistance));
            var1.write(String.format("spawn-animals=%s%n", var0.spawnAnimals));
            var1.write(String.format("generate-structures=%s%n", var0.getWorldGenSettings(this.registryHolder).generateFeatures()));
            var1.write(String.format("use-native=%s%n", var0.useNativeTransport));
            var1.write(String.format("rate-limit=%d%n", var0.rateLimitPacketsPerSecond));
        }

    }

    @Override
    public void onServerExit() {
        if (this.textFilterClient != null) {
            this.textFilterClient.close();
        }

        if (this.gui != null) {
            this.gui.close();
        }

        if (this.rconThread != null) {
            this.rconThread.stop();
        }

        if (this.queryThreadGs4 != null) {
            this.queryThreadGs4.stop();
        }

    }

    @Override
    public void tickChildren(BooleanSupplier param0) {
        super.tickChildren(param0);
        this.handleConsoleInputs();
    }

    @Override
    public boolean isNetherEnabled() {
        return this.getProperties().allowNether;
    }

    public void handleConsoleInput(String param0, CommandSourceStack param1) {
        this.consoleInput.add(new ConsoleInput(param0, param1));
    }

    public void handleConsoleInputs() {
        while(!this.consoleInput.isEmpty()) {
            ConsoleInput var0 = this.consoleInput.remove(0);
            this.getCommands().performCommand(var0.source, var0.msg);
        }

    }

    @Override
    public boolean isDedicatedServer() {
        return true;
    }

    @Override
    public int getRateLimitPacketsPerSecond() {
        return this.getProperties().rateLimitPacketsPerSecond;
    }

    @Override
    public boolean isEpollEnabled() {
        return this.getProperties().useNativeTransport;
    }

    public DedicatedPlayerList getPlayerList() {
        return (DedicatedPlayerList)super.getPlayerList();
    }

    @Override
    public boolean isPublished() {
        return true;
    }

    @Override
    public String getServerIp() {
        return this.getLocalIp();
    }

    @Override
    public int getServerPort() {
        return this.getPort();
    }

    @Override
    public String getServerName() {
        return this.getMotd();
    }

    public void showGui() {
        if (this.gui == null) {
            this.gui = MinecraftServerGui.showFrameFor(this);
        }

    }

    @Override
    public boolean hasGui() {
        return this.gui != null;
    }

    @Override
    public boolean isCommandBlockEnabled() {
        return this.getProperties().enableCommandBlock;
    }

    @Override
    public int getSpawnProtectionRadius() {
        return this.getProperties().spawnProtection;
    }

    @Override
    public boolean isUnderSpawnProtection(ServerLevel param0, BlockPos param1, Player param2) {
        if (param0.dimension() != Level.OVERWORLD) {
            return false;
        } else if (this.getPlayerList().getOps().isEmpty()) {
            return false;
        } else if (this.getPlayerList().isOp(param2.getGameProfile())) {
            return false;
        } else if (this.getSpawnProtectionRadius() <= 0) {
            return false;
        } else {
            BlockPos var0 = param0.getSharedSpawnPos();
            int var1 = Mth.abs(param1.getX() - var0.getX());
            int var2 = Mth.abs(param1.getZ() - var0.getZ());
            int var3 = Math.max(var1, var2);
            return var3 <= this.getSpawnProtectionRadius();
        }
    }

    @Override
    public boolean repliesToStatus() {
        return this.getProperties().enableStatus;
    }

    @Override
    public boolean hidesOnlinePlayers() {
        return this.getProperties().hideOnlinePlayers;
    }

    @Override
    public int getOperatorUserPermissionLevel() {
        return this.getProperties().opPermissionLevel;
    }

    @Override
    public int getFunctionCompilationLevel() {
        return this.getProperties().functionPermissionLevel;
    }

    @Override
    public void setPlayerIdleTimeout(int param0) {
        super.setPlayerIdleTimeout(param0);
        this.settings.update(param1 -> param1.playerIdleTimeout.update(this.registryAccess(), param0));
    }

    @Override
    public boolean shouldRconBroadcast() {
        return this.getProperties().broadcastRconToOps;
    }

    @Override
    public boolean shouldInformAdmins() {
        return this.getProperties().broadcastConsoleToOps;
    }

    @Override
    public int getAbsoluteMaxWorldSize() {
        return this.getProperties().maxWorldSize;
    }

    @Override
    public int getCompressionThreshold() {
        return this.getProperties().networkCompressionThreshold;
    }

    protected boolean convertOldUsers() {
        boolean var0 = false;

        for(int var1 = 0; !var0 && var1 <= 2; ++var1) {
            if (var1 > 0) {
                LOGGER.warn("Encountered a problem while converting the user banlist, retrying in a few seconds");
                this.waitForRetry();
            }

            var0 = OldUsersConverter.convertUserBanlist(this);
        }

        boolean var2 = false;

        for(int var7 = 0; !var2 && var7 <= 2; ++var7) {
            if (var7 > 0) {
                LOGGER.warn("Encountered a problem while converting the ip banlist, retrying in a few seconds");
                this.waitForRetry();
            }

            var2 = OldUsersConverter.convertIpBanlist(this);
        }

        boolean var3 = false;

        for(int var8 = 0; !var3 && var8 <= 2; ++var8) {
            if (var8 > 0) {
                LOGGER.warn("Encountered a problem while converting the op list, retrying in a few seconds");
                this.waitForRetry();
            }

            var3 = OldUsersConverter.convertOpsList(this);
        }

        boolean var4 = false;

        for(int var9 = 0; !var4 && var9 <= 2; ++var9) {
            if (var9 > 0) {
                LOGGER.warn("Encountered a problem while converting the whitelist, retrying in a few seconds");
                this.waitForRetry();
            }

            var4 = OldUsersConverter.convertWhiteList(this);
        }

        boolean var5 = false;

        for(int var10 = 0; !var5 && var10 <= 2; ++var10) {
            if (var10 > 0) {
                LOGGER.warn("Encountered a problem while converting the player save files, retrying in a few seconds");
                this.waitForRetry();
            }

            var5 = OldUsersConverter.convertPlayers(this);
        }

        return var0 || var2 || var3 || var4 || var5;
    }

    private void waitForRetry() {
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException var2) {
        }
    }

    public long getMaxTickLength() {
        return this.getProperties().maxTickTime;
    }

    @Override
    public String getPluginNames() {
        return "";
    }

    @Override
    public String runCommand(String param0) {
        this.rconConsoleSource.prepareForCommand();
        this.executeBlocking(() -> this.getCommands().performCommand(this.rconConsoleSource.createCommandSourceStack(), param0));
        return this.rconConsoleSource.getCommandResponse();
    }

    public void storeUsingWhiteList(boolean param0) {
        this.settings.update(param1 -> param1.whiteList.update(this.registryAccess(), param0));
    }

    @Override
    public void stopServer() {
        super.stopServer();
        Util.shutdownExecutors();
        SkullBlockEntity.clear();
    }

    @Override
    public boolean isSingleplayerOwner(GameProfile param0) {
        return false;
    }

    @Override
    public int getScaledTrackingDistance(int param0) {
        return this.getProperties().entityBroadcastRangePercentage * param0 / 100;
    }

    @Override
    public String getLevelIdName() {
        return this.storageSource.getLevelId();
    }

    @Override
    public boolean forceSynchronousWrites() {
        return this.settings.getProperties().syncChunkWrites;
    }

    @Override
    public TextFilter createTextFilterForPlayer(ServerPlayer param0) {
        return this.textFilterClient != null ? this.textFilterClient.createContext(param0.getGameProfile()) : TextFilter.DUMMY;
    }

    @Override
    public boolean isResourcePackRequired() {
        return this.settings.getProperties().requireResourcePack;
    }

    @Nullable
    @Override
    public GameType getForcedGameType() {
        return this.settings.getProperties().forceGameMode ? this.worldData.getGameType() : null;
    }

    @Nullable
    private static Component parseResourcePackPrompt(DedicatedServerSettings param0) {
        String var0 = param0.getProperties().resourcePackPrompt;
        if (!Strings.isNullOrEmpty(var0)) {
            try {
                return Component.Serializer.fromJson(var0);
            } catch (Exception var3) {
                LOGGER.warn("Failed to parse resource pack prompt '{}'", var0, var3);
            }
        }

        return null;
    }

    @Nullable
    @Override
    public Component getResourcePackPrompt() {
        return this.resourcePackPrompt;
    }
}
