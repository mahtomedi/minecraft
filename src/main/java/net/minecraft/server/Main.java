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
import java.util.function.BooleanSupplier;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
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
        OptionSpec<Void> var7 = var0.accepts("help").forHelp();
        OptionSpec<String> var8 = var0.accepts("singleplayer").withRequiredArg();
        OptionSpec<String> var9 = var0.accepts("universe").withRequiredArg().defaultsTo(".");
        OptionSpec<String> var10 = var0.accepts("world").withRequiredArg();
        OptionSpec<Integer> var11 = var0.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
        OptionSpec<String> var12 = var0.accepts("serverId").withRequiredArg();
        OptionSpec<String> var13 = var0.nonOptions();

        try {
            OptionSet var14 = var0.parse(param0);
            if (var14.has(var7)) {
                var0.printHelpOn(System.err);
                return;
            }

            CrashReport.preload();
            Bootstrap.bootStrap();
            Bootstrap.validate();
            Path var15 = Paths.get("server.properties");
            DedicatedServerSettings var16 = new DedicatedServerSettings(var15);
            var16.forceSave();
            Path var17 = Paths.get("eula.txt");
            Eula var18 = new Eula(var17);
            if (var14.has(var2)) {
                LOGGER.info("Initialized '{}' and '{}'", var15.toAbsolutePath(), var17.toAbsolutePath());
                return;
            }

            if (!var18.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }

            File var19 = new File(var14.valueOf(var9));
            YggdrasilAuthenticationService var20 = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
            MinecraftSessionService var21 = var20.createMinecraftSessionService();
            GameProfileRepository var22 = var20.createProfileRepository();
            GameProfileCache var23 = new GameProfileCache(var22, new File(var19, MinecraftServer.USERID_CACHE_FILE.getName()));
            String var24 = Optional.ofNullable(var14.valueOf(var10)).orElse(var16.getProperties().levelName);
            LevelStorageSource var25 = LevelStorageSource.createDefault(var19.toPath());
            LevelStorageSource.LevelStorageAccess var26 = var25.createAccess(var24);
            MinecraftServer.convertFromRegionFormatIfNeeded(var26);
            if (var14.has(var5)) {
                forceUpgrade(var26, DataFixers.getDataFixer(), var14.has(var6), () -> true);
            }

            WorldData var27 = var26.getDataTag();
            if (var27 == null) {
                LevelSettings var28;
                if (var14.has(var3)) {
                    var28 = MinecraftServer.DEMO_SETTINGS;
                } else {
                    DedicatedServerProperties var29 = var16.getProperties();
                    var28 = new LevelSettings(
                        var29.levelName,
                        var29.gamemode,
                        var29.hardcore,
                        var29.difficulty,
                        false,
                        new GameRules(),
                        var14.has(var4) ? var29.worldGenSettings.withBonusChest() : var29.worldGenSettings
                    );
                }

                var27 = new PrimaryLevelData(var28);
            }

            final DedicatedServer var31 = new DedicatedServer(
                var26, var27, var16, DataFixers.getDataFixer(), var21, var22, var23, LoggerChunkProgressListener::new
            );
            var31.setSingleplayerName(var14.valueOf(var8));
            var31.setPort(var14.valueOf(var11));
            var31.setDemo(var14.has(var3));
            var31.setId(var14.valueOf(var12));
            boolean var32 = !var14.has(var1) && !var14.valuesOf(var13).contains("nogui");
            if (var32 && !GraphicsEnvironment.isHeadless()) {
                var31.showGui();
            }

            var31.forkAndRun();
            Thread var33 = new Thread("Server Shutdown Thread") {
                @Override
                public void run() {
                    var31.halt(true);
                }
            };
            var33.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(var33);
        } catch (Exception var321) {
            LOGGER.fatal("Failed to start the minecraft server", (Throwable)var321);
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
