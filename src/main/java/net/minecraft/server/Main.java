package net.minecraft.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class Main {
    private static final Logger LOGGER = LogUtils.getLogger();

    @DontObfuscate
    public static void main(String[] param0) {
        SharedConstants.tryDetectVersion();
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
        OptionSpec<Void> var14 = var0.accepts("jfrProfile");
        OptionSpec<String> var15 = var0.nonOptions();

        try {
            OptionSet var16 = var0.parse(param0);
            if (var16.has(var8)) {
                var0.printHelpOn(System.err);
                return;
            }

            CrashReport.preload();
            if (var16.has(var14)) {
                JvmProfiler.INSTANCE.start(Environment.SERVER);
            }

            Bootstrap.bootStrap();
            Bootstrap.validate();
            Util.startTimerHackThread();
            RegistryAccess.RegistryHolder var17 = RegistryAccess.builtin();
            Path var18 = Paths.get("server.properties");
            DedicatedServerSettings var19 = new DedicatedServerSettings(var18);
            var19.forceSave();
            Path var20 = Paths.get("eula.txt");
            Eula var21 = new Eula(var20);
            if (var16.has(var2)) {
                LOGGER.info("Initialized '{}' and '{}'", var18.toAbsolutePath(), var20.toAbsolutePath());
                return;
            }

            if (!var21.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }

            File var22 = new File(var16.valueOf(var10));
            YggdrasilAuthenticationService var23 = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
            MinecraftSessionService var24 = var23.createMinecraftSessionService();
            GameProfileRepository var25 = var23.createProfileRepository();
            GameProfileCache var26 = new GameProfileCache(var25, new File(var22, MinecraftServer.USERID_CACHE_FILE.getName()));
            String var27 = Optional.ofNullable(var16.valueOf(var11)).orElse(var19.getProperties().levelName);
            LevelStorageSource var28 = LevelStorageSource.createDefault(var22.toPath());
            LevelStorageSource.LevelStorageAccess var29 = var28.createAccess(var27);
            LevelSummary var30 = var29.getSummary();
            if (var30 != null) {
                if (var30.requiresManualConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }

                if (!var30.isCompatible()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            }

            DataPackConfig var31 = var29.getDataPacks();
            boolean var32 = var16.has(var7);
            if (var32) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }

            PackRepository var33 = new PackRepository(
                PackType.SERVER_DATA,
                new ServerPacksSource(),
                new FolderRepositorySource(var29.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD)
            );
            DataPackConfig var34 = MinecraftServer.configurePackRepository(var33, var31 == null ? DataPackConfig.DEFAULT : var31, var32);
            CompletableFuture<ServerResources> var35 = ServerResources.loadResources(
                var33.openAllSelected(),
                var17,
                Commands.CommandSelection.DEDICATED,
                var19.getProperties().functionPermissionLevel,
                Util.backgroundExecutor(),
                Runnable::run
            );

            ServerResources var36;
            try {
                var36 = var35.get();
            } catch (Exception var43) {
                LOGGER.warn(
                    "Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode",
                    (Throwable)var43
                );
                var33.close();
                return;
            }

            var36.updateGlobals();
            RegistryReadOps<Tag> var39 = RegistryReadOps.createAndLoad(NbtOps.INSTANCE, var36.getResourceManager(), var17);
            var19.getProperties().getWorldGenSettings(var17);
            WorldData var40 = var29.getDataTag(var39, var34);
            if (var40 == null) {
                LevelSettings var41;
                WorldGenSettings var42;
                if (var16.has(var3)) {
                    var41 = MinecraftServer.DEMO_SETTINGS;
                    var42 = WorldGenSettings.demoSettings(var17);
                } else {
                    DedicatedServerProperties var43 = var19.getProperties();
                    var41 = new LevelSettings(var43.levelName, var43.gamemode, var43.hardcore, var43.difficulty, false, new GameRules(), var34);
                    var42 = var16.has(var4) ? var43.getWorldGenSettings(var17).withBonusChest() : var43.getWorldGenSettings(var17);
                }

                var40 = new PrimaryLevelData(var41, var42, Lifecycle.stable());
            }

            if (var16.has(var5)) {
                forceUpgrade(var29, DataFixers.getDataFixer(), var16.has(var6), () -> true, var40.worldGenSettings());
            }

            var29.saveDataTag(var17, var40);
            WorldData var46 = var40;
            final DedicatedServer var47 = MinecraftServer.spin(
                param16 -> {
                    DedicatedServer var0x = new DedicatedServer(
                        param16, var17, var29, var33, var36, var46, var19, DataFixers.getDataFixer(), var24, var25, var26, LoggerChunkProgressListener::new
                    );
                    var0x.setSingleplayerName(var16.valueOf(var9));
                    var0x.setPort(var16.valueOf(var12));
                    var0x.setDemo(var16.has(var3));
                    var0x.setId(var16.valueOf(var13));
                    boolean var1x = !var16.has(var1) && !var16.valuesOf(var15).contains("nogui");
                    if (var1x && !GraphicsEnvironment.isHeadless()) {
                        var0x.showGui();
                    }
    
                    return var0x;
                }
            );
            Thread var48 = new Thread("Server Shutdown Thread") {
                @Override
                public void run() {
                    var47.halt(true);
                }
            };
            var48.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(var48);
        } catch (Exception var44) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", (Throwable)var44);
        }

    }

    private static void forceUpgrade(
        LevelStorageSource.LevelStorageAccess param0, DataFixer param1, boolean param2, BooleanSupplier param3, WorldGenSettings param4
    ) {
        LOGGER.info("Forcing world upgrade!");
        WorldUpgrader var0 = new WorldUpgrader(param0, param1, param4, param2);
        Component var1 = null;

        while(!var0.isFinished()) {
            Component var2 = var0.getStatus();
            if (var1 != var2) {
                var1 = var2;
                LOGGER.info(var0.getStatus().getString());
            }

            int var3 = var0.getTotalChunks();
            if (var3 > 0) {
                int var4 = var0.getConverted() + var0.getSkipped();
                LOGGER.info("{}% completed ({} / {} chunks)...", Mth.floor((float)var4 / (float)var3 * 100.0F), var4, var3);
            }

            if (!param3.getAsBoolean()) {
                var0.cancel();
            } else {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException var10) {
                }
            }
        }

    }
}
