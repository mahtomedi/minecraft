package net.minecraft.server.dedicated;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.DefaultUncaughtExceptionHandlerWithName;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.ConsoleInput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerInterface;
import net.minecraft.server.gui.MinecraftServerGui;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.server.rcon.RconConsoleSource;
import net.minecraft.server.rcon.thread.QueryThreadGs4;
import net.minecraft.server.rcon.thread.RconThread;
import net.minecraft.util.Crypt;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Snooper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DedicatedServer extends MinecraftServer implements ServerInterface {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
    private final List<ConsoleInput> consoleInput = Collections.synchronizedList(Lists.newArrayList());
    private QueryThreadGs4 queryThreadGs4;
    private final RconConsoleSource rconConsoleSource;
    private RconThread rconThread;
    private final DedicatedServerSettings settings;
    private GameType gameType;
    @Nullable
    private MinecraftServerGui gui;

    public DedicatedServer(
        File param0,
        DedicatedServerSettings param1,
        DataFixer param2,
        YggdrasilAuthenticationService param3,
        MinecraftSessionService param4,
        GameProfileRepository param5,
        GameProfileCache param6,
        ChunkProgressListenerFactory param7,
        String param8
    ) {
        super(param0, Proxy.NO_PROXY, param2, new Commands(true), param3, param4, param5, param6, param7, param8);
        this.settings = param1;
        this.rconConsoleSource = new RconConsoleSource(this);
        new Thread("Server Infinisleeper") {
            {
                this.setDaemon(true);
                this.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(DedicatedServer.LOGGER));
                this.start();
            }

            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(2147483647L);
                    } catch (InterruptedException var2) {
                    }
                }
            }
        };
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
        LOGGER.info("Starting minecraft server version " + SharedConstants.getCurrentVersion().getName());
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

        this.setAnimals(var1.spawnAnimals);
        this.setNpcsEnabled(var1.spawnNpcs);
        this.setPvpAllowed(var1.pvp);
        this.setFlightAllowed(var1.allowFlight);
        this.setResourcePack(var1.resourcePack, this.getPackHash());
        this.setMotd(var1.motd);
        this.setForceGameType(var1.forceGameMode);
        super.setPlayerIdleTimeout(var1.playerIdleTimeout.get());
        this.setEnforceWhitelist(var1.enforceWhitelist);
        this.gameType = var1.gamemode;
        LOGGER.info("Default game type: {}", this.gameType);
        InetAddress var2 = null;
        if (!this.getLocalIp().isEmpty()) {
            var2 = InetAddress.getByName(this.getLocalIp());
        }

        if (this.getPort() < 0) {
            this.setPort(var1.serverPort);
        }

        LOGGER.info("Generating keypair");
        this.setKeyPair(Crypt.generateKeyPair());
        LOGGER.info("Starting Minecraft server on {}:{}", this.getLocalIp().isEmpty() ? "*" : this.getLocalIp(), this.getPort());

        try {
            this.getConnection().startTcpServerListener(var2, this.getPort());
        } catch (IOException var17) {
            LOGGER.warn("**** FAILED TO BIND TO PORT!");
            LOGGER.warn("The exception was: {}", var17.toString());
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
            this.setPlayerList(new DedicatedPlayerList(this));
            long var4 = Util.getNanos();
            String var5 = var1.levelSeed;
            String var6 = var1.generatorSettings;
            long var7 = new Random().nextLong();
            if (!var5.isEmpty()) {
                try {
                    long var8 = Long.parseLong(var5);
                    if (var8 != 0L) {
                        var7 = var8;
                    }
                } catch (NumberFormatException var16) {
                    var7 = (long)var5.hashCode();
                }
            }

            LevelType var10 = var1.levelType;
            this.setMaxBuildHeight(var1.maxBuildHeight);
            SkullBlockEntity.setProfileCache(this.getProfileCache());
            SkullBlockEntity.setSessionService(this.getSessionService());
            GameProfileCache.setUsesAuthentication(this.usesAuthentication());
            LOGGER.info("Preparing level \"{}\"", this.getLevelIdName());
            JsonObject var11 = new JsonObject();
            if (var10 == LevelType.FLAT) {
                var11.addProperty("flat_world_options", var6);
            } else if (!var6.isEmpty()) {
                var11 = GsonHelper.parse(var6);
            }

            this.loadLevel(this.getLevelIdName(), this.getLevelIdName(), var7, var10, var11);
            long var12 = Util.getNanos() - var4;
            String var13 = String.format(Locale.ROOT, "%.3fs", (double)var12 / 1.0E9);
            LOGGER.info("Done ({})! For help, type \"help\"", var13);
            if (var1.announcePlayerAchievements != null) {
                this.getGameRules().getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(var1.announcePlayerAchievements, this);
            }

            if (var1.enableQuery) {
                LOGGER.info("Starting GS4 status listener");
                this.queryThreadGs4 = new QueryThreadGs4(this);
                this.queryThreadGs4.start();
            }

            if (var1.enableRcon) {
                LOGGER.info("Starting remote control listener");
                this.rconThread = new RconThread(this);
                this.rconThread.start();
            }

            if (this.getMaxTickLength() > 0L) {
                Thread var14 = new Thread(new ServerWatchdog(this));
                var14.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandlerWithName(LOGGER));
                var14.setName("Server Watchdog");
                var14.setDaemon(true);
                var14.start();
            }

            Items.AIR.fillItemCategory(CreativeModeTab.TAB_SEARCH, NonNullList.create());
            return true;
        }
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
    public void setDefaultGameMode(GameType param0) {
        super.setDefaultGameMode(param0);
        this.gameType = param0;
    }

    @Override
    public DedicatedServerProperties getProperties() {
        return this.settings.getProperties();
    }

    @Override
    public boolean canGenerateStructures() {
        return this.getProperties().generateStructures;
    }

    @Override
    public GameType getDefaultGameType() {
        return this.gameType;
    }

    @Override
    public Difficulty getDefaultDifficulty() {
        return this.getProperties().difficulty;
    }

    @Override
    public boolean isHardcore() {
        return this.getProperties().hardcore;
    }

    @Override
    public CrashReport fillReport(CrashReport param0) {
        param0 = super.fillReport(param0);
        param0.getSystemDetails().setDetail("Is Modded", () -> this.getModdedStatus().orElse("Unknown (can't tell)"));
        param0.getSystemDetails().setDetail("Type", () -> "Dedicated Server (map_server.txt)");
        return param0;
    }

    @Override
    public Optional<String> getModdedStatus() {
        String var0 = this.getServerModName();
        return !"vanilla".equals(var0) ? Optional.of("Definitely; Server brand changed to '" + var0 + "'") : Optional.empty();
    }

    @Override
    public void onServerExit() {
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

    @Override
    public boolean getSpawnMonsters() {
        return this.getProperties().spawnMonsters;
    }

    @Override
    public void populateSnooper(Snooper param0) {
        param0.setDynamicData("whitelist_enabled", this.getPlayerList().isUsingWhitelist());
        param0.setDynamicData("whitelist_count", this.getPlayerList().getWhiteListNames().length);
        super.populateSnooper(param0);
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
    public boolean publishServer(GameType param0, boolean param1, int param2) {
        return false;
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
    public boolean isUnderSpawnProtection(Level param0, BlockPos param1, Player param2) {
        if (param0.dimension.getType() != DimensionType.OVERWORLD) {
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
        this.settings.update(param1 -> param1.playerIdleTimeout.update(param0));
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
        this.settings.update(param1 -> param1.whiteList.update(param0));
    }

    @Override
    public void stopServer() {
        super.stopServer();
        Util.shutdownBackgroundExecutor();
    }

    @Override
    public boolean isSingleplayerOwner(GameProfile param0) {
        return false;
    }
}
