package net.minecraft.server.level;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.util.Mth;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChunkHolder {
    public static final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> UNLOADED_CHUNK = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
    public static final CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(
        UNLOADED_CHUNK
    );
    public static final Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> UNLOADED_LEVEL_CHUNK = Either.right(ChunkHolder.ChunkLoadingFailure.UNLOADED);
    private static final CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> UNLOADED_LEVEL_CHUNK_FUTURE = CompletableFuture.completedFuture(
        UNLOADED_LEVEL_CHUNK
    );
    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.getStatusList();
    private static final ChunkHolder.FullChunkStatus[] FULL_CHUNK_STATUSES = ChunkHolder.FullChunkStatus.values();
    private final AtomicReferenceArray<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> futures = new AtomicReferenceArray<>(
        CHUNK_STATUSES.size()
    );
    private final LevelHeightAccessor levelHeightAccessor;
    private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private volatile CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private CompletableFuture<ChunkAccess> chunkToSave = CompletableFuture.completedFuture(null);
    private int oldTicketLevel;
    private int ticketLevel;
    private int queueLevel;
    private final ChunkPos pos;
    private boolean hasChangedSections;
    private final ShortSet[] changedBlocksPerSection;
    private final BitSet blockChangedLightSectionFilter = new BitSet();
    private final BitSet skyChangedLightSectionFilter = new BitSet();
    private final LevelLightEngine lightEngine;
    private final ChunkHolder.LevelChangeListener onLevelChange;
    private final ChunkHolder.PlayerProvider playerProvider;
    private boolean wasAccessibleSinceLastSave;
    private boolean resendLight;
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
        this.oldTicketLevel = ChunkMap.MAX_CHUNK_DISTANCE + 1;
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
        return getStatus(this.ticketLevel).isOrAfter(param0) ? this.getFutureIfPresentUnchecked(param0) : UNLOADED_CHUNK_FUTURE;
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
    @OnlyIn(Dist.CLIENT)
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
            byte var1 = (byte)this.levelHeightAccessor.getSectionIndex(param0.getY());
            if (this.changedBlocksPerSection[var1] == null) {
                this.hasChangedSections = true;
                this.changedBlocksPerSection[var1] = new ShortArraySet();
            }

            this.changedBlocksPerSection[var1].add(SectionPos.sectionRelativePos(param0));
        }
    }

    public void sectionLightChanged(LightLayer param0, int param1) {
        LevelChunk var0 = this.getTickingChunk();
        if (var0 != null) {
            var0.setUnsaved(true);
            int var1 = this.lightEngine.getMinLightSection();
            int var2 = this.lightEngine.getMaxLightSection();
            if (param1 >= var1 && param1 <= var2) {
                int var3 = param1 - var1;
                if (param0 == LightLayer.SKY) {
                    this.skyChangedLightSectionFilter.set(var3);
                } else {
                    this.blockChangedLightSectionFilter.set(var3);
                }

            }
        }
    }

    public void broadcastChanges(LevelChunk param0) {
        if (this.hasChangedSections || !this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
            Level var0 = param0.getLevel();
            int var1 = 0;

            for(int var2 = 0; var2 < this.changedBlocksPerSection.length; ++var2) {
                var1 += this.changedBlocksPerSection[var2] != null ? this.changedBlocksPerSection[var2].size() : 0;
            }

            this.resendLight |= var1 >= 64;
            if (!this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
                this.broadcast(
                    new ClientboundLightUpdatePacket(
                        param0.getPos(), this.lightEngine, this.skyChangedLightSectionFilter, this.blockChangedLightSectionFilter, true
                    ),
                    !this.resendLight
                );
                this.skyChangedLightSectionFilter.clear();
                this.blockChangedLightSectionFilter.clear();
            }

            for(int var3 = 0; var3 < this.changedBlocksPerSection.length; ++var3) {
                ShortSet var4 = this.changedBlocksPerSection[var3];
                if (var4 != null) {
                    int var5 = this.levelHeightAccessor.getSectionYFromSectionIndex(var3);
                    SectionPos var6 = SectionPos.of(param0.getPos(), var5);
                    if (var4.size() == 1) {
                        BlockPos var7 = var6.relativeToBlockPos(var4.iterator().nextShort());
                        BlockState var8 = var0.getBlockState(var7);
                        this.broadcast(new ClientboundBlockUpdatePacket(var7, var8), false);
                        this.broadcastBlockEntityIfNeeded(var0, var7, var8);
                    } else {
                        LevelChunkSection var9 = param0.getSections()[var3];
                        ClientboundSectionBlocksUpdatePacket var10 = new ClientboundSectionBlocksUpdatePacket(var6, var4, var9, this.resendLight);
                        this.broadcast(var10, false);
                        var10.runUpdates((param1, param2) -> this.broadcastBlockEntityIfNeeded(var0, param1, param2));
                    }

                    this.changedBlocksPerSection[var3] = null;
                }
            }

            this.hasChangedSections = false;
        }
    }

    private void broadcastBlockEntityIfNeeded(Level param0, BlockPos param1, BlockState param2) {
        if (param2.hasBlockEntity()) {
            this.broadcastBlockEntity(param0, param1);
        }

    }

    private void broadcastBlockEntity(Level param0, BlockPos param1) {
        BlockEntity var0 = param0.getBlockEntity(param1);
        if (var0 != null) {
            ClientboundBlockEntityDataPacket var1 = var0.getUpdatePacket();
            if (var1 != null) {
                this.broadcast(var1, false);
            }
        }

    }

    private void broadcast(Packet<?> param0, boolean param1) {
        this.playerProvider.getPlayers(this.pos, param1).forEach(param1x -> param1x.connection.send(param0));
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> getOrScheduleFuture(ChunkStatus param0, ChunkMap param1) {
        int var0 = param0.getIndex();
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var1 = this.futures.get(var0);
        if (var1 != null) {
            Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> var2 = var1.getNow(null);
            if (var2 == null || var2.left().isPresent()) {
                return var1;
            }
        }

        if (getStatus(this.ticketLevel).isOrAfter(param0)) {
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var3 = param1.schedule(this, param0);
            this.updateChunkToSave(var3);
            this.futures.set(var0, var3);
            return var3;
        } else {
            return var1 == null ? UNLOADED_CHUNK_FUTURE : var1;
        }
    }

    private void updateChunkToSave(CompletableFuture<? extends Either<? extends ChunkAccess, ChunkHolder.ChunkLoadingFailure>> param0) {
        this.chunkToSave = this.chunkToSave.thenCombine(param0, (param0x, param1) -> param1.map(param0xx -> param0xx, param1x -> param0x));
    }

    @OnlyIn(Dist.CLIENT)
    public ChunkHolder.FullChunkStatus getFullStatus() {
        return getFullChunkStatus(this.ticketLevel);
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
        ChunkMap param0, CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> param1, Executor param2, ChunkHolder.FullChunkStatus param3
    ) {
        this.pendingFullStateConfirmation.cancel(false);
        CompletableFuture<Void> var0 = new CompletableFuture<>();
        var0.thenRunAsync(() -> param0.onFullChunkStatusChange(this.pos, param3), param2);
        this.pendingFullStateConfirmation = var0;
        param1.thenAccept(param1x -> param1x.ifLeft(param1xx -> var0.complete(null)));
    }

    private void demoteFullChunk(ChunkMap param0, ChunkHolder.FullChunkStatus param1) {
        this.pendingFullStateConfirmation.cancel(false);
        param0.onFullChunkStatusChange(this.pos, param1);
    }

    protected void updateFutures(ChunkMap param0, Executor param1) {
        ChunkStatus var0 = getStatus(this.oldTicketLevel);
        ChunkStatus var1 = getStatus(this.ticketLevel);
        boolean var2 = this.oldTicketLevel <= ChunkMap.MAX_CHUNK_DISTANCE;
        boolean var3 = this.ticketLevel <= ChunkMap.MAX_CHUNK_DISTANCE;
        ChunkHolder.FullChunkStatus var4 = getFullChunkStatus(this.oldTicketLevel);
        ChunkHolder.FullChunkStatus var5 = getFullChunkStatus(this.ticketLevel);
        if (var2) {
            Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> var6 = Either.right(new ChunkHolder.ChunkLoadingFailure() {
                @Override
                public String toString() {
                    return "Unloaded ticket level " + ChunkHolder.this.pos;
                }
            });

            for(int var7 = var3 ? var1.getIndex() + 1 : 0; var7 <= var0.getIndex(); ++var7) {
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var8 = this.futures.get(var7);
                if (var8 != null) {
                    var8.complete(var6);
                } else {
                    this.futures.set(var7, CompletableFuture.completedFuture(var6));
                }
            }
        }

        boolean var9 = var4.isOrAfter(ChunkHolder.FullChunkStatus.BORDER);
        boolean var10 = var5.isOrAfter(ChunkHolder.FullChunkStatus.BORDER);
        this.wasAccessibleSinceLastSave |= var10;
        if (!var9 && var10) {
            this.fullChunkFuture = param0.prepareAccessibleChunk(this);
            this.scheduleFullChunkPromotion(param0, this.fullChunkFuture, param1, ChunkHolder.FullChunkStatus.BORDER);
            this.updateChunkToSave(this.fullChunkFuture);
        }

        if (var9 && !var10) {
            CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> var11 = this.fullChunkFuture;
            this.fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
            this.updateChunkToSave(var11.thenApply(param1x -> param1x.ifLeft(param0::packTicks)));
        }

        boolean var12 = var4.isOrAfter(ChunkHolder.FullChunkStatus.TICKING);
        boolean var13 = var5.isOrAfter(ChunkHolder.FullChunkStatus.TICKING);
        if (!var12 && var13) {
            this.tickingChunkFuture = param0.prepareTickingChunk(this);
            this.scheduleFullChunkPromotion(param0, this.tickingChunkFuture, param1, ChunkHolder.FullChunkStatus.TICKING);
            this.updateChunkToSave(this.tickingChunkFuture);
        }

        if (var12 && !var13) {
            this.tickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }

        boolean var14 = var4.isOrAfter(ChunkHolder.FullChunkStatus.ENTITY_TICKING);
        boolean var15 = var5.isOrAfter(ChunkHolder.FullChunkStatus.ENTITY_TICKING);
        if (!var14 && var15) {
            if (this.entityTickingChunkFuture != UNLOADED_LEVEL_CHUNK_FUTURE) {
                throw (IllegalStateException)Util.pauseInIde(new IllegalStateException());
            }

            this.entityTickingChunkFuture = param0.prepareEntityTickingChunk(this.pos);
            this.scheduleFullChunkPromotion(param0, this.entityTickingChunkFuture, param1, ChunkHolder.FullChunkStatus.ENTITY_TICKING);
            this.updateChunkToSave(this.entityTickingChunkFuture);
        }

        if (var14 && !var15) {
            this.entityTickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }

        if (!var5.isOrAfter(var4)) {
            this.demoteFullChunk(param0, var5);
        }

        this.onLevelChange.onLevelChange(this.pos, this::getQueueLevel, this.ticketLevel, this::setQueueLevel);
        this.oldTicketLevel = this.ticketLevel;
    }

    public static ChunkStatus getStatus(int param0) {
        return param0 < 33 ? ChunkStatus.FULL : ChunkStatus.getStatusAroundFullChunk(param0 - 33);
    }

    public static ChunkHolder.FullChunkStatus getFullChunkStatus(int param0) {
        return FULL_CHUNK_STATUSES[Mth.clamp(33 - param0 + 1, 0, FULL_CHUNK_STATUSES.length - 1)];
    }

    public boolean wasAccessibleSinceLastSave() {
        return this.wasAccessibleSinceLastSave;
    }

    public void refreshAccessibility() {
        this.wasAccessibleSinceLastSave = getFullChunkStatus(this.ticketLevel).isOrAfter(ChunkHolder.FullChunkStatus.BORDER);
    }

    public void replaceProtoChunk(ImposterProtoChunk param0) {
        for(int var0 = 0; var0 < this.futures.length(); ++var0) {
            CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> var1 = this.futures.get(var0);
            if (var1 != null) {
                Optional<ChunkAccess> var2 = var1.getNow(UNLOADED_CHUNK).left();
                if (var2.isPresent() && var2.get() instanceof ProtoChunk) {
                    this.futures.set(var0, CompletableFuture.completedFuture(Either.left(param0)));
                }
            }
        }

        this.updateChunkToSave(CompletableFuture.completedFuture(Either.left(param0.getWrapped())));
    }

    public interface ChunkLoadingFailure {
        ChunkHolder.ChunkLoadingFailure UNLOADED = new ChunkHolder.ChunkLoadingFailure() {
            @Override
            public String toString() {
                return "UNLOADED";
            }
        };
    }

    public static enum FullChunkStatus {
        INACCESSIBLE,
        BORDER,
        TICKING,
        ENTITY_TICKING;

        public boolean isOrAfter(ChunkHolder.FullChunkStatus param0) {
            return this.ordinal() >= param0.ordinal();
        }
    }

    @FunctionalInterface
    public interface LevelChangeListener {
        void onLevelChange(ChunkPos var1, IntSupplier var2, int var3, IntConsumer var4);
    }

    public interface PlayerProvider {
        Stream<ServerPlayer> getPlayers(ChunkPos var1, boolean var2);
    }
}
