package net.minecraft.server;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
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
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

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
            RegistryAccess.RegistryHolder var16 = RegistryAccess.builtin();
            Path var17 = Paths.get("server.properties");
            DedicatedServerSettings var18 = new DedicatedServerSettings(var17);
            var18.forceSave();
            Path var19 = Paths.get("eula.txt");
            Eula var20 = new Eula(var19);
            if (var15.has(var2)) {
                LOGGER.info("Initialized '{}' and '{}'", var17.toAbsolutePath(), var19.toAbsolutePath());
                return;
            }

            if (!var20.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }

            File var21 = new File(var15.valueOf(var10));
            YggdrasilAuthenticationService var22 = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
            MinecraftSessionService var23 = var22.createMinecraftSessionService();
            GameProfileRepository var24 = var22.createProfileRepository();
            GameProfileCache var25 = new GameProfileCache(var24, new File(var21, MinecraftServer.USERID_CACHE_FILE.getName()));
            String var26 = Optional.ofNullable(var15.valueOf(var11)).orElse(var18.getProperties().levelName);
            LevelStorageSource var27 = LevelStorageSource.createDefault(var21.toPath());
            LevelStorageSource.LevelStorageAccess var28 = var27.createAccess(var26);
            MinecraftServer.convertFromRegionFormatIfNeeded(var28);
            LevelSummary var29 = var28.getSummary();
            if (var29 != null && var29.isIncompatibleWorldHeight()) {
                LOGGER.info("Loading of worlds with extended height is disabled.");
                return;
            }

            DataPackConfig var30 = var28.getDataPacks();
            boolean var31 = var15.has(var7);
            if (var31) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }

            PackRepository var32 = new PackRepository(
                PackType.SERVER_DATA,
                new ServerPacksSource(),
                new FolderRepositorySource(var28.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD)
            );
            DataPackConfig var33 = MinecraftServer.configurePackRepository(var32, var30 == null ? DataPackConfig.DEFAULT : var30, var31);
            CompletableFuture<ServerResources> var34 = ServerResources.loadResources(
                var32.openAllSelected(),
                var16,
                Commands.CommandSelection.DEDICATED,
                var18.getProperties().functionPermissionLevel,
                Util.backgroundExecutor(),
                Runnable::run
            );

            ServerResources var35;
            try {
                var35 = var34.get();
            } catch (Exception var42) {
                LOGGER.warn(
                    "Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode",
                    (Throwable)var42
                );
                var32.close();
                return;
            }

            var35.updateGlobals();
            RegistryReadOps<Tag> var38 = RegistryReadOps.createAndLoad(NbtOps.INSTANCE, var35.getResourceManager(), var16);
            var18.getProperties().getWorldGenSettings(var16);
            WorldData var39 = var28.getDataTag(var38, var33);
            if (var39 == null) {
                LevelSettings var40;
                WorldGenSettings var41;
                if (var15.has(var3)) {
                    var40 = MinecraftServer.DEMO_SETTINGS;
                    var41 = WorldGenSettings.demoSettings(var16);
                } else {
                    DedicatedServerProperties var42 = var18.getProperties();
                    var40 = new LevelSettings(var42.levelName, var42.gamemode, var42.hardcore, var42.difficulty, false, new GameRules(), var33);
                    var41 = var15.has(var4) ? var42.getWorldGenSettings(var16).withBonusChest() : var42.getWorldGenSettings(var16);
                }

                var39 = new PrimaryLevelData(var40, var41, Lifecycle.stable());
            }

            if (var15.has(var5)) {
                forceUpgrade(var28, DataFixers.getDataFixer(), var15.has(var6), () -> true, var39.worldGenSettings().levels());
            }

            var28.saveDataTag(var16, var39);
            WorldData var45 = var39;
            final DedicatedServer var46 = MinecraftServer.spin(
                param16 -> {
                    DedicatedServer var0x = new DedicatedServer(
                        param16, var16, var28, var32, var35, var45, var18, DataFixers.getDataFixer(), var23, var24, var25, LoggerChunkProgressListener::new
                    );
                    var0x.setSingleplayerName(var15.valueOf(var9));
                    var0x.setPort(var15.valueOf(var12));
                    var0x.setDemo(var15.has(var3));
                    var0x.setId(var15.valueOf(var13));
                    boolean var1x = !var15.has(var1) && !var15.valuesOf(var14).contains("nogui");
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
        } catch (Exception var43) {
            LOGGER.fatal("Failed to start the minecraft server", (Throwable)var43);
        }

    }

    private static void forceUpgrade(
        LevelStorageSource.LevelStorageAccess param0, DataFixer param1, boolean param2, BooleanSupplier param3, ImmutableSet<ResourceKey<Level>> param4
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
