package net.minecraft.server.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
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
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkMap extends ChunkStorage implements ChunkHolder.PlayerProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int MAX_CHUNK_DISTANCE = 33 + ChunkStatus.maxDistance();
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap = new Long2ObjectLinkedOpenHashMap<>();
    private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap = this.updatingChunkMap.clone();
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads = new Long2ObjectLinkedOpenHashMap<>();
    private final LongSet entitiesInLevel = new LongOpenHashSet();
    private final ServerLevel level;
    private final ThreadedLevelLightEngine lightEngine;
    private final BlockableEventLoop<Runnable> mainThreadExecutor;
    private final ChunkGenerator generator;
    private final Supplier<DimensionDataStorage> overworldDataStorage;
    private final PoiManager poiManager;
    private final LongSet toDrop = new LongOpenHashSet();
    private boolean modified;
    private final ChunkTaskPriorityQueueSorter queueSorter;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> worldgenMailbox;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;
    private final ChunkProgressListener progressListener;
    private final ChunkMap.DistanceManager distanceManager;
    private final AtomicInteger tickingGenerated = new AtomicInteger();
    private final StructureManager structureManager;
    private final File storageFolder;
    private final PlayerMap playerMap = new PlayerMap();
    private final Int2ObjectMap<ChunkMap.TrackedEntity> entityMap = new Int2ObjectOpenHashMap<>();
    private final Long2ByteMap chunkTypeCache = new Long2ByteOpenHashMap();
    private final Queue<Runnable> unloadQueue = Queues.newConcurrentLinkedQueue();
    private int viewDistance;

    public ChunkMap(
        ServerLevel param0,
        LevelStorageSource.LevelStorageAccess param1,
        DataFixer param2,
        StructureManager param3,
        Executor param4,
        BlockableEventLoop<Runnable> param5,
        LightChunkGetter param6,
        ChunkGenerator param7,
        ChunkProgressListener param8,
        Supplier<DimensionDataStorage> param9,
        int param10,
        boolean param11
    ) {
        super(new File(param1.getDimensionPath(param0.dimensionType()), "region"), param2, param11);
        this.structureManager = param3;
        this.storageFolder = param1.getDimensionPath(param0.dimensionType());
        this.level = param0;
        this.generator = param7;
        this.mainThreadExecutor = param5;
        ProcessorMailbox<Runnable> var0 = ProcessorMailbox.create(param4, "worldgen");
        ProcessorHandle<Runnable> var1 = ProcessorHandle.of("main", param5::tell);
        this.progressListener = param8;
        ProcessorMailbox<Runnable> var2 = ProcessorMailbox.create(param4, "light");
        this.queueSorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(var0, var1, var2), param4, Integer.MAX_VALUE);
        this.worldgenMailbox = this.queueSorter.getProcessor(var0, false);
        this.mainThreadMailbox = this.queueSorter.getProcessor(var1, false);
        this.lightEngine = new ThreadedLevelLightEngine(
            param6, this, this.level.dimensionType().hasSkyLight(), var2, this.queueSorter.getProcessor(var2, false)
        );
        this.distanceManager = new ChunkMap.DistanceManager(param4, param5);
        this.overworldDataStorage = param9;
        this.poiManager = new PoiManager(new File(this.storageFolder, "poi"), param2, param11);
        this.setViewDistance(param10);
    }

    private static double euclideanDistanceSquared(ChunkPos param0, Entity param1) {
        double var0 = (double)(param0.x * 16 + 8);
        double var1 = (double)(param0.z * 16 + 8);
        double var2 = var0 - param1.getX();
        double var3 = var1 - param1.getZ();
        return var2 * var2 + var3 * var3;
    }

    private static int checkerboardDistance(ChunkPos param0, ServerPlayer param1, boolean param2) {
        int var1;
        int var2;
        if (param2) {
            SectionPos var0 = param1.getLastSectionPos();
            var1 = var0.x();
            var2 = var0.z();
        } else {
            var1 = Mth.floor(param1.getX() / 16.0);
            var2 = Mth.floor(param1.getZ() / 16.0);
        }

        return checkerboardDistance(param0, var1, var2);
    }

    private static int checkerboardDistance(ChunkPos param0, int param1, int param2) {
        int var0 = param0.x - param1;
        int var1 = param0.z - param2;
        return Math.max(Math.abs(var0), Math.abs(var1));
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

    @OnlyIn(Dist.CLIENT)
    public String getChunkDebugData(ChunkPos param0) {
        ChunkHolder var0 = this.getVisibleChunkIfPresent(param0.toLong());
        if (var0 == null) {
            return "null";
        } else {
            String var1 = var0.getTicketLevel() + "\n";
            ChunkStatus var2 = var0.getLastAvailableStatus();
            ChunkAccess var3 = var0.getLastAvailable();
            if (var2 != null) {
                var1 = var1 + "St: \u00a7" + var2.getIndex() + var2 + '\u00a7' + "r\n";
            }

            if (var3 != null) {
                var1 = var1 + "Ch: \u00a7" + var3.getStatus().getIndex() + var3.getStatus() + '\u00a7' + "r\n";
            }

            ChunkHolder.FullChunkStatus var4 = var0.getFullStatus();
            var1 = var1 + "\u00a7" + var4.ordinal() + var4;
            return var1 + '\u00a7' + "r";
        }
    }

    private CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> getChunkRangeFuture(
        ChunkPos param0, int param1, IntFunction<ChunkStatus> param2
    ) {
        List<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> var0 = Lists.newArrayList();
        int var1 = param0.x;
        int var2 = param0.z;

        for(int var3 = -param1; var3 <= param1; ++var3) {
            for(int var4 = -param1; var4 <= param1; ++var4) {
                int var5 = Math.max(Math.abs(var4), Math.abs(var3));
                final ChunkPos var6 = new ChunkPos(var1 + var4, var2 + var3);
                long var7 = var6.toLong();
                ChunkHolder var8 = this.getUpdatingChunkIfPresent(var7);
                if (var8 == null) {
                    return CompletableFuture.completedFuture(Either.right(new ChunkHolder.ChunkLoadingFailure() {
                        @Override
                        public String toString() {
                            return "Unloaded " + var6.toString();
                        }
                    }));
                }

                ChunkStatus var9 = param2.apply(var5);
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var10 = var8.getOrScheduleFuture(var9, this);
                var0.add(var10);
            }
        }

        CompletableFuture<List<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> var11 = Util.sequence(var0);
        return var11.thenApply(
            param3 -> {
                List<ChunkAccess> var0x = Lists.newArrayList();
                int var1x = 0;
    
                for(final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> var2x : param3) {
                    Optional<ChunkAccess> var3x = var2x.left();
                    if (!var3x.isPresent()) {
                        final int var4x = var1x;
                        return Either.right(
                            new ChunkHolder.ChunkLoadingFailure() {
                                @Override
                                public String toString() {
                                    return "Unloaded "
                                        + new ChunkPos(var1 + var4x % (param1 * 2 + 1), var2 + var4x / (param1 * 2 + 1))
                                        + " "
                                        + var2x.right().get().toString();
                                }
                            }
                        );
                    }
    
                    var0x.add(var3x.get());
                    ++var1x;
                }
    
                return Either.left(var0x);
            }
        );
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getEntityTickingRangeFuture(ChunkPos param0) {
        return this.getChunkRangeFuture(param0, 2, param0x -> ChunkStatus.FULL)
            .thenApplyAsync(param0x -> param0x.mapLeft(param0xx -> (LevelChunk)param0xx.get(param0xx.size() / 2)), this.mainThreadExecutor);
    }

    @Nullable
    private ChunkHolder updateChunkScheduling(long param0, int param1, @Nullable ChunkHolder param2, int param3) {
        if (param3 > MAX_CHUNK_DISTANCE && param1 > MAX_CHUNK_DISTANCE) {
            return param2;
        } else {
            if (param2 != null) {
                param2.setTicketLevel(param1);
            }

            if (param2 != null) {
                if (param1 > MAX_CHUNK_DISTANCE) {
                    this.toDrop.add(param0);
                } else {
                    this.toDrop.remove(param0);
                }
            }

            if (param1 <= MAX_CHUNK_DISTANCE && param2 == null) {
                param2 = this.pendingUnloads.remove(param0);
                if (param2 != null) {
                    param2.setTicketLevel(param1);
                } else {
                    param2 = new ChunkHolder(new ChunkPos(param0), param1, this.lightEngine, this.queueSorter, this);
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
                .collect(Collectors.toList());
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
            LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", this.storageFolder.getName());
        } else {
            this.visibleChunkMap.values().stream().filter(ChunkHolder::wasAccessibleSinceLastSave).forEach(param0x -> {
                ChunkAccess var0x = param0x.getChunkToSave().getNow(null);
                if (var0x instanceof ImposterProtoChunk || var0x instanceof LevelChunk) {
                    this.save(var0x);
                    param0x.refreshAccessibility();
                }

            });
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

        while((param0.getAsBoolean() || this.unloadQueue.size() > 2000) && (var2 = (long)this.unloadQueue.poll()) != null) {
            var2.run();
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
                    if (this.entitiesInLevel.remove(param0) && param3 instanceof LevelChunk) {
                        LevelChunk var1x = (LevelChunk)param3;
                        this.level.unload(var1x);
                    }

                    this.lightEngine.updateChunkStatus(param3.getPos());
                    this.lightEngine.tryScheduleUpdate();
                    this.progressListener.onStatusChange(param3.getPos(), null);
                }

            }
        }, this.unloadQueue::add).whenComplete((param1x, param2) -> {
            if (param2 != null) {
                LOGGER.error("Failed to save chunk " + param1.getPos(), param2);
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
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var1 = param0.getOrScheduleFuture(param1.getParent(), this);
            return var1.thenComposeAsync(param3 -> {
                Optional<ChunkAccess> var0x = param3.left();
                if (!var0x.isPresent()) {
                    return CompletableFuture.completedFuture(param3);
                } else {
                    if (param1 == ChunkStatus.LIGHT) {
                        this.distanceManager.addTicket(TicketType.LIGHT, var0, 33 + ChunkStatus.getDistance(ChunkStatus.FEATURES), var0);
                    }

                    ChunkAccess var1x = var0x.get();
                    if (var1x.getStatus().isOrAfter(param1)) {
                        CompletableFuture var3x;
                        if (param1 == ChunkStatus.LIGHT) {
                            var3x = this.scheduleChunkGeneration(param0, param1);
                        } else {
                            var3x = param1.load(this.level, this.structureManager, this.lightEngine, param1x -> this.protoChunkToFullChunk(param0), var1x);
                        }

                        this.progressListener.onStatusChange(var0, param1);
                        return var3x;
                    } else {
                        return this.scheduleChunkGeneration(param0, param1);
                    }
                }
            }, this.mainThreadExecutor);
        }
    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkLoad(ChunkPos param0) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.level.getProfiler().incrementCounter("chunkLoad");
                CompoundTag var0 = this.readChunk(param0);
                if (var0 != null) {
                    boolean var1 = var0.contains("Level", 10) && var0.getCompound("Level").contains("Status", 8);
                    if (var1) {
                        ChunkAccess var2 = ChunkSerializer.read(this.level, this.structureManager, this.poiManager, param0, var0);
                        var2.setLastSaveTime(this.level.getGameTime());
                        this.markPosition(param0, var2.getStatus().getChunkType());
                        return Either.left(var2);
                    }

                    LOGGER.error("Chunk file at {} is missing level data, skipping", param0);
                }
            } catch (ReportedException var51) {
                Throwable var4 = var51.getCause();
                if (!(var4 instanceof IOException)) {
                    this.markPositionReplaceable(param0);
                    throw var51;
                }

                LOGGER.error("Couldn't load chunk {}", param0, var4);
            } catch (Exception var6) {
                LOGGER.error("Couldn't load chunk {}", param0, var6);
            }

            this.markPositionReplaceable(param0);
            return Either.left(new ProtoChunk(param0, UpgradeData.EMPTY));
        }, this.mainThreadExecutor);
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
            var0, param1.getRange(), param1x -> this.getDependencyStatus(param1, param1x)
        );
        this.level.getProfiler().incrementCounter(() -> "chunkGenerate " + param1.getName());
        return var1.thenComposeAsync(
            param3 -> param3.map(
                    param3x -> {
                        try {
                            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var0x = param1.generate(
                                this.level, this.generator, this.structureManager, this.lightEngine, param1x -> this.protoChunkToFullChunk(param0), param3x
                            );
                            this.progressListener.onStatusChange(var0, param1);
                            return var0x;
                        } catch (Exception var8) {
                            CrashReport var2x = CrashReport.forThrowable(var8, "Exception generating new chunk");
                            CrashReportCategory var3x = var2x.addCategory("Chunk to be generated");
                            var3x.setDetail("Location", String.format("%d,%d", var0.x, var0.z));
                            var3x.setDetail("Position hash", ChunkPos.asLong(var0.x, var0.z));
                            var3x.setDetail("Generator", this.generator);
                            throw new ReportedException(var2x);
                        }
                    },
                    param1x -> {
                        this.releaseLightTicket(var0);
                        return CompletableFuture.completedFuture(Either.right(param1x));
                    }
                ),
            param1x -> this.worldgenMailbox.tell(ChunkTaskPriorityQueueSorter.message(param0, param1x))
        );
    }

    protected void releaseLightTicket(ChunkPos param0) {
        this.mainThreadExecutor
            .tell(
                Util.name(
                    () -> this.distanceManager.removeTicket(TicketType.LIGHT, param0, 33 + ChunkStatus.getDistance(ChunkStatus.FEATURES), param0),
                    () -> "release light ticket " + param0
                )
            );
    }

    private ChunkStatus getDependencyStatus(ChunkStatus param0, int param1) {
        ChunkStatus var0;
        if (param1 == 0) {
            var0 = param0.getParent();
        } else {
            var0 = ChunkStatus.getStatus(ChunkStatus.getDistance(param0) + param1);
        }

        return var0;
    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> protoChunkToFullChunk(ChunkHolder param0) {
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var0 = param0.getFutureIfPresentUnchecked(ChunkStatus.FULL.getParent());
        return var0.thenApplyAsync(param1 -> {
            ChunkStatus var0x = ChunkHolder.getStatus(param0.getTicketLevel());
            return !var0x.isOrAfter(ChunkStatus.FULL) ? ChunkHolder.UNLOADED_CHUNK : param1.mapLeft(param1x -> {
                ChunkPos var0xx = param0.getPos();
                LevelChunk var2x;
                if (param1x instanceof ImposterProtoChunk) {
                    var2x = ((ImposterProtoChunk)param1x).getWrapped();
                } else {
                    var2x = new LevelChunk(this.level, (ProtoChunk)param1x);
                    param0.replaceProtoChunk(new ImposterProtoChunk(var2x));
                }

                var2x.setFullStatus(() -> ChunkHolder.getFullChunkStatus(param0.getTicketLevel()));
                var2x.runPostLoad();
                if (this.entitiesInLevel.add(var0xx.toLong())) {
                    var2x.setLoaded(true);
                    this.level.addAllPendingBlockEntities(var2x.getBlockEntities().values());
                    List<Entity> var3x = null;
                    ClassInstanceMultiMap[] var6 = var2x.getEntitySections();
                    int var7 = var6.length;

                    for(int var8 = 0; var8 < var7; ++var8) {
                        for(Entity var5 : var6[var8]) {
                            if (!(var5 instanceof Player) && !this.level.loadFromChunk(var5)) {
                                if (var3x == null) {
                                    var3x = Lists.newArrayList(var5);
                                } else {
                                    var3x.add(var5);
                                }
                            }
                        }
                    }

                    if (var3x != null) {
                        var3x.forEach(var2x::removeEntity);
                    }
                }

                return var2x;
            });
        }, param1 -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(param1, param0.getPos().toLong(), param0::getTicketLevel)));
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> postProcess(ChunkHolder param0) {
        ChunkPos var0 = param0.getPos();
        CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> var1 = this.getChunkRangeFuture(var0, 1, param0x -> ChunkStatus.FULL);
        CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> var2 = var1.thenApplyAsync(param0x -> param0x.flatMap(param0xx -> {
                LevelChunk var0x = (LevelChunk)param0xx.get(param0xx.size() / 2);
                var0x.postProcessGeneration();
                return Either.left(var0x);
            }), param1 -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(param0, param1)));
        var2.thenAcceptAsync(param1 -> param1.mapLeft(param1x -> {
                this.tickingGenerated.getAndIncrement();
                Packet<?>[] var0x = new Packet[2];
                this.getPlayers(var0, false).forEach(param2 -> this.playerLoadedChunk(param2, var0x, param1x));
                return Either.left(param1x);
            }), param1 -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(param0, param1)));
        return var2;
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> unpackTicks(ChunkHolder param0) {
        return param0.getOrScheduleFuture(ChunkStatus.FULL, this).thenApplyAsync(param0x -> param0x.mapLeft(param0xx -> {
                LevelChunk var0x = (LevelChunk)param0xx;
                var0x.unpackTicks();
                return var0x;
            }), param1 -> this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(param0, param1)));
    }

    public int getTickingGenerated() {
        return this.tickingGenerated.get();
    }

    private boolean save(ChunkAccess param0x) {
        this.poiManager.flush(param0x.getPos());
        if (!param0x.isUnsaved()) {
            return false;
        } else {
            param0x.setLastSaveTime(this.level.getGameTime());
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
                var1 = this.readChunk(param0);
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

    protected void setViewDistance(int param0) {
        int var0 = Mth.clamp(param0 + 1, 3, 33);
        if (var0 != this.viewDistance) {
            int var1 = this.viewDistance;
            this.viewDistance = var0;
            this.distanceManager.updatePlayerTickets(this.viewDistance);

            for(ChunkHolder var2 : this.updatingChunkMap.values()) {
                ChunkPos var3 = var2.getPos();
                Packet<?>[] var4 = new Packet[2];
                this.getPlayers(var3, false).forEach(param3 -> {
                    int var0x = checkerboardDistance(var3, param3, true);
                    boolean var1x = var0x <= var1;
                    boolean var2x = var0x <= this.viewDistance;
                    this.updateChunkTracking(param3, var3, var4, var1x, var2x);
                });
            }
        }

    }

    protected void updateChunkTracking(ServerPlayer param0, ChunkPos param1, Packet<?>[] param2, boolean param3, boolean param4) {
        if (param0.level == this.level) {
            if (param4 && !param3) {
                ChunkHolder var0 = this.getVisibleChunkIfPresent(param1.toLong());
                if (var0 != null) {
                    LevelChunk var1 = var0.getTickingChunk();
                    if (var1 != null) {
                        this.playerLoadedChunk(param0, param2, var1);
                    }

                    DebugPackets.sendPoiPacketsForChunk(this.level, param1);
                }
            }

            if (!param4 && param3) {
                param0.untrackChunk(param1);
            }

        }
    }

    public int size() {
        return this.visibleChunkMap.size();
    }

    protected ChunkMap.DistanceManager getDistanceManager() {
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
            .addColumn("entity_count")
            .addColumn("block_entity_count")
            .build(param0);

        for(Entry<ChunkHolder> var1 : this.visibleChunkMap.long2ObjectEntrySet()) {
            ChunkPos var2 = new ChunkPos(var1.getLongKey());
            ChunkHolder var3 = var1.getValue();
            Optional<ChunkAccess> var4 = Optional.ofNullable(var3.getLastAvailable());
            Optional<LevelChunk> var5 = var4.flatMap(param0x -> param0x instanceof LevelChunk ? Optional.of((LevelChunk)param0x) : Optional.empty());
            var0.writeRow(
                var2.x,
                var2.z,
                var3.getTicketLevel(),
                var4.isPresent(),
                var4.map(ChunkAccess::getStatus).orElse(null),
                var5.map(LevelChunk::getFullStatus).orElse(null),
                printFuture(var3.getFullChunkFuture()),
                printFuture(var3.getTickingChunkFuture()),
                printFuture(var3.getEntityTickingChunkFuture()),
                this.distanceManager.getTicketDebugString(var1.getLongKey()),
                !this.noPlayersCloseForSpawning(var2),
                var5.<Integer>map(param0x -> Stream.of(param0x.getEntitySections()).mapToInt(ClassInstanceMultiMap::size).sum()).orElse(0),
                var5.<Integer>map(param0x -> param0x.getBlockEntities().size()).orElse(0)
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

    @Nullable
    private CompoundTag readChunk(ChunkPos param0) throws IOException {
        CompoundTag var0 = this.read(param0);
        return var0 == null ? null : this.upgradeChunkTag(this.level.dimensionType(), this.overworldDataStorage, var0);
    }

    boolean noPlayersCloseForSpawning(ChunkPos param0) {
        long var0 = param0.toLong();
        return !this.distanceManager.hasPlayersNearby(var0)
            ? true
            : this.playerMap.getPlayers(var0).noneMatch(param1 -> !param1.isSpectator() && euclideanDistanceSquared(param0, param1) < 16384.0);
    }

    private boolean skipPlayer(ServerPlayer param0) {
        return param0.isSpectator() && !this.level.getGameRules().getBoolean(GameRules.RULE_SPECTATORSGENERATECHUNKS);
    }

    void updatePlayerStatus(ServerPlayer param0, boolean param1) {
        boolean var0 = this.skipPlayer(param0);
        boolean var1 = this.playerMap.ignoredOrUnknown(param0);
        int var2 = Mth.floor(param0.getX()) >> 4;
        int var3 = Mth.floor(param0.getZ()) >> 4;
        if (param1) {
            this.playerMap.addPlayer(ChunkPos.asLong(var2, var3), param0, var0);
            this.updatePlayerPos(param0);
            if (!var0) {
                this.distanceManager.addPlayer(SectionPos.of(param0), param0);
            }
        } else {
            SectionPos var4 = param0.getLastSectionPos();
            this.playerMap.removePlayer(var4.chunk().toLong(), param0);
            if (!var1) {
                this.distanceManager.removePlayer(var4, param0);
            }
        }

        for(int var5 = var2 - this.viewDistance; var5 <= var2 + this.viewDistance; ++var5) {
            for(int var6 = var3 - this.viewDistance; var6 <= var3 + this.viewDistance; ++var6) {
                ChunkPos var7 = new ChunkPos(var5, var6);
                this.updateChunkTracking(param0, var7, new Packet[2], !param1, param1);
            }
        }

    }

    private SectionPos updatePlayerPos(ServerPlayer param0) {
        SectionPos var0 = SectionPos.of(param0);
        param0.setLastSectionPos(var0);
        param0.connection.send(new ClientboundSetChunkCacheCenterPacket(var0.x(), var0.z()));
        return var0;
    }

    public void move(ServerPlayer param0) {
        for(ChunkMap.TrackedEntity var0 : this.entityMap.values()) {
            if (var0.entity == param0) {
                var0.updatePlayers(this.level.players());
            } else {
                var0.updatePlayer(param0);
            }
        }

        int var1 = Mth.floor(param0.getX()) >> 4;
        int var2 = Mth.floor(param0.getZ()) >> 4;
        SectionPos var3 = param0.getLastSectionPos();
        SectionPos var4 = SectionPos.of(param0);
        long var5 = var3.chunk().toLong();
        long var6 = var4.chunk().toLong();
        boolean var7 = this.playerMap.ignored(param0);
        boolean var8 = this.skipPlayer(param0);
        boolean var9 = var3.asLong() != var4.asLong();
        if (var9 || var7 != var8) {
            this.updatePlayerPos(param0);
            if (!var7) {
                this.distanceManager.removePlayer(var3, param0);
            }

            if (!var8) {
                this.distanceManager.addPlayer(var4, param0);
            }

            if (!var7 && var8) {
                this.playerMap.ignorePlayer(param0);
            }

            if (var7 && !var8) {
                this.playerMap.unIgnorePlayer(param0);
            }

            if (var5 != var6) {
                this.playerMap.updatePlayer(var5, var6, param0);
            }
        }

        int var10 = var3.x();
        int var11 = var3.z();
        if (Math.abs(var10 - var1) <= this.viewDistance * 2 && Math.abs(var11 - var2) <= this.viewDistance * 2) {
            int var12 = Math.min(var1, var10) - this.viewDistance;
            int var13 = Math.min(var2, var11) - this.viewDistance;
            int var14 = Math.max(var1, var10) + this.viewDistance;
            int var15 = Math.max(var2, var11) + this.viewDistance;

            for(int var16 = var12; var16 <= var14; ++var16) {
                for(int var17 = var13; var17 <= var15; ++var17) {
                    ChunkPos var18 = new ChunkPos(var16, var17);
                    boolean var19 = checkerboardDistance(var18, var10, var11) <= this.viewDistance;
                    boolean var20 = checkerboardDistance(var18, var1, var2) <= this.viewDistance;
                    this.updateChunkTracking(param0, var18, new Packet[2], var19, var20);
                }
            }
        } else {
            for(int var21 = var10 - this.viewDistance; var21 <= var10 + this.viewDistance; ++var21) {
                for(int var22 = var11 - this.viewDistance; var22 <= var11 + this.viewDistance; ++var22) {
                    ChunkPos var23 = new ChunkPos(var21, var22);
                    boolean var24 = true;
                    boolean var25 = false;
                    this.updateChunkTracking(param0, var23, new Packet[2], true, false);
                }
            }

            for(int var26 = var1 - this.viewDistance; var26 <= var1 + this.viewDistance; ++var26) {
                for(int var27 = var2 - this.viewDistance; var27 <= var2 + this.viewDistance; ++var27) {
                    ChunkPos var28 = new ChunkPos(var26, var27);
                    boolean var29 = false;
                    boolean var30 = true;
                    this.updateChunkTracking(param0, var28, new Packet[2], false, true);
                }
            }
        }

    }

    @Override
    public Stream<ServerPlayer> getPlayers(ChunkPos param0, boolean param1) {
        return this.playerMap.getPlayers(param0.toLong()).filter(param2 -> {
            int var0 = checkerboardDistance(param0, param2, true);
            if (var0 > this.viewDistance) {
                return false;
            } else {
                return !param1 || var0 == this.viewDistance;
            }
        });
    }

    protected void addEntity(Entity param0) {
        if (!(param0 instanceof EnderDragonPart)) {
            if (!(param0 instanceof LightningBolt)) {
                EntityType<?> var0 = param0.getType();
                int var1 = var0.clientTrackingRange() * 16;
                int var2 = var0.updateInterval();
                if (this.entityMap.containsKey(param0.getId())) {
                    throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
                } else {
                    ChunkMap.TrackedEntity var3 = new ChunkMap.TrackedEntity(param0, var1, var2, var0.trackDeltas());
                    this.entityMap.put(param0.getId(), var3);
                    var3.updatePlayers(this.level.players());
                    if (param0 instanceof ServerPlayer) {
                        ServerPlayer var4 = (ServerPlayer)param0;
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
        if (param0 instanceof ServerPlayer) {
            ServerPlayer var0 = (ServerPlayer)param0;
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
        List<ServerPlayer> var0 = Lists.newArrayList();
        List<ServerPlayer> var1 = this.level.players();

        for(ChunkMap.TrackedEntity var2 : this.entityMap.values()) {
            SectionPos var3 = var2.lastSectionPos;
            SectionPos var4 = SectionPos.of(var2.entity);
            if (!Objects.equals(var3, var4)) {
                var2.updatePlayers(var1);
                Entity var5 = var2.entity;
                if (var5 instanceof ServerPlayer) {
                    var0.add((ServerPlayer)var5);
                }

                var2.lastSectionPos = var4;
            }

            var2.serverEntity.sendChanges();
        }

        if (!var0.isEmpty()) {
            for(ChunkMap.TrackedEntity var6 : this.entityMap.values()) {
                var6.updatePlayers(var0);
            }
        }

    }

    protected void broadcast(Entity param0, Packet<?> param1) {
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

    private void playerLoadedChunk(ServerPlayer param0, Packet<?>[] param1, LevelChunk param2) {
        if (param1[0] == null) {
            param1[0] = new ClientboundLevelChunkPacket(param2, 65535);
            param1[1] = new ClientboundLightUpdatePacket(param2.getPos(), this.lightEngine);
        }

        param0.trackChunk(param2.getPos(), param1[0], param1[1]);
        DebugPackets.sendPoiPacketsForChunk(this.level, param2.getPos());
        List<Entity> var0 = Lists.newArrayList();
        List<Entity> var1 = Lists.newArrayList();

        for(ChunkMap.TrackedEntity var2 : this.entityMap.values()) {
            Entity var3 = var2.entity;
            if (var3 != param0 && var3.xChunk == param2.getPos().x && var3.zChunk == param2.getPos().z) {
                var2.updatePlayer(param0);
                if (var3 instanceof Mob && ((Mob)var3).getLeashHolder() != null) {
                    var0.add(var3);
                }

                if (!var3.getPassengers().isEmpty()) {
                    var1.add(var3);
                }
            }
        }

        if (!var0.isEmpty()) {
            for(Entity var4 : var0) {
                param0.connection.send(new ClientboundSetEntityLinkPacket(var4, ((Mob)var4).getLeashHolder()));
            }
        }

        if (!var1.isEmpty()) {
            for(Entity var5 : var1) {
                param0.connection.send(new ClientboundSetPassengersPacket(var5));
            }
        }

    }

    protected PoiManager getPoiManager() {
        return this.poiManager;
    }

    public CompletableFuture<Void> packTicks(LevelChunk param0) {
        return this.mainThreadExecutor.submit(() -> param0.packTicks(this.level));
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
        private final ServerEntity serverEntity;
        private final Entity entity;
        private final int range;
        private SectionPos lastSectionPos;
        private final Set<ServerPlayer> seenBy = Sets.newHashSet();

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
            for(ServerPlayer var0 : this.seenBy) {
                var0.connection.send(param0x);
            }

        }

        public void broadcastAndSend(Packet<?> param0) {
            this.broadcast(param0);
            if (this.entity instanceof ServerPlayer) {
                ((ServerPlayer)this.entity).connection.send(param0);
            }

        }

        public void broadcastRemoved() {
            for(ServerPlayer var0 : this.seenBy) {
                this.serverEntity.removePairing(var0);
            }

        }

        public void removePlayer(ServerPlayer param0) {
            if (this.seenBy.remove(param0)) {
                this.serverEntity.removePairing(param0);
            }

        }

        public void updatePlayer(ServerPlayer param0) {
            if (param0 != this.entity) {
                Vec3 var0 = param0.position().subtract(this.serverEntity.sentPos());
                int var1 = Math.min(this.getEffectiveRange(), (ChunkMap.this.viewDistance - 1) * 16);
                boolean var2 = var0.x >= (double)(-var1)
                    && var0.x <= (double)var1
                    && var0.z >= (double)(-var1)
                    && var0.z <= (double)var1
                    && this.entity.broadcastToPlayer(param0);
                if (var2) {
                    boolean var3 = this.entity.forcedLoading;
                    if (!var3) {
                        ChunkPos var4 = new ChunkPos(this.entity.xChunk, this.entity.zChunk);
                        ChunkHolder var5 = ChunkMap.this.getVisibleChunkIfPresent(var4.toLong());
                        if (var5 != null && var5.getTickingChunk() != null) {
                            var3 = ChunkMap.checkerboardDistance(var4, param0, false) <= ChunkMap.this.viewDistance;
                        }
                    }

                    if (var3 && this.seenBy.add(param0)) {
                        this.serverEntity.addPairing(param0);
                    }
                } else if (this.seenBy.remove(param0)) {
                    this.serverEntity.removePairing(param0);
                }

            }
        }

        private int scaledRange(int param0) {
            return ChunkMap.this.level.getServer().getScaledTrackingDistance(param0);
        }

        private int getEffectiveRange() {
            Collection<Entity> var0 = this.entity.getIndirectPassengers();
            int var1 = this.range;

            for(Entity var2 : var0) {
                int var3 = var2.getType().clientTrackingRange() * 16;
                if (var3 > var1) {
                    var1 = var3;
                }
            }

            return this.scaledRange(var1);
        }

        public void updatePlayers(List<ServerPlayer> param0) {
            for(ServerPlayer var0 : param0) {
                this.updatePlayer(var0);
            }

        }
    }
}
