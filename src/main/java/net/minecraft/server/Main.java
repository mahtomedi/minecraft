package net.minecraft.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
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
import net.minecraft.resources.RegistryOps;
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
import net.minecraft.world.level.levelgen.presets.WorldPresets;
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
            Path var17 = Paths.get("server.properties");
            DedicatedServerSettings var18 = new DedicatedServerSettings(var17);
            var18.forceSave();
            Path var19 = Paths.get("eula.txt");
            Eula var20 = new Eula(var19);
            if (var16.has(var2)) {
                LOGGER.info("Initialized '{}' and '{}'", var17.toAbsolutePath(), var19.toAbsolutePath());
                return;
            }

            if (!var20.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }

            File var21 = new File(var16.valueOf(var10));
            YggdrasilAuthenticationService var22 = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
            MinecraftSessionService var23 = var22.createMinecraftSessionService();
            GameProfileRepository var24 = var22.createProfileRepository();
            GameProfileCache var25 = new GameProfileCache(var24, new File(var21, MinecraftServer.USERID_CACHE_FILE.getName()));
            String var26 = Optional.ofNullable(var16.valueOf(var11)).orElse(var18.getProperties().levelName);
            LevelStorageSource var27 = LevelStorageSource.createDefault(var21.toPath());
            LevelStorageSource.LevelStorageAccess var28 = var27.createAccess(var26);
            LevelSummary var29 = var28.getSummary();
            if (var29 != null) {
                if (var29.requiresManualConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }

                if (!var29.isCompatible()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            }

            boolean var30 = var16.has(var7);
            if (var30) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }

            PackRepository var31 = new PackRepository(
                PackType.SERVER_DATA,
                new ServerPacksSource(),
                new FolderRepositorySource(var28.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD)
            );

            WorldStem var35;
            try {
                DataPackConfig var32 = Objects.requireNonNullElse(var28.getDataPacks(), DataPackConfig.DEFAULT);
                WorldLoader.PackConfig var33 = new WorldLoader.PackConfig(var31, var32, var30);
                WorldLoader.InitConfig var34 = new WorldLoader.InitConfig(
                    var33, Commands.CommandSelection.DEDICATED, var18.getProperties().functionPermissionLevel
                );
                var35 = Util.<WorldStem>blockUntilDone(param6 -> WorldStem.load(var34, (param5x, param6x) -> {
                        RegistryAccess.Writable var0x = RegistryAccess.builtinCopy();
                        DynamicOps<Tag> var1x = RegistryOps.createAndLoad(NbtOps.INSTANCE, var0x, param5x);
                        WorldData var2x = var28.getDataTag(var1x, param6x, var0x.allElementsLifecycle());
                        if (var2x != null) {
                            return Pair.of(var2x, var0x.freeze());
                        } else {
                            LevelSettings var3x;
                            WorldGenSettings var4x;
                            if (var16.has(var3)) {
                                var3x = MinecraftServer.DEMO_SETTINGS;
                                var4x = WorldPresets.demoSettings(var0x);
                            } else {
                                DedicatedServerProperties var5x = var18.getProperties();
                                var3x = new LevelSettings(var5x.levelName, var5x.gamemode, var5x.hardcore, var5x.difficulty, false, new GameRules(), param6x);
                                var4x = var16.has(var4) ? var5x.getWorldGenSettings(var0x).withBonusChest() : var5x.getWorldGenSettings(var0x);
                            }

                            PrimaryLevelData var13x = new PrimaryLevelData(var3x, var4x, Lifecycle.stable());
                            return Pair.of(var13x, var0x.freeze());
                        }
                    }, Util.backgroundExecutor(), param6)).get();
            } catch (Exception var381) {
                LOGGER.warn(
                    "Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode",
                    (Throwable)var381
                );
                return;
            }

            RegistryAccess.Frozen var38 = var35.registryAccess();
            var18.getProperties().getWorldGenSettings(var38);
            WorldData var39 = var35.worldData();
            if (var16.has(var5)) {
                forceUpgrade(var28, DataFixers.getDataFixer(), var16.has(var6), () -> true, var39.worldGenSettings());
            }

            var28.saveDataTag(var38, var39);
            final DedicatedServer var40 = MinecraftServer.spin(
                param14 -> {
                    DedicatedServer var0x = new DedicatedServer(
                        param14, var28, var31, var35, var18, DataFixers.getDataFixer(), var23, var24, var25, LoggerChunkProgressListener::new
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
            Thread var41 = new Thread("Server Shutdown Thread") {
                @Override
                public void run() {
                    var40.halt(true);
                }
            };
            var41.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(var41);
        } catch (Exception var391) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", (Throwable)var391);
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
