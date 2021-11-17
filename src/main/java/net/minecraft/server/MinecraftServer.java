package net.minecraft.server;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
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
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.ModCheck;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
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
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class MinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements CommandSource, AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String VANILLA_BRAND = "vanilla";
    private static final float AVERAGE_TICK_TIME_SMOOTHING = 0.8F;
    private static final int TICK_STATS_SPAN = 100;
    public static final int MS_PER_TICK = 50;
    private static final int OVERLOADED_THRESHOLD = 2000;
    private static final int OVERLOADED_WARNING_INTERVAL = 15000;
    public static final String LEVEL_STORAGE_PROTOCOL = "level";
    public static final String LEVEL_STORAGE_SCHEMA = "level://";
    private static final long STATUS_EXPIRE_TIME_NS = 5000000000L;
    private static final int MAX_STATUS_PLAYER_SAMPLE = 12;
    public static final String MAP_RESOURCE_FILE = "resources.zip";
    public static final File USERID_CACHE_FILE = new File("usercache.json");
    public static final int START_CHUNK_RADIUS = 11;
    private static final int START_TICKING_CHUNK_COUNT = 441;
    private static final int AUTOSAVE_INTERVAL = 6000;
    private static final int MAX_TICK_LATENCY = 3;
    public static final int ABSOLUTE_MAX_WORLD_SIZE = 29999984;
    public static final LevelSettings DEMO_SETTINGS = new LevelSettings(
        "Demo World", GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(), DataPackConfig.DEFAULT
    );
    private static final long DELAYED_TASKS_TICK_EXTENSION = 50L;
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
    private final ServerStatus status = new ServerStatus();
    private final Random random = new Random();
    private final DataFixer fixerUpper;
    private String localIp;
    private int port = -1;
    protected final RegistryAccess.RegistryHolder registryHolder;
    private final Map<ResourceKey<Level>, ServerLevel> levels = Maps.newLinkedHashMap();
    private PlayerList playerList;
    private volatile boolean running = true;
    private boolean stopped;
    private int tickCount;
    protected final Proxy proxy;
    private boolean onlineMode;
    private boolean preventProxyConnections;
    private boolean pvp;
    private boolean allowFlight;
    @Nullable
    private String motd;
    private int playerIdleTimeout;
    public final long[] tickTimes = new long[100];
    @Nullable
    private KeyPair keyPair;
    @Nullable
    private String singleplayerName;
    private boolean isDemo;
    private String resourcePack = "";
    private String resourcePackHash = "";
    private volatile boolean isReady;
    private long lastOverloadWarning;
    private final MinecraftSessionService sessionService;
    @Nullable
    private final GameProfileRepository profileRepository;
    @Nullable
    private final GameProfileCache profileCache;
    private long lastServerStatus;
    private final Thread serverThread;
    private long nextTickTime = Util.getMillis();
    private long delayedTasksMaxNextTickTime;
    private boolean mayHaveDelayedTasks;
    private final PackRepository packRepository;
    private final ServerScoreboard scoreboard = new ServerScoreboard(this);
    @Nullable
    private CommandStorage commandStorage;
    private final CustomBossEvents customBossEvents = new CustomBossEvents();
    private final ServerFunctionManager functionManager;
    private final FrameTimer frameTimer = new FrameTimer();
    private boolean enforceWhitelist;
    private float averageTickTime;
    private final Executor executor;
    @Nullable
    private String serverId;
    private ServerResources resources;
    private final StructureManager structureManager;
    protected final WorldData worldData;
    private volatile boolean isSaving;

    public static <S extends MinecraftServer> S spin(Function<Thread, S> param0) {
        AtomicReference<S> var0 = new AtomicReference<>();
        Thread var1 = new Thread(() -> var0.get().runServer(), "Server thread");
        var1.setUncaughtExceptionHandler((param0x, param1) -> LOGGER.error(param1));
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
        RegistryAccess.RegistryHolder param1,
        LevelStorageSource.LevelStorageAccess param2,
        WorldData param3,
        PackRepository param4,
        Proxy param5,
        DataFixer param6,
        ServerResources param7,
        @Nullable MinecraftSessionService param8,
        @Nullable GameProfileRepository param9,
        @Nullable GameProfileCache param10,
        ChunkProgressListenerFactory param11
    ) {
        super("Server");
        this.registryHolder = param1;
        this.worldData = param3;
        this.proxy = param5;
        this.packRepository = param4;
        this.resources = param7;
        this.sessionService = param8;
        this.profileRepository = param9;
        this.profileCache = param10;
        if (param10 != null) {
            param10.setExecutor(this);
        }

        this.connection = new ServerConnectionListener(this);
        this.progressListenerFactory = param11;
        this.storageSource = param2;
        this.playerDataStorage = param2.createPlayerStorage();
        this.fixerUpper = param6;
        this.functionManager = new ServerFunctionManager(this, param7.getFunctionLibrary());
        this.structureManager = new StructureManager(param7.getResourceManager(), param2, param6);
        this.serverThread = param0;
        this.executor = Util.backgroundExecutor();
    }

    private void readScoreboard(DimensionDataStorage param0) {
        param0.computeIfAbsent(this.getScoreboard()::createData, this.getScoreboard()::createData, "scoreboard");
    }

    protected abstract boolean initServer() throws IOException;

    protected void loadLevel() {
        if (!JvmProfiler.INSTANCE.isRunning()) {
        }

        boolean var0 = false;
        ProfiledDuration var1 = JvmProfiler.INSTANCE.onWorldLoadedStarted();
        this.detectBundledResources();
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
        WorldGenSettings var1 = this.worldData.worldGenSettings();
        boolean var2 = var1.isDebug();
        long var3 = var1.seed();
        long var4 = BiomeManager.obfuscateSeed(var3);
        List<CustomSpawner> var5 = ImmutableList.of(
            new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(var0)
        );
        MappedRegistry<LevelStem> var6 = var1.dimensions();
        LevelStem var7 = var6.get(LevelStem.OVERWORLD);
        ChunkGenerator var9;
        DimensionType var8;
        if (var7 == null) {
            var8 = this.registryHolder.<DimensionType>registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).getOrThrow(DimensionType.OVERWORLD_LOCATION);
            var9 = WorldGenSettings.makeDefaultOverworld(this.registryHolder, new Random().nextLong());
        } else {
            var8 = var7.type();
            var9 = var7.generator();
        }

        ServerLevel var12 = new ServerLevel(this, this.executor, this.storageSource, var0, Level.OVERWORLD, var8, param0, var9, var2, var4, var5, true);
        this.levels.put(Level.OVERWORLD, var12);
        DimensionDataStorage var13 = var12.getDataStorage();
        this.readScoreboard(var13);
        this.commandStorage = new CommandStorage(var13);
        WorldBorder var14 = var12.getWorldBorder();
        if (!var0.isInitialized()) {
            try {
                setInitialSpawn(var12, var0, var1.generateBonusChest(), var2);
                var0.setInitialized(true);
                if (var2) {
                    this.setupDebugLevel(this.worldData);
                }
            } catch (Throwable var26) {
                CrashReport var16 = CrashReport.forThrowable(var26, "Exception initializing level");

                try {
                    var12.fillReportDetails(var16);
                } catch (Throwable var25) {
                }

                throw new ReportedException(var16);
            }

            var0.setInitialized(true);
        }

        this.getPlayerList().addWorldborderListener(var12);
        if (this.worldData.getCustomBossEvents() != null) {
            this.getCustomBossEvents().load(this.worldData.getCustomBossEvents());
        }

        for(Entry<ResourceKey<LevelStem>, LevelStem> var17 : var6.entrySet()) {
            ResourceKey<LevelStem> var18 = var17.getKey();
            if (var18 != LevelStem.OVERWORLD) {
                ResourceKey<Level> var19 = ResourceKey.create(Registry.DIMENSION_REGISTRY, var18.location());
                DimensionType var20 = var17.getValue().type();
                ChunkGenerator var21 = var17.getValue().generator();
                DerivedLevelData var22 = new DerivedLevelData(this.worldData, var0);
                ServerLevel var23 = new ServerLevel(
                    this, this.executor, this.storageSource, var22, var19, var20, param0, var21, var2, var4, ImmutableList.of(), false
                );
                var14.addListener(new BorderChangeListener.DelegateBorderChangeListener(var23.getWorldBorder()));
                this.levels.put(var19, var23);
            }
        }

        var14.applySettings(var0.getWorldBorder());
    }

    private static void setInitialSpawn(ServerLevel param0, ServerLevelData param1, boolean param2, boolean param3) {
        if (param3) {
            param1.setSpawn(BlockPos.ZERO.above(80), 0.0F);
        } else {
            ChunkGenerator var0 = param0.getChunkSource().getGenerator();
            ChunkPos var1 = new ChunkPos(var0.climateSampler().findSpawnPosition());
            int var2 = var0.getSpawnHeight(param0);
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
                ConfiguredFeature<?, ?> var12 = MiscOverworldFeatures.BONUS_CHEST;
                var12.place(param0, var0, param0.random, new BlockPos(param1.getXSpawn(), param1.getYSpawn(), param1.getZSpawn()));
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
        var2.getLightEngine().setTaskPerBatch(500);
        this.nextTickTime = Util.getMillis();
        var2.addRegionTicket(TicketType.START, new ChunkPos(var1), 11, Unit.INSTANCE);

        while(var2.getTickingGenerated() != 441) {
            this.nextTickTime = Util.getMillis() + 10L;
            this.waitUntilNextTick();
        }

        this.nextTickTime = Util.getMillis() + 10L;
        this.waitUntilNextTick();

        for(ServerLevel var3 : this.levels.values()) {
            ForcedChunksSavedData var4 = var3.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
            if (var4 != null) {
                LongIterator var5 = var4.getChunks().iterator();

                while(var5.hasNext()) {
                    long var6 = var5.nextLong();
                    ChunkPos var7 = new ChunkPos(var6);
                    var3.getChunkSource().updateChunkForced(var7, true);
                }
            }
        }

        this.nextTickTime = Util.getMillis() + 10L;
        this.waitUntilNextTick();
        param0.stop();
        var2.getLightEngine().setTaskPerBatch(5);
        this.updateMobSpawningFlags();
    }

    protected void detectBundledResources() {
        File var0 = this.storageSource.getLevelPath(LevelResource.MAP_RESOURCE_FILE).toFile();
        if (var0.isFile()) {
            String var1 = this.storageSource.getLevelId();

            try {
                this.setResourcePack("level://" + URLEncoder.encode(var1, StandardCharsets.UTF_8.toString()) + "/resources.zip", "");
            } catch (UnsupportedEncodingException var4) {
                LOGGER.warn("Something went wrong url encoding {}", var1);
            }
        }

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
        this.storageSource.saveDataTag(this.registryHolder, this.worldData, this.getPlayerList().getSingleplayerData());
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
        LOGGER.info("Stopping server");
        if (this.getConnection() != null) {
            this.getConnection().stop();
        }

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

        this.saveAllChunks(false, true, false);

        for(ServerLevel var1 : this.getAllLevels()) {
            if (var1 != null) {
                try {
                    var1.close();
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
            if (this.initServer()) {
                this.nextTickTime = Util.getMillis();
                this.status.setDescription(new TextComponent(this.motd));
                this.status
                    .setVersion(
                        new ServerStatus.Version(SharedConstants.getCurrentVersion().getName(), SharedConstants.getCurrentVersion().getProtocolVersion())
                    );
                this.updateStatusIcon(this.status);

                while(this.running) {
                    long var0 = Util.getMillis() - this.nextTickTime;
                    if (var0 > 2000L && this.nextTickTime - this.lastOverloadWarning >= 15000L) {
                        long var1 = var0 / 50L;
                        LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", var0, var1);
                        this.nextTickTime += var1 * 50L;
                        this.lastOverloadWarning = this.nextTickTime;
                    }

                    if (this.debugCommandProfilerDelayStart) {
                        this.debugCommandProfilerDelayStart = false;
                        this.debugCommandProfiler = new MinecraftServer.TimeProfiler(Util.getNanos(), this.tickCount);
                    }

                    this.nextTickTime += 50L;
                    this.startMetricsRecordingTick();
                    this.profiler.push("tick");
                    this.tickServer(this::haveTime);
                    this.profiler.popPush("nextTickWait");
                    this.mayHaveDelayedTasks = true;
                    this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + 50L, this.nextTickTime);
                    this.waitUntilNextTick();
                    this.profiler.pop();
                    this.endMetricsRecordingTick();
                    this.isReady = true;
                    JvmProfiler.INSTANCE.onServerTick(this.averageTickTime);
                }
            } else {
                this.onServerCrash(null);
            }
        } catch (Throwable var44) {
            LOGGER.error("Encountered an unexpected exception", var44);
            CrashReport var4;
            if (var44 instanceof ReportedException) {
                var4 = ((ReportedException)var44).getReport();
            } else {
                var4 = new CrashReport("Exception in server tick loop", var44);
            }

            this.fillSystemReport(var4.getSystemReport());
            File var6 = new File(
                new File(this.getServerDirectory(), "crash-reports"), "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt"
            );
            if (var4.saveToFile(var6)) {
                LOGGER.error("This crash report has been saved to: {}", var6.getAbsolutePath());
            } else {
                LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.onServerCrash(var4);
        } finally {
            try {
                this.stopped = true;
                this.stopServer();
            } catch (Throwable var42) {
                LOGGER.error("Exception stopping the server", var42);
            } finally {
                if (this.profileCache != null) {
                    this.profileCache.clearExecutor();
                }

                this.onServerExit();
            }

        }

    }

    private boolean haveTime() {
        return this.runningTask() || Util.getMillis() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTime : this.nextTickTime);
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
            if (this.haveTime()) {
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

    private void updateStatusIcon(ServerStatus param0) {
        Optional<File> var0 = Optional.of(this.getFile("server-icon.png")).filter(File::isFile);
        if (!var0.isPresent()) {
            var0 = this.storageSource.getIconFile().map(Path::toFile).filter(File::isFile);
        }

        var0.ifPresent(param1 -> {
            try {
                BufferedImage var0x = ImageIO.read(param1);
                Validate.validState(var0x.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(var0x.getHeight() == 64, "Must be 64 pixels high");
                ByteArrayOutputStream var1x = new ByteArrayOutputStream();
                ImageIO.write(var0x, "PNG", var1x);
                byte[] var2x = Base64.getEncoder().encode(var1x.toByteArray());
                param0.setFavicon("data:image/png;base64," + new String(var2x, StandardCharsets.UTF_8));
            } catch (Exception var5) {
                LOGGER.error("Couldn't load server icon", (Throwable)var5);
            }

        });
    }

    public Optional<Path> getWorldScreenshotFile() {
        return this.storageSource.getIconFile();
    }

    public File getServerDirectory() {
        return new File(".");
    }

    protected void onServerCrash(CrashReport param0) {
    }

    public void onServerExit() {
    }

    public void tickServer(BooleanSupplier param0) {
        long var0 = Util.getNanos();
        ++this.tickCount;
        this.tickChildren(param0);
        if (var0 - this.lastServerStatus >= 5000000000L) {
            this.lastServerStatus = var0;
            this.status.setPlayers(new ServerStatus.Players(this.getMaxPlayers(), this.getPlayerCount()));
            if (!this.hidesOnlinePlayers()) {
                GameProfile[] var1 = new GameProfile[Math.min(this.getPlayerCount(), 12)];
                int var2 = Mth.nextInt(this.random, 0, this.getPlayerCount() - var1.length);

                for(int var3 = 0; var3 < var1.length; ++var3) {
                    ServerPlayer var4 = this.playerList.getPlayers().get(var2 + var3);
                    if (var4.allowsListing()) {
                        var1[var3] = var4.getGameProfile();
                    } else {
                        var1[var3] = ANONYMOUS_PLAYER_PROFILE;
                    }
                }

                Collections.shuffle(Arrays.asList(var1));
                this.status.getPlayers().setSample(var1);
            }
        }

        if (this.tickCount % 6000 == 0) {
            LOGGER.debug("Autosave started");
            this.profiler.push("save");
            this.saveEverything(true, false, false);
            this.profiler.pop();
            LOGGER.debug("Autosave finished");
        }

        this.profiler.push("tallying");
        long var5 = this.tickTimes[this.tickCount % 100] = Util.getNanos() - var0;
        this.averageTickTime = this.averageTickTime * 0.8F + (float)var5 / 1000000.0F * 0.19999999F;
        long var6 = Util.getNanos();
        this.frameTimer.logFrameDuration(var6 - var0);
        this.profiler.pop();
    }

    public void tickChildren(BooleanSupplier param0) {
        this.profiler.push("commandFunctions");
        this.getFunctions().tick();
        this.profiler.popPush("levels");

        for(ServerLevel var0 : this.getAllLevels()) {
            this.profiler.push(() -> var0 + " " + var0.dimension().location());
            if (this.tickCount % 20 == 0) {
                this.profiler.push("timeSync");
                this.playerList
                    .broadcastAll(
                        new ClientboundSetTimePacket(var0.getGameTime(), var0.getDayTime(), var0.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)),
                        var0.dimension()
                    );
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
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            GameTestTicker.SINGLETON.tick();
        }

        this.profiler.popPush("server gui refresh");

        for(int var3 = 0; var3 < this.tickables.size(); ++var3) {
            this.tickables.get(var3).run();
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

    public String getServerVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    public int getPlayerCount() {
        return this.playerList.getPlayerCount();
    }

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
        if (this.playerList != null) {
            param0.setDetail(
                "Player Count", () -> this.playerList.getPlayerCount() + " / " + this.playerList.getMaxPlayers() + "; " + this.playerList.getPlayers()
            );
        }

        param0.setDetail("Data Packs", () -> {
            StringBuilder var0 = new StringBuilder();

            for(Pack var1x : this.packRepository.getSelectedPacks()) {
                if (var0.length() > 0) {
                    var0.append(", ");
                }

                var0.append(var1x.getId());
                if (!var1x.getCompatibility().isCompatible()) {
                    var0.append(" (incompatible)");
                }
            }

            return var0.toString();
        });
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
    public void sendMessage(Component param0, UUID param1) {
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

    public String getSingleplayerName() {
        return this.singleplayerName;
    }

    public void setSingleplayerName(String param0) {
        this.singleplayerName = param0;
    }

    public boolean isSingleplayer() {
        return this.singleplayerName != null;
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
        LevelData var0 = param0x.getLevel().getLevelData();
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

    public String getResourcePack() {
        return this.resourcePack;
    }

    public String getResourcePackHash() {
        return this.resourcePackHash;
    }

    public void setResourcePack(String param0, String param1) {
        this.resourcePack = param0;
        this.resourcePackHash = param1;
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

    @Nullable
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
        return this.sessionService;
    }

    public GameProfileRepository getProfileRepository() {
        return this.profileRepository;
    }

    public GameProfileCache getProfileCache() {
        return this.profileCache;
    }

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
    public Thread getRunningThread() {
        return this.serverThread;
    }

    public int getCompressionThreshold() {
        return 256;
    }

    public long getNextTickTime() {
        return this.nextTickTime;
    }

    public DataFixer getFixerUpper() {
        return this.fixerUpper;
    }

    public int getSpawnRadius(@Nullable ServerLevel param0) {
        return param0 != null ? param0.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS) : 10;
    }

    public ServerAdvancementManager getAdvancements() {
        return this.resources.getAdvancements();
    }

    public ServerFunctionManager getFunctions() {
        return this.functionManager;
    }

    public CompletableFuture<Void> reloadResources(Collection<String> param0) {
        CompletableFuture<Void> var0 = CompletableFuture.<ImmutableList>supplyAsync(
                () -> param0.stream().map(this.packRepository::getPack).filter(Objects::nonNull).map(Pack::open).collect(ImmutableList.toImmutableList()), this
            )
            .thenCompose(
                param0x -> ServerResources.loadResources(
                        param0x,
                        this.registryHolder,
                        this.isDedicatedServer() ? Commands.CommandSelection.DEDICATED : Commands.CommandSelection.INTEGRATED,
                        this.getFunctionCompilationLevel(),
                        this.executor,
                        this
                    )
            )
            .thenAcceptAsync(param1 -> {
                this.resources.close();
                this.resources = param1;
                this.packRepository.setSelected(param0);
                this.worldData.setDataPackConfig(getSelectedPacks(this.packRepository));
                param1.updateGlobals();
                this.getPlayerList().saveAll();
                this.getPlayerList().reloadResources();
                this.functionManager.replaceLibrary(this.resources.getFunctionLibrary());
                this.structureManager.onResourceManagerReload(this.resources.getResourceManager());
            }, this);
        if (this.isSameThread()) {
            this.managedBlock(var0::isDone);
        }

        return var0;
    }

    public static DataPackConfig configurePackRepository(PackRepository param0, DataPackConfig param1, boolean param2) {
        param0.reload();
        if (param2) {
            param0.setSelected(Collections.singleton("vanilla"));
            return new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of());
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
                if (!param1.getDisabled().contains(var3) && !var0.contains(var3)) {
                    LOGGER.info("Found new data pack {}, loading it automatically", var3);
                    var0.add(var3);
                }
            }

            if (var0.isEmpty()) {
                LOGGER.info("No datapacks selected, forcing vanilla");
                var0.add("vanilla");
            }

            param0.setSelected(var0);
            return getSelectedPacks(param0);
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
                    var3.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.not_whitelisted"));
                }
            }

        }
    }

    public PackRepository getPackRepository() {
        return this.packRepository;
    }

    public Commands getCommands() {
        return this.resources.getCommands();
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
            new TextComponent("Server"),
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
        return this.resources.getRecipeManager();
    }

    public TagContainer getTags() {
        return this.resources.getTags();
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

    public LootTables getLootTables() {
        return this.resources.getLootTables();
    }

    public PredicateManager getPredicateManager() {
        return this.resources.getPredicateManager();
    }

    public ItemModifierManager getItemModifierManager() {
        return this.resources.getItemModifierManager();
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

    public float getAverageTickTime() {
        return this.averageTickTime;
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

    public FrameTimer getFrameTimer() {
        return this.frameTimer;
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
            var0.write(String.format("pending_tasks: %d\n", this.getPendingTasksCount()));
            var0.write(String.format("average_tick_time: %f\n", this.getAverageTickTime()));
            var0.write(String.format("tick_times: %s\n", Arrays.toString(this.tickTimes)));
            var0.write(String.format("queue: %s\n", Util.backgroundExecutor()));
        }

    }

    private void dumpGameRules(Path param0) throws IOException {
        try (Writer var0 = Files.newBufferedWriter(param0)) {
            final List<String> var1 = Lists.newArrayList();
            final GameRules var2 = this.getGameRules();
            GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
                @Override
                public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> param0, GameRules.Type<T> param1) {
                    var1.add(String.format("%s=%s\n", param0.getId(), var2.<T>getRule(param0)));
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

    public Path getWorldPath(LevelResource param0) {
        return this.storageSource.getLevelPath(param0);
    }

    public boolean forceSynchronousWrites() {
        return true;
    }

    public StructureManager getStructureManager() {
        return this.structureManager;
    }

    public WorldData getWorldData() {
        return this.worldData;
    }

    public RegistryAccess registryAccess() {
        return this.registryHolder;
    }

    public TextFilter createTextFilterForPlayer(ServerPlayer param0) {
        return TextFilter.DUMMY;
    }

    public boolean isResourcePackRequired() {
        return false;
    }

    public ServerPlayerGameMode createGameModeForPlayer(ServerPlayer param0) {
        return (ServerPlayerGameMode)(this.isDemo() ? new DemoMode(param0) : new ServerPlayerGameMode(param0));
    }

    @Nullable
    public GameType getForcedGameType() {
        return null;
    }

    public ResourceManager getResourceManager() {
        return this.resources.getResourceManager();
    }

    @Nullable
    public Component getResourcePackPrompt() {
        return null;
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
