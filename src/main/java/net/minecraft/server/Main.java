package net.minecraft.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.UnopenedPack;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] param0) {
        OptionParser var0 = new OptionParser();
        OptionSpec<Void> var1 = var0.accepts("nogui");
        OptionSpec<Void> var2 = var0.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpec<Void> var3 = var0.accepts("demo");
        OptionSpec<Void> var4 = var0.accepts("bonusChest");
        OptionSpec<Void> var5 = var0.accepts("forceUpgrade");
        OptionSpec<Void> var6 = var0.accepts("eraseCache");
        OptionSpec<Void> var7 = var0.accepts("safeMode", "Loads level with vanilla datapack only");
        OptionSpec<Void> var8 = var0.accepts("help").forHelp();
        OptionSpec<String> var9 = var0.accepts("singleplayer").withRequiredArg();
        OptionSpec<String> var10 = var0.accepts("universe").withRequiredArg().defaultsTo(".");
        OptionSpec<String> var11 = var0.accepts("world").withRequiredArg();
        OptionSpec<Integer> var12 = var0.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
        OptionSpec<String> var13 = var0.accepts("serverId").withRequiredArg();
        OptionSpec<String> var14 = var0.nonOptions();

        try {
            OptionSet var15 = var0.parse(param0);
            if (var15.has(var8)) {
                var0.printHelpOn(System.err);
                return;
            }

            CrashReport.preload();
            Bootstrap.bootStrap();
            Bootstrap.validate();
            Util.startTimerHackThread();
            Path var16 = Paths.get("server.properties");
            DedicatedServerSettings var17 = new DedicatedServerSettings(var16);
            var17.forceSave();
            Path var18 = Paths.get("eula.txt");
            Eula var19 = new Eula(var18);
            if (var15.has(var2)) {
                LOGGER.info("Initialized '{}' and '{}'", var16.toAbsolutePath(), var18.toAbsolutePath());
                return;
            }

            if (!var19.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }

            File var20 = new File(var15.valueOf(var10));
            YggdrasilAuthenticationService var21 = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
            MinecraftSessionService var22 = var21.createMinecraftSessionService();
            GameProfileRepository var23 = var21.createProfileRepository();
            GameProfileCache var24 = new GameProfileCache(var23, new File(var20, MinecraftServer.USERID_CACHE_FILE.getName()));
            String var25 = Optional.ofNullable(var15.valueOf(var11)).orElse(var17.getProperties().levelName);
            LevelStorageSource var26 = LevelStorageSource.createDefault(var20.toPath());
            LevelStorageSource.LevelStorageAccess var27 = var26.createAccess(var25);
            MinecraftServer.convertFromRegionFormatIfNeeded(var27);
            if (var15.has(var5)) {
                forceUpgrade(var27, DataFixers.getDataFixer(), var15.has(var6), () -> true);
            }

            WorldData var28 = var27.getDataTag();
            if (var28 == null) {
                LevelSettings var29;
                if (var15.has(var3)) {
                    var29 = MinecraftServer.DEMO_SETTINGS;
                } else {
                    DedicatedServerProperties var30 = var17.getProperties();
                    var29 = new LevelSettings(
                        var30.levelName,
                        var30.gamemode,
                        var30.hardcore,
                        var30.difficulty,
                        false,
                        new GameRules(),
                        var15.has(var4) ? var30.worldGenSettings.withBonusChest() : var30.worldGenSettings
                    );
                }

                var28 = new PrimaryLevelData(var29);
            }

            boolean var32 = var15.has(var7);
            if (var32) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }

            PackRepository<UnopenedPack> var33 = MinecraftServer.createPackRepository(var27.getLevelPath(LevelResource.DATAPACK_DIR), var28, var32);
            CompletableFuture<ServerResources> var34 = ServerResources.loadResources(
                var33.openAllSelected(), true, var17.getProperties().functionPermissionLevel, Util.backgroundExecutor(), Runnable::run
            );

            ServerResources var35;
            try {
                var35 = var34.get();
            } catch (Exception var371) {
                LOGGER.warn(
                    "Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode",
                    (Throwable)var371
                );
                var33.close();
                return;
            }

            var35.updateGlobals();
            final DedicatedServer var38 = new DedicatedServer(
                var27, var33, var35, var28, var17, DataFixers.getDataFixer(), var22, var23, var24, LoggerChunkProgressListener::new
            );
            var38.setSingleplayerName(var15.valueOf(var9));
            var38.setPort(var15.valueOf(var12));
            var38.setDemo(var15.has(var3));
            var38.setId(var15.valueOf(var13));
            boolean var39 = !var15.has(var1) && !var15.valuesOf(var14).contains("nogui");
            if (var39 && !GraphicsEnvironment.isHeadless()) {
                var38.showGui();
            }

            var38.forkAndRun();
            Thread var40 = new Thread("Server Shutdown Thread") {
                @Override
                public void run() {
                    var38.halt(true);
                }
            };
            var40.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(var40);
        } catch (Exception var381) {
            LOGGER.fatal("Failed to start the minecraft server", (Throwable)var381);
        }

    }

    private static void forceUpgrade(LevelStorageSource.LevelStorageAccess param0, DataFixer param1, boolean param2, BooleanSupplier param3) {
        LOGGER.info("Forcing world upgrade!");
        WorldData var0 = param0.getDataTag();
        if (var0 != null) {
            WorldUpgrader var1 = new WorldUpgrader(param0, param1, var0, param2);
            Component var2 = null;

            while(!var1.isFinished()) {
                Component var3 = var1.getStatus();
                if (var2 != var3) {
                    var2 = var3;
                    LOGGER.info(var1.getStatus().getString());
                }

                int var4 = var1.getTotalChunks();
                if (var4 > 0) {
                    int var5 = var1.getConverted() + var1.getSkipped();
                    LOGGER.info("{}% completed ({} / {} chunks)...", Mth.floor((float)var5 / (float)var4 * 100.0F), var5, var4);
                }

                if (!param3.getAsBoolean()) {
                    var1.cancel();
                } else {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException var10) {
                    }
                }
            }
        }

    }
}
