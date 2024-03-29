package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.EuclideanGameEventListenerRegistry;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;
import org.slf4j.Logger;

public class LevelChunk extends ChunkAccess {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final TickingBlockEntity NULL_TICKER = new TickingBlockEntity() {
        @Override
        public void tick() {
        }

        @Override
        public boolean isRemoved() {
            return true;
        }

        @Override
        public BlockPos getPos() {
            return BlockPos.ZERO;
        }

        @Override
        public String getType() {
            return "<null>";
        }
    };
    private final Map<BlockPos, LevelChunk.RebindableTickingBlockEntityWrapper> tickersInLevel = Maps.newHashMap();
    private boolean loaded;
    final Level level;
    @Nullable
    private Supplier<FullChunkStatus> fullStatus;
    @Nullable
    private LevelChunk.PostLoadProcessor postLoad;
    private final Int2ObjectMap<GameEventListenerRegistry> gameEventListenerRegistrySections;
    private final LevelChunkTicks<Block> blockTicks;
    private final LevelChunkTicks<Fluid> fluidTicks;

    public LevelChunk(Level param0, ChunkPos param1) {
        this(param0, param1, UpgradeData.EMPTY, new LevelChunkTicks<>(), new LevelChunkTicks<>(), 0L, null, null, null);
    }

    public LevelChunk(
        Level param0,
        ChunkPos param1,
        UpgradeData param2,
        LevelChunkTicks<Block> param3,
        LevelChunkTicks<Fluid> param4,
        long param5,
        @Nullable LevelChunkSection[] param6,
        @Nullable LevelChunk.PostLoadProcessor param7,
        @Nullable BlendingData param8
    ) {
        super(param1, param2, param0, param0.registryAccess().registryOrThrow(Registries.BIOME), param5, param6, param8);
        this.level = param0;
        this.gameEventListenerRegistrySections = new Int2ObjectOpenHashMap<>();

        for(Heightmap.Types var0 : Heightmap.Types.values()) {
            if (ChunkStatus.FULL.heightmapsAfter().contains(var0)) {
                this.heightmaps.put(var0, new Heightmap(this, var0));
            }
        }

        this.postLoad = param7;
        this.blockTicks = param3;
        this.fluidTicks = param4;
    }

    public LevelChunk(ServerLevel param0, ProtoChunk param1, @Nullable LevelChunk.PostLoadProcessor param2) {
        this(
            param0,
            param1.getPos(),
            param1.getUpgradeData(),
            param1.unpackBlockTicks(),
            param1.unpackFluidTicks(),
            param1.getInhabitedTime(),
            param1.getSections(),
            param2,
            param1.getBlendingData()
        );

        for(BlockEntity var0 : param1.getBlockEntities().values()) {
            this.setBlockEntity(var0);
        }

        this.pendingBlockEntities.putAll(param1.getBlockEntityNbts());

        for(int var1 = 0; var1 < param1.getPostProcessing().length; ++var1) {
            this.postProcessing[var1] = param1.getPostProcessing()[var1];
        }

        this.setAllStarts(param1.getAllStarts());
        this.setAllReferences(param1.getAllReferences());

        for(Entry<Heightmap.Types, Heightmap> var2 : param1.getHeightmaps()) {
            if (ChunkStatus.FULL.heightmapsAfter().contains(var2.getKey())) {
                this.setHeightmap(var2.getKey(), var2.getValue().getRawData());
            }
        }

        this.skyLightSources = param1.skyLightSources;
        this.setLightCorrect(param1.isLightCorrect());
        this.unsaved = true;
    }

    @Override
    public TickContainerAccess<Block> getBlockTicks() {
        return this.blockTicks;
    }

    @Override
    public TickContainerAccess<Fluid> getFluidTicks() {
        return this.fluidTicks;
    }

    @Override
    public ChunkAccess.TicksToSave getTicksForSerialization() {
        return new ChunkAccess.TicksToSave(this.blockTicks, this.fluidTicks);
    }

