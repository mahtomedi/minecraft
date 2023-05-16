package net.minecraft.server.level;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.util.DebugBuffer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ChunkHolder {
    public static final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> UNLOADED_CHUNK = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
    public static final CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(
        UNLOADED_CHUNK
    );
    public static final Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> UNLOADED_LEVEL_CHUNK = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
    private static final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> NOT_DONE_YET = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
    private static final CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> UNLOADED_LEVEL_CHUNK_FUTURE = CompletableFuture.completedFuture(
        UNLOADED_LEVEL_CHUNK
    );
    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
    private final AtomicReferenceArray<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> futures = new AtomicReferenceArray<>(
        CHUNK_STATUSES.size()
    );
    private final LevelHeightAccessor levelHeightAccessor;
    private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private CompletableFuture<ChunkAccess> chunkToSave = CompletableFuture.completedFuture(null);
    @Nullable
    private final DebugBuffer<ChunkHolder.ChunkSaveDebug> chunkToSaveHistory = null;
    private int oldTicketLevel;
    private int ticketLevel;
    private int queueLevel;
    final ChunkPos pos;
    private boolean hasChangedSections;
    private final ShortSet[] changedBlocksPerSection;
    private final BitSet blockChangedLightSectionFilter = new BitSet();
    private final BitSet skyChangedLightSectionFilter = new BitSet();
    private final LevelLightEngine lightEngine;
    private final ChunkHolder.LevelChangeListener onLevelChange;
    private final ChunkHolder.PlayerProvider playerProvider;
    private boolean wasAccessibleSinceLastSave;
    private CompletableFuture<Void> pendingFullStateConfirmation = CompletableFuture.completedFuture(null);

    public ChunkHolder(
        ChunkPos param0,
        int param1,
        LevelHeightAccessor param2,
        LevelLightEngine param3,
        ChunkHolder.LevelChangeListener param4,
        ChunkHolder.PlayerProvider param5
    ) {
        this.pos = param0;
        this.levelHeightAccessor = param2;
        this.lightEngine = param3;
        this.onLevelChange = param4;
        this.playerProvider = param5;
        this.oldTicketLevel = ChunkLevel.MAX_LEVEL + 1;
        this.ticketLevel = this.oldTicketLevel;
        this.queueLevel = this.oldTicketLevel;
        this.setTicketLevel(param1);
        this.changedBlocksPerSection = new ShortSet[param2.getSectionsCount()];
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getFutureIfPresentUnchecked(ChunkStatus param0) {
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var0 = this.futures.get(param0.getIndex());
        return var0 == null ? UNLOADED_CHUNK_FUTURE : var0;
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getFutureIfPresent(ChunkStatus param0) {
        return ChunkLevel.generationStatus(this.ticketLevel).isOrAfter(param0) ? this.getFutureIfPresentUnchecked(param0) : UNLOADED_CHUNK_FUTURE;
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getTickingChunkFuture() {
        return this.tickingChunkFuture;
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getEntityTickingChunkFuture() {
        return this.entityTickingChunkFuture;
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> getFullChunkFuture() {
        return this.fullChunkFuture;
    }

    @Nullable
    public LevelChunk getTickingChunk() {
        CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> var0 = this.getTickingChunkFuture();
        Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> var1 = var0.getNow(null);
        return var1 == null ? null : var1.left().orElse(null);
    }

    @Nullable
    public LevelChunk getFullChunk() {
        CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> var0 = this.getFullChunkFuture();
        Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> var1 = var0.getNow(null);
        return var1 == null ? null : var1.left().orElse(null);
    }

    @Nullable
    public ChunkStatus getLastAvailableStatus() {
        for(int var0 = CHUNK_STATUSES.size() - 1; var0 >= 0; --var0) {
            ChunkStatus var1 = CHUNK_STATUSES.get(var0);
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var2 = this.getFutureIfPresentUnchecked(var1);
            if (var2.getNow(UNLOADED_CHUNK).left().isPresent()) {
                return var1;
            }
        }

        return null;
    }

    @Nullable
    public ChunkAccess getLastAvailable() {
        for(int var0 = CHUNK_STATUSES.size() - 1; var0 >= 0; --var0) {
            ChunkStatus var1 = CHUNK_STATUSES.get(var0);
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var2 = this.getFutureIfPresentUnchecked(var1);
            if (!var2.isCompletedExceptionally()) {
                Optional<ChunkAccess> var3 = var2.getNow(UNLOADED_CHUNK).left();
                if (var3.isPresent()) {
                    return var3.get();
                }
            }
        }

        return null;
    }

    public CompletableFuture<ChunkAccess> getChunkToSave() {
        return this.chunkToSave;
    }

    public void blockChanged(BlockPos param0) {
        LevelChunk var0 = this.getTickingChunk();
        if (var0 != null) {
            int var1 = this.levelHeightAccessor.getSectionIndex(param0.getY());
            if (this.changedBlocksPerSection[var1] == null) {
                this.hasChangedSections = true;
                this.changedBlocksPerSection[var1] = new ShortOpenHashSet();
            }

            this.changedBlocksPerSection[var1].add(SectionPos.sectionRelativePos(param0));
        }
    }

    public void sectionLightChanged(LightLayer param0, int param1) {
        Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> var0 = this.getFutureIfPresent(ChunkStatus.INITIALIZE_LIGHT).getNow(null);
        if (var0 != null) {
            ChunkAccess var1 = var0.left().orElse(null);
            if (var1 != null) {
                var1.setUnsaved(true);
                LevelChunk var2 = this.getTickingChunk();
                if (var2 != null) {
                    int var3 = this.lightEngine.getMinLightSection();
                    int var4 = this.lightEngine.getMaxLightSection();
                    if (param1 >= var3 && param1 <= var4) {
                        int var5 = param1 - var3;
                        if (param0 == LightLayer.SKY) {
                            this.skyChangedLightSectionFilter.set(var5);
                        } else {
                            this.blockChangedLightSectionFilter.set(var5);
                        }

                    }
                }
            }
        }
    }

    public void broadcastChanges(LevelChunk param0) {
        if (this.hasChangedSections || !this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
            Level var0 = param0.getLevel();
            if (!this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
                List<ServerPlayer> var1 = this.playerProvider.getPlayers(this.pos, true);
                if (!var1.isEmpty()) {
                    ClientboundLightUpdatePacket var2 = new ClientboundLightUpdatePacket(
                        param0.getPos(), this.lightEngine, this.skyChangedLightSectionFilter, this.blockChangedLightSectionFilter
                    );
                    this.broadcast(var1, var2);
                }

                this.skyChangedLightSectionFilter.clear();
                this.blockChangedLightSectionFilter.clear();
            }

            if (this.hasChangedSections) {
                List<ServerPlayer> var3 = this.playerProvider.getPlayers(this.pos, false);

                for(int var4 = 0; var4 < this.changedBlocksPerSection.length; ++var4) {
                    ShortSet var5 = this.changedBlocksPerSection[var4];
                    if (var5 != null) {
                        this.changedBlocksPerSection[var4] = null;
                        if (!var3.isEmpty()) {
                            int var6 = this.levelHeightAccessor.getSectionYFromSectionIndex(var4);
                            SectionPos var7 = SectionPos.of(param0.getPos(), var6);
                            if (var5.size() == 1) {
                                BlockPos var8 = var7.relativeToBlockPos(var5.iterator().nextShort());
                                BlockState var9 = var0.getBlockState(var8);
                                this.broadcast(var3, new ClientboundBlockUpdatePacket(var8, var9));
                                this.broadcastBlockEntityIfNeeded(var3, var0, var8, var9);
                            } else {
                                LevelChunkSection var10 = param0.getSection(var4);
                                ClientboundSectionBlocksUpdatePacket var11 = new ClientboundSectionBlocksUpdatePacket(var7, var5, var10);
                                this.broadcast(var3, var11);
                                var11.runUpdates((param2, param3) -> this.broadcastBlockEntityIfNeeded(var3, var0, param2, param3));
                            }
                        }
                    }
                }

                this.hasChangedSections = false;
            }
        }
    }

    private void broadcastBlockEntityIfNeeded(List<ServerPlayer> param0, Level param1, BlockPos param2, BlockState param3) {
        if (param3.hasBlockEntity()) {
            this.broadcastBlockEntity(param0, param1, param2);
        }

    }

    private void broadcastBlockEntity(List<ServerPlayer> param0, Level param1, BlockPos param2) {
        BlockEntity var0 = param1.getBlockEntity(param2);
        if (var0 != null) {
            Packet<?> var1 = var0.getUpdatePacket();
            if (var1 != null) {
                this.broadcast(param0, var1);
            }
        }

    }

    private void broadcast(List<ServerPlayer> param0, Packet<?> param1) {
        param0.forEach(param1x -> param1x.connection.send(param1));
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getOrScheduleFuture(ChunkStatus param0, ChunkMap param1) {
        int var0 = param0.getIndex();
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var1 = this.futures.get(var0);
        if (var1 != null) {
            Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> var2 = var1.getNow(NOT_DONE_YET);
            if (var2 == null) {
                String var3 = "value in future for status: " + param0 + " was incorrectly set to null at chunk: " + this.pos;
                throw param1.debugFuturesAndCreateReportedException(new IllegalStateException("null value previously set for chunk status"), var3);
            }

            if (var2 == NOT_DONE_YET || var2.right().isEmpty()) {
                return var1;
            }
        }

        if (ChunkLevel.generationStatus(this.ticketLevel).isOrAfter(param0)) {
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var4 = param1.schedule(this, param0);
            this.updateChunkToSave(var4, "schedule " + param0);
            this.futures.set(var0, var4);
            return var4;
        } else {
            return var1 == null ? UNLOADED_CHUNK_FUTURE : var1;
        }
    }

    protected void addSaveDependency(String param0, CompletableFuture<?> param1) {
        if (this.chunkToSaveHistory != null) {
            this.chunkToSaveHistory.push(new ChunkHolder.ChunkSaveDebug(Thread.currentThread(), param1, param0));
        }

        this.chunkToSave = this.chunkToSave.thenCombine(param1, (param0x, param1x) -> param0x);
    }

    private void updateChunkToSave(CompletableFuture<? extends Either<? extends ChunkAccess, ChunkHolder.ChunkLoadingFailure>> param0, String param1) {
        if (this.chunkToSaveHistory != null) {
            this.chunkToSaveHistory.push(new ChunkHolder.ChunkSaveDebug(Thread.currentThread(), param0, param1));
        }

        this.chunkToSave = this.chunkToSave.thenCombine(param0, (param0x, param1x) -> param1x.map(param0xx -> param0xx, param1xx -> param0x));
    }

    public FullChunkStatus getFullStatus() {
        return ChunkLevel.fullStatus(this.ticketLevel);
    }

    public ChunkPos getPos() {
        return this.pos;
    }

    public int getTicketLevel() {
        return this.ticketLevel;
    }

    public int getQueueLevel() {
        return this.queueLevel;
    }

    private void setQueueLevel(int param0) {
        this.queueLevel = param0;
    }

    public void setTicketLevel(int param0) {
        this.ticketLevel = param0;
    }

    private void scheduleFullChunkPromotion(
        ChunkMap param0, CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> param1, Executor param2, FullChunkStatus param3
    ) {
        this.pendingFullStateConfirmation.cancel(false);
        CompletableFuture<Void> var0 = new CompletableFuture<>();
        var0.thenRunAsync(() -> param0.onFullChunkStatusChange(this.pos, param3), param2);
        this.pendingFullStateConfirmation = var0;
        param1.thenAccept(param1x -> param1x.ifLeft(param1xx -> var0.complete(null)));
    }

    private void demoteFullChunk(ChunkMap param0, FullChunkStatus param1) {
        this.pendingFullStateConfirmation.cancel(false);
        param0.onFullChunkStatusChange(this.pos, param1);
    }

    protected void updateFutures(ChunkMap param0, Executor param1) {
        ChunkStatus var0 = ChunkLevel.generationStatus(this.oldTicketLevel);
        ChunkStatus var1 = ChunkLevel.generationStatus(this.ticketLevel);
        boolean var2 = ChunkLevel.isLoaded(this.oldTicketLevel);
        boolean var3 = ChunkLevel.isLoaded(this.ticketLevel);
        FullChunkStatus var4 = ChunkLevel.fullStatus(this.oldTicketLevel);
        FullChunkStatus var5 = ChunkLevel.fullStatus(this.ticketLevel);
        if (var2) {
            Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> var6 = Either.right(new ChunkHolder.ChunkLoadingFailure() {
                @Override
                public String toString() {
                    return "Unloaded ticket level " + ChunkHolder.this.pos;
                }
            });

            for(int var7 = var3 ? var1.getIndex() + 1 : 0; var7 <= var0.getIndex(); ++var7) {
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var8 = this.futures.get(var7);
                if (var8 == null) {
                    this.futures.set(var7, CompletableFuture.completedFuture(var6));
                }
            }
        }

        boolean var9 = var4.isOrAfter(FullChunkStatus.FULL);
        boolean var10 = var5.isOrAfter(FullChunkStatus.FULL);
        this.wasAccessibleSinceLastSave |= var10;
        if (!var9 && var10) {
            this.fullChunkFuture = param0.prepareAccessibleChunk(this);
            this.scheduleFullChunkPromotion(param0, this.fullChunkFuture, param1, FullChunkStatus.FULL);
            this.updateChunkToSave(this.fullChunkFuture, "full");
        }

        if (var9 && !var10) {
            this.fullChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }

        boolean var11 = var4.isOrAfter(FullChunkStatus.BLOCK_TICKING);
        boolean var12 = var5.isOrAfter(FullChunkStatus.BLOCK_TICKING);
        if (!var11 && var12) {
            this.tickingChunkFuture = param0.prepareTickingChunk(this);
            this.scheduleFullChunkPromotion(param0, this.tickingChunkFuture, param1, FullChunkStatus.BLOCK_TICKING);
            this.updateChunkToSave(this.tickingChunkFuture, "ticking");
        }

        if (var11 && !var12) {
            this.tickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }

        boolean var13 = var4.isOrAfter(FullChunkStatus.ENTITY_TICKING);
        boolean var14 = var5.isOrAfter(FullChunkStatus.ENTITY_TICKING);
        if (!var13 && var14) {
            if (this.entityTickingChunkFuture != UNLOADED_LEVEL_CHUNK_FUTURE) {
                throw (IllegalStateException)Util.pauseInIde(new IllegalStateException());
            }

            this.entityTickingChunkFuture = param0.prepareEntityTickingChunk(this);
            this.scheduleFullChunkPromotion(param0, this.entityTickingChunkFuture, param1, FullChunkStatus.ENTITY_TICKING);
            this.updateChunkToSave(this.entityTickingChunkFuture, "entity ticking");
        }

        if (var13 && !var14) {
            this.entityTickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }

        if (!var5.isOrAfter(var4)) {
            this.demoteFullChunk(param0, var5);
        }

        this.onLevelChange.onLevelChange(this.pos, this::getQueueLevel, this.ticketLevel, this::setQueueLevel);
        this.oldTicketLevel = this.ticketLevel;
    }

    public boolean wasAccessibleSinceLastSave() {
        return this.wasAccessibleSinceLastSave;
    }

    public void refreshAccessibility() {
        this.wasAccessibleSinceLastSave = ChunkLevel.fullStatus(this.ticketLevel).isOrAfter(FullChunkStatus.FULL);
    }

    public void replaceProtoChunk(ImposterProtoChunk param0) {
        for(int var0 = 0; var0 < this.futures.length(); ++var0) {
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var1 = this.futures.get(var0);
            if (var1 != null) {
                Optional<ChunkAccess> var2 = var1.getNow(UNLOADED_CHUNK).left();
                if (!var2.isEmpty() && var2.get() instanceof ProtoChunk) {
                    this.futures.set(var0, CompletableFuture.completedFuture(Either.left(param0)));
                }
            }
        }

        this.updateChunkToSave(CompletableFuture.completedFuture(Either.left(param0.getWrapped())), "replaceProto");
    }

    public List<Pair<ChunkStatus, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>>> getAllFutures() {
        List<Pair<ChunkStatus, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>>> var0 = new ArrayList<>();

        for(int var1 = 0; var1 < CHUNK_STATUSES.size(); ++var1) {
            var0.add(Pair.of(CHUNK_STATUSES.get(var1), this.futures.get(var1)));
        }

        return var0;
    }

    public interface ChunkLoadingFailure {
        ChunkHolder.ChunkLoadingFailure UNLOADED = new ChunkHolder.ChunkLoadingFailure() {
            @Override
            public String toString() {
                return "UNLOADED";
            }
        };
    }

    static final class ChunkSaveDebug {
        private final Thread thread;
        private final CompletableFuture<?> future;
        private final String source;

        ChunkSaveDebug(Thread param0, CompletableFuture<?> param1, String param2) {
            this.thread = param0;
            this.future = param1;
            this.source = param2;
        }
    }

    @FunctionalInterface
    public interface LevelChangeListener {
        void onLevelChange(ChunkPos var1, IntSupplier var2, int var3, IntConsumer var4);
    }

    public interface PlayerProvider {
        List<ServerPlayer> getPlayers(ChunkPos var1, boolean var2);
    }
}
