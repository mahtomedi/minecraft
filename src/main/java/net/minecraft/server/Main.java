package net.minecraft.server;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
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
        OptionSpec<String> var9 = var0.accepts("universe").withRequiredArg().defaultsTo(".");
        OptionSpec<String> var10 = var0.accepts("world").withRequiredArg();
        OptionSpec<Integer> var11 = var0.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
        OptionSpec<String> var12 = var0.accepts("serverId").withRequiredArg();
        OptionSpec<Void> var13 = var0.accepts("jfrProfile");
        OptionSpec<Path> var14 = var0.accepts("pidFile").withRequiredArg().withValuesConvertedBy(new PathConverter());
        OptionSpec<String> var15 = var0.nonOptions();

        try {
            OptionSet var16 = var0.parse(param0);
            if (var16.has(var8)) {
                var0.printHelpOn(System.err);
                return;
            }

            Path var17 = var16.valueOf(var14);
            if (var17 != null) {
                writePidFile(var17);
            }

            CrashReport.preload();
            if (var16.has(var13)) {
                JvmProfiler.INSTANCE.start(Environment.SERVER);
            }

            Bootstrap.bootStrap();
            Bootstrap.validate();
            Util.startTimerHackThread();
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

            File var22 = new File(var16.valueOf(var9));
            Services var23 = Services.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), var22);
            String var24 = Optional.ofNullable(var16.valueOf(var10)).orElse(var19.getProperties().levelName);
            LevelStorageSource var25 = LevelStorageSource.createDefault(var22.toPath());
            LevelStorageSource.LevelStorageAccess var26 = var25.validateAndCreateAccess(var24);
            Dynamic<?> var27;
            if (var26.hasWorldData()) {
                LevelSummary var28;
                try {
                    var27 = var26.getDataTag();
                    var28 = var26.getSummary(var27);
                } catch (NbtException | ReportedNbtException | IOException var391) {
                    LevelStorageSource.LevelDirectory var30 = var26.getLevelDirectory();
                    LOGGER.warn("Failed to load world data from {}", var30.dataFile(), var391);
                    LOGGER.info("Attempting to use fallback");

                    try {
                        var27 = var26.getDataTagFallback();
                        var28 = var26.getSummary(var27);
                    } catch (NbtException | ReportedNbtException | IOException var381) {
                        LOGGER.error("Failed to load world data from {}", var30.oldDataFile(), var381);
                        LOGGER.error(
                            "Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", var30.dataFile(), var30.oldDataFile()
                        );
                        return;
                    }

                    var26.restoreLevelDataFromOld();
                }

                if (var28.requiresManualConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }

                if (!var28.isCompatible()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            } else {
                var27 = null;
            }

            Dynamic<?> var37 = var27;
            boolean var38 = var16.has(var7);
            if (var38) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }

            PackRepository var39 = ServerPacksSource.createPackRepository(var26);

            WorldStem var41;
            try {
                WorldLoader.InitConfig var40 = loadOrCreateConfig(var19.getProperties(), var37, var38, var39);
                var41 = Util.<WorldStem>blockUntilDone(
                        param6 -> WorldLoader.load(
                                var40,
                                param5x -> {
                                    Registry<LevelStem> var0x = param5x.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
                                    if (var37 != null) {
                                        LevelDataAndDimensions var12x = LevelStorageSource.getLevelDataAndDimensions(
                                            var37, param5x.dataConfiguration(), var0x, param5x.datapackWorldgen()
                                        );
                                        return new WorldLoader.DataLoadOutput<>(var12x.worldData(), var12x.dimensions().dimensionsRegistryAccess());
                                    } else {
                                        LOGGER.info("No existing world data, creating new world");
                                        LevelSettings var2x;
                                        WorldOptions var3x;
                                        WorldDimensions var4x;
                                        if (var16.has(var3)) {
                                            var2x = MinecraftServer.DEMO_SETTINGS;
                                            var3x = WorldOptions.DEMO_OPTIONS;
                                            var4x = WorldPresets.createNormalWorldDimensions(param5x.datapackWorldgen());
                                        } else {
                                            DedicatedServerProperties var5x = var19.getProperties();
                                            var2x = new LevelSettings(
                                                var5x.levelName,
                                                var5x.gamemode,
                                                var5x.hardcore,
                                                var5x.difficulty,
                                                false,
                                                new GameRules(),
                                                param5x.dataConfiguration()
                                            );
                                            var3x = var16.has(var4) ? var5x.worldOptions.withBonusChest(true) : var5x.worldOptions;
                                            var4x = var5x.createDimensions(param5x.datapackWorldgen());
                                        }
            
                                        WorldDimensions.Complete var13x = var4x.bake(var0x);
                                        Lifecycle var10x = var13x.lifecycle().add(param5x.datapackWorldgen().allRegistriesLifecycle());
                                        return new WorldLoader.DataLoadOutput<>(
                                            new PrimaryLevelData(var2x, var3x, var13x.specialWorldProperty(), var10x), var13x.dimensionsRegistryAccess()
                                        );
                                    }
                                },
                                WorldStem::new,
                                Util.backgroundExecutor(),
                                param6
                            )
                    )
                    .get();
            } catch (Exception var371) {
                LOGGER.warn(
                    "Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode",
                    (Throwable)var371
                );
                return;
            }

            RegistryAccess.Frozen var44 = var41.registries().compositeAccess();
            if (var16.has(var5)) {
                forceUpgrade(var26, DataFixers.getDataFixer(), var16.has(var6), () -> true, var44.registryOrThrow(Registries.LEVEL_STEM));
            }

            WorldData var45 = var41.worldData();
            var26.saveDataTag(var44, var45);
            final DedicatedServer var46 = MinecraftServer.spin(
                param11 -> {
                    DedicatedServer var0x = new DedicatedServer(
                        param11, var26, var39, var41, var19, DataFixers.getDataFixer(), var23, LoggerChunkProgressListener::new
                    );
                    var0x.setPort(var16.valueOf(var11));
                    var0x.setDemo(var16.has(var3));
                    var0x.setId(var16.valueOf(var12));
                    boolean var1x = !var16.has(var1) && !var16.valuesOf(var15).contains("nogui");
                    if (var1x && !GraphicsEnvironment.isHeadless()) {
                        var0x.showGui();
                    }
    
                    return var0x;
                }
            );
            Thread var47 = new Thread("Server Shutdown Thread") {
                @Override
                public void run() {
                    var46.halt(true);
                }
            };
            var47.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(var47);
        } catch (Exception var40) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", (Throwable)var40);
        }

    }

    private static void writePidFile(Path param0) {
        try {
            long var0 = ProcessHandle.current().pid();
            Files.writeString(param0, Long.toString(var0));
        } catch (IOException var3) {
            throw new UncheckedIOException(var3);
        }
    }

    private static WorldLoader.InitConfig loadOrCreateConfig(
        DedicatedServerProperties param0, @Nullable Dynamic<?> param1, boolean param2, PackRepository param3
    ) {
        boolean var1;
        WorldDataConfiguration var2;
        if (param1 != null) {
            WorldDataConfiguration var0 = LevelStorageSource.readDataConfig(param1);
            var1 = false;
            var2 = var0;
        } else {
            var1 = true;
            var2 = new WorldDataConfiguration(param0.initialDataPackConfiguration, FeatureFlags.DEFAULT_FLAGS);
        }

        WorldLoader.PackConfig var5 = new WorldLoader.PackConfig(param3, var2, param2, var1);
        return new WorldLoader.InitConfig(var5, Commands.CommandSelection.DEDICATED, param0.functionPermissionLevel);
    }

    private static void forceUpgrade(
        LevelStorageSource.LevelStorageAccess param0, DataFixer param1, boolean param2, BooleanSupplier param3, Registry<LevelStem> param4
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