    @Override
    public GameEventListenerRegistry getListenerRegistry(int param0) {
        Level var3 = this.level;
        return var3 instanceof ServerLevel var0
            ? this.gameEventListenerRegistrySections
                .computeIfAbsent(param0, param2 -> new EuclideanGameEventListenerRegistry(var0, param0, this::removeGameEventListenerRegistry))
            : super.getListenerRegistry(param0);
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        int var0 = param0.getX();
        int var1 = param0.getY();
        int var2 = param0.getZ();
        if (this.level.isDebug()) {
            BlockState var3 = null;
            if (var1 == 60) {
                var3 = Blocks.BARRIER.defaultBlockState();
            }

            if (var1 == 70) {
                var3 = DebugLevelSource.getBlockStateFor(var0, var2);
            }

            return var3 == null ? Blocks.AIR.defaultBlockState() : var3;
        } else {
            try {
                int var4 = this.getSectionIndex(var1);
                if (var4 >= 0 && var4 < this.sections.length) {
                    LevelChunkSection var5 = this.sections[var4];
                    if (!var5.hasOnlyAir()) {
                        return var5.getBlockState(var0 & 15, var1 & 15, var2 & 15);
                    }
                }

                return Blocks.AIR.defaultBlockState();
            } catch (Throwable var81) {
                CrashReport var7 = CrashReport.forThrowable(var81, "Getting block state");
                CrashReportCategory var8 = var7.addCategory("Block being got");
                var8.setDetail("Location", () -> CrashReportCategory.formatLocation(this, var0, var1, var2));
                throw new ReportedException(var7);
            }
        }
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        return this.getFluidState(param0.getX(), param0.getY(), param0.getZ());
    }

    public FluidState getFluidState(int param0, int param1, int param2) {
        try {
            int var0 = this.getSectionIndex(param1);
            if (var0 >= 0 && var0 < this.sections.length) {
                LevelChunkSection var1 = this.sections[var0];
                if (!var1.hasOnlyAir()) {
                    return var1.getFluidState(param0 & 15, param1 & 15, param2 & 15);
                }
            }

            return Fluids.EMPTY.defaultFluidState();
        } catch (Throwable var7) {
            CrashReport var3 = CrashReport.forThrowable(var7, "Getting fluid state");
            CrashReportCategory var4 = var3.addCategory("Block being got");
            var4.setDetail("Location", () -> CrashReportCategory.formatLocation(this, param0, param1, param2));
            throw new ReportedException(var3);
        }
    }

    @Nullable
    @Override
    public BlockState setBlockState(BlockPos param0, BlockState param1, boolean param2) {
        int var0 = param0.getY();
        LevelChunkSection var1 = this.getSection(this.getSectionIndex(var0));
        boolean var2 = var1.hasOnlyAir();
        if (var2 && param1.isAir()) {
            return null;
        } else {
            int var3 = param0.getX() & 15;
            int var4 = var0 & 15;
            int var5 = param0.getZ() & 15;
            BlockState var6 = var1.setBlockState(var3, var4, var5, param1);
            if (var6 == param1) {
                return null;
            } else {
                Block var7 = param1.getBlock();
                this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(var3, var0, var5, param1);
                this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(var3, var0, var5, param1);
                this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(var3, var0, var5, param1);
                this.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(var3, var0, var5, param1);
                boolean var8 = var1.hasOnlyAir();
                if (var2 != var8) {
                    this.level.getChunkSource().getLightEngine().updateSectionStatus(param0, var8);
                }

                if (LightEngine.hasDifferentLightProperties(this, param0, var6, param1)) {
                    ProfilerFiller var9 = this.level.getProfiler();
                    var9.push("updateSkyLightSources");
                    this.skyLightSources.update(this, var3, var0, var5);
                    var9.popPush("queueCheckLight");
                    this.level.getChunkSource().getLightEngine().checkBlock(param0);
                    var9.pop();
                }

                boolean var10 = var6.hasBlockEntity();
                if (!this.level.isClientSide) {
                    var6.onRemove(this.level, param0, param1, param2);
                } else if (!var6.is(var7) && var10) {
                    this.removeBlockEntity(param0);
                }

                if (!var1.getBlockState(var3, var4, var5).is(var7)) {
                    return null;
                } else {
                    if (!this.level.isClientSide) {
                        param1.onPlace(this.level, param0, var6, param2);
                    }

                    if (param1.hasBlockEntity()) {
                        BlockEntity var11 = this.getBlockEntity(param0, LevelChunk.EntityCreationType.CHECK);
                        if (var11 == null) {
                            var11 = ((EntityBlock)var7).newBlockEntity(param0, param1);
                            if (var11 != null) {
                                this.addAndRegisterBlockEntity(var11);
                            }
                        } else {
                            var11.setBlockState(param1);
                            this.updateBlockEntityTicker(var11);
                        }
                    }

                    this.unsaved = true;
                    return var6;
                }
            }
        }
    }

    @Deprecated
    @Override
    public void addEntity(Entity param0) {
    }

