package net.minecraft.server;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.DemoMode;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.ModCheck;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.ServerMetricsSamplersProvider;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public abstract class MinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements ServerInfo, CommandSource, AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String VANILLA_BRAND = "vanilla";
    private static final float AVERAGE_TICK_TIME_SMOOTHING = 0.8F;
    private static final int TICK_STATS_SPAN = 100;
    private static final long OVERLOADED_THRESHOLD_NANOS = 20L * TimeUtil.NANOSECONDS_PER_SECOND / 20L;
    private static final int OVERLOADED_TICKS_THRESHOLD = 20;
    private static final long OVERLOADED_WARNING_INTERVAL_NANOS = 10L * TimeUtil.NANOSECONDS_PER_SECOND;
    private static final int OVERLOADED_TICKS_WARNING_INTERVAL = 100;
    private static final long STATUS_EXPIRE_TIME_NANOS = 5L * TimeUtil.NANOSECONDS_PER_SECOND;
    private static final long PREPARE_LEVELS_DEFAULT_DELAY_NANOS = 10L * TimeUtil.NANOSECONDS_PER_MILLISECOND;
    private static final int MAX_STATUS_PLAYER_SAMPLE = 12;
    public static final int START_CHUNK_RADIUS = 11;
    private static final int START_TICKING_CHUNK_COUNT = 441;
    private static final int AUTOSAVE_INTERVAL = 6000;
    private static final int MIMINUM_AUTOSAVE_TICKS = 100;
    private static final int MAX_TICK_LATENCY = 3;
    public static final int ABSOLUTE_MAX_WORLD_SIZE = 29999984;
    public static final LevelSettings DEMO_SETTINGS = new LevelSettings(
        "Demo World", GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(), WorldDataConfiguration.DEFAULT
    );
    public static final GameProfile ANONYMOUS_PLAYER_PROFILE = new GameProfile(Util.NIL_UUID, "Anonymous Player");
    protected final LevelStorageSource.LevelStorageAccess storageSource;
    protected final PlayerDataStorage playerDataStorage;
    private final List<Runnable> tickables = Lists.newArrayList();
    private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    private ProfilerFiller profiler = this.metricsRecorder.getProfiler();
    private Consumer<ProfileResults> onMetricsRecordingStopped = param0x -> this.stopRecordingMetrics();
    private Consumer<Path> onMetricsRecordingFinished = param0x -> {
    };
    private boolean willStartRecordingMetrics;
    @Nullable
    private MinecraftServer.TimeProfiler debugCommandProfiler;
    private boolean debugCommandProfilerDelayStart;
    private final ServerConnectionListener connection;
    private final ChunkProgressListenerFactory progressListenerFactory;
    @Nullable
    private ServerStatus status;
    @Nullable
    private ServerStatus.Favicon statusIcon;
    private final RandomSource random = RandomSource.create();
    private final DataFixer fixerUpper;
    private String localIp;
    private int port = -1;
    private final LayeredRegistryAccess<RegistryLayer> registries;
    private final Map<ResourceKey<Level>, ServerLevel> levels = Maps.newLinkedHashMap();
    private PlayerList playerList;
    private volatile boolean running = true;
    private boolean stopped;
    private int tickCount;
    private int ticksUntilAutosave = 6000;
    protected final Proxy proxy;
    private boolean onlineMode;
    private boolean preventProxyConnections;
    private boolean pvp;
    private boolean allowFlight;
    @Nullable
    private String motd;
    private int playerIdleTimeout;
    private final long[] tickTimesNanos = new long[100];
    private long aggregatedTickTimesNanos = 0L;
    @Nullable
    private KeyPair keyPair;
    @Nullable
    private GameProfile singleplayerProfile;
    private boolean isDemo;
    private volatile boolean isReady;
    private long lastOverloadWarningNanos;
    protected final Services services;
    private long lastServerStatus;
    private final Thread serverThread;
    private long nextTickTimeNanos = Util.getNanos();
    private long delayedTasksMaxNextTickTimeNanos;
    private boolean mayHaveDelayedTasks;
    private final PackRepository packRepository;
    private final ServerScoreboard scoreboard = new ServerScoreboard(this);
    @Nullable
    private CommandStorage commandStorage;
    private final CustomBossEvents customBossEvents = new CustomBossEvents();
    private final ServerFunctionManager functionManager;
    private boolean enforceWhitelist;
    private float smoothedTickTimeMillis;
    private final Executor executor;
    @Nullable
    private String serverId;
    private MinecraftServer.ReloadableResources resources;
    private final StructureTemplateManager structureTemplateManager;
    private final ServerTickRateManager tickRateManager;
    protected final WorldData worldData;
    private volatile boolean isSaving;

    public static <S extends MinecraftServer> S spin(Function<Thread, S> param0) {
        AtomicReference<S> var0 = new AtomicReference<>();
        Thread var1 = new Thread(() -> var0.get().runServer(), "Server thread");
        var1.setUncaughtExceptionHandler((param0x, param1) -> LOGGER.error("Uncaught exception in server thread", param1));
        if (Runtime.getRuntime().availableProcessors() > 4) {
            var1.setPriority(8);
        }

        S var2 = param0.apply(var1);
        var0.set(var2);
        var1.start();
        return var2;
    }

    public MinecraftServer(
        Thread param0,
        LevelStorageSource.LevelStorageAccess param1,
        PackRepository param2,
        WorldStem param3,
        Proxy param4,
        DataFixer param5,
        Services param6,
        ChunkProgressListenerFactory param7
    ) {
        super("Server");
        this.registries = param3.registries();
        this.worldData = param3.worldData();
        if (!this.registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM).containsKey(LevelStem.OVERWORLD)) {
            throw new IllegalStateException("Missing Overworld dimension data");
        } else {
            this.proxy = param4;
            this.packRepository = param2;
            this.resources = new MinecraftServer.ReloadableResources(param3.resourceManager(), param3.dataPackResources());
            this.services = param6;
            if (param6.profileCache() != null) {
                param6.profileCache().setExecutor(this);
            }

            this.connection = new ServerConnectionListener(this);
            this.tickRateManager = new ServerTickRateManager(this);
            this.progressListenerFactory = param7;
            this.storageSource = param1;
            this.playerDataStorage = param1.createPlayerStorage();
            this.fixerUpper = param5;
            this.functionManager = new ServerFunctionManager(this, this.resources.managers.getFunctionLibrary());
            HolderGetter<Block> var0 = this.registries
                .compositeAccess()
                .<Block>registryOrThrow(Registries.BLOCK)
                .asLookup()
                .filterFeatures(this.worldData.enabledFeatures());
            this.structureTemplateManager = new StructureTemplateManager(param3.resourceManager(), param1, param5, var0);
            this.serverThread = param0;
            this.executor = Util.backgroundExecutor();
        }
    }

    private void readScoreboard(DimensionDataStorage param0) {
        param0.computeIfAbsent(this.getScoreboard().dataFactory(), "scoreboard");
    }

    protected abstract boolean initServer() throws IOException;

    protected void loadLevel() {
        if (!JvmProfiler.INSTANCE.isRunning()) {
        }

        boolean var0 = false;
        ProfiledDuration var1 = JvmProfiler.INSTANCE.onWorldLoadedStarted();
        this.worldData.setModdedInfo(this.getServerModName(), this.getModdedStatus().shouldReportAsModified());
        ChunkProgressListener var2 = this.progressListenerFactory.create(11);
        this.createLevels(var2);
        this.forceDifficulty();
        this.prepareLevels(var2);
        if (var1 != null) {
            var1.finish();
        }

        if (var0) {
            try {
                JvmProfiler.INSTANCE.stop();
            } catch (Throwable var5) {
                LOGGER.warn("Failed to stop JFR profiling", var5);
            }
        }

    }

    protected void forceDifficulty() {
    }

    protected void createLevels(ChunkProgressListener param0) {
        ServerLevelData var0 = this.worldData.overworldData();
        boolean var1 = this.worldData.isDebugWorld();
        Registry<LevelStem> var2 = this.registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM);
        WorldOptions var3 = this.worldData.worldGenOptions();
        long var4 = var3.seed();
        long var5 = BiomeManager.obfuscateSeed(var4);
        List<CustomSpawner> var6 = ImmutableList.of(
            new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(var0)
        );
        LevelStem var7 = var2.get(LevelStem.OVERWORLD);
        ServerLevel var8 = new ServerLevel(this, this.executor, this.storageSource, var0, Level.OVERWORLD, var7, param0, var1, var5, var6, true, null);
        this.levels.put(Level.OVERWORLD, var8);
        DimensionDataStorage var9 = var8.getDataStorage();
        this.readScoreboard(var9);
        this.commandStorage = new CommandStorage(var9);
        WorldBorder var10 = var8.getWorldBorder();
        if (!var0.isInitialized()) {
            try {
                setInitialSpawn(var8, var0, var3.generateBonusChest(), var1);
                var0.setInitialized(true);
                if (var1) {
                    this.setupDebugLevel(this.worldData);
                }
            } catch (Throwable var23) {
                CrashReport var12 = CrashReport.forThrowable(var23, "Exception initializing level");

                try {
                    var8.fillReportDetails(var12);
                } catch (Throwable var22) {
                }

                throw new ReportedException(var12);
            }

            var0.setInitialized(true);
        }

        this.getPlayerList().addWorldborderListener(var8);
        if (this.worldData.getCustomBossEvents() != null) {
            this.getCustomBossEvents().load(this.worldData.getCustomBossEvents());
        }

        RandomSequences var13 = var8.getRandomSequences();

        for(Entry<ResourceKey<LevelStem>, LevelStem> var14 : var2.entrySet()) {
            ResourceKey<LevelStem> var15 = var14.getKey();
            if (var15 != LevelStem.OVERWORLD) {
                ResourceKey<Level> var16 = ResourceKey.create(Registries.DIMENSION, var15.location());
                DerivedLevelData var17 = new DerivedLevelData(this.worldData, var0);
                ServerLevel var18 = new ServerLevel(
                    this, this.executor, this.storageSource, var17, var16, var14.getValue(), param0, var1, var5, ImmutableList.of(), false, var13
                );
                var10.addListener(new BorderChangeListener.DelegateBorderChangeListener(var18.getWorldBorder()));
                this.levels.put(var16, var18);
            }
        }

        var10.applySettings(var0.getWorldBorder());
    }

    private static void setInitialSpawn(ServerLevel param0, ServerLevelData param1, boolean param2, boolean param3) {
        if (param3) {
            param1.setSpawn(BlockPos.ZERO.above(80), 0.0F);
        } else {
            ServerChunkCache var0 = param0.getChunkSource();
            ChunkPos var1 = new ChunkPos(var0.randomState().sampler().findSpawnPosition());
            int var2 = var0.getGenerator().getSpawnHeight(param0);
            if (var2 < param0.getMinBuildHeight()) {
                BlockPos var3 = var1.getWorldPosition();
                var2 = param0.getHeight(Heightmap.Types.WORLD_SURFACE, var3.getX() + 8, var3.getZ() + 8);
            }

            param1.setSpawn(var1.getWorldPosition().offset(8, var2, 8), 0.0F);
            int var4 = 0;
            int var5 = 0;
            int var6 = 0;
            int var7 = -1;
            int var8 = 5;

            for(int var9 = 0; var9 < Mth.square(11); ++var9) {
                if (var4 >= -5 && var4 <= 5 && var5 >= -5 && var5 <= 5) {
                    BlockPos var10 = PlayerRespawnLogic.getSpawnPosInChunk(param0, new ChunkPos(var1.x + var4, var1.z + var5));
                    if (var10 != null) {
                        param1.setSpawn(var10, 0.0F);
                        break;
                    }
                }

                if (var4 == var5 || var4 < 0 && var4 == -var5 || var4 > 0 && var4 == 1 - var5) {
                    int var11 = var6;
                    var6 = -var7;
                    var7 = var11;
                }

                var4 += var6;
                var5 += var7;
            }

            if (param2) {
                param0.registryAccess()
                    .registry(Registries.CONFIGURED_FEATURE)
                    .flatMap(param0x -> param0x.getHolder(MiscOverworldFeatures.BONUS_CHEST))
                    .ifPresent(
                        param3x -> param3x.value()
                                .place(param0, var0.getGenerator(), param0.random, new BlockPos(param1.getXSpawn(), param1.getYSpawn(), param1.getZSpawn()))
                    );
            }

        }
    }

    private void setupDebugLevel(WorldData param0) {
        param0.setDifficulty(Difficulty.PEACEFUL);
        param0.setDifficultyLocked(true);
        ServerLevelData var0 = param0.overworldData();
        var0.setRaining(false);
        var0.setThundering(false);
        var0.setClearWeatherTime(1000000000);
        var0.setDayTime(6000L);
        var0.setGameType(GameType.SPECTATOR);
    }

    private void prepareLevels(ChunkProgressListener param0) {
        ServerLevel var0 = this.overworld();
        LOGGER.info("Preparing start region for dimension {}", var0.dimension().location());
        BlockPos var1 = var0.getSharedSpawnPos();
        param0.updateSpawnPos(new ChunkPos(var1));
        ServerChunkCache var2 = var0.getChunkSource();
        this.nextTickTimeNanos = Util.getNanos();
        var2.addRegionTicket(TicketType.START, new ChunkPos(var1), 11, Unit.INSTANCE);

        while(var2.getTickingGenerated() != 441) {
            this.nextTickTimeNanos = Util.getNanos() + PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
            this.waitUntilNextTick();
        }

        this.nextTickTimeNanos = Util.getNanos() + PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
        this.waitUntilNextTick();

        for(ServerLevel var3 : this.levels.values()) {
            ForcedChunksSavedData var4 = var3.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");
            if (var4 != null) {
                LongIterator var5 = var4.getChunks().iterator();

                while(var5.hasNext()) {
                    long var6 = var5.nextLong();
                    ChunkPos var7 = new ChunkPos(var6);
                    var3.getChunkSource().updateChunkForced(var7, true);
                }
            }
        }

        this.nextTickTimeNanos = Util.getNanos() + PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
        this.waitUntilNextTick();
        param0.stop();
        this.updateMobSpawningFlags();
    }

    public GameType getDefaultGameType() {
        return this.worldData.getGameType();
    }

    public boolean isHardcore() {
        return this.worldData.isHardcore();
    }

    public abstract int getOperatorUserPermissionLevel();

    public abstract int getFunctionCompilationLevel();

    public abstract boolean shouldRconBroadcast();

    public boolean saveAllChunks(boolean param0, boolean param1, boolean param2) {
        boolean var0 = false;

        for(ServerLevel var1 : this.getAllLevels()) {
            if (!param0) {
                LOGGER.info("Saving chunks for level '{}'/{}", var1, var1.dimension().location());
            }

            var1.save(null, param1, var1.noSave && !param2);
            var0 = true;
        }

        ServerLevel var2 = this.overworld();
        ServerLevelData var3 = this.worldData.overworldData();
        var3.setWorldBorder(var2.getWorldBorder().createSettings());
        this.worldData.setCustomBossEvents(this.getCustomBossEvents().save());
        this.storageSource.saveDataTag(this.registryAccess(), this.worldData, this.getPlayerList().getSingleplayerData());
        if (param1) {
            for(ServerLevel var4 : this.getAllLevels()) {
                LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", var4.getChunkSource().chunkMap.getStorageName());
            }

            LOGGER.info("ThreadedAnvilChunkStorage: All dimensions are saved");
        }

        return var0;
    }

    public boolean saveEverything(boolean param0, boolean param1, boolean param2) {
        boolean var4;
        try {
            this.isSaving = true;
            this.getPlayerList().saveAll();
            var4 = this.saveAllChunks(param0, param1, param2);
        } finally {
            this.isSaving = false;
        }

        return var4;
    }

    @Override
    public void close() {
        this.stopServer();
    }

    public void stopServer() {
        if (this.metricsRecorder.isRecording()) {
            this.cancelRecordingMetrics();
        }

        LOGGER.info("Stopping server");
        this.getConnection().stop();
        this.isSaving = true;
        if (this.playerList != null) {
            LOGGER.info("Saving players");
            this.playerList.saveAll();
            this.playerList.removeAll();
        }

        LOGGER.info("Saving worlds");

        for(ServerLevel var0 : this.getAllLevels()) {
            if (var0 != null) {
                var0.noSave = false;
            }
        }

        while(this.levels.values().stream().anyMatch(param0 -> param0.getChunkSource().chunkMap.hasWork())) {
            this.nextTickTimeNanos = Util.getNanos() + TimeUtil.NANOSECONDS_PER_MILLISECOND;

            for(ServerLevel var1 : this.getAllLevels()) {
                var1.getChunkSource().removeTicketsOnClosing();
                var1.getChunkSource().tick(() -> true, false);
            }

            this.waitUntilNextTick();
        }

        this.saveAllChunks(false, true, false);

        for(ServerLevel var2 : this.getAllLevels()) {
            if (var2 != null) {
                try {
                    var2.close();
                } catch (IOException var5) {
                    LOGGER.error("Exception closing the level", (Throwable)var5);
                }
            }
        }

        this.isSaving = false;
        this.resources.close();

        try {
            this.storageSource.close();
        } catch (IOException var4) {
            LOGGER.error("Failed to unlock level {}", this.storageSource.getLevelId(), var4);
        }

    }

    public String getLocalIp() {
        return this.localIp;
    }

    public void setLocalIp(String param0) {
        this.localIp = param0;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void halt(boolean param0) {
        this.running = false;
        if (param0) {
            try {
                this.serverThread.join();
            } catch (InterruptedException var3) {
                LOGGER.error("Error while shutting down", (Throwable)var3);
            }
        }

    }

    protected void runServer() {
        try {
            if (!this.initServer()) {
                throw new IllegalStateException("Failed to initialize server");
            }

            this.nextTickTimeNanos = Util.getNanos();
            this.statusIcon = this.loadStatusIcon().orElse(null);
            this.status = this.buildServerStatus();

            while(this.running) {
                long var0;
                if (!this.isPaused() && this.tickRateManager.isSprinting() && this.tickRateManager.checkShouldSprintThisTick()) {
                    var0 = 0L;
                    this.nextTickTimeNanos = Util.getNanos();
                    this.lastOverloadWarningNanos = this.nextTickTimeNanos;
                } else {
                    var0 = this.tickRateManager.nanosecondsPerTick();
                    long var2 = Util.getNanos() - this.nextTickTimeNanos;
                    if (var2 > OVERLOADED_THRESHOLD_NANOS + 20L * var0
                        && this.nextTickTimeNanos - this.lastOverloadWarningNanos >= OVERLOADED_WARNING_INTERVAL_NANOS + 100L * var0) {
                        long var3 = var2 / var0;
                        LOGGER.warn(
                            "Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", var2 / TimeUtil.NANOSECONDS_PER_MILLISECOND, var3
                        );
                        this.nextTickTimeNanos += var3 * var0;
                        this.lastOverloadWarningNanos = this.nextTickTimeNanos;
                    }
                }

                boolean var4 = var0 == 0L;
                if (this.debugCommandProfilerDelayStart) {
                    this.debugCommandProfilerDelayStart = false;
                    this.debugCommandProfiler = new MinecraftServer.TimeProfiler(Util.getNanos(), this.tickCount);
                }

                this.nextTickTimeNanos += var0;
                this.startMetricsRecordingTick();
                this.profiler.push("tick");
                this.tickServer(var4 ? () -> false : this::haveTime);
                this.profiler.popPush("nextTickWait");
                this.mayHaveDelayedTasks = true;
                this.delayedTasksMaxNextTickTimeNanos = Math.max(Util.getNanos() + var0, this.nextTickTimeNanos);
                this.waitUntilNextTick();
                if (var4) {
                    this.tickRateManager.endTickWork();
                }

                this.profiler.pop();
                this.endMetricsRecordingTick();
                this.isReady = true;
                JvmProfiler.INSTANCE.onServerTick(this.smoothedTickTimeMillis);
            }
        } catch (Throwable var46) {
            LOGGER.error("Encountered an unexpected exception", var46);
            CrashReport var7 = constructOrExtractCrashReport(var46);
            this.fillSystemReport(var7.getSystemReport());
            File var8 = new File(new File(this.getServerDirectory(), "crash-reports"), "crash-" + Util.getFilenameFormattedDateTime() + "-server.txt");
            if (var7.saveToFile(var8)) {
                LOGGER.error("This crash report has been saved to: {}", var8.getAbsolutePath());
            } else {
                LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.onServerCrash(var7);
        } finally {
            try {
                this.stopped = true;
                this.stopServer();
            } catch (Throwable var44) {
                LOGGER.error("Exception stopping the server", var44);
            } finally {
                if (this.services.profileCache() != null) {
                    this.services.profileCache().clearExecutor();
                }

                this.onServerExit();
            }

        }

    }

    private static CrashReport constructOrExtractCrashReport(Throwable param0) {
        ReportedException var0 = null;

        for(Throwable var1 = param0; var1 != null; var1 = var1.getCause()) {
            if (var1 instanceof ReportedException var2) {
                var0 = var2;
            }
        }

        CrashReport var3;
        if (var0 != null) {
            var3 = var0.getReport();
            if (var0 != param0) {
                var3.addCategory("Wrapped in").setDetailError("Wrapping exception", param0);
            }
        } else {
            var3 = new CrashReport("Exception in server tick loop", param0);
        }

        return var3;
    }

    private boolean haveTime() {
        return this.runningTask() || Util.getNanos() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTimeNanos : this.nextTickTimeNanos);
    }

    protected void waitUntilNextTick() {
        this.runAllTasks();
        this.managedBlock(() -> !this.haveTime());
    }

    protected TickTask wrapRunnable(Runnable param0) {
        return new TickTask(this.tickCount, param0);
    }

    protected boolean shouldRun(TickTask param0) {
        return param0.getTick() + 3 < this.tickCount || this.haveTime();
    }

    @Override
    public boolean pollTask() {
        boolean var0 = this.pollTaskInternal();
        this.mayHaveDelayedTasks = var0;
        return var0;
    }

    private boolean pollTaskInternal() {
        if (super.pollTask()) {
            return true;
        } else {
            if (this.tickRateManager.isSprinting() || this.haveTime()) {
                for(ServerLevel var0 : this.getAllLevels()) {
                    if (var0.getChunkSource().pollTask()) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    protected void doRunTask(TickTask param0) {
        this.getProfiler().incrementCounter("runTask");
        super.doRunTask(param0);
    }

    private Optional<ServerStatus.Favicon> loadStatusIcon() {
        Optional<Path> var0 = Optional.of(this.getFile("server-icon.png").toPath())
            .filter(param0 -> Files.isRegularFile(param0))
            .or(() -> this.storageSource.getIconFile().filter(param0 -> Files.isRegularFile(param0)));
        return var0.flatMap(param0 -> {
            try {
                BufferedImage var0x = ImageIO.read(param0.toFile());
                Preconditions.checkState(var0x.getWidth() == 64, "Must be 64 pixels wide");
                Preconditions.checkState(var0x.getHeight() == 64, "Must be 64 pixels high");
                ByteArrayOutputStream var1x = new ByteArrayOutputStream();
                ImageIO.write(var0x, "PNG", var1x);
                return Optional.of(new ServerStatus.Favicon(var1x.toByteArray()));
            } catch (Exception var3) {
                LOGGER.error("Couldn't load server icon", (Throwable)var3);
                return Optional.empty();
            }
        });
    }

    public Optional<Path> getWorldScreenshotFile() {
        return this.storageSource.getIconFile();
    }

    public File getServerDirectory() {
        return new File(".");
    }

    public void onServerCrash(CrashReport param0) {
    }

    public void onServerExit() {
    }

    public boolean isPaused() {
        return false;
    }

    public void tickServer(BooleanSupplier param0) {
        long var0 = Util.getNanos();
        ++this.tickCount;
        this.tickRateManager.tick();
        this.tickChildren(param0);
        if (var0 - this.lastServerStatus >= STATUS_EXPIRE_TIME_NANOS) {
            this.lastServerStatus = var0;
            this.status = this.buildServerStatus();
        }

        --this.ticksUntilAutosave;
        if (this.ticksUntilAutosave <= 0) {
            this.ticksUntilAutosave = this.computeNextAutosaveInterval();
            LOGGER.debug("Autosave started");
            this.profiler.push("save");
            this.saveEverything(true, false, false);
            this.profiler.pop();
            LOGGER.debug("Autosave finished");
        }

        this.profiler.push("tallying");
        long var1 = Util.getNanos() - var0;
        int var2 = this.tickCount % 100;
        this.aggregatedTickTimesNanos -= this.tickTimesNanos[var2];
        this.aggregatedTickTimesNanos += var1;
        this.tickTimesNanos[var2] = var1;
        this.smoothedTickTimeMillis = this.smoothedTickTimeMillis * 0.8F + (float)var1 / (float)TimeUtil.NANOSECONDS_PER_MILLISECOND * 0.19999999F;
        long var3 = Util.getNanos();
        this.logTickTime(var3 - var0);
        this.profiler.pop();
    }

    private int computeNextAutosaveInterval() {
        float var1;
        if (this.tickRateManager.isSprinting()) {
            long var0 = this.getAverageTickTimeNanos() + 1L;
            var1 = (float)TimeUtil.NANOSECONDS_PER_SECOND / (float)var0;
        } else {
            var1 = this.tickRateManager.tickrate();
        }

        int var3 = 300;
        return Math.max(100, (int)(var1 * 300.0F));
    }

    public void onTickRateChanged() {
        int var0 = this.computeNextAutosaveInterval();
        if (var0 < this.ticksUntilAutosave) {
            this.ticksUntilAutosave = var0;
        }

    }

    protected void logTickTime(long param0) {
    }

    private ServerStatus buildServerStatus() {
        ServerStatus.Players var0 = this.buildPlayerStatus();
        return new ServerStatus(
            Component.nullToEmpty(this.motd),
            Optional.of(var0),
            Optional.of(ServerStatus.Version.current()),
            Optional.ofNullable(this.statusIcon),
            this.enforceSecureProfile()
        );
    }

    private ServerStatus.Players buildPlayerStatus() {
        List<ServerPlayer> var0 = this.playerList.getPlayers();
        int var1 = this.getMaxPlayers();
        if (this.hidesOnlinePlayers()) {
            return new ServerStatus.Players(var1, var0.size(), List.of());
        } else {
            int var2 = Math.min(var0.size(), 12);
            ObjectArrayList<GameProfile> var3 = new ObjectArrayList<>(var2);
            int var4 = Mth.nextInt(this.random, 0, var0.size() - var2);

            for(int var5 = 0; var5 < var2; ++var5) {
                ServerPlayer var6 = var0.get(var4 + var5);
                var3.add(var6.allowsListing() ? var6.getGameProfile() : ANONYMOUS_PLAYER_PROFILE);
            }

            Util.shuffle(var3, this.random);
            return new ServerStatus.Players(var1, var0.size(), var3);
        }
    }

    public void tickChildren(BooleanSupplier param0) {
        this.getPlayerList().getPlayers().forEach(param0x -> param0x.connection.suspendFlushing());
        this.profiler.push("commandFunctions");
        this.getFunctions().tick();
        this.profiler.popPush("levels");

        for(ServerLevel var0 : this.getAllLevels()) {
            this.profiler.push(() -> var0 + " " + var0.dimension().location());
            if (this.tickCount % 20 == 0) {
                this.profiler.push("timeSync");
                this.synchronizeTime(var0);
                this.profiler.pop();
            }

            this.profiler.push("tick");

            try {
                var0.tick(param0);
            } catch (Throwable var6) {
                CrashReport var2 = CrashReport.forThrowable(var6, "Exception ticking world");
                var0.fillReportDetails(var2);
                throw new ReportedException(var2);
            }

            this.profiler.pop();
            this.profiler.pop();
        }

        this.profiler.popPush("connection");
        this.getConnection().tick();
        this.profiler.popPush("players");
        this.playerList.tick();
        if (SharedConstants.IS_RUNNING_IN_IDE && this.tickRateManager.runsNormally()) {
            GameTestTicker.SINGLETON.tick();
        }

        this.profiler.popPush("server gui refresh");

        for(int var3 = 0; var3 < this.tickables.size(); ++var3) {
            this.tickables.get(var3).run();
        }

        this.profiler.popPush("send chunks");

        for(ServerPlayer var4 : this.playerList.getPlayers()) {
            var4.connection.chunkSender.sendNextChunks(var4);
            var4.connection.resumeFlushing();
        }

        this.profiler.pop();
    }

    private void synchronizeTime(ServerLevel param0) {
        this.playerList
            .broadcastAll(
                new ClientboundSetTimePacket(param0.getGameTime(), param0.getDayTime(), param0.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)),
                param0.dimension()
            );
    }

    public void forceTimeSynchronization() {
        this.profiler.push("timeSync");

        for(ServerLevel var0 : this.getAllLevels()) {
            this.synchronizeTime(var0);
        }

        this.profiler.pop();
    }

    public boolean isNetherEnabled() {
        return true;
    }

    public void addTickable(Runnable param0) {
        this.tickables.add(param0);
    }

    protected void setId(String param0) {
        this.serverId = param0;
    }

    public boolean isShutdown() {
        return !this.serverThread.isAlive();
    }

    public File getFile(String param0) {
        return new File(this.getServerDirectory(), param0);
    }

    public final ServerLevel overworld() {
        return this.levels.get(Level.OVERWORLD);
    }

    @Nullable
    public ServerLevel getLevel(ResourceKey<Level> param0) {
        return this.levels.get(param0);
    }

    public Set<ResourceKey<Level>> levelKeys() {
        return this.levels.keySet();
    }

    public Iterable<ServerLevel> getAllLevels() {
        return this.levels.values();
    }

    @Override
    public String getServerVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    @Override
    public int getPlayerCount() {
        return this.playerList.getPlayerCount();
    }

    @Override
    public int getMaxPlayers() {
        return this.playerList.getMaxPlayers();
    }

    public String[] getPlayerNames() {
        return this.playerList.getPlayerNamesArray();
    }

    @DontObfuscate
    public String getServerModName() {
        return "vanilla";
    }

    public SystemReport fillSystemReport(SystemReport param0) {
        param0.setDetail("Server Running", () -> Boolean.toString(this.running));
        if (this.playerList != null) {
            param0.setDetail(
                "Player Count", () -> this.playerList.getPlayerCount() + " / " + this.playerList.getMaxPlayers() + "; " + this.playerList.getPlayers()
            );
        }

        param0.setDetail(
            "Data Packs",
            () -> this.packRepository
                    .getSelectedPacks()
                    .stream()
                    .map(param0x -> param0x.getId() + (param0x.getCompatibility().isCompatible() ? "" : " (incompatible)"))
                    .collect(Collectors.joining(", "))
        );
        param0.setDetail(
            "Enabled Feature Flags",
            () -> FeatureFlags.REGISTRY.toNames(this.worldData.enabledFeatures()).stream().map(ResourceLocation::toString).collect(Collectors.joining(", "))
        );
        param0.setDetail("World Generation", () -> this.worldData.worldGenSettingsLifecycle().toString());
        if (this.serverId != null) {
            param0.setDetail("Server Id", () -> this.serverId);
        }

        return this.fillServerSystemReport(param0);
    }

    public abstract SystemReport fillServerSystemReport(SystemReport var1);

    public ModCheck getModdedStatus() {
        return ModCheck.identify("vanilla", this::getServerModName, "Server", MinecraftServer.class);
    }

    @Override
    public void sendSystemMessage(Component param0) {
        LOGGER.info(param0.getString());
    }

    public KeyPair getKeyPair() {
        return this.keyPair;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int param0) {
        this.port = param0;
    }

    @Nullable
    public GameProfile getSingleplayerProfile() {
        return this.singleplayerProfile;
    }

    public void setSingleplayerProfile(@Nullable GameProfile param0) {
        this.singleplayerProfile = param0;
    }

    public boolean isSingleplayer() {
        return this.singleplayerProfile != null;
    }

    protected void initializeKeyPair() {
        LOGGER.info("Generating keypair");

        try {
            this.keyPair = Crypt.generateKeyPair();
        } catch (CryptException var2) {
            throw new IllegalStateException("Failed to generate key pair", var2);
        }
    }

    public void setDifficulty(Difficulty param0, boolean param1) {
        if (param1 || !this.worldData.isDifficultyLocked()) {
            this.worldData.setDifficulty(this.worldData.isHardcore() ? Difficulty.HARD : param0);
            this.updateMobSpawningFlags();
            this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
        }
    }

    public int getScaledTrackingDistance(int param0) {
        return param0;
    }

    private void updateMobSpawningFlags() {
        for(ServerLevel var0 : this.getAllLevels()) {
            var0.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());
        }

    }

    public void setDifficultyLocked(boolean param0) {
        this.worldData.setDifficultyLocked(param0);
        this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
    }

    private void sendDifficultyUpdate(ServerPlayer param0x) {
        LevelData var0 = param0x.level().getLevelData();
        param0x.connection.send(new ClientboundChangeDifficultyPacket(var0.getDifficulty(), var0.isDifficultyLocked()));
    }

    public boolean isSpawningMonsters() {
        return this.worldData.getDifficulty() != Difficulty.PEACEFUL;
    }

    public boolean isDemo() {
        return this.isDemo;
    }

    public void setDemo(boolean param0) {
        this.isDemo = param0;
    }

    public Optional<MinecraftServer.ServerResourcePackInfo> getServerResourcePack() {
        return Optional.empty();
    }

    public boolean isResourcePackRequired() {
        return this.getServerResourcePack().filter(MinecraftServer.ServerResourcePackInfo::isRequired).isPresent();
    }

    public abstract boolean isDedicatedServer();

    public abstract int getRateLimitPacketsPerSecond();

    public boolean usesAuthentication() {
        return this.onlineMode;
    }

    public void setUsesAuthentication(boolean param0) {
        this.onlineMode = param0;
    }

    public boolean getPreventProxyConnections() {
        return this.preventProxyConnections;
    }

    public void setPreventProxyConnections(boolean param0) {
        this.preventProxyConnections = param0;
    }

    public boolean isSpawningAnimals() {
        return true;
    }

    public boolean areNpcsEnabled() {
        return true;
    }

    public abstract boolean isEpollEnabled();

    public boolean isPvpAllowed() {
        return this.pvp;
    }

    public void setPvpAllowed(boolean param0) {
        this.pvp = param0;
    }

    public boolean isFlightAllowed() {
        return this.allowFlight;
    }

    public void setFlightAllowed(boolean param0) {
        this.allowFlight = param0;
    }

    public abstract boolean isCommandBlockEnabled();

    @Override
    public String getMotd() {
        return this.motd;
    }

    public void setMotd(String param0) {
        this.motd = param0;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public PlayerList getPlayerList() {
        return this.playerList;
    }

    public void setPlayerList(PlayerList param0) {
        this.playerList = param0;
    }

    public abstract boolean isPublished();

    public void setDefaultGameType(GameType param0) {
        this.worldData.setGameType(param0);
    }

    public ServerConnectionListener getConnection() {
        return this.connection;
    }

    public boolean isReady() {
        return this.isReady;
    }

    public boolean hasGui() {
        return false;
    }

    public boolean publishServer(@Nullable GameType param0, boolean param1, int param2) {
        return false;
    }

    public int getTickCount() {
        return this.tickCount;
    }

    public int getSpawnProtectionRadius() {
        return 16;
    }

    public boolean isUnderSpawnProtection(ServerLevel param0, BlockPos param1, Player param2) {
        return false;
    }

    public boolean repliesToStatus() {
        return true;
    }

    public boolean hidesOnlinePlayers() {
        return false;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public int getPlayerIdleTimeout() {
        return this.playerIdleTimeout;
    }

    public void setPlayerIdleTimeout(int param0) {
        this.playerIdleTimeout = param0;
    }

    public MinecraftSessionService getSessionService() {
        return this.services.sessionService();
    }

    @Nullable
    public SignatureValidator getProfileKeySignatureValidator() {
        return this.services.profileKeySignatureValidator();
    }

    public GameProfileRepository getProfileRepository() {
        return this.services.profileRepository();
    }

    @Nullable
    public GameProfileCache getProfileCache() {
        return this.services.profileCache();
    }

    @Nullable
    public ServerStatus getStatus() {
        return this.status;
    }

    public void invalidateStatus() {
        this.lastServerStatus = 0L;
    }

    public int getAbsoluteMaxWorldSize() {
        return 29999984;
    }

    @Override
    public boolean scheduleExecutables() {
        return super.scheduleExecutables() && !this.isStopped();
    }

    @Override
    public void executeIfPossible(Runnable param0) {
        if (this.isStopped()) {
            throw new RejectedExecutionException("Server already shutting down");
        } else {
            super.executeIfPossible(param0);
        }
    }

    @Override
    public Thread getRunningThread() {
        return this.serverThread;
    }

    public int getCompressionThreshold() {
        return 256;
    }

    public boolean enforceSecureProfile() {
        return false;
    }

    public long getNextTickTime() {
        return this.nextTickTimeNanos;
    }

    public DataFixer getFixerUpper() {
        return this.fixerUpper;
    }

    public int getSpawnRadius(@Nullable ServerLevel param0) {
        return param0 != null ? param0.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS) : 10;
    }

    public ServerAdvancementManager getAdvancements() {
        return this.resources.managers.getAdvancements();
    }

    public ServerFunctionManager getFunctions() {
        return this.functionManager;
    }

    public CompletableFuture<Void> reloadResources(Collection<String> param0) {
        RegistryAccess.Frozen var0 = this.registries.getAccessForLoading(RegistryLayer.RELOADABLE);
        CompletableFuture<Void> var1 = CompletableFuture.<ImmutableList>supplyAsync(
                () -> param0.stream().map(this.packRepository::getPack).filter(Objects::nonNull).map(Pack::open).collect(ImmutableList.toImmutableList()), this
            )
            .thenCompose(
                param1 -> {
                    CloseableResourceManager var0x = new MultiPackResourceManager(PackType.SERVER_DATA, param1);
                    return ReloadableServerResources.loadResources(
                            var0x,
                            var0,
                            this.worldData.enabledFeatures(),
                            this.isDedicatedServer() ? Commands.CommandSelection.DEDICATED : Commands.CommandSelection.INTEGRATED,
                            this.getFunctionCompilationLevel(),
                            this.executor,
                            this
                        )
                        .whenComplete((param1x, param2) -> {
                            if (param2 != null) {
                                var0x.close();
                            }
            
                        })
                        .thenApply(param1x -> new MinecraftServer.ReloadableResources(var0x, param1x));
                }
            )
            .thenAcceptAsync(param1 -> {
                this.resources.close();
                this.resources = param1;
                this.packRepository.setSelected(param0);
                WorldDataConfiguration var0x = new WorldDataConfiguration(getSelectedPacks(this.packRepository), this.worldData.enabledFeatures());
                this.worldData.setDataConfiguration(var0x);
                this.resources.managers.updateRegistryTags(this.registryAccess());
                this.getPlayerList().saveAll();
                this.getPlayerList().reloadResources();
                this.functionManager.replaceLibrary(this.resources.managers.getFunctionLibrary());
                this.structureTemplateManager.onResourceManagerReload(this.resources.resourceManager);
            }, this);
        if (this.isSameThread()) {
            this.managedBlock(var1::isDone);
        }

        return var1;
    }

    public static WorldDataConfiguration configurePackRepository(PackRepository param0, DataPackConfig param1, boolean param2, FeatureFlagSet param3) {
        param0.reload();
        if (param2) {
            param0.setSelected(Collections.singleton("vanilla"));
            return WorldDataConfiguration.DEFAULT;
        } else {
            Set<String> var0 = Sets.newLinkedHashSet();

            for(String var1 : param1.getEnabled()) {
                if (param0.isAvailable(var1)) {
                    var0.add(var1);
                } else {
                    LOGGER.warn("Missing data pack {}", var1);
                }
            }

            for(Pack var2 : param0.getAvailablePacks()) {
                String var3 = var2.getId();
                if (!param1.getDisabled().contains(var3)) {
                    FeatureFlagSet var4 = var2.getRequestedFeatures();
                    boolean var5 = var0.contains(var3);
                    if (!var5 && var2.getPackSource().shouldAddAutomatically()) {
                        if (var4.isSubsetOf(param3)) {
                            LOGGER.info("Found new data pack {}, loading it automatically", var3);
                            var0.add(var3);
                        } else {
                            LOGGER.info(
                                "Found new data pack {}, but can't load it due to missing features {}", var3, FeatureFlags.printMissingFlags(param3, var4)
                            );
                        }
                    }

                    if (var5 && !var4.isSubsetOf(param3)) {
                        LOGGER.warn(
                            "Pack {} requires features {} that are not enabled for this world, disabling pack.",
                            var3,
                            FeatureFlags.printMissingFlags(param3, var4)
                        );
                        var0.remove(var3);
                    }
                }
            }

            if (var0.isEmpty()) {
                LOGGER.info("No datapacks selected, forcing vanilla");
                var0.add("vanilla");
            }

            param0.setSelected(var0);
            DataPackConfig var6 = getSelectedPacks(param0);
            FeatureFlagSet var7 = param0.getRequestedFeatureFlags();
            return new WorldDataConfiguration(var6, var7);
        }
    }

    private static DataPackConfig getSelectedPacks(PackRepository param0) {
        Collection<String> var0 = param0.getSelectedIds();
        List<String> var1 = ImmutableList.copyOf(var0);
        List<String> var2 = param0.getAvailableIds().stream().filter(param1 -> !var0.contains(param1)).collect(ImmutableList.toImmutableList());
        return new DataPackConfig(var1, var2);
    }

    public void kickUnlistedPlayers(CommandSourceStack param0) {
        if (this.isEnforceWhitelist()) {
            PlayerList var0 = param0.getServer().getPlayerList();
            UserWhiteList var1 = var0.getWhiteList();

            for(ServerPlayer var3 : Lists.newArrayList(var0.getPlayers())) {
                if (!var1.isWhiteListed(var3.getGameProfile())) {
                    var3.connection.disconnect(Component.translatable("multiplayer.disconnect.not_whitelisted"));
                }
            }

        }
    }

    public PackRepository getPackRepository() {
        return this.packRepository;
    }

    public Commands getCommands() {
        return this.resources.managers.getCommands();
    }

    public CommandSourceStack createCommandSourceStack() {
        ServerLevel var0 = this.overworld();
        return new CommandSourceStack(
            this,
            var0 == null ? Vec3.ZERO : Vec3.atLowerCornerOf(var0.getSharedSpawnPos()),
            Vec2.ZERO,
            var0,
            4,
            "Server",
            Component.literal("Server"),
            this,
            null
        );
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public abstract boolean shouldInformAdmins();

    public RecipeManager getRecipeManager() {
        return this.resources.managers.getRecipeManager();
    }

    public ServerScoreboard getScoreboard() {
        return this.scoreboard;
    }

    public CommandStorage getCommandStorage() {
        if (this.commandStorage == null) {
            throw new NullPointerException("Called before server init");
        } else {
            return this.commandStorage;
        }
    }

    public LootDataManager getLootData() {
        return this.resources.managers.getLootData();
    }

    public GameRules getGameRules() {
        return this.overworld().getGameRules();
    }

    public CustomBossEvents getCustomBossEvents() {
        return this.customBossEvents;
    }

    public boolean isEnforceWhitelist() {
        return this.enforceWhitelist;
    }

    public void setEnforceWhitelist(boolean param0) {
        this.enforceWhitelist = param0;
    }

    public float getCurrentSmoothedTickTime() {
        return this.smoothedTickTimeMillis;
    }

    public ServerTickRateManager tickRateManager() {
        return this.tickRateManager;
    }

    public long getAverageTickTimeNanos() {
        return this.aggregatedTickTimesNanos / (long)Math.min(100, Math.max(this.tickCount, 1));
    }

    public long[] getTickTimesNanos() {
        return this.tickTimesNanos;
    }

    public int getProfilePermissions(GameProfile param0) {
        if (this.getPlayerList().isOp(param0)) {
            ServerOpListEntry var0 = this.getPlayerList().getOps().get(param0);
            if (var0 != null) {
                return var0.getLevel();
            } else if (this.isSingleplayerOwner(param0)) {
                return 4;
            } else if (this.isSingleplayer()) {
                return this.getPlayerList().isAllowCheatsForAllPlayers() ? 4 : 0;
            } else {
                return this.getOperatorUserPermissionLevel();
            }
        } else {
            return 0;
        }
    }

    public ProfilerFiller getProfiler() {
        return this.profiler;
    }

    public abstract boolean isSingleplayerOwner(GameProfile var1);

    public void dumpServerProperties(Path param0) throws IOException {
    }

    private void saveDebugReport(Path param0) {
        Path var0 = param0.resolve("levels");

        try {
            for(Entry<ResourceKey<Level>, ServerLevel> var1 : this.levels.entrySet()) {
                ResourceLocation var2 = var1.getKey().location();
                Path var3 = var0.resolve(var2.getNamespace()).resolve(var2.getPath());
                Files.createDirectories(var3);
                var1.getValue().saveDebugReport(var3);
            }

            this.dumpGameRules(param0.resolve("gamerules.txt"));
            this.dumpClasspath(param0.resolve("classpath.txt"));
            this.dumpMiscStats(param0.resolve("stats.txt"));
            this.dumpThreads(param0.resolve("threads.txt"));
            this.dumpServerProperties(param0.resolve("server.properties.txt"));
            this.dumpNativeModules(param0.resolve("modules.txt"));
        } catch (IOException var7) {
            LOGGER.warn("Failed to save debug report", (Throwable)var7);
        }

    }

    private void dumpMiscStats(Path param0) throws IOException {
        try (Writer var0 = Files.newBufferedWriter(param0)) {
            var0.write(String.format(Locale.ROOT, "pending_tasks: %d\n", this.getPendingTasksCount()));
            var0.write(String.format(Locale.ROOT, "average_tick_time: %f\n", this.getCurrentSmoothedTickTime()));
            var0.write(String.format(Locale.ROOT, "tick_times: %s\n", Arrays.toString(this.tickTimesNanos)));
            var0.write(String.format(Locale.ROOT, "queue: %s\n", Util.backgroundExecutor()));
        }

    }

    private void dumpGameRules(Path param0) throws IOException {
        try (Writer var0 = Files.newBufferedWriter(param0)) {
            final List<String> var1 = Lists.newArrayList();
            final GameRules var2 = this.getGameRules();
            GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
                @Override
                public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> param0, GameRules.Type<T> param1) {
                    var1.add(String.format(Locale.ROOT, "%s=%s\n", param0.getId(), var2.<T>getRule(param0)));
                }
            });

            for(String var3 : var1) {
                var0.write(var3);
            }
        }

    }

    private void dumpClasspath(Path param0) throws IOException {
        try (Writer var0 = Files.newBufferedWriter(param0)) {
            String var1 = System.getProperty("java.class.path");
            String var2 = System.getProperty("path.separator");

            for(String var3 : Splitter.on(var2).split(var1)) {
                var0.write(var3);
                var0.write("\n");
            }
        }

    }

    private void dumpThreads(Path param0) throws IOException {
        ThreadMXBean var0 = ManagementFactory.getThreadMXBean();
        ThreadInfo[] var1 = var0.dumpAllThreads(true, true);
        Arrays.sort(var1, Comparator.comparing(ThreadInfo::getThreadName));

        try (Writer var2 = Files.newBufferedWriter(param0)) {
            for(ThreadInfo var3 : var1) {
                var2.write(var3.toString());
                var2.write(10);
            }
        }

    }

    private void dumpNativeModules(Path param0) throws IOException {
        try (Writer var0 = Files.newBufferedWriter(param0)) {
            List<NativeModuleLister.NativeModuleInfo> var1;
            try {
                var1 = Lists.newArrayList(NativeModuleLister.listModules());
            } catch (Throwable var7) {
                LOGGER.warn("Failed to list native modules", var7);
                return;
            }

            var1.sort(Comparator.comparing(param0x -> param0x.name));

            for(NativeModuleLister.NativeModuleInfo var4 : var1) {
                var0.write(var4.toString());
                var0.write(10);
            }

        }
    }

    private void startMetricsRecordingTick() {
        if (this.willStartRecordingMetrics) {
            this.metricsRecorder = ActiveMetricsRecorder.createStarted(
                new ServerMetricsSamplersProvider(Util.timeSource, this.isDedicatedServer()),
                Util.timeSource,
                Util.ioPool(),
                new MetricsPersister("server"),
                this.onMetricsRecordingStopped,
                param0 -> {
                    this.executeBlocking(() -> this.saveDebugReport(param0.resolve("server")));
                    this.onMetricsRecordingFinished.accept(param0);
                }
            );
            this.willStartRecordingMetrics = false;
        }

        this.profiler = SingleTickProfiler.decorateFiller(this.metricsRecorder.getProfiler(), SingleTickProfiler.createTickProfiler("Server"));
        this.metricsRecorder.startTick();
        this.profiler.startTick();
    }

    private void endMetricsRecordingTick() {
        this.profiler.endTick();
        this.metricsRecorder.endTick();
    }

    public boolean isRecordingMetrics() {
        return this.metricsRecorder.isRecording();
    }

    public void startRecordingMetrics(Consumer<ProfileResults> param0, Consumer<Path> param1) {
        this.onMetricsRecordingStopped = param1x -> {
            this.stopRecordingMetrics();
            param0.accept(param1x);
        };
        this.onMetricsRecordingFinished = param1;
        this.willStartRecordingMetrics = true;
    }

    public void stopRecordingMetrics() {
        this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    }

    public void finishRecordingMetrics() {
        this.metricsRecorder.end();
    }

    public void cancelRecordingMetrics() {
        this.metricsRecorder.cancel();
        this.profiler = this.metricsRecorder.getProfiler();
    }

    public Path getWorldPath(LevelResource param0) {
        return this.storageSource.getLevelPath(param0);
    }

    public boolean forceSynchronousWrites() {
        return true;
    }

    public StructureTemplateManager getStructureManager() {
        return this.structureTemplateManager;
    }

    public WorldData getWorldData() {
        return this.worldData;
    }

    public RegistryAccess.Frozen registryAccess() {
        return this.registries.compositeAccess();
    }

    public LayeredRegistryAccess<RegistryLayer> registries() {
        return this.registries;
    }

    public TextFilter createTextFilterForPlayer(ServerPlayer param0) {
        return TextFilter.DUMMY;
    }

    public ServerPlayerGameMode createGameModeForPlayer(ServerPlayer param0) {
        return (ServerPlayerGameMode)(this.isDemo() ? new DemoMode(param0) : new ServerPlayerGameMode(param0));
    }

    @Nullable
    public GameType getForcedGameType() {
        return null;
    }

    public ResourceManager getResourceManager() {
        return this.resources.resourceManager;
    }

    public boolean isCurrentlySaving() {
        return this.isSaving;
    }

    public boolean isTimeProfilerRunning() {
        return this.debugCommandProfilerDelayStart || this.debugCommandProfiler != null;
    }

    public void startTimeProfiler() {
        this.debugCommandProfilerDelayStart = true;
    }

    public ProfileResults stopTimeProfiler() {
        if (this.debugCommandProfiler == null) {
            return EmptyProfileResults.EMPTY;
        } else {
            ProfileResults var0 = this.debugCommandProfiler.stop(Util.getNanos(), this.tickCount);
            this.debugCommandProfiler = null;
            return var0;
        }
    }

    public int getMaxChainedNeighborUpdates() {
        return 1000000;
    }

    public void logChatMessage(Component param0, ChatType.Bound param1, @Nullable String param2) {
        String var0 = param1.decorate(param0).getString();
        if (param2 != null) {
            LOGGER.info("[{}] {}", param2, var0);
        } else {
            LOGGER.info("{}", var0);
        }

    }

    public ChatDecorator getChatDecorator() {
        return ChatDecorator.PLAIN;
    }

    public boolean logIPs() {
        return true;
    }

    static record ReloadableResources(CloseableResourceManager resourceManager, ReloadableServerResources managers) implements AutoCloseable {
        @Override
        public void close() {
            this.resourceManager.close();
        }
    }

    public static record ServerResourcePackInfo(UUID id, String url, String hash, boolean isRequired, @Nullable Component prompt) {
    }

    static class TimeProfiler {
        final long startNanos;
        final int startTick;

        TimeProfiler(long param0, int param1) {
            this.startNanos = param0;
            this.startTick = param1;
        }

        ProfileResults stop(final long param0, final int param1) {
            return new ProfileResults() {
                @Override
                public List<ResultField> getTimes(String param0x) {
                    return Collections.emptyList();
                }

                @Override
                public boolean saveResults(Path param0x) {
                    return false;
                }

                @Override
                public long getStartTimeNano() {
                    return TimeProfiler.this.startNanos;
                }

                @Override
                public int getStartTimeTicks() {
                    return TimeProfiler.this.startTick;
                }

                @Override
                public long getEndTimeNano() {
                    return param0;
                }

                @Override
                public int getEndTimeTicks() {
                    return param1;
                }

                @Override
                public String getProfilerResults() {
                    return "";
                }
            };
        }
    }
}
