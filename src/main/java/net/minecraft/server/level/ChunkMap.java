package net.minecraft.server.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public class ChunkMap extends ChunkStorage implements ChunkHolder.PlayerProvider {
    private static final byte CHUNK_TYPE_REPLACEABLE = -1;
    private static final byte CHUNK_TYPE_UNKNOWN = 0;
    private static final byte CHUNK_TYPE_FULL = 1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHUNK_SAVED_PER_TICK = 200;
    private static final int CHUNK_SAVED_EAGERLY_PER_TICK = 20;
    private static final int EAGER_CHUNK_SAVE_COOLDOWN_IN_MILLIS = 10000;
    public static final int MIN_VIEW_DISTANCE = 2;
    public static final int MAX_VIEW_DISTANCE = 32;
    public static final int FORCED_TICKET_LEVEL = ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING);
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap = new Long2ObjectLinkedOpenHashMap<>();
    private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap = this.updatingChunkMap.clone();
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads = new Long2ObjectLinkedOpenHashMap<>();
    private final LongSet entitiesInLevel = new LongOpenHashSet();
    final ServerLevel level;
    private final ThreadedLevelLightEngine lightEngine;
    private final BlockableEventLoop<Runnable> mainThreadExecutor;
    private ChunkGenerator generator;
    private final RandomState randomState;
    private final ChunkGeneratorStructureState chunkGeneratorState;
    private final Supplier<DimensionDataStorage> overworldDataStorage;
    private final PoiManager poiManager;
    final LongSet toDrop = new LongOpenHashSet();
    private boolean modified;
    private final ChunkTaskPriorityQueueSorter queueSorter;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> worldgenMailbox;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;
    private final ChunkProgressListener progressListener;
    private final ChunkStatusUpdateListener chunkStatusListener;
    private final ChunkMap.DistanceManager distanceManager;
    private final AtomicInteger tickingGenerated = new AtomicInteger();
    private final StructureTemplateManager structureTemplateManager;
    private final String storageName;
    private final PlayerMap playerMap = new PlayerMap();
    private final Int2ObjectMap<ChunkMap.TrackedEntity> entityMap = new Int2ObjectOpenHashMap<>();
    private final Long2ByteMap chunkTypeCache = new Long2ByteOpenHashMap();
    private final Long2LongMap chunkSaveCooldowns = new Long2LongOpenHashMap();
    private final Queue<Runnable> unloadQueue = Queues.newConcurrentLinkedQueue();
    private int serverViewDistance;

    public ChunkMap(
        ServerLevel param0,
        LevelStorageSource.LevelStorageAccess param1,
        DataFixer param2,
        StructureTemplateManager param3,
        Executor param4,
        BlockableEventLoop<Runnable> param5,
        LightChunkGetter param6,
        ChunkGenerator param7,
        ChunkProgressListener param8,
        ChunkStatusUpdateListener param9,
        Supplier<DimensionDataStorage> param10,
        int param11,
        boolean param12
    ) {
        super(param1.getDimensionPath(param0.dimension()).resolve("region"), param2, param12);
        this.structureTemplateManager = param3;
        Path var0 = param1.getDimensionPath(param0.dimension());
        this.storageName = var0.getFileName().toString();
        this.level = param0;
        this.generator = param7;
        RegistryAccess var1 = param0.registryAccess();
        long var2 = param0.getSeed();
        if (param7 instanceof NoiseBasedChunkGenerator var3) {
            this.randomState = RandomState.create(var3.generatorSettings().value(), var1.lookupOrThrow(Registries.NOISE), var2);
        } else {
            this.randomState = RandomState.create(NoiseGeneratorSettings.dummy(), var1.lookupOrThrow(Registries.NOISE), var2);
        }

        this.chunkGeneratorState = param7.createState(var1.lookupOrThrow(Registries.STRUCTURE_SET), this.randomState, var2);
        this.mainThreadExecutor = param5;
        ProcessorMailbox<Runnable> var4 = ProcessorMailbox.create(param4, "worldgen");
        ProcessorHandle<Runnable> var5 = ProcessorHandle.of("main", param5::tell);
        this.progressListener = param8;
        this.chunkStatusListener = param9;
        ProcessorMailbox<Runnable> var6 = ProcessorMailbox.create(param4, "light");
        this.queueSorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(var4, var5, var6), param4, Integer.MAX_VALUE);
        this.worldgenMailbox = this.queueSorter.getProcessor(var4, false);
        this.mainThreadMailbox = this.queueSorter.getProcessor(var5, false);
        this.lightEngine = new ThreadedLevelLightEngine(
            param6, this, this.level.dimensionType().hasSkyLight(), var6, this.queueSorter.getProcessor(var6, false)
        );
        this.distanceManager = new ChunkMap.DistanceManager(param4, param5);
        this.overworldDataStorage = param10;
        this.poiManager = new PoiManager(var0.resolve("poi"), param2, param12, var1, param0);
        this.setServerViewDistance(param11);
    }

    protected ChunkGenerator generator() {
        return this.generator;
    }

    protected ChunkGeneratorStructureState generatorState() {
        return this.chunkGeneratorState;
    }

    protected RandomState randomState() {
        return this.randomState;
    }

    public void debugReloadGenerator() {
        DataResult<JsonElement> var0 = ChunkGenerator.CODEC.encodeStart(JsonOps.INSTANCE, this.generator);
        DataResult<ChunkGenerator> var1 = var0.flatMap(param0 -> ChunkGenerator.CODEC.parse(JsonOps.INSTANCE, param0));
        var1.result().ifPresent(param0 -> this.generator = param0);
    }

    private static double euclideanDistanceSquared(ChunkPos param0, Entity param1) {
        double var0 = (double)SectionPos.sectionToBlockCoord(param0.x, 8);
        double var1 = (double)SectionPos.sectionToBlockCoord(param0.z, 8);
        double var2 = var0 - param1.getX();
        double var3 = var1 - param1.getZ();
        return var2 * var2 + var3 * var3;
    }

    boolean isChunkTracked(ServerPlayer param0, int param1, int param2) {
        return param0.getChunkTrackingView().contains(param1, param2) && !param0.connection.chunkSender.isPending(ChunkPos.asLong(param1, param2));
    }

    private boolean isChunkOnTrackedBorder(ServerPlayer param0, int param1, int param2) {
        if (!this.isChunkTracked(param0, param1, param2)) {
            return false;
        } else {
            for(int var0 = -1; var0 <= 1; ++var0) {
                for(int var1 = -1; var1 <= 1; ++var1) {
                    if ((var0 != 0 || var1 != 0) && !this.isChunkTracked(param0, param1 + var0, param2 + var1)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    protected ThreadedLevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    @Nullable
    protected ChunkHolder getUpdatingChunkIfPresent(long param0) {
        return this.updatingChunkMap.get(param0);
    }

    @Nullable
    protected ChunkHolder getVisibleChunkIfPresent(long param0) {
        return this.visibleChunkMap.get(param0);
    }

    protected IntSupplier getChunkQueueLevel(long param0) {
        return () -> {
            ChunkHolder var0 = this.getVisibleChunkIfPresent(param0);
            return var0 == null
                ? ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1
                : Math.min(var0.getQueueLevel(), ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1);
        };
    }

    public String getChunkDebugData(ChunkPos param0) {
        ChunkHolder var0 = this.getVisibleChunkIfPresent(param0.toLong());
        if (var0 == null) {
            return "null";
        } else {
            String var1 = var0.getTicketLevel() + "\n";
            ChunkStatus var2 = var0.getLastAvailableStatus();
            ChunkAccess var3 = var0.getLastAvailable();
            if (var2 != null) {
                var1 = var1 + "St: \u00a7" + var2.getIndex() + var2 + "\u00a7r\n";
            }

            if (var3 != null) {
                var1 = var1 + "Ch: \u00a7" + var3.getStatus().getIndex() + var3.getStatus() + "\u00a7r\n";
            }

            FullChunkStatus var4 = var0.getFullStatus();
            var1 = var1 + 167 + var4.ordinal() + var4;
            return var1 + "\u00a7r";
        }
    }

    private CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> getChunkRangeFuture(
        ChunkHolder param0, int param1, IntFunction<ChunkStatus> param2
    ) {
        if (param1 == 0) {
            ChunkStatus var0 = param2.apply(0);
            return param0.getOrScheduleFuture(var0, this).thenApply(param0x -> param0x.mapLeft(List::of));
        } else {
            List<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> var1 = new ArrayList<>();
            List<ChunkHolder> var2 = new ArrayList<>();
            ChunkPos var3 = param0.getPos();
            int var4 = var3.x;
            int var5 = var3.z;

            for(int var6 = -param1; var6 <= param1; ++var6) {
                for(int var7 = -param1; var7 <= param1; ++var7) {
                    int var8 = Math.max(Math.abs(var7), Math.abs(var6));
                    final ChunkPos var9 = new ChunkPos(var4 + var7, var5 + var6);
                    long var10 = var9.toLong();
                    ChunkHolder var11 = this.getUpdatingChunkIfPresent(var10);
                    if (var11 == null) {
                        return CompletableFuture.completedFuture(Either.right(new ChunkHolder.ChunkLoadingFailure() {
                            @Override
                            public String toString() {
                                return "Unloaded " + var9;
                            }
                        }));
                    }

                    ChunkStatus var12 = param2.apply(var8);
                    CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var13 = var11.getOrScheduleFuture(var12, this);
                    var2.add(var11);
                    var1.add(var13);
                }
            }

            CompletableFuture<List<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> var14 = Util.sequence(var1);
            CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> var15 = var14.thenApply(param3 -> {
                List<ChunkAccess> var0x = Lists.newArrayList();
                int var1x = 0;

                for(final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> var2x : param3) {
                    if (var2x == null) {
                        throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                    }

                    Optional<ChunkAccess> var3x = var2x.left();
                    if (var3x.isEmpty()) {
                        final int var4x = var1x;
                        return Either.right(new ChunkHolder.ChunkLoadingFailure() {
                            @Override
                            public String toString() {
                                return "Unloaded " + new ChunkPos(var4 + var4x % (param1 * 2 + 1), var5 + var4x / (param1 * 2 + 1)) + " " + var2x.right().get();
                            }
                        });
                    }

                    var0x.add(var3x.get());
                    ++var1x;
                }

                return Either.left(var0x);
            });

            for(ChunkHolder var16 : var2) {
                var16.addSaveDependency("getChunkRangeFuture " + var3 + " " + param1, var15);
            }

            return var15;
        }
    }

    public ReportedException debugFuturesAndCreateReportedException(IllegalStateException param0, String param1) {
        StringBuilder var0 = new StringBuilder();
        Consumer<ChunkHolder> var1 = param1x -> param1x.getAllFutures().forEach(param2 -> {
                ChunkStatus var0x = param2.getFirst();
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var1x = param2.getSecond();
                if (var1x != null && var1x.isDone() && var1x.join() == null) {
                    var0.append(param1x.getPos()).append(" - status: ").append(var0x).append(" future: ").append(var1x).append(System.lineSeparator());
                }

            });
        var0.append("Updating:").append(System.lineSeparator());
        this.updatingChunkMap.values().forEach(var1);
        var0.append("Visible:").append(System.lineSeparator());
        this.visibleChunkMap.values().forEach(var1);
        CrashReport var2 = CrashReport.forThrowable(param0, "Chunk loading");
        CrashReportCategory var3 = var2.addCategory("Chunk loading");
        var3.setDetail("Details", param1);
        var3.setDetail("Futures", var0);
        return new ReportedException(var2);
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareEntityTickingChunk(ChunkHolder param0) {
        return this.getChunkRangeFuture(param0, 2, param0x -> ChunkStatus.FULL)
            .thenApplyAsync(param0x -> param0x.mapLeft(param0xx -> (LevelChunk)param0xx.get(param0xx.size() / 2)), this.mainThreadExecutor);
    }

    @Nullable
    ChunkHolder updateChunkScheduling(long param0, int param1, @Nullable ChunkHolder param2, int param3) {
        if (!ChunkLevel.isLoaded(param3) && !ChunkLevel.isLoaded(param1)) {
            return param2;
        } else {
            if (param2 != null) {
                param2.setTicketLevel(param1);
            }

            if (param2 != null) {
                if (!ChunkLevel.isLoaded(param1)) {
                    this.toDrop.add(param0);
                } else {
                    this.toDrop.remove(param0);
                }
            }

            if (ChunkLevel.isLoaded(param1) && param2 == null) {
                param2 = this.pendingUnloads.remove(param0);
                if (param2 != null) {
                    param2.setTicketLevel(param1);
                } else {
                    param2 = new ChunkHolder(new ChunkPos(param0), param1, this.level, this.lightEngine, this.queueSorter, this);
                }

                this.updatingChunkMap.put(param0, param2);
                this.modified = true;
            }

            return param2;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            this.queueSorter.close();
            this.poiManager.close();
        } finally {
            super.close();
        }

    }

    protected void saveAllChunks(boolean param0) {
        if (param0) {
            List<ChunkHolder> var0 = this.visibleChunkMap
                .values()
                .stream()
                .filter(ChunkHolder::wasAccessibleSinceLastSave)
                .peek(ChunkHolder::refreshAccessibility)
                .toList();
            MutableBoolean var1 = new MutableBoolean();

            do {
                var1.setFalse();
                var0.stream()
                    .map(param0x -> {
                        CompletableFuture<ChunkAccess> var0x;
                        do {
                            var0x = param0x.getChunkToSave();
                            this.mainThreadExecutor.managedBlock(var0x::isDone);
                        } while(var0x != param0x.getChunkToSave());
    
                        return var0x.join();
                    })
                    .filter(param0x -> param0x instanceof ImposterProtoChunk || param0x instanceof LevelChunk)
                    .filter(this::save)
                    .forEach(param1 -> var1.setTrue());
            } while(var1.isTrue());

            this.processUnloads(() -> true);
            this.flushWorker();
        } else {
            this.visibleChunkMap.values().forEach(this::saveChunkIfNeeded);
        }

    }

    protected void tick(BooleanSupplier param0) {
        ProfilerFiller var0 = this.level.getProfiler();
        var0.push("poi");
        this.poiManager.tick(param0);
        var0.popPush("chunk_unload");
        if (!this.level.noSave()) {
            this.processUnloads(param0);
        }

        var0.pop();
    }

    public boolean hasWork() {
        return this.lightEngine.hasLightWork()
            || !this.pendingUnloads.isEmpty()
            || !this.updatingChunkMap.isEmpty()
            || this.poiManager.hasWork()
            || !this.toDrop.isEmpty()
            || !this.unloadQueue.isEmpty()
            || this.queueSorter.hasWork()
            || this.distanceManager.hasTickets();
    }

    private void processUnloads(BooleanSupplier param0) {
        LongIterator var0 = this.toDrop.iterator();

        long var2;
        for(int var1 = 0; var0.hasNext() && (param0.getAsBoolean() || var1 < 200 || this.toDrop.size() > 2000); var0.remove()) {
            var2 = var0.nextLong();
            ChunkHolder var3 = this.updatingChunkMap.remove(var2);
            if (var3 != null) {
                this.pendingUnloads.put(var2, var3);
                this.modified = true;
                ++var1;
                this.scheduleUnload(var2, var3);
            }
        }

        int var4 = Math.max(0, this.unloadQueue.size() - 2000);

        while((param0.getAsBoolean() || var4 > 0) && (var2 = (long)this.unloadQueue.poll()) != null) {
            --var4;
            var2.run();
        }

        int var6 = 0;
        ObjectIterator<ChunkHolder> var7 = this.visibleChunkMap.values().iterator();

        while(var6 < 20 && param0.getAsBoolean() && var7.hasNext()) {
            if (this.saveChunkIfNeeded(var7.next())) {
                ++var6;
            }
        }

    }

    private void scheduleUnload(long param0, ChunkHolder param1) {
        CompletableFuture<ChunkAccess> var0 = param1.getChunkToSave();
        var0.thenAcceptAsync(param3 -> {
            CompletableFuture<ChunkAccess> var0x = param1.getChunkToSave();
            if (var0x != var0) {
                this.scheduleUnload(param0, param1);
            } else {
                if (this.pendingUnloads.remove(param0, param1) && param3 != null) {
                    if (param3 instanceof LevelChunk) {
                        ((LevelChunk)param3).setLoaded(false);
                    }

                    this.save(param3);
                    if (this.entitiesInLevel.remove(param0) && param3 instanceof LevelChunk var1x) {
                        this.level.unload(var1x);
                    }

                    this.lightEngine.updateChunkStatus(param3.getPos());
                    this.lightEngine.tryScheduleUpdate();
                    this.progressListener.onStatusChange(param3.getPos(), null);
                    this.chunkSaveCooldowns.remove(param3.getPos().toLong());
                }

            }
        }, this.unloadQueue::add).whenComplete((param1x, param2) -> {
            if (param2 != null) {
                LOGGER.error("Failed to save chunk {}", param1.getPos(), param2);
            }

        });
    }

    protected boolean promoteChunkMap() {
        if (!this.modified) {
            return false;
        } else {
            this.visibleChunkMap = this.updatingChunkMap.clone();
            this.modified = false;
            return true;
        }
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> schedule(ChunkHolder param0, ChunkStatus param1) {
        ChunkPos var0 = param0.getPos();
        if (param1 == ChunkStatus.EMPTY) {
            return this.scheduleChunkLoad(var0);
        } else {
            if (param1 == ChunkStatus.LIGHT) {
                this.distanceManager.addTicket(TicketType.LIGHT, var0, ChunkLevel.byStatus(ChunkStatus.LIGHT), var0);
            }

            if (!param1.hasLoadDependencies()) {
                Optional<ChunkAccess> var1 = param0.getOrScheduleFuture(param1.getParent(), this).getNow(ChunkHolder.UNLOADED_CHUNK).left();
                if (var1.isPresent() && var1.get().getStatus().isOrAfter(param1)) {
                    CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var2 = param1.load(
                        this.level, this.structureTemplateManager, this.lightEngine, param1x -> this.protoChunkToFullChunk(param0), var1.get()
                    );
                    this.progressListener.onStatusChange(var0, param1);
                    return var2;
                }
            }

            return this.scheduleChunkGeneration(param0, param1);
        }
    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkLoad(ChunkPos param0) {
        return this.readChunk(param0).thenApply(param1 -> param1.filter(param1x -> {
                boolean var0x = isChunkDataValid(param1x);
                if (!var0x) {
                    LOGGER.error("Chunk file at {} is missing level data, skipping", param0);
                }

                return var0x;
            })).thenApplyAsync(param1 -> {
            this.level.getProfiler().incrementCounter("chunkLoad");
            if (param1.isPresent()) {
                ChunkAccess var0 = ChunkSerializer.read(this.level, this.poiManager, param0, param1.get());
                this.markPosition(param0, var0.getStatus().getChunkType());
                return Either.left(var0);
            } else {
                return Either.left(this.createEmptyChunk(param0));
            }
        }, this.mainThreadExecutor).exceptionallyAsync(param1 -> this.handleChunkLoadFailure(param1, param0), this.mainThreadExecutor);
    }

    private static boolean isChunkDataValid(CompoundTag param0) {
        return param0.contains("Status", 8);
    }

    private Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> handleChunkLoadFailure(Throwable param0, ChunkPos param1) {
        if (param0 instanceof ReportedException var0) {
            Throwable var1 = var0.getCause();
            if (!(var1 instanceof IOException)) {
                this.markPositionReplaceable(param1);
                throw var0;
            }

            LOGGER.error("Couldn't load chunk {}", param1, var1);
        } else if (param0 instanceof IOException) {
            LOGGER.error("Couldn't load chunk {}", param1, param0);
        }

        return Either.left(this.createEmptyChunk(param1));
    }

    private ChunkAccess createEmptyChunk(ChunkPos param0) {
        this.markPositionReplaceable(param0);
        return new ProtoChunk(param0, UpgradeData.EMPTY, this.level, this.level.registryAccess().registryOrThrow(Registries.BIOME), null);
    }

    private void markPositionReplaceable(ChunkPos param0) {
        this.chunkTypeCache.put(param0.toLong(), (byte)-1);
    }

    private byte markPosition(ChunkPos param0, ChunkStatus.ChunkType param1) {
        return this.chunkTypeCache.put(param0.toLong(), (byte)(param1 == ChunkStatus.ChunkType.PROTOCHUNK ? -1 : 1));
    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkGeneration(ChunkHolder param0, ChunkStatus param1) {
        ChunkPos var0 = param0.getPos();
        CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> var1 = this.getChunkRangeFuture(
            param0, param1.getRange(), param1x -> this.getDependencyStatus(param1, param1x)
        );
        this.level.getProfiler().incrementCounter(() -> "chunkGenerate " + param1);
        Executor var2 = param1x -> this.worldgenMailbox.tell(ChunkTaskPriorityQueueSorter.message(param0, param1x));
        return var1.thenComposeAsync(
            param4 -> param4.map(
                    param4x -> {
                        try {
                            ChunkAccess var3x = param4x.get(param4x.size() / 2);
                            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var1x;
                            if (var3x.getStatus().isOrAfter(param1)) {
                                var1x = param1.load(
                                    this.level, this.structureTemplateManager, this.lightEngine, param1x -> this.protoChunkToFullChunk(param0), var3x
                                );
                            } else {
                                var1x = param1.generate(
                                    var2,
                                    this.level,
                                    this.generator,
                                    this.structureTemplateManager,
                                    this.lightEngine,
                                    param1x -> this.protoChunkToFullChunk(param0),
                                    param4x
                                );
                            }
        
                            this.progressListener.onStatusChange(var0, param1);
                            return var1x;
                        } catch (Exception var9) {
                            var9.getStackTrace();
                            CrashReport var4x = CrashReport.forThrowable(var9, "Exception generating new chunk");
                            CrashReportCategory var5x = var4x.addCategory("Chunk to be generated");
                            var5x.setDetail("Location", String.format(Locale.ROOT, "%d,%d", var0.x, var0.z));
                            var5x.setDetail("Position hash", ChunkPos.asLong(var0.x, var0.z));
                            var5x.setDetail("Generator", this.generator);
                            this.mainThreadExecutor.execute(() -> {
                                throw new ReportedException(var4x);
                            });
                            throw new ReportedException(var4x);
                        }
                    },
                    param1x -> {
                        this.releaseLightTicket(var0);
                        return CompletableFuture.completedFuture(Either.right(param1x));
                    }
                ),
            var2
        );
    }

    protected void releaseLightTicket(ChunkPos param0) {
        this.mainThreadExecutor
            .tell(
                Util.name(
                    () -> this.distanceManager.removeTicket(TicketType.LIGHT, param0, ChunkLevel.byStatus(ChunkStatus.LIGHT), param0),
                    () -> "release light ticket " + param0
                )
            );
    }

    private ChunkStatus getDependencyStatus(ChunkStatus param0, int param1) {
        ChunkStatus var0;
        if (param1 == 0) {
            var0 = param0.getParent();
        } else {
            var0 = ChunkStatus.getStatusAroundFullChunk(ChunkStatus.getDistance(param0) + param1);
        }

        return var0;
    }

    private static void postLoadProtoChunk(ServerLevel param0, List<CompoundTag> param1) {
        if (!param1.isEmpty()) {
            param0.addWorldGenChunkEntities(EntityType.loadEntitiesRecursive(param1, param0));
        }

    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> protoChunkToFullChunk(ChunkHolder param0) {
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var0 = param0.getFutureIfPresentUnchecked(ChunkStatus.FULL.getParent());
        return var0.thenApplyAsync(param1 -> {
            ChunkStatus var0x = ChunkLevel.generationStatus(param0.getTicketLevel());
            return !var0x.isOrAfter(ChunkStatus.FULL) ? ChunkHolder.UNLOADED_CHUNK : param1.mapLeft(param1x -> {
                ChunkPos var0xx = param0.getPos();
                ProtoChunk var1x = (ProtoChunk)param1x;
                LevelChunk var3x;
                if (var1x instanceof ImposterProtoChunk) {
                    var3x = ((ImposterProtoChunk)var1x).getWrapped();
                } else {
                    var3x = new LevelChunk(this.level, var1x, param1xx -> postLoadProtoChunk(this.level, var1x.getEntities()));
                    param0.replaceProtoChunk(new ImposterProtoChunk(var3x, false));
                }

                var3x.setFullStatus(() -> ChunkLevel.fullStatus(param0.getTicketLevel()));
                var3x.runPostLoad();
                if (this.entitiesInLevel.add(var0xx.toLong())) {
                    var3x.setLoaded(true);
                    var3x.registerAllBlockEntitiesAfterLevelLoad();
                    var3x.registerTickContainerInLevel(this.level);
                }

                return var3x;
            });
        }, param1 -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(param1, param0.getPos().toLong(), param0::getTicketLevel)));
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareTickingChunk(ChunkHolder param0) {
        CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> var0 = this.getChunkRangeFuture(param0, 1, param0x -> ChunkStatus.FULL);
        CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> var1 = var0.<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>>thenApplyAsync(
                param0x -> param0x.mapLeft(param0xx -> (LevelChunk)param0xx.get(param0xx.size() / 2)),
                param1 -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(param0, param1))
            )
            .thenApplyAsync(param1 -> param1.ifLeft(param1x -> {
                    param1x.postProcessGeneration();
                    this.level.startTickingChunk(param1x);
                    CompletableFuture<?> var0x = param0.getChunkSendSyncFuture();
                    if (var0x.isDone()) {
                        this.onChunkReadyToSend(param1x);
                    } else {
                        var0x.thenAcceptAsync(param1xx -> this.onChunkReadyToSend(param1x), this.mainThreadExecutor);
                    }
    
                }), this.mainThreadExecutor);
        var1.handle((param0x, param1) -> {
            this.tickingGenerated.getAndIncrement();
            return null;
        });
        return var1;
    }

    private void onChunkReadyToSend(LevelChunk param0) {
        ChunkPos var0 = param0.getPos();

        for(ServerPlayer var1 : this.playerMap.getAllPlayers()) {
            if (var1.getChunkTrackingView().contains(var0)) {
                markChunkPendingToSend(var1, param0);
            }
        }

    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareAccessibleChunk(ChunkHolder param0) {
        return this.getChunkRangeFuture(param0, 1, ChunkStatus::getStatusAroundFullChunk)
            .thenApplyAsync(
                param0x -> param0x.mapLeft(param0xx -> (LevelChunk)param0xx.get(param0xx.size() / 2)),
                param1 -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(param0, param1))
            );
    }

    public int getTickingGenerated() {
        return this.tickingGenerated.get();
    }

    private boolean saveChunkIfNeeded(ChunkHolder param0x) {
        if (!param0x.wasAccessibleSinceLastSave()) {
            return false;
        } else {
            ChunkAccess var0x = param0x.getChunkToSave().getNow(null);
            if (!(var0x instanceof ImposterProtoChunk) && !(var0x instanceof LevelChunk)) {
                return false;
            } else {
                long var1x = var0x.getPos().toLong();
                long var2 = this.chunkSaveCooldowns.getOrDefault(var1x, -1L);
                long var3 = System.currentTimeMillis();
                if (var3 < var2) {
                    return false;
                } else {
                    boolean var4 = this.save(var0x);
                    param0x.refreshAccessibility();
                    if (var4) {
                        this.chunkSaveCooldowns.put(var1x, var3 + 10000L);
                    }

                    return var4;
                }
            }
        }
    }

    private boolean save(ChunkAccess param0x) {
        this.poiManager.flush(param0x.getPos());
        if (!param0x.isUnsaved()) {
            return false;
        } else {
            param0x.setUnsaved(false);
            ChunkPos var0x = param0x.getPos();

            try {
                ChunkStatus var1x = param0x.getStatus();
                if (var1x.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK) {
                    if (this.isExistingChunkFull(var0x)) {
                        return false;
                    }

                    if (var1x == ChunkStatus.EMPTY && param0x.getAllStarts().values().stream().noneMatch(StructureStart::isValid)) {
                        return false;
                    }
                }

                this.level.getProfiler().incrementCounter("chunkSave");
                CompoundTag var2 = ChunkSerializer.write(this.level, param0x);
                this.write(var0x, var2);
                this.markPosition(var0x, var1x.getChunkType());
                return true;
            } catch (Exception var5) {
                LOGGER.error("Failed to save chunk {},{}", var0x.x, var0x.z, var5);
                return false;
            }
        }
    }

    private boolean isExistingChunkFull(ChunkPos param0) {
        byte var0 = this.chunkTypeCache.get(param0.toLong());
        if (var0 != 0) {
            return var0 == 1;
        } else {
            CompoundTag var1;
            try {
                var1 = this.readChunk(param0).join().orElse(null);
                if (var1 == null) {
                    this.markPositionReplaceable(param0);
                    return false;
                }
            } catch (Exception var5) {
                LOGGER.error("Failed to read chunk {}", param0, var5);
                this.markPositionReplaceable(param0);
                return false;
            }

            ChunkStatus.ChunkType var4 = ChunkSerializer.getChunkTypeFromTag(var1);
            return this.markPosition(param0, var4) == 1;
        }
    }

    protected void setServerViewDistance(int param0) {
        int var0 = Mth.clamp(param0, 2, 32);
        if (var0 != this.serverViewDistance) {
            this.serverViewDistance = var0;
            this.distanceManager.updatePlayerTickets(this.serverViewDistance);

            for(ServerPlayer var1 : this.playerMap.getAllPlayers()) {
                this.updateChunkTracking(var1);
            }
        }

    }

    int getPlayerViewDistance(ServerPlayer param0) {
        return Mth.clamp(param0.requestedViewDistance(), 2, this.serverViewDistance);
    }

    private void markChunkPendingToSend(ServerPlayer param0, ChunkPos param1) {
        LevelChunk var0 = this.getChunkToSend(param1.toLong());
        if (var0 != null) {
            markChunkPendingToSend(param0, var0);
        }

    }

    private static void markChunkPendingToSend(ServerPlayer param0, LevelChunk param1) {
        param0.connection.chunkSender.markChunkPendingToSend(param1);
    }

    private static void dropChunk(ServerPlayer param0, ChunkPos param1) {
        param0.connection.chunkSender.dropChunk(param0, param1);
    }

    @Nullable
    public LevelChunk getChunkToSend(long param0) {
        ChunkHolder var0 = this.getVisibleChunkIfPresent(param0);
        return var0 == null ? null : var0.getChunkToSend();
    }

    public int size() {
        return this.visibleChunkMap.size();
    }

    public net.minecraft.server.level.DistanceManager getDistanceManager() {
        return this.distanceManager;
    }

    protected Iterable<ChunkHolder> getChunks() {
        return Iterables.unmodifiableIterable(this.visibleChunkMap.values());
    }

    void dumpChunks(Writer param0) throws IOException {
        CsvOutput var0 = CsvOutput.builder()
            .addColumn("x")
            .addColumn("z")
            .addColumn("level")
            .addColumn("in_memory")
            .addColumn("status")
            .addColumn("full_status")
            .addColumn("accessible_ready")
            .addColumn("ticking_ready")
            .addColumn("entity_ticking_ready")
            .addColumn("ticket")
            .addColumn("spawning")
            .addColumn("block_entity_count")
            .addColumn("ticking_ticket")
            .addColumn("ticking_level")
            .addColumn("block_ticks")
            .addColumn("fluid_ticks")
            .build(param0);
        TickingTracker var1 = this.distanceManager.tickingTracker();

        for(Entry<ChunkHolder> var2 : this.visibleChunkMap.long2ObjectEntrySet()) {
            long var3 = var2.getLongKey();
            ChunkPos var4 = new ChunkPos(var3);
            ChunkHolder var5 = var2.getValue();
            Optional<ChunkAccess> var6 = Optional.ofNullable(var5.getLastAvailable());
            Optional<LevelChunk> var7 = var6.flatMap(param0x -> param0x instanceof LevelChunk ? Optional.of((LevelChunk)param0x) : Optional.empty());
            var0.writeRow(
                var4.x,
                var4.z,
                var5.getTicketLevel(),
                var6.isPresent(),
                var6.map(ChunkAccess::getStatus).orElse(null),
                var7.map(LevelChunk::getFullStatus).orElse(null),
                printFuture(var5.getFullChunkFuture()),
                printFuture(var5.getTickingChunkFuture()),
                printFuture(var5.getEntityTickingChunkFuture()),
                this.distanceManager.getTicketDebugString(var3),
                this.anyPlayerCloseEnoughForSpawning(var4),
                var7.<Integer>map(param0x -> param0x.getBlockEntities().size()).orElse(0),
                var1.getTicketDebugString(var3),
                var1.getLevel(var3),
                var7.<Integer>map(param0x -> param0x.getBlockTicks().count()).orElse(0),
                var7.<Integer>map(param0x -> param0x.getFluidTicks().count()).orElse(0)
            );
        }

    }

    private static String printFuture(CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> param0) {
        try {
            Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> var0 = param0.getNow(null);
            return var0 != null ? var0.map(param0x -> "done", param0x -> "unloaded") : "not completed";
        } catch (CompletionException var21) {
            return "failed " + var21.getCause().getMessage();
        } catch (CancellationException var3) {
            return "cancelled";
        }
    }

    private CompletableFuture<Optional<CompoundTag>> readChunk(ChunkPos param0) {
        return this.read(param0).thenApplyAsync(param0x -> param0x.map(this::upgradeChunkTag), Util.backgroundExecutor());
    }

    private CompoundTag upgradeChunkTag(CompoundTag param0) {
        return this.upgradeChunkTag(this.level.dimension(), this.overworldDataStorage, param0, this.generator.getTypeNameForDataFixer());
    }

    boolean anyPlayerCloseEnoughForSpawning(ChunkPos param0) {
        if (!this.distanceManager.hasPlayersNearby(param0.toLong())) {
            return false;
        } else {
            for(ServerPlayer var0 : this.playerMap.getAllPlayers()) {
                if (this.playerIsCloseEnoughForSpawning(var0, param0)) {
                    return true;
                }
            }

            return false;
        }
    }

    public List<ServerPlayer> getPlayersCloseForSpawning(ChunkPos param0) {
        long var0 = param0.toLong();
        if (!this.distanceManager.hasPlayersNearby(var0)) {
            return List.of();
        } else {
            Builder<ServerPlayer> var1 = ImmutableList.builder();

            for(ServerPlayer var2 : this.playerMap.getAllPlayers()) {
                if (this.playerIsCloseEnoughForSpawning(var2, param0)) {
                    var1.add(var2);
                }
            }

            return var1.build();
        }
    }

    private boolean playerIsCloseEnoughForSpawning(ServerPlayer param0, ChunkPos param1) {
        if (param0.isSpectator()) {
            return false;
        } else {
            double var0 = euclideanDistanceSquared(param1, param0);
            return var0 < 16384.0;
        }
    }

    private boolean skipPlayer(ServerPlayer param0) {
        return param0.isSpectator() && !this.level.getGameRules().getBoolean(GameRules.RULE_SPECTATORSGENERATECHUNKS);
    }

    void updatePlayerStatus(ServerPlayer param0, boolean param1) {
        boolean var0 = this.skipPlayer(param0);
        boolean var1 = this.playerMap.ignoredOrUnknown(param0);
        if (param1) {
            this.playerMap.addPlayer(param0, var0);
            this.updatePlayerPos(param0);
            if (!var0) {
                this.distanceManager.addPlayer(SectionPos.of(param0), param0);
            }

            param0.setChunkTrackingView(ChunkTrackingView.EMPTY);
            this.updateChunkTracking(param0);
        } else {
            SectionPos var2 = param0.getLastSectionPos();
            this.playerMap.removePlayer(param0);
            if (!var1) {
                this.distanceManager.removePlayer(var2, param0);
            }

            this.applyChunkTrackingView(param0, ChunkTrackingView.EMPTY);
        }

    }

    private void updatePlayerPos(ServerPlayer param0) {
        SectionPos var0 = SectionPos.of(param0);
        param0.setLastSectionPos(var0);
    }

    public void move(ServerPlayer param0) {
        for(ChunkMap.TrackedEntity var0 : this.entityMap.values()) {
            if (var0.entity == param0) {
                var0.updatePlayers(this.level.players());
            } else {
                var0.updatePlayer(param0);
            }
        }

        SectionPos var1 = param0.getLastSectionPos();
        SectionPos var2 = SectionPos.of(param0);
        boolean var3 = this.playerMap.ignored(param0);
        boolean var4 = this.skipPlayer(param0);
        boolean var5 = var1.asLong() != var2.asLong();
        if (var5 || var3 != var4) {
            this.updatePlayerPos(param0);
            if (!var3) {
                this.distanceManager.removePlayer(var1, param0);
            }

            if (!var4) {
                this.distanceManager.addPlayer(var2, param0);
            }

            if (!var3 && var4) {
                this.playerMap.ignorePlayer(param0);
            }

            if (var3 && !var4) {
                this.playerMap.unIgnorePlayer(param0);
            }

            this.updateChunkTracking(param0);
        }

    }

    private void updateChunkTracking(ServerPlayer param0) {
        ChunkPos var0 = param0.chunkPosition();
        int var1 = this.getPlayerViewDistance(param0);
        ChunkTrackingView var5 = param0.getChunkTrackingView();
        if (var5 instanceof ChunkTrackingView.Positioned var2 && var2.center().equals(var0) && var2.viewDistance() == var1) {
            return;
        }

        this.applyChunkTrackingView(param0, ChunkTrackingView.of(var0, var1));
    }

    private void applyChunkTrackingView(ServerPlayer param0, ChunkTrackingView param1) {
        if (param0.level() == this.level) {
            ChunkTrackingView var0 = param0.getChunkTrackingView();
            if (param1 instanceof ChunkTrackingView.Positioned var1
                && (!(var0 instanceof ChunkTrackingView.Positioned var2) || !var2.center().equals(var1.center()))) {
                param0.connection.send(new ClientboundSetChunkCacheCenterPacket(var1.center().x, var1.center().z));
            }

            ChunkTrackingView.difference(var0, param1, param1x -> this.markChunkPendingToSend(param0, param1x), param1x -> dropChunk(param0, param1x));
            param0.setChunkTrackingView(param1);
        }
    }

    @Override
    public List<ServerPlayer> getPlayers(ChunkPos param0, boolean param1) {
        Set<ServerPlayer> var0 = this.playerMap.getAllPlayers();
        Builder<ServerPlayer> var1 = ImmutableList.builder();

        for(ServerPlayer var2 : var0) {
            if (param1 && this.isChunkOnTrackedBorder(var2, param0.x, param0.z) || !param1 && this.isChunkTracked(var2, param0.x, param0.z)) {
                var1.add(var2);
            }
        }

        return var1.build();
    }

    protected void addEntity(Entity param0) {
        if (!(param0 instanceof EnderDragonPart)) {
            EntityType<?> var0 = param0.getType();
            int var1 = var0.clientTrackingRange() * 16;
            if (var1 != 0) {
                int var2 = var0.updateInterval();
                if (this.entityMap.containsKey(param0.getId())) {
                    throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
                } else {
                    ChunkMap.TrackedEntity var3 = new ChunkMap.TrackedEntity(param0, var1, var2, var0.trackDeltas());
                    this.entityMap.put(param0.getId(), var3);
                    var3.updatePlayers(this.level.players());
                    if (param0 instanceof ServerPlayer var4) {
                        this.updatePlayerStatus(var4, true);

                        for(ChunkMap.TrackedEntity var5 : this.entityMap.values()) {
                            if (var5.entity != var4) {
                                var5.updatePlayer(var4);
                            }
                        }
                    }

                }
            }
        }
    }

    protected void removeEntity(Entity param0) {
        if (param0 instanceof ServerPlayer var0) {
            this.updatePlayerStatus(var0, false);

            for(ChunkMap.TrackedEntity var1 : this.entityMap.values()) {
                var1.removePlayer(var0);
            }
        }

        ChunkMap.TrackedEntity var2 = this.entityMap.remove(param0.getId());
        if (var2 != null) {
            var2.broadcastRemoved();
        }

    }

    protected void tick() {
        for(ServerPlayer var0 : this.playerMap.getAllPlayers()) {
            this.updateChunkTracking(var0);
        }

        List<ServerPlayer> var1 = Lists.newArrayList();
        List<ServerPlayer> var2 = this.level.players();

        for(ChunkMap.TrackedEntity var3 : this.entityMap.values()) {
            SectionPos var4 = var3.lastSectionPos;
            SectionPos var5 = SectionPos.of(var3.entity);
            boolean var6 = !Objects.equals(var4, var5);
            if (var6) {
                var3.updatePlayers(var2);
                Entity var7 = var3.entity;
                if (var7 instanceof ServerPlayer) {
                    var1.add((ServerPlayer)var7);
                }

                var3.lastSectionPos = var5;
            }

            if (var6 || this.distanceManager.inEntityTickingRange(var5.chunk().toLong())) {
                var3.serverEntity.sendChanges();
            }
        }

        if (!var1.isEmpty()) {
            for(ChunkMap.TrackedEntity var8 : this.entityMap.values()) {
                var8.updatePlayers(var1);
            }
        }

    }

    public void broadcast(Entity param0, Packet<?> param1) {
        ChunkMap.TrackedEntity var0 = this.entityMap.get(param0.getId());
        if (var0 != null) {
            var0.broadcast(param1);
        }

    }

    protected void broadcastAndSend(Entity param0, Packet<?> param1) {
        ChunkMap.TrackedEntity var0 = this.entityMap.get(param0.getId());
        if (var0 != null) {
            var0.broadcastAndSend(param1);
        }

    }

    public void resendBiomesForChunks(List<ChunkAccess> param0) {
        Map<ServerPlayer, List<LevelChunk>> var0 = new HashMap<>();

        for(ChunkAccess var1 : param0) {
            ChunkPos var2 = var1.getPos();
            LevelChunk var4;
            if (var1 instanceof LevelChunk var3) {
                var4 = var3;
            } else {
                var4 = this.level.getChunk(var2.x, var2.z);
            }

            for(ServerPlayer var6 : this.getPlayers(var2, false)) {
                var0.computeIfAbsent(var6, param0x -> new ArrayList()).add(var4);
            }
        }

        var0.forEach((param0x, param1) -> param0x.connection.send(ClientboundChunksBiomesPacket.forChunks(param1)));
    }

    protected PoiManager getPoiManager() {
        return this.poiManager;
    }

    public String getStorageName() {
        return this.storageName;
    }

    void onFullChunkStatusChange(ChunkPos param0, FullChunkStatus param1) {
        this.chunkStatusListener.onChunkStatusChange(param0, param1);
    }

    public void waitForLightBeforeSending(ChunkPos param0, int param1) {
        int var0 = param1 + 1;
        ChunkPos.rangeClosed(param0, var0).forEach(param0x -> {
            ChunkHolder var0x = this.getVisibleChunkIfPresent(param0x.toLong());
            if (var0x != null) {
                var0x.addSendDependency(this.lightEngine.waitForPendingTasks(param0x.x, param0x.z));
            }

        });
    }

    class DistanceManager extends net.minecraft.server.level.DistanceManager {
        protected DistanceManager(Executor param0, Executor param1) {
            super(param0, param1);
        }

        @Override
        protected boolean isChunkToRemove(long param0) {
            return ChunkMap.this.toDrop.contains(param0);
        }

        @Nullable
        @Override
        protected ChunkHolder getChunk(long param0) {
            return ChunkMap.this.getUpdatingChunkIfPresent(param0);
        }

        @Nullable
        @Override
        protected ChunkHolder updateChunkScheduling(long param0, int param1, @Nullable ChunkHolder param2, int param3) {
            return ChunkMap.this.updateChunkScheduling(param0, param1, param2, param3);
        }
    }

    class TrackedEntity {
        final ServerEntity serverEntity;
        final Entity entity;
        private final int range;
        SectionPos lastSectionPos;
        private final Set<ServerPlayerConnection> seenBy = Sets.newIdentityHashSet();

        public TrackedEntity(Entity param0, int param1, int param2, boolean param3) {
            this.serverEntity = new ServerEntity(ChunkMap.this.level, param0, param2, param3, this::broadcast);
            this.entity = param0;
            this.range = param1;
            this.lastSectionPos = SectionPos.of(param0);
        }

        @Override
        public boolean equals(Object param0) {
            if (param0 instanceof ChunkMap.TrackedEntity) {
                return ((ChunkMap.TrackedEntity)param0).entity.getId() == this.entity.getId();
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return this.entity.getId();
        }

        public void broadcast(Packet<?> param0x) {
            for(ServerPlayerConnection var0 : this.seenBy) {
                var0.send(param0x);
            }

        }

        public void broadcastAndSend(Packet<?> param0) {
            this.broadcast(param0);
            if (this.entity instanceof ServerPlayer) {
                ((ServerPlayer)this.entity).connection.send(param0);
            }

        }

        public void broadcastRemoved() {
            for(ServerPlayerConnection var0 : this.seenBy) {
                this.serverEntity.removePairing(var0.getPlayer());
            }

        }

        public void removePlayer(ServerPlayer param0) {
            if (this.seenBy.remove(param0.connection)) {
                this.serverEntity.removePairing(param0);
            }

        }

        public void updatePlayer(ServerPlayer param0) {
            if (param0 != this.entity) {
                Vec3 var0 = param0.position().subtract(this.entity.position());
                int var1 = ChunkMap.this.getPlayerViewDistance(param0);
                double var2 = (double)Math.min(this.getEffectiveRange(), var1 * 16);
                double var3 = var0.x * var0.x + var0.z * var0.z;
                double var4 = var2 * var2;
                boolean var5 = var3 <= var4
                    && this.entity.broadcastToPlayer(param0)
                    && ChunkMap.this.isChunkTracked(param0, this.entity.chunkPosition().x, this.entity.chunkPosition().z);
                if (var5) {
                    if (this.seenBy.add(param0.connection)) {
                        this.serverEntity.addPairing(param0);
                    }
                } else if (this.seenBy.remove(param0.connection)) {
                    this.serverEntity.removePairing(param0);
                }

            }
        }

        private int scaledRange(int param0) {
            return ChunkMap.this.level.getServer().getScaledTrackingDistance(param0);
        }

        private int getEffectiveRange() {
            int var0 = this.range;

            for(Entity var1 : this.entity.getIndirectPassengers()) {
                int var2 = var1.getType().clientTrackingRange() * 16;
                if (var2 > var0) {
                    var0 = var2;
                }
            }

            return this.scaledRange(var0);
        }

        public void updatePlayers(List<ServerPlayer> param0) {
            for(ServerPlayer var0 : param0) {
                this.updatePlayer(var0);
            }

        }
    }
}
