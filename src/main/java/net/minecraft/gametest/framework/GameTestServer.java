package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.net.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class GameTestServer extends MinecraftServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int PROGRESS_REPORT_INTERVAL = 20;
    private final List<GameTestBatch> testBatches;
    private final BlockPos spawnPos;
    private static final GameRules TEST_GAME_RULES = Util.make(new GameRules(), param0 -> {
        param0.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, null);
        param0.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, null);
    });
    private static final LevelSettings TEST_SETTINGS = new LevelSettings(
        "Test Level", GameType.CREATIVE, false, Difficulty.NORMAL, true, TEST_GAME_RULES, DataPackConfig.DEFAULT
    );
    @Nullable
    private MultipleTestTracker testTracker;

    public static GameTestServer create(
        Thread param0, LevelStorageSource.LevelStorageAccess param1, PackRepository param2, Collection<GameTestBatch> param3, BlockPos param4
    ) {
        if (param3.isEmpty()) {
            throw new IllegalArgumentException("No test batches were given!");
        } else {
            WorldStem.InitConfig var0 = new WorldStem.InitConfig(param2, Commands.CommandSelection.DEDICATED, 4, false);

            try {
                WorldStem var1 = WorldStem.load(
                        var0,
                        () -> DataPackConfig.DEFAULT,
                        (param0x, param1x) -> {
                            RegistryAccess.Frozen var0x = RegistryAccess.BUILTIN.get();
                            Registry<Biome> var1x = var0x.registryOrThrow(Registry.BIOME_REGISTRY);
                            Registry<StructureSet> var2x = var0x.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY);
                            Registry<DimensionType> var3x = var0x.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
                            WorldData var4x = new PrimaryLevelData(
                                TEST_SETTINGS,
                                new WorldGenSettings(
                                    0L,
                                    false,
                                    false,
                                    WorldGenSettings.withOverworld(
                                        var3x,
                                        DimensionType.defaultDimensions(var0x, 0L),
                                        new FlatLevelSource(var2x, FlatLevelGeneratorSettings.getDefault(var1x))
                                    )
                                ),
                                Lifecycle.stable()
                            );
                            return Pair.of(var4x, var0x);
                        },
                        Util.backgroundExecutor(),
                        Runnable::run
                    )
                    .get();
                var1.updateGlobals();
                return new GameTestServer(param0, param1, param2, var1, param3, param4);
            } catch (Exception var7) {
                LOGGER.warn("Failed to load vanilla datapack, bit oops", (Throwable)var7);
                System.exit(-1);
                throw new IllegalStateException();
            }
        }
    }

    private GameTestServer(
        Thread param0, LevelStorageSource.LevelStorageAccess param1, PackRepository param2, WorldStem param3, Collection<GameTestBatch> param4, BlockPos param5
    ) {
        super(param0, param1, param2, param3, Proxy.NO_PROXY, DataFixers.getDataFixer(), null, null, null, LoggerChunkProgressListener::new);
        this.testBatches = Lists.newArrayList(param4);
        this.spawnPos = param5;
    }

    @Override
    public boolean initServer() {
        this.setPlayerList(new PlayerList(this, this.registryAccess(), this.playerDataStorage, 1) {
        });
        this.loadLevel();
        ServerLevel var0 = this.overworld();
        var0.setDefaultSpawnPos(this.spawnPos, 0.0F);
        int var1 = 20000000;
        var0.setWeatherParameters(20000000, 20000000, false, false);
        return true;
    }

    @Override
    public void tickServer(BooleanSupplier param0) {
        super.tickServer(param0);
        ServerLevel var0 = this.overworld();
        if (!this.haveTestsStarted()) {
            this.startTests(var0);
        }

        if (var0.getGameTime() % 20L == 0L) {
            LOGGER.info(this.testTracker.getProgressBar());
        }

        if (this.testTracker.isDone()) {
            this.halt(false);
            LOGGER.info(this.testTracker.getProgressBar());
            GlobalTestReporter.finish();
            LOGGER.info("========= {} GAME TESTS COMPLETE ======================", this.testTracker.getTotalCount());
            if (this.testTracker.hasFailedRequired()) {
                LOGGER.info("{} required tests failed :(", this.testTracker.getFailedRequiredCount());
                this.testTracker.getFailedRequired().forEach(param0x -> LOGGER.info("   - {}", param0x.getTestName()));
            } else {
                LOGGER.info("All {} required tests passed :)", this.testTracker.getTotalCount());
            }

            if (this.testTracker.hasFailedOptional()) {
                LOGGER.info("{} optional tests failed", this.testTracker.getFailedOptionalCount());
                this.testTracker.getFailedOptional().forEach(param0x -> LOGGER.info("   - {}", param0x.getTestName()));
            }

            LOGGER.info("====================================================");
        }

    }

    @Override
    public SystemReport fillServerSystemReport(SystemReport param0) {
        param0.setDetail("Type", "Game test server");
        return param0;
    }

    @Override
    public void onServerExit() {
        super.onServerExit();
        System.exit(this.testTracker.getFailedRequiredCount());
    }

    @Override
    public void onServerCrash(CrashReport param0) {
        System.exit(1);
    }

    private void startTests(ServerLevel param0) {
        Collection<GameTestInfo> var0 = GameTestRunner.runTestBatches(
            this.testBatches, new BlockPos(0, -60, 0), Rotation.NONE, param0, GameTestTicker.SINGLETON, 8
        );
        this.testTracker = new MultipleTestTracker(var0);
        LOGGER.info("{} tests are now running!", this.testTracker.getTotalCount());
    }

    private boolean haveTestsStarted() {
        return this.testTracker != null;
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public int getOperatorUserPermissionLevel() {
        return 0;
    }

    @Override
    public int getFunctionCompilationLevel() {
        return 4;
    }

    @Override
    public boolean shouldRconBroadcast() {
        return false;
    }

    @Override
    public boolean isDedicatedServer() {
        return false;
    }

    @Override
    public int getRateLimitPacketsPerSecond() {
        return 0;
    }

    @Override
    public boolean isEpollEnabled() {
        return false;
    }

    @Override
    public boolean isCommandBlockEnabled() {
        return true;
    }

    @Override
    public boolean isPublished() {
        return false;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }

    @Override
    public boolean isSingleplayerOwner(GameProfile param0) {
        return false;
    }
}