    @Nullable
    private BlockEntity createBlockEntity(BlockPos param0) {
        BlockState var0 = this.getBlockState(param0);
        return !var0.hasBlockEntity() ? null : ((EntityBlock)var0.getBlock()).newBlockEntity(param0, var0);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        return this.getBlockEntity(param0, LevelChunk.EntityCreationType.CHECK);
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos param0, LevelChunk.EntityCreationType param1) {
        BlockEntity var0 = this.blockEntities.get(param0);
        if (var0 == null) {
            CompoundTag var1 = this.pendingBlockEntities.remove(param0);
            if (var1 != null) {
                BlockEntity var2 = this.promotePendingBlockEntity(param0, var1);
                if (var2 != null) {
                    return var2;
                }
            }
        }

        if (var0 == null) {
            if (param1 == LevelChunk.EntityCreationType.IMMEDIATE) {
                var0 = this.createBlockEntity(param0);
                if (var0 != null) {
                    this.addAndRegisterBlockEntity(var0);
                }
            }
        } else if (var0.isRemoved()) {
            this.blockEntities.remove(param0);
            return null;
        }

        return var0;
    }

    public void addAndRegisterBlockEntity(BlockEntity param0) {
        this.setBlockEntity(param0);
        if (this.isInLevel()) {
            Level var3 = this.level;
            if (var3 instanceof ServerLevel var0) {
                this.addGameEventListener(param0, var0);
            }

            this.updateBlockEntityTicker(param0);
        }

    }

    private boolean isInLevel() {
        return this.loaded || this.level.isClientSide();
    }

