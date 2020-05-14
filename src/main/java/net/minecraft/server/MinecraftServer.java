package net.minecraft.server;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.DerivedServerLevel;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.repository.UnopenedPack;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagManager;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Snooper;
import net.minecraft.world.SnooperPopulator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.saveddata.SaveDataDirtyRunnable;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.ScoreboardSaveData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class MinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements SnooperPopulator, CommandSource, AutoCloseable, Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final File USERID_CACHE_FILE = new File("usercache.json");
    private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
    public static final LevelSettings DEMO_SETTINGS = new LevelSettings(
        "Demo World", GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(), WorldGenSettings.DEMO_SETTINGS
    );
    protected final LevelStorageSource.LevelStorageAccess storageSource;
    protected final PlayerDataStorage playerDataStorage;
    private final Snooper snooper = new Snooper("server", this, Util.getMillis());
    private final List<Runnable> tickables = Lists.newArrayList();
    private ContinuousProfiler continousProfiler = new ContinuousProfiler(Util.timeSource, this::getTickCount);
    private ProfilerFiller profiler = InactiveProfiler.INSTANCE;
    private final ServerConnectionListener connection;
    protected final ChunkProgressListenerFactory progressListenerFactory;
    private final ServerStatus status = new ServerStatus();
    private final Random random = new Random();
    private final DataFixer fixerUpper;
    private String localIp;
    private int port = -1;
    private final Map<DimensionType, ServerLevel> levels = Maps.newIdentityHashMap();
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
    private int maxBuildHeight;
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
    private boolean delayProfilerStart;
    private boolean forceGameType;
    private final MinecraftSessionService sessionService;
    private final GameProfileRepository profileRepository;
    private final GameProfileCache profileCache;
    private long lastServerStatus;
    protected final Thread serverThread = Util.make(
        new Thread(this, "Server thread"), param0x -> param0x.setUncaughtExceptionHandler((param0xx, param1x) -> LOGGER.error(param1x))
    );
    private long nextTickTime = Util.getMillis();
    private long delayedTasksMaxNextTickTime;
    private boolean mayHaveDelayedTasks;
    @OnlyIn(Dist.CLIENT)
    private boolean hasWorldScreenshot;
    private final ReloadableResourceManager resources = new SimpleReloadableResourceManager(PackType.SERVER_DATA, this.serverThread);
    private final PackRepository<UnopenedPack> packRepository = new PackRepository<>(UnopenedPack::new);
    @Nullable
    private FolderRepositorySource folderPackSource;
    private final Commands commands;
    private final RecipeManager recipes = new RecipeManager();
    private final TagManager tags = new TagManager();
    private final ServerScoreboard scoreboard = new ServerScoreboard(this);
    @Nullable
    private CommandStorage commandStorage;
    private final CustomBossEvents customBossEvents = new CustomBossEvents(this);
    private final PredicateManager predicateManager = new PredicateManager();
    private final LootTables lootTables = new LootTables(this.predicateManager);
    private final ServerAdvancementManager advancements = new ServerAdvancementManager(this.predicateManager);
    private final ServerFunctionManager functions = new ServerFunctionManager(this);
    private final FrameTimer frameTimer = new FrameTimer();
    private boolean enforceWhitelist;
    private float averageTickTime;
    private final Executor executor;
    @Nullable
    private String serverId;
    private final StructureManager structureManager;
    protected final WorldData worldData;

    public MinecraftServer(
        LevelStorageSource.LevelStorageAccess param0,
        WorldData param1,
        Proxy param2,
        DataFixer param3,
        Commands param4,
        MinecraftSessionService param5,
        GameProfileRepository param6,
        GameProfileCache param7,
        ChunkProgressListenerFactory param8
    ) {
        super("Server");
        this.worldData = param1;
        this.proxy = param2;
        this.commands = param4;
        this.sessionService = param5;
        this.profileRepository = param6;
        this.profileCache = param7;
        this.connection = new ServerConnectionListener(this);
        this.progressListenerFactory = param8;
        this.storageSource = param0;
        this.playerDataStorage = param0.createPlayerStorage();
        this.fixerUpper = param3;
        this.resources.registerReloadListener(this.tags);
        this.resources.registerReloadListener(this.predicateManager);
        this.resources.registerReloadListener(this.recipes);
        this.resources.registerReloadListener(this.lootTables);
        this.resources.registerReloadListener(this.functions);
        this.resources.registerReloadListener(this.advancements);
        this.executor = Util.backgroundExecutor();
        this.structureManager = new StructureManager(this, param0, param3);
    }

    private void readScoreboard(DimensionDataStorage param0) {
        ScoreboardSaveData var0 = param0.computeIfAbsent(ScoreboardSaveData::new, "scoreboard");
        var0.setScoreboard(this.getScoreboard());
        this.getScoreboard().addDirtyListener(new SaveDataDirtyRunnable(var0));
    }

    protected abstract boolean initServer() throws IOException;

    public static void ensureLevelConversion(
        LevelStorageSource.LevelStorageAccess param0, DataFixer param1, boolean param2, boolean param3, BooleanSupplier param4
    ) {
        if (param0.requiresConversion()) {
            LOGGER.info("Converting map!");
            param0.convertLevel(new ProgressListener() {
                private long timeStamp = Util.getMillis();

                @Override
                public void progressStartNoAbort(Component param0) {
                }

                @OnlyIn(Dist.CLIENT)
                @Override
                public void progressStart(Component param0) {
                }

                @Override
                public void progressStagePercentage(int param0) {
                    if (Util.getMillis() - this.timeStamp >= 1000L) {
                        this.timeStamp = Util.getMillis();
                        MinecraftServer.LOGGER.info("Converting... {}%", param0);
                    }

                }

                @OnlyIn(Dist.CLIENT)
                @Override
                public void stop() {
                }

                @Override
                public void progressStage(Component param0) {
                }
            });
        }

        if (param2) {
            LOGGER.info("Forcing world upgrade!");
            WorldData var0 = param0.getDataTag();
            if (var0 != null) {
                WorldUpgrader var1 = new WorldUpgrader(param0, param1, var0, param3);
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

                    if (!param4.getAsBoolean()) {
                        var1.cancel();
                    } else {
                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException var11) {
                        }
                    }
                }
            }
        }

    }

    protected void loadLevel() {
        this.detectBundledResources();
        this.worldData.setModdedInfo(this.getServerModName(), this.getModdedStatus().isPresent());
        this.loadDataPacks();
        ChunkProgressListener var0 = this.progressListenerFactory.create(11);
        this.createLevels(var0);
        this.forceDifficulty();
        this.prepareLevels(var0);
    }

    protected void forceDifficulty() {
    }

    protected void createLevels(ChunkProgressListener param0) {
        ServerLevelData var0 = this.worldData.overworldData();
        WorldGenSettings var1 = this.worldData.worldGenSettings();
        boolean var2 = var1.isDebug();
        long var3 = var1.seed();
        long var4 = BiomeManager.obfuscateSeed(var3);
        ServerLevel var5 = new ServerLevel(this, this.executor, this.storageSource, var0, DimensionType.OVERWORLD, param0, var1.overworld(), var2, var4);
        this.levels.put(DimensionType.OVERWORLD, var5);
        DimensionDataStorage var6 = var5.getDataStorage();
        this.readScoreboard(var6);
        this.commandStorage = new CommandStorage(var6);
        var5.getWorldBorder().applySettings(var0.getWorldBorder());
        ServerLevel var7 = this.getLevel(DimensionType.OVERWORLD);
        if (!var0.isInitialized()) {
            try {
                setInitialSpawn(var7, var7.getDimension(), var0, var1.generateBonusChest(), var2);
                var0.setInitialized(true);
                if (var2) {
                    this.setupDebugLevel(this.worldData);
                }
            } catch (Throwable var16) {
                CrashReport var9 = CrashReport.forThrowable(var16, "Exception initializing level");

                try {
                    var7.fillReportDetails(var9);
                } catch (Throwable var15) {
                }

                throw new ReportedException(var9);
            }

            var0.setInitialized(true);
        }

        this.getPlayerList().setLevel(var7);
        if (this.worldData.getCustomBossEvents() != null) {
            this.getCustomBossEvents().load(this.worldData.getCustomBossEvents());
        }

        for(Entry<DimensionType, ChunkGenerator> var10 : var1.generators().entrySet()) {
            DimensionType var11 = var10.getKey();
            if (var11 != DimensionType.OVERWORLD) {
                this.levels
                    .put(
                        var11,
                        new DerivedServerLevel(
                            var7, this.worldData.overworldData(), this, this.executor, this.storageSource, var11, param0, var10.getValue(), var2, var4
                        )
                    );
            }
        }

    }

    private static void setInitialSpawn(ServerLevel param0, Dimension param1, ServerLevelData param2, boolean param3, boolean param4) {
        ChunkGenerator var0 = param0.getChunkSource().getGenerator();
        if (!param1.mayRespawn()) {
            param2.setSpawn(BlockPos.ZERO.above(var0.getSpawnHeight()));
        } else if (param4) {
            param2.setSpawn(BlockPos.ZERO.above());
        } else {
            BiomeSource var1 = var0.getBiomeSource();
            List<Biome> var2 = var1.getPlayerSpawnBiomes();
            Random var3 = new Random(param0.getSeed());
            BlockPos var4 = var1.findBiomeHorizontal(0, param0.getSeaLevel(), 0, 256, var2, var3);
            ChunkPos var5 = var4 == null ? new ChunkPos(0, 0) : new ChunkPos(var4);
            if (var4 == null) {
                LOGGER.warn("Unable to find spawn biome");
            }

            boolean var6 = false;

            for(Block var7 : BlockTags.VALID_SPAWN.getValues()) {
                if (var1.getSurfaceBlocks().contains(var7.defaultBlockState())) {
                    var6 = true;
                    break;
                }
            }

            param2.setSpawn(var5.getWorldPosition().offset(8, var0.getSpawnHeight(), 8));
            int var8 = 0;
            int var9 = 0;
            int var10 = 0;
            int var11 = -1;
            int var12 = 32;

            for(int var13 = 0; var13 < 1024; ++var13) {
                if (var8 > -16 && var8 <= 16 && var9 > -16 && var9 <= 16) {
                    BlockPos var14 = param1.getSpawnPosInChunk(param0.getSeed(), new ChunkPos(var5.x + var8, var5.z + var9), var6);
                    if (var14 != null) {
                        param2.setSpawn(var14);
                        break;
                    }
                }

                if (var8 == var9 || var8 < 0 && var8 == -var9 || var8 > 0 && var8 == 1 - var9) {
                    int var15 = var10;
                    var10 = -var11;
                    var11 = var15;
                }

                var8 += var10;
                var9 += var11;
            }

            if (param3) {
                ConfiguredFeature<?, ?> var16 = Feature.BONUS_CHEST.configured(FeatureConfiguration.NONE);
                var16.place(
                    param0, param0.structureFeatureManager(), var0, param0.random, new BlockPos(param2.getXSpawn(), param2.getYSpawn(), param2.getZSpawn())
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

    private void loadDataPacks() {
        this.packRepository.addSource(new ServerPacksSource());
        this.folderPackSource = new FolderRepositorySource(this.storageSource.getLevelPath(LevelResource.DATAPACK_DIR).toFile());
        this.packRepository.addSource(this.folderPackSource);
        this.packRepository.reload();
        List<UnopenedPack> var0 = Lists.newArrayList();

        for(String var1 : this.worldData.getEnabledDataPacks()) {
            UnopenedPack var2 = this.packRepository.getPack(var1);
            if (var2 != null) {
                var0.add(var2);
            } else {
                LOGGER.warn("Missing data pack {}", var1);
            }
        }

        this.packRepository.setSelected(var0);
        this.updateSelectedPacks();
        this.refreshRegistries();
    }

    private void prepareLevels(ChunkProgressListener param0) {
        ServerLevel var0 = this.getLevel(DimensionType.OVERWORLD);
        LOGGER.info("Preparing start region for dimension " + DimensionType.getName(var0.dimensionType()));
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

        for(DimensionType var3 : DimensionType.getAllTypes()) {
            ForcedChunksSavedData var4 = this.getLevel(var3).getDataStorage().get(ForcedChunksSavedData::new, "chunks");
            if (var4 != null) {
                ServerLevel var5 = this.getLevel(var3);
                LongIterator var6 = var4.getChunks().iterator();

                while(var6.hasNext()) {
                    long var7 = var6.nextLong();
                    ChunkPos var8 = new ChunkPos(var7);
                    var5.getChunkSource().updateChunkForced(var8, true);
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
                this.setResourcePack("level://" + URLEncoder.encode(var1, StandardCharsets.UTF_8.toString()) + "/" + "resources.zip", "");
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
                LOGGER.info("Saving chunks for level '{}'/{}", var1, DimensionType.getName(var1.dimensionType()));
            }

            var1.save(null, param1, var1.noSave && !param2);
            var0 = true;
        }

        ServerLevel var2 = this.getLevel(DimensionType.OVERWORLD);
        ServerLevelData var3 = this.worldData.overworldData();
        var3.setWorldBorder(var2.getWorldBorder().createSettings());
        this.worldData.setCustomBossEvents(this.getCustomBossEvents().save());
        this.storageSource.saveDataTag(this.worldData, this.getPlayerList().getSingleplayerData());
        return var0;
    }

    @Override
    public void close() {
        this.stopServer();
    }

    protected void stopServer() {
        LOGGER.info("Stopping server");
        if (this.getConnection() != null) {
            this.getConnection().stop();
        }

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

        if (this.snooper.isStarted()) {
            this.snooper.interrupt();
        }

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

    @Override
    public void run() {
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

                    this.nextTickTime += 50L;
                    SingleTickProfiler var2 = SingleTickProfiler.createTickProfiler("Server");
                    this.startProfilerTick(var2);
                    this.profiler.startTick();
                    this.profiler.push("tick");
                    this.tickServer(this::haveTime);
                    this.profiler.popPush("nextTickWait");
                    this.mayHaveDelayedTasks = true;
                    this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + 50L, this.nextTickTime);
                    this.waitUntilNextTick();
                    this.profiler.pop();
                    this.profiler.endTick();
                    this.endProfilerTick(var2);
                    this.isReady = true;
                }
            } else {
                this.onServerCrash(null);
            }
        } catch (Throwable var44) {
            LOGGER.error("Encountered an unexpected exception", var44);
            CrashReport var5;
            if (var44 instanceof ReportedException) {
                var5 = this.fillReport(((ReportedException)var44).getReport());
            } else {
                var5 = this.fillReport(new CrashReport("Exception in server tick loop", var44));
            }

            File var7 = new File(
                new File(this.getServerDirectory(), "crash-reports"), "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt"
            );
            if (var5.saveToFile(var7)) {
                LOGGER.error("This crash report has been saved to: {}", var7.getAbsolutePath());
            } else {
                LOGGER.error("We were unable to save this crash report to disk.");
            }

            this.onServerCrash(var5);
        } finally {
            try {
                this.stopped = true;
                this.stopServer();
            } catch (Throwable var42) {
                LOGGER.error("Exception stopping the server", var42);
            } finally {
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

    public void updateStatusIcon(ServerStatus param0) {
        File var0 = this.getFile("server-icon.png");
        if (!var0.exists()) {
            var0 = this.storageSource.getIconFile();
        }

        if (var0.isFile()) {
            ByteBuf var1 = Unpooled.buffer();

            try {
                BufferedImage var2 = ImageIO.read(var0);
                Validate.validState(var2.getWidth() == 64, "Must be 64 pixels wide");
                Validate.validState(var2.getHeight() == 64, "Must be 64 pixels high");
                ImageIO.write(var2, "PNG", new ByteBufOutputStream(var1));
                ByteBuffer var3 = Base64.getEncoder().encode(var1.nioBuffer());
                param0.setFavicon("data:image/png;base64," + StandardCharsets.UTF_8.decode(var3));
            } catch (Exception var9) {
                LOGGER.error("Couldn't load server icon", (Throwable)var9);
            } finally {
                var1.release();
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public boolean hasWorldScreenshot() {
        this.hasWorldScreenshot = this.hasWorldScreenshot || this.getWorldScreenshotFile().isFile();
        return this.hasWorldScreenshot;
    }

    @OnlyIn(Dist.CLIENT)
    public File getWorldScreenshotFile() {
        return this.storageSource.getIconFile();
    }

    public File getServerDirectory() {
        return new File(".");
    }

    protected void onServerCrash(CrashReport param0) {
    }

    protected void onServerExit() {
    }

    protected void tickServer(BooleanSupplier param0) {
        long var0 = Util.getNanos();
        ++this.tickCount;
        this.tickChildren(param0);
        if (var0 - this.lastServerStatus >= 5000000000L) {
            this.lastServerStatus = var0;
            this.status.setPlayers(new ServerStatus.Players(this.getMaxPlayers(), this.getPlayerCount()));
            GameProfile[] var1 = new GameProfile[Math.min(this.getPlayerCount(), 12)];
            int var2 = Mth.nextInt(this.random, 0, this.getPlayerCount() - var1.length);

            for(int var3 = 0; var3 < var1.length; ++var3) {
                var1[var3] = this.playerList.getPlayers().get(var2 + var3).getGameProfile();
            }

            Collections.shuffle(Arrays.asList(var1));
            this.status.getPlayers().setSample(var1);
        }

        if (this.tickCount % 6000 == 0) {
            LOGGER.debug("Autosave started");
            this.profiler.push("save");
            this.playerList.saveAll();
            this.saveAllChunks(true, false, false);
            this.profiler.pop();
            LOGGER.debug("Autosave finished");
        }

        this.profiler.push("snooper");
        if (!this.snooper.isStarted() && this.tickCount > 100) {
            this.snooper.start();
        }

        if (this.tickCount % 6000 == 0) {
            this.snooper.prepare();
        }

        this.profiler.pop();
        this.profiler.push("tallying");
        long var4 = this.tickTimes[this.tickCount % 100] = Util.getNanos() - var0;
        this.averageTickTime = this.averageTickTime * 0.8F + (float)var4 / 1000000.0F * 0.19999999F;
        long var5 = Util.getNanos();
        this.frameTimer.logFrameDuration(var5 - var0);
        this.profiler.pop();
    }

    protected void tickChildren(BooleanSupplier param0) {
        this.profiler.push("commandFunctions");
        this.getFunctions().tick();
        this.profiler.popPush("levels");

        for(ServerLevel var0 : this.getAllLevels()) {
            if (var0.dimensionType() == DimensionType.OVERWORLD || this.isNetherEnabled()) {
                this.profiler.push(() -> var0 + " " + Registry.DIMENSION_TYPE.getKey(var0.dimensionType()));
                if (this.tickCount % 20 == 0) {
                    this.profiler.push("timeSync");
                    this.playerList
                        .broadcastAll(
                            new ClientboundSetTimePacket(var0.getGameTime(), var0.getDayTime(), var0.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)),
                            var0.dimensionType()
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
        }

        this.profiler.popPush("connection");
        this.getConnection().tick();
        this.profiler.popPush("players");
        this.playerList.tick();
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            GameTestTicker.singleton.tick();
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

    public void forkAndRun() {
        this.serverThread.start();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isShutdown() {
        return !this.serverThread.isAlive();
    }

    public File getFile(String param0) {
        return new File(this.getServerDirectory(), param0);
    }

    public ServerLevel getLevel(DimensionType param0) {
        return this.levels.get(param0);
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

    public String getServerModName() {
        return "vanilla";
    }

    public CrashReport fillReport(CrashReport param0) {
        if (this.playerList != null) {
            param0.getSystemDetails()
                .setDetail(
                    "Player Count", () -> this.playerList.getPlayerCount() + " / " + this.playerList.getMaxPlayers() + "; " + this.playerList.getPlayers()
                );
        }

        param0.getSystemDetails().setDetail("Data Packs", () -> {
            StringBuilder var0 = new StringBuilder();

            for(UnopenedPack var1x : this.packRepository.getSelected()) {
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
            param0.getSystemDetails().setDetail("Server Id", () -> this.serverId);
        }

        return param0;
    }

    public abstract Optional<String> getModdedStatus();

    @Override
    public void sendMessage(Component param0) {
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

    public void setKeyPair(KeyPair param0) {
        this.keyPair = param0;
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

    protected boolean isSpawningMonsters() {
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

    @Override
    public void populateSnooper(Snooper param0) {
        param0.setDynamicData("whitelist_enabled", false);
        param0.setDynamicData("whitelist_count", 0);
        if (this.playerList != null) {
            param0.setDynamicData("players_current", this.getPlayerCount());
            param0.setDynamicData("players_max", this.getMaxPlayers());
            param0.setDynamicData("players_seen", this.playerDataStorage.getSeenPlayers().length);
        }

        param0.setDynamicData("uses_auth", this.onlineMode);
        param0.setDynamicData("gui_state", this.hasGui() ? "enabled" : "disabled");
        param0.setDynamicData("run_time", (Util.getMillis() - param0.getStartupTime()) / 60L * 1000L);
        param0.setDynamicData("avg_tick_ms", (int)(Mth.average(this.tickTimes) * 1.0E-6));
        int var0 = 0;

        for(ServerLevel var1 : this.getAllLevels()) {
            if (var1 != null) {
                param0.setDynamicData("world[" + var0 + "][dimension]", var1.dimensionType());
                param0.setDynamicData("world[" + var0 + "][mode]", this.worldData.getGameType());
                param0.setDynamicData("world[" + var0 + "][difficulty]", var1.getDifficulty());
                param0.setDynamicData("world[" + var0 + "][hardcore]", this.worldData.isHardcore());
                param0.setDynamicData("world[" + var0 + "][height]", this.maxBuildHeight);
                param0.setDynamicData("world[" + var0 + "][chunks_loaded]", var1.getChunkSource().getLoadedChunksCount());
                ++var0;
            }
        }

        param0.setDynamicData("worlds", var0);
    }

    public abstract boolean isDedicatedServer();

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

    public int getMaxBuildHeight() {
        return this.maxBuildHeight;
    }

    public void setMaxBuildHeight(int param0) {
        this.maxBuildHeight = param0;
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

    @OnlyIn(Dist.CLIENT)
    public boolean isReady() {
        return this.isReady;
    }

    public boolean hasGui() {
        return false;
    }

    public abstract boolean publishServer(GameType var1, boolean var2, int var3);

    public int getTickCount() {
        return this.tickCount;
    }

    @OnlyIn(Dist.CLIENT)
    public Snooper getSnooper() {
        return this.snooper;
    }

    public int getSpawnProtectionRadius() {
        return 16;
    }

    public boolean isUnderSpawnProtection(ServerLevel param0, BlockPos param1, Player param2) {
        return false;
    }

    public void setForceGameType(boolean param0) {
        this.forceGameType = param0;
    }

    public boolean getForceGameType() {
        return this.forceGameType;
    }

    public boolean repliesToStatus() {
        return true;
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
        return this.advancements;
    }

    public ServerFunctionManager getFunctions() {
        return this.functions;
    }

    public void reloadResources() {
        if (!this.isSameThread()) {
            this.execute(this::reloadResources);
        } else {
            this.getPlayerList().saveAll();
            this.packRepository.reload();
            this.updateSelectedPacks();
            this.getPlayerList().reloadResources();
            this.refreshRegistries();
        }
    }

    private void updateSelectedPacks() {
        List<UnopenedPack> var0 = Lists.newArrayList(this.packRepository.getSelected());

        for(UnopenedPack var1 : this.packRepository.getAvailable()) {
            if (!this.worldData.getDisabledDataPacks().contains(var1.getId()) && !var0.contains(var1)) {
                LOGGER.info("Found new data pack {}, loading it automatically", var1.getId());
                var1.getDefaultPosition().insert(var0, var1, param0 -> param0, false);
            }
        }

        this.packRepository.setSelected(var0);
        List<Pack> var2 = Lists.newArrayList();
        this.packRepository.getSelected().forEach(param1 -> var2.add(param1.open()));
        CompletableFuture<Unit> var3 = this.resources.reload(this.executor, this, var2, DATA_RELOAD_INITIAL_TASK);
        this.managedBlock(var3::isDone);

        try {
            var3.get();
        } catch (Exception var5) {
            LOGGER.error("Failed to reload data packs", (Throwable)var5);
        }

        this.worldData.getEnabledDataPacks().clear();
        this.worldData.getDisabledDataPacks().clear();
        this.packRepository.getSelected().forEach(param0 -> this.worldData.getEnabledDataPacks().add(param0.getId()));
        this.packRepository.getAvailable().forEach(param0 -> {
            if (!this.packRepository.getSelected().contains(param0)) {
                this.worldData.getDisabledDataPacks().add(param0.getId());
            }

        });
    }

    public void kickUnlistedPlayers(CommandSourceStack param0) {
        if (this.isEnforceWhitelist()) {
            PlayerList var0 = param0.getServer().getPlayerList();
            UserWhiteList var1 = var0.getWhiteList();
            if (var1.isEnabled()) {
                for(ServerPlayer var3 : Lists.newArrayList(var0.getPlayers())) {
                    if (!var1.isWhiteListed(var3.getGameProfile())) {
                        var3.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.not_whitelisted"));
                    }
                }

            }
        }
    }

    public ReloadableResourceManager getResources() {
        return this.resources;
    }

    public PackRepository<UnopenedPack> getPackRepository() {
        return this.packRepository;
    }

    public Commands getCommands() {
        return this.commands;
    }

    public CommandSourceStack createCommandSourceStack() {
        return new CommandSourceStack(
            this,
            this.getLevel(DimensionType.OVERWORLD) == null ? Vec3.ZERO : Vec3.atLowerCornerOf(this.getLevel(DimensionType.OVERWORLD).getSharedSpawnPos()),
            Vec2.ZERO,
            this.getLevel(DimensionType.OVERWORLD),
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

    public RecipeManager getRecipeManager() {
        return this.recipes;
    }

    public TagManager getTags() {
        return this.tags;
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
        return this.lootTables;
    }

    public PredicateManager getPredicateManager() {
        return this.predicateManager;
    }

    public GameRules getGameRules() {
        return this.getLevel(DimensionType.OVERWORLD).getGameRules();
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

    @OnlyIn(Dist.CLIENT)
    public FrameTimer getFrameTimer() {
        return this.frameTimer;
    }

    public ProfilerFiller getProfiler() {
        return this.profiler;
    }

    public Executor getBackgroundTaskExecutor() {
        return this.executor;
    }

    public abstract boolean isSingleplayerOwner(GameProfile var1);

    public void saveDebugReport(Path param0) throws IOException {
        Path var0 = param0.resolve("levels");

        for(Entry<DimensionType, ServerLevel> var1 : this.levels.entrySet()) {
            ResourceLocation var2 = DimensionType.getName(var1.getKey());
            Path var3 = var0.resolve(var2.getNamespace()).resolve(var2.getPath());
            Files.createDirectories(var3);
            var1.getValue().saveDebugReport(var3);
        }

        this.dumpGameRules(param0.resolve("gamerules.txt"));
        this.dumpClasspath(param0.resolve("classpath.txt"));
        this.dumpCrashCategory(param0.resolve("example_crash.txt"));
        this.dumpMiscStats(param0.resolve("stats.txt"));
        this.dumpThreads(param0.resolve("threads.txt"));
    }

    private void dumpMiscStats(Path param0) throws IOException {
        try (Writer var0 = Files.newBufferedWriter(param0)) {
            var0.write(String.format("pending_tasks: %d\n", this.getPendingTasksCount()));
            var0.write(String.format("average_tick_time: %f\n", this.getAverageTickTime()));
            var0.write(String.format("tick_times: %s\n", Arrays.toString(this.tickTimes)));
            var0.write(String.format("queue: %s\n", Util.backgroundExecutor()));
        }

    }

    private void dumpCrashCategory(Path param0) throws IOException {
        CrashReport var0 = new CrashReport("Server dump", new Exception("dummy"));
        this.fillReport(var0);

        try (Writer var1 = Files.newBufferedWriter(param0)) {
            var1.write(var0.getFriendlyReport());
        }

    }

    private void dumpGameRules(Path param0) throws IOException {
        try (Writer var0 = Files.newBufferedWriter(param0)) {
            final List<String> var1 = Lists.newArrayList();
            final GameRules var2 = this.getGameRules();
            GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
                @Override
                public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> param0, GameRules.Type<T> param1) {
                    var1.add(String.format("%s=%s\n", param0.getId(), var2.<T>getRule(param0).toString()));
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

    private void refreshRegistries() {
        Blocks.rebuildCache();
    }

    private void startProfilerTick(@Nullable SingleTickProfiler param0) {
        if (this.delayProfilerStart) {
            this.delayProfilerStart = false;
            this.continousProfiler.enable();
        }

        this.profiler = SingleTickProfiler.decorateFiller(this.continousProfiler.getFiller(), param0);
    }

    private void endProfilerTick(@Nullable SingleTickProfiler param0) {
        if (param0 != null) {
            param0.endTick();
        }

        this.profiler = this.continousProfiler.getFiller();
    }

    public boolean isProfiling() {
        return this.continousProfiler.isEnabled();
    }

    public void startProfiling() {
        this.delayProfilerStart = true;
    }

    public ProfileResults finishProfiling() {
        ProfileResults var0 = this.continousProfiler.getResults();
        this.continousProfiler.disable();
        return var0;
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
}
