package net.minecraft.server;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
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
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.RegistryOps;
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
        OptionSpec<Path> var15 = var0.accepts("pidFile").withRequiredArg().withValuesConvertedBy(new PathConverter());
        OptionSpec<String> var16 = var0.nonOptions();

        try {
            OptionSet var17 = var0.parse(param0);
            if (var17.has(var8)) {
                var0.printHelpOn(System.err);
                return;
            }

            Path var18 = var17.valueOf(var15);
            if (var18 != null) {
                writePidFile(var18);
            }

            CrashReport.preload();
            if (var17.has(var14)) {
                JvmProfiler.INSTANCE.start(Environment.SERVER);
            }

            Bootstrap.bootStrap();
            Bootstrap.validate();
            Util.startTimerHackThread();
            Path var19 = Paths.get("server.properties");
            DedicatedServerSettings var20 = new DedicatedServerSettings(var19);
            var20.forceSave();
            Path var21 = Paths.get("eula.txt");
            Eula var22 = new Eula(var21);
            if (var17.has(var2)) {
                LOGGER.info("Initialized '{}' and '{}'", var19.toAbsolutePath(), var21.toAbsolutePath());
                return;
            }

            if (!var22.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }

            File var23 = new File(var17.valueOf(var10));
            Services var24 = Services.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), var23);
            String var25 = Optional.ofNullable(var17.valueOf(var11)).orElse(var20.getProperties().levelName);
            LevelStorageSource var26 = LevelStorageSource.createDefault(var23.toPath());
            LevelStorageSource.LevelStorageAccess var27 = var26.validateAndCreateAccess(var25);
            LevelSummary var28 = var27.getSummary();
            if (var28 != null) {
                if (var28.requiresManualConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }

                if (!var28.isCompatible()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            }

            boolean var29 = var17.has(var7);
            if (var29) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }

            PackRepository var30 = ServerPacksSource.createPackRepository(var27.getLevelPath(LevelResource.DATAPACK_DIR));

            WorldStem var32;
            try {
                WorldLoader.InitConfig var31 = loadOrCreateConfig(var20.getProperties(), var27, var29, var30);
                var32 = Util.<WorldStem>blockUntilDone(
                        param6 -> WorldLoader.load(
                                var31,
                                param5x -> {
                                    Registry<LevelStem> var0x = param5x.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
                                    DynamicOps<Tag> var1x = RegistryOps.create(NbtOps.INSTANCE, param5x.datapackWorldgen());
                                    Pair<WorldData, WorldDimensions.Complete> var2x = var27.getDataTag(
                                        var1x, param5x.dataConfiguration(), var0x, param5x.datapackWorldgen().allRegistriesLifecycle()
                                    );
                                    if (var2x != null) {
                                        return new WorldLoader.DataLoadOutput<>(var2x.getFirst(), var2x.getSecond().dimensionsRegistryAccess());
                                    } else {
                                        LevelSettings var3x;
                                        WorldOptions var4x;
                                        WorldDimensions var5x;
                                        if (var17.has(var3)) {
                                            var3x = MinecraftServer.DEMO_SETTINGS;
                                            var4x = WorldOptions.DEMO_OPTIONS;
                                            var5x = WorldPresets.createNormalWorldDimensions(param5x.datapackWorldgen());
                                        } else {
                                            DedicatedServerProperties var6x = var20.getProperties();
                                            var3x = new LevelSettings(
                                                var6x.levelName,
                                                var6x.gamemode,
                                                var6x.hardcore,
                                                var6x.difficulty,
                                                false,
                                                new GameRules(),
                                                param5x.dataConfiguration()
                                            );
                                            var4x = var17.has(var4) ? var6x.worldOptions.withBonusChest(true) : var6x.worldOptions;
                                            var5x = var6x.createDimensions(param5x.datapackWorldgen());
                                        }
            
                                        WorldDimensions.Complete var14x = var5x.bake(var0x);
                                        Lifecycle var11x = var14x.lifecycle().add(param5x.datapackWorldgen().allRegistriesLifecycle());
                                        return new WorldLoader.DataLoadOutput<>(
                                            new PrimaryLevelData(var3x, var4x, var14x.specialWorldProperty(), var11x), var14x.dimensionsRegistryAccess()
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

            RegistryAccess.Frozen var35 = var32.registries().compositeAccess();
            if (var17.has(var5)) {
                forceUpgrade(var27, DataFixers.getDataFixer(), var17.has(var6), () -> true, var35.registryOrThrow(Registries.LEVEL_STEM));
            }

            WorldData var36 = var32.worldData();
            var27.saveDataTag(var35, var36);
            final DedicatedServer var37 = MinecraftServer.spin(
                param12 -> {
                    DedicatedServer var0x = new DedicatedServer(
                        param12, var27, var30, var32, var20, DataFixers.getDataFixer(), var24, LoggerChunkProgressListener::new
                    );
                    var0x.setSingleplayerProfile(var17.has(var9) ? new GameProfile(null, var17.valueOf(var9)) : null);
                    var0x.setPort(var17.valueOf(var12));
                    var0x.setDemo(var17.has(var3));
                    var0x.setId(var17.valueOf(var13));
                    boolean var1x = !var17.has(var1) && !var17.valuesOf(var16).contains("nogui");
                    if (var1x && !GraphicsEnvironment.isHeadless()) {
                        var0x.showGui();
                    }
    
                    return var0x;
                }
            );
            Thread var38 = new Thread("Server Shutdown Thread") {
                @Override
                public void run() {
                    var37.halt(true);
                }
            };
            var38.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(var38);
        } catch (Exception var381) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", (Throwable)var381);
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
        DedicatedServerProperties param0, LevelStorageSource.LevelStorageAccess param1, boolean param2, PackRepository param3
    ) {
        WorldDataConfiguration var0 = param1.getDataConfiguration();
        WorldDataConfiguration var2;
        boolean var1;
        if (var0 != null) {
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
