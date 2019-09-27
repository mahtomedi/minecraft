package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerChunkCache extends ChunkSource {
    private static final int MAGIC_NUMBER = (int)Math.pow(17.0, 2.0);
    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
    private final DistanceManager distanceManager;
    private final ChunkGenerator<?> generator;
    private final ServerLevel level;
    private final Thread mainThread;
    private final ThreadedLevelLightEngine lightEngine;
    private final ServerChunkCache.MainThreadExecutor mainThreadProcessor;
    public final ChunkMap chunkMap;
    private final DimensionDataStorage dataStorage;
    private long lastInhabitedUpdate;
    private boolean spawnEnemies = true;
    private boolean spawnFriendlies = true;
    private final long[] lastChunkPos = new long[4];
    private final ChunkStatus[] lastChunkStatus = new ChunkStatus[4];
    private final ChunkAccess[] lastChunk = new ChunkAccess[4];

    public ServerChunkCache(
        ServerLevel param0,
        File param1,
        DataFixer param2,
        StructureManager param3,
        Executor param4,
        ChunkGenerator<?> param5,
        int param6,
        ChunkProgressListener param7,
        Supplier<DimensionDataStorage> param8
    ) {
        this.level = param0;
        this.mainThreadProcessor = new ServerChunkCache.MainThreadExecutor(param0);
        this.generator = param5;
        this.mainThread = Thread.currentThread();
        File var0 = param0.getDimension().getType().getStorageFolder(param1);
        File var1 = new File(var0, "data");
        var1.mkdirs();
        this.dataStorage = new DimensionDataStorage(var1, param2);
        this.chunkMap = new ChunkMap(param0, param1, param2, param3, param4, this.mainThreadProcessor, this, this.getGenerator(), param7, param8, param6);
        this.lightEngine = this.chunkMap.getLightEngine();
        this.distanceManager = this.chunkMap.getDistanceManager();
        this.clearCache();
    }

    public ThreadedLevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    @Nullable
    private ChunkHolder getVisibleChunkIfPresent(long param0) {
        return this.chunkMap.getVisibleChunkIfPresent(param0);
    }

    public int getTickingGenerated() {
        return this.chunkMap.getTickingGenerated();
    }

    private void storeInCache(long param0, ChunkAccess param1, ChunkStatus param2) {
        for(int var0 = 3; var0 > 0; --var0) {
            this.lastChunkPos[var0] = this.lastChunkPos[var0 - 1];
            this.lastChunkStatus[var0] = this.lastChunkStatus[var0 - 1];
            this.lastChunk[var0] = this.lastChunk[var0 - 1];
        }

        this.lastChunkPos[0] = param0;
        this.lastChunkStatus[0] = param2;
        this.lastChunk[0] = param1;
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int param0, int param1, ChunkStatus param2, boolean param3) {
        if (Thread.currentThread() != this.mainThread) {
            return CompletableFuture.<ChunkAccess>supplyAsync(() -> this.getChunk(param0, param1, param2, param3), this.mainThreadProcessor).join();
        } else {
            long var0 = ChunkPos.asLong(param0, param1);

            for(int var1 = 0; var1 < 4; ++var1) {
                if (var0 == this.lastChunkPos[var1] && param2 == this.lastChunkStatus[var1]) {
                    ChunkAccess var2 = this.lastChunk[var1];
                    if (var2 != null || !param3) {
                        return var2;
                    }
                }
            }

            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var3 = this.getChunkFutureMainThread(param0, param1, param2, param3);
            this.mainThreadProcessor.managedBlock(var3::isDone);
            ChunkAccess var4 = var3.join().map(param0x -> param0x, param1x -> {
                if (param3) {
                    throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Chunk not there when requested: " + param1x));
                } else {
                    return null;
                }
            });
            this.storeInCache(var0, var4, param2);
            return var4;
        }
    }

    @Nullable
    @Override
    public LevelChunk getChunkNow(int param0, int param1) {
        if (Thread.currentThread() != this.mainThread) {
            return null;
        } else {
            long var0 = ChunkPos.asLong(param0, param1);

            for(int var1 = 0; var1 < 4; ++var1) {
                if (var0 == this.lastChunkPos[var1] && this.lastChunkStatus[var1] == ChunkStatus.FULL) {
                    ChunkAccess var2 = this.lastChunk[var1];
                    return var2 instanceof LevelChunk ? (LevelChunk)var2 : null;
                }
            }

            ChunkHolder var3 = this.getVisibleChunkIfPresent(var0);
            if (var3 == null) {
                return null;
            } else {
                Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> var4 = var3.getFutureIfPresent(ChunkStatus.FULL).getNow(null);
                if (var4 == null) {
                    return null;
                } else {
                    ChunkAccess var5 = var4.left().orElse(null);
                    if (var5 != null) {
                        this.storeInCache(var0, var5, ChunkStatus.FULL);
                        if (var5 instanceof LevelChunk) {
                            return (LevelChunk)var5;
                        }
                    }

                    return null;
                }
            }
        }
    }

    private void clearCache() {
        Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
        Arrays.fill(this.lastChunkStatus, null);
        Arrays.fill(this.lastChunk, null);
    }

    @OnlyIn(Dist.CLIENT)
    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFuture(int param0, int param1, ChunkStatus param2, boolean param3) {
        boolean var0 = Thread.currentThread() == this.mainThread;
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var1;
        if (var0) {
            var1 = this.getChunkFutureMainThread(param0, param1, param2, param3);
            this.mainThreadProcessor.managedBlock(var1::isDone);
        } else {
            var1 = CompletableFuture.<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>>supplyAsync(
                    () -> this.getChunkFutureMainThread(param0, param1, param2, param3), this.mainThreadProcessor
                )
                .thenCompose(param0x -> param0x);
        }

        return var1;
    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getChunkFutureMainThread(
        int param0, int param1, ChunkStatus param2, boolean param3
    ) {
        ChunkPos var0 = new ChunkPos(param0, param1);
        long var1 = var0.toLong();
        int var2 = 33 + ChunkStatus.getDistance(param2);
        ChunkHolder var3 = this.getVisibleChunkIfPresent(var1);
        if (param3) {
            this.distanceManager.addTicket(TicketType.UNKNOWN, var0, var2, var0);
            if (this.chunkAbsent(var3, var2)) {
                ProfilerFiller var4 = this.level.getProfiler();
                var4.push("chunkLoad");
                this.runDistanceManagerUpdates();
                var3 = this.getVisibleChunkIfPresent(var1);
                var4.pop();
                if (this.chunkAbsent(var3, var2)) {
                    throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("No chunk holder after ticket has been added"));
                }
            }
        }

        return this.chunkAbsent(var3, var2) ? ChunkHolder.UNLOADED_CHUNK_FUTURE : var3.getOrScheduleFuture(param2, this.chunkMap);
    }

    private boolean chunkAbsent(@Nullable ChunkHolder param0, int param1) {
        return param0 == null || param0.getTicketLevel() > param1;
    }

    @Override
    public boolean hasChunk(int param0, int param1) {
        ChunkHolder var0 = this.getVisibleChunkIfPresent(new ChunkPos(param0, param1).toLong());
        int var1 = 33 + ChunkStatus.getDistance(ChunkStatus.FULL);
        return !this.chunkAbsent(var0, var1);
    }

    @Override
    public BlockGetter getChunkForLighting(int param0, int param1) {
        long var0 = ChunkPos.asLong(param0, param1);
        ChunkHolder var1 = this.getVisibleChunkIfPresent(var0);
        if (var1 == null) {
            return null;
        } else {
            int var2 = CHUNK_STATUSES.size() - 1;

            while(true) {
                ChunkStatus var3 = CHUNK_STATUSES.get(var2);
                Optional<ChunkAccess> var4 = var1.getFutureIfPresentUnchecked(var3).getNow(ChunkHolder.UNLOADED_CHUNK).left();
                if (var4.isPresent()) {
                    return var4.get();
                }

                if (var3 == ChunkStatus.LIGHT.getParent()) {
                    return null;
                }

                --var2;
            }
        }
    }

    public Level getLevel() {
        return this.level;
    }

    public boolean pollTask() {
        return this.mainThreadProcessor.pollTask();
    }

    private boolean runDistanceManagerUpdates() {
        boolean var0 = this.distanceManager.runAllUpdates(this.chunkMap);
        boolean var1 = this.chunkMap.promoteChunkMap();
        if (!var0 && !var1) {
            return false;
        } else {
            this.clearCache();
            return true;
        }
    }

    @Override
    public boolean isEntityTickingChunk(Entity param0) {
        long var0 = ChunkPos.asLong(Mth.floor(param0.x) >> 4, Mth.floor(param0.z) >> 4);
        return this.checkChunkFuture(var0, ChunkHolder::getEntityTickingChunkFuture);
    }

    @Override
    public boolean isEntityTickingChunk(ChunkPos param0) {
        return this.checkChunkFuture(param0.toLong(), ChunkHolder::getEntityTickingChunkFuture);
    }

    @Override
    public boolean isTickingChunk(BlockPos param0) {
        long var0 = ChunkPos.asLong(param0.getX() >> 4, param0.getZ() >> 4);
        return this.checkChunkFuture(var0, ChunkHolder::getTickingChunkFuture);
    }

    public boolean isInAccessibleChunk(Entity param0) {
        long var0 = ChunkPos.asLong(Mth.floor(param0.x) >> 4, Mth.floor(param0.z) >> 4);
        return this.checkChunkFuture(var0, ChunkHolder::getFullChunkFuture);
    }

    private boolean checkChunkFuture(long param0, Function<ChunkHolder, CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>>> param1) {
        ChunkHolder var0 = this.getVisibleChunkIfPresent(param0);
        if (var0 == null) {
            return false;
        } else {
            Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> var1 = param1.apply(var0).getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK);
            return var1.left().isPresent();
        }
    }

    public void save(boolean param0) {
        this.runDistanceManagerUpdates();
        this.chunkMap.saveAllChunks(param0);
    }

    @Override
    public void close() throws IOException {
        this.save(true);
        this.lightEngine.close();
        this.chunkMap.close();
    }

    @Override
    public void tick(BooleanSupplier param0) {
        this.level.getProfiler().push("purge");
        this.distanceManager.purgeStaleTickets();
        this.runDistanceManagerUpdates();
        this.level.getProfiler().popPush("chunks");
        this.tickChunks();
        this.level.getProfiler().popPush("unload");
        this.chunkMap.tick(param0);
        this.level.getProfiler().pop();
        this.clearCache();
    }

    private void tickChunks() {
        long var0 = this.level.getGameTime();
        long var1 = var0 - this.lastInhabitedUpdate;
        this.lastInhabitedUpdate = var0;
        LevelData var2 = this.level.getLevelData();
        boolean var3 = var2.getGeneratorType() == LevelType.DEBUG_ALL_BLOCK_STATES;
        boolean var4 = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        if (!var3) {
            this.level.getProfiler().push("pollingChunks");
            int var5 = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
            BlockPos var6 = this.level.getSharedSpawnPos();
            boolean var7 = var2.getGameTime() % 400L == 0L;
            this.level.getProfiler().push("naturalSpawnCount");
            int var8 = this.distanceManager.getNaturalSpawnChunkCount();
            MobCategory[] var9 = MobCategory.values();
            Object2IntMap<MobCategory> var10 = this.level.getMobCategoryCounts();
            this.level.getProfiler().pop();
            this.chunkMap
                .getChunks()
                .forEach(
                    param8 -> {
                        Optional<LevelChunk> var0x = param8.getEntityTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();
                        if (var0x.isPresent()) {
                            LevelChunk var1x = var0x.get();
                            this.level.getProfiler().push("broadcast");
                            param8.broadcastChanges(var1x);
                            this.level.getProfiler().pop();
                            ChunkPos var2x = param8.getPos();
                            if (!this.chunkMap.noPlayersCloseForSpawning(var2x)) {
                                var1x.setInhabitedTime(var1x.getInhabitedTime() + var1);
                                if (var4 && (this.spawnEnemies || this.spawnFriendlies) && this.level.getWorldBorder().isWithinBounds(var1x.getPos())) {
                                    this.level.getProfiler().push("spawner");
        
                                    for(MobCategory var3x : var9) {
                                        if (var3x != MobCategory.MISC
                                            && (!var3x.isFriendly() || this.spawnFriendlies)
                                            && (var3x.isFriendly() || this.spawnEnemies)
                                            && (!var3x.isPersistent() || var7)) {
                                            int var4x = var3x.getMaxInstancesPerChunk() * var8 / MAGIC_NUMBER;
                                            if (var10.getInt(var3x) <= var4x) {
                                                NaturalSpawner.spawnCategoryForChunk(var3x, this.level, var1x, var6);
                                            }
                                        }
                                    }
        
                                    this.level.getProfiler().pop();
                                }
        
                                this.level.tickChunk(var1x, var5);
                            }
                        }
                    }
                );
            this.level.getProfiler().push("customSpawners");
            if (var4) {
                this.generator.tickCustomSpawners(this.level, this.spawnEnemies, this.spawnFriendlies);
            }

            this.level.getProfiler().pop();
            this.level.getProfiler().pop();
        }

        this.chunkMap.tick();
    }

    @Override
    public String gatherStats() {
        return "ServerChunkCache: " + this.getLoadedChunksCount();
    }

    @VisibleForTesting
    public int getPendingTasksCount() {
        return this.mainThreadProcessor.getPendingTasksCount();
    }

    public ChunkGenerator<?> getGenerator() {
        return this.generator;
    }

    public int getLoadedChunksCount() {
        return this.chunkMap.size();
    }

    public void blockChanged(BlockPos param0) {
        int var0 = param0.getX() >> 4;
        int var1 = param0.getZ() >> 4;
        ChunkHolder var2 = this.getVisibleChunkIfPresent(ChunkPos.asLong(var0, var1));
        if (var2 != null) {
            var2.blockChanged(param0.getX() & 15, param0.getY(), param0.getZ() & 15);
        }

    }

    @Override
    public void onLightUpdate(LightLayer param0, SectionPos param1) {
        this.mainThreadProcessor.execute(() -> {
            ChunkHolder var0 = this.getVisibleChunkIfPresent(param1.chunk().toLong());
            if (var0 != null) {
                var0.sectionLightChanged(param0, param1.y());
            }

        });
    }

    public <T> void addRegionTicket(TicketType<T> param0, ChunkPos param1, int param2, T param3) {
        this.distanceManager.addRegionTicket(param0, param1, param2, param3);
    }

    public <T> void removeRegionTicket(TicketType<T> param0, ChunkPos param1, int param2, T param3) {
        this.distanceManager.removeRegionTicket(param0, param1, param2, param3);
    }

    @Override
    public void updateChunkForced(ChunkPos param0, boolean param1) {
        this.distanceManager.updateChunkForced(param0, param1);
    }

    public void move(ServerPlayer param0) {
        this.chunkMap.move(param0);
    }

    public void removeEntity(Entity param0) {
        this.chunkMap.removeEntity(param0);
    }

    public void addEntity(Entity param0) {
        this.chunkMap.addEntity(param0);
    }

    public void broadcastAndSend(Entity param0, Packet<?> param1) {
        this.chunkMap.broadcastAndSend(param0, param1);
    }

    public void broadcast(Entity param0, Packet<?> param1) {
        this.chunkMap.broadcast(param0, param1);
    }

    public void setViewDistance(int param0) {
        this.chunkMap.setViewDistance(param0);
    }

    @Override
    public void setSpawnSettings(boolean param0, boolean param1) {
        this.spawnEnemies = param0;
        this.spawnFriendlies = param1;
    }

    @OnlyIn(Dist.CLIENT)
    public String getChunkDebugData(ChunkPos param0) {
        return this.chunkMap.getChunkDebugData(param0);
    }

    public DimensionDataStorage getDataStorage() {
        return this.dataStorage;
    }

    public PoiManager getPoiManager() {
        return this.chunkMap.getPoiManager();
    }

    final class MainThreadExecutor extends BlockableEventLoop<Runnable> {
        private MainThreadExecutor(Level param0) {
            super("Chunk source main thread executor for " + Registry.DIMENSION_TYPE.getKey(param0.getDimension().getType()));
        }

        @Override
        protected Runnable wrapRunnable(Runnable param0) {
            return param0;
        }

        @Override
        protected boolean shouldRun(Runnable param0) {
            return true;
        }

        @Override
        protected boolean scheduleExecutables() {
            return true;
        }

        @Override
        protected Thread getRunningThread() {
            return ServerChunkCache.this.mainThread;
        }

        @Override
        protected boolean pollTask() {
            if (ServerChunkCache.this.runDistanceManagerUpdates()) {
                return true;
            } else {
                ServerChunkCache.this.lightEngine.tryScheduleUpdate();
                return super.pollTask();
            }
        }
    }
}