    boolean isTicking(BlockPos param0) {
        if (!this.level.getWorldBorder().isWithinBounds(param0)) {
            return false;
        } else {
            Level var3 = this.level;
            if (!(var3 instanceof ServerLevel)) {
                return true;
            } else {
                ServerLevel var0 = (ServerLevel)var3;
                return this.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING) && var0.areEntitiesLoaded(ChunkPos.asLong(param0));
            }
        }
    }

    @Override
    public void setBlockEntity(BlockEntity param0) {
        BlockPos var0 = param0.getBlockPos();
        if (this.getBlockState(var0).hasBlockEntity()) {
            param0.setLevel(this.level);
            param0.clearRemoved();
            BlockEntity var1 = this.blockEntities.put(var0.immutable(), param0);
            if (var1 != null && var1 != param0) {
                var1.setRemoved();
            }

        }
    }

    @Nullable
    @Override
    public CompoundTag getBlockEntityNbtForSaving(BlockPos param0) {
        BlockEntity var0 = this.getBlockEntity(param0);
        if (var0 != null && !var0.isRemoved()) {
            CompoundTag var1 = var0.saveWithFullMetadata();
            var1.putBoolean("keepPacked", false);
            return var1;
        } else {
            CompoundTag var2 = this.pendingBlockEntities.get(param0);
            if (var2 != null) {
                var2 = var2.copy();
                var2.putBoolean("keepPacked", true);
            }

            return var2;
        }
    }

    @Override
    public void removeBlockEntity(BlockPos param0) {
        if (this.isInLevel()) {
            BlockEntity var0 = this.blockEntities.remove(param0);
            if (var0 != null) {
                Level var4 = this.level;
                if (var4 instanceof ServerLevel var1) {
                    this.removeGameEventListener(var0, var1);
                }

                var0.setRemoved();
            }
        }

        this.removeBlockEntityTicker(param0);
    }

    private <T extends BlockEntity> void removeGameEventListener(T param0, ServerLevel param1) {
        Block var0 = param0.getBlockState().getBlock();
        if (var0 instanceof EntityBlock) {
            GameEventListener var1 = ((EntityBlock)var0).getListener(param1, param0);
            if (var1 != null) {
                int var2 = SectionPos.blockToSectionCoord(param0.getBlockPos().getY());
                GameEventListenerRegistry var3 = this.getListenerRegistry(var2);
                var3.unregister(var1);
            }
        }

    }

    private void removeGameEventListenerRegistry(int param0) {
        this.gameEventListenerRegistrySections.remove(param0);
    }

    private void removeBlockEntityTicker(BlockPos param0) {
        LevelChunk.RebindableTickingBlockEntityWrapper var0 = this.tickersInLevel.remove(param0);
        if (var0 != null) {
            var0.rebind(NULL_TICKER);
        }

    }

    public void runPostLoad() {
        if (this.postLoad != null) {
            this.postLoad.run(this);
            this.postLoad = null;
        }

    }

    public boolean isEmpty() {
        return false;
    }

    public void replaceWithPacketData(FriendlyByteBuf param0, CompoundTag param1, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> param2) {
        this.clearAllBlockEntities();

        for(LevelChunkSection var0 : this.sections) {
            var0.read(param0);
        }

        for(Heightmap.Types var1 : Heightmap.Types.values()) {
            String var2 = var1.getSerializationKey();
            if (param1.contains(var2, 12)) {
                this.setHeightmap(var1, param1.getLongArray(var2));
            }
        }

        this.initializeLightSources();
        param2.accept((param0x, param1x, param2x) -> {
            BlockEntity var0x = this.getBlockEntity(param0x, LevelChunk.EntityCreationType.IMMEDIATE);
            if (var0x != null && param2x != null && var0x.getType() == param1x) {
                var0x.load(param2x);
            }

        });
    }

    public void replaceBiomes(FriendlyByteBuf param0) {
        for(LevelChunkSection var0 : this.sections) {
            var0.readBiomes(param0);
        }

    }

    public void setLoaded(boolean param0) {
        this.loaded = param0;
    }

    public Level getLevel() {
        return this.level;
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    public void postProcessGeneration() {
        ChunkPos var0 = this.getPos();

        for(int var1 = 0; var1 < this.postProcessing.length; ++var1) {
            if (this.postProcessing[var1] != null) {
                for(Short var2 : this.postProcessing[var1]) {
                    BlockPos var3 = ProtoChunk.unpackOffsetCoordinates(var2, this.getSectionYFromSectionIndex(var1), var0);
                    BlockState var4 = this.getBlockState(var3);
                    FluidState var5 = var4.getFluidState();
                    if (!var5.isEmpty()) {
                        var5.tick(this.level, var3);
                    }

                    if (!(var4.getBlock() instanceof LiquidBlock)) {
                        BlockState var6 = Block.updateFromNeighbourShapes(var4, this.level, var3);
                        this.level.setBlock(var3, var6, 20);
                    }
                }

                this.postProcessing[var1].clear();
            }
        }

        for(BlockPos var7 : ImmutableList.copyOf(this.pendingBlockEntities.keySet())) {
            this.getBlockEntity(var7);
        }

        this.pendingBlockEntities.clear();
        this.upgradeData.upgrade(this);
    }

    @Nullable
    private BlockEntity promotePendingBlockEntity(BlockPos param0, CompoundTag param1) {
        BlockState var0 = this.getBlockState(param0);
        BlockEntity var1;
        if ("DUMMY".equals(param1.getString("id"))) {
            if (var0.hasBlockEntity()) {
                var1 = ((EntityBlock)var0.getBlock()).newBlockEntity(param0, var0);
            } else {
                var1 = null;
                LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", param0, var0);
            }
        } else {
            var1 = BlockEntity.loadStatic(param0, var0, param1);
        }

        if (var1 != null) {
            var1.setLevel(this.level);
            this.addAndRegisterBlockEntity(var1);
        } else {
            LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", var0, param0);
        }

        return var1;
    }

    public void unpackTicks(long param0) {
        this.blockTicks.unpack(param0);
        this.fluidTicks.unpack(param0);
    }

    public void registerTickContainerInLevel(ServerLevel param0) {
        param0.getBlockTicks().addContainer(this.chunkPos, this.blockTicks);
        param0.getFluidTicks().addContainer(this.chunkPos, this.fluidTicks);
    }

    public void unregisterTickContainerFromLevel(ServerLevel param0) {
        param0.getBlockTicks().removeContainer(this.chunkPos);
        param0.getFluidTicks().removeContainer(this.chunkPos);
    }

    @Override
    public ChunkStatus getStatus() {
        return ChunkStatus.FULL;
    }

    public FullChunkStatus getFullStatus() {
        return this.fullStatus == null ? FullChunkStatus.FULL : this.fullStatus.get();
    }

    public void setFullStatus(Supplier<FullChunkStatus> param0) {
        this.fullStatus = param0;
    }

    public void clearAllBlockEntities() {
        this.blockEntities.values().forEach(BlockEntity::setRemoved);
        this.blockEntities.clear();
        this.tickersInLevel.values().forEach(param0 -> param0.rebind(NULL_TICKER));
        this.tickersInLevel.clear();
    }

    public void registerAllBlockEntitiesAfterLevelLoad() {
        this.blockEntities.values().forEach(param0 -> {
            Level var0 = this.level;
            if (var0 instanceof ServerLevel var1) {
                this.addGameEventListener(param0, var1);
            }

            this.updateBlockEntityTicker(param0);
        });
    }

    private <T extends BlockEntity> void addGameEventListener(T param0, ServerLevel param1) {
        Block var0 = param0.getBlockState().getBlock();
        if (var0 instanceof EntityBlock) {
            GameEventListener var1 = ((EntityBlock)var0).getListener(param1, param0);
            if (var1 != null) {
                this.getListenerRegistry(SectionPos.blockToSectionCoord(param0.getBlockPos().getY())).register(var1);
            }
        }

    }

    private <T extends BlockEntity> void updateBlockEntityTicker(T param0) {
        BlockState var0 = param0.getBlockState();
        BlockEntityTicker<T> var1 = var0.getTicker(this.level, param0.getType());
        if (var1 == null) {
            this.removeBlockEntityTicker(param0.getBlockPos());
        } else {
            this.tickersInLevel.compute(param0.getBlockPos(), (param2, param3) -> {
                TickingBlockEntity var0x = this.createTicker(param0, var1);
                if (param3 != null) {
                    param3.rebind(var0x);
                    return param3;
                } else if (this.isInLevel()) {
                    LevelChunk.RebindableTickingBlockEntityWrapper var1x = new LevelChunk.RebindableTickingBlockEntityWrapper(var0x);
                    this.level.addBlockEntityTicker(var1x);
                    return var1x;
                } else {
                    return null;
                }
            });
        }

    }

    private <T extends BlockEntity> TickingBlockEntity createTicker(T param0, BlockEntityTicker<T> param1) {
        return new LevelChunk.BoundTickingBlockEntity<>(param0, param1);
    }

    class BoundTickingBlockEntity<T extends BlockEntity> implements TickingBlockEntity {
        private final T blockEntity;
        private final BlockEntityTicker<T> ticker;
        private boolean loggedInvalidBlockState;

        BoundTickingBlockEntity(T param0, BlockEntityTicker<T> param1) {
            this.blockEntity = param0;
            this.ticker = param1;
        }

        @Override
        public void tick() {
            if (!this.blockEntity.isRemoved() && this.blockEntity.hasLevel()) {
                BlockPos var0 = this.blockEntity.getBlockPos();
                if (LevelChunk.this.isTicking(var0)) {
                    try {
                        ProfilerFiller var1 = LevelChunk.this.level.getProfiler();
                        var1.push(this::getType);
                        BlockState var2 = LevelChunk.this.getBlockState(var0);
                        if (this.blockEntity.getType().isValid(var2)) {
                            this.ticker.tick(LevelChunk.this.level, this.blockEntity.getBlockPos(), var2, this.blockEntity);
                            this.loggedInvalidBlockState = false;
                        } else if (!this.loggedInvalidBlockState) {
                            this.loggedInvalidBlockState = true;
                            LevelChunk.LOGGER
                                .warn("Block entity {} @ {} state {} invalid for ticking:", LogUtils.defer(this::getType), LogUtils.defer(this::getPos), var2);
                        }

                        var1.pop();
                    } catch (Throwable var51) {
                        CrashReport var4 = CrashReport.forThrowable(var51, "Ticking block entity");
                        CrashReportCategory var5 = var4.addCategory("Block entity being ticked");
                        this.blockEntity.fillCrashReportCategory(var5);
                        throw new ReportedException(var4);
                    }
                }
            }

        }

        @Override
        public boolean isRemoved() {
            return this.blockEntity.isRemoved();
        }

        @Override
        public BlockPos getPos() {
            return this.blockEntity.getBlockPos();
        }

        @Override
        public String getType() {
            return BlockEntityType.getKey(this.blockEntity.getType()).toString();
        }

        @Override
        public String toString() {
            return "Level ticker for " + this.getType() + "@" + this.getPos();
        }
    }

    public static enum EntityCreationType {
        IMMEDIATE,
        QUEUED,
        CHECK;
    }

    @FunctionalInterface
    public interface PostLoadProcessor {
        void run(LevelChunk var1);
    }

    class RebindableTickingBlockEntityWrapper implements TickingBlockEntity {
        private TickingBlockEntity ticker;

        RebindableTickingBlockEntityWrapper(TickingBlockEntity param0) {
            this.ticker = param0;
        }

        void rebind(TickingBlockEntity param0) {
            this.ticker = param0;
        }

        @Override
        public void tick() {
            this.ticker.tick();
        }

        @Override
        public boolean isRemoved() {
            return this.ticker.isRemoved();
        }

        @Override
        public BlockPos getPos() {
            return this.ticker.getPos();
        }

        @Override
        public String getType() {
            return this.ticker.getType();
        }

        @Override
        public String toString() {
            return this.ticker + " <wrapped>";
        }
    }
}
