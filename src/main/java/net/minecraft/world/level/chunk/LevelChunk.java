package net.minecraft.world.level.chunk;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.EuclideanGameEventDispatcher;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LevelChunk implements ChunkAccess {
    static final Logger LOGGER = LogManager.getLogger();
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
    @Nullable
    public static final LevelChunkSection EMPTY_SECTION = null;
    private final LevelChunkSection[] sections;
    private ChunkBiomeContainer biomes;
    private final Map<BlockPos, CompoundTag> pendingBlockEntities = Maps.newHashMap();
    private final Map<BlockPos, LevelChunk.RebindableTickingBlockEntityWrapper> tickersInLevel = Maps.newHashMap();
    private boolean loaded;
    final Level level;
    private final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
    private final UpgradeData upgradeData;
    private final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();
    private final Map<StructureFeature<?>, StructureStart<?>> structureStarts = Maps.newHashMap();
    private final Map<StructureFeature<?>, LongSet> structuresRefences = Maps.newHashMap();
    private final ShortList[] postProcessing;
    private TickList<Block> blockTicks;
    private TickList<Fluid> liquidTicks;
    private volatile boolean unsaved;
    private long inhabitedTime;
    @Nullable
    private Supplier<ChunkHolder.FullChunkStatus> fullStatus;
    @Nullable
    private Consumer<LevelChunk> postLoad;
    private final ChunkPos chunkPos;
    private volatile boolean isLightCorrect;
    private final Int2ObjectMap<GameEventDispatcher> gameEventDispatcherSections;

    public LevelChunk(Level param0, ChunkPos param1, ChunkBiomeContainer param2) {
        this(param0, param1, param2, UpgradeData.EMPTY, EmptyTickList.empty(), EmptyTickList.empty(), 0L, null, null);
    }

    public LevelChunk(
        Level param0,
        ChunkPos param1,
        ChunkBiomeContainer param2,
        UpgradeData param3,
        TickList<Block> param4,
        TickList<Fluid> param5,
        long param6,
        @Nullable LevelChunkSection[] param7,
        @Nullable Consumer<LevelChunk> param8
    ) {
        this.level = param0;
        this.chunkPos = param1;
        this.upgradeData = param3;
        this.gameEventDispatcherSections = new Int2ObjectOpenHashMap<>();

        for(Heightmap.Types var0 : Heightmap.Types.values()) {
            if (ChunkStatus.FULL.heightmapsAfter().contains(var0)) {
                this.heightmaps.put(var0, new Heightmap(this, var0));
            }
        }

        this.biomes = param2;
        this.blockTicks = param4;
        this.liquidTicks = param5;
        this.inhabitedTime = param6;
        this.postLoad = param8;
        this.sections = new LevelChunkSection[param0.getSectionsCount()];
        if (param7 != null) {
            if (this.sections.length == param7.length) {
                System.arraycopy(param7, 0, this.sections, 0, this.sections.length);
            } else {
                LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", param7.length, this.sections.length);
            }
        }

        this.postProcessing = new ShortList[param0.getSectionsCount()];
    }

    public LevelChunk(ServerLevel param0, ProtoChunk param1, @Nullable Consumer<LevelChunk> param2) {
        this(
            param0,
            param1.getPos(),
            param1.getBiomes(),
            param1.getUpgradeData(),
            param1.getBlockTicks(),
            param1.getLiquidTicks(),
            param1.getInhabitedTime(),
            param1.getSections(),
            param2
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

        this.setLightCorrect(param1.isLightCorrect());
        this.unsaved = true;
    }

    @Override
    public GameEventDispatcher getEventDispatcher(int param0) {
        return this.gameEventDispatcherSections.computeIfAbsent(param0, param0x -> new EuclideanGameEventDispatcher(this.level));
    }

    @Override
    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types param0) {
        return this.heightmaps.computeIfAbsent(param0, param0x -> new Heightmap(this, param0x));
    }

    @Override
    public Set<BlockPos> getBlockEntitiesPos() {
        Set<BlockPos> var0 = Sets.newHashSet(this.pendingBlockEntities.keySet());
        var0.addAll(this.blockEntities.keySet());
        return var0;
    }

    @Override
    public LevelChunkSection[] getSections() {
        return this.sections;
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
                    if (!LevelChunkSection.isEmpty(var5)) {
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
                if (!LevelChunkSection.isEmpty(var1)) {
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
        int var1 = this.getSectionIndex(var0);
        LevelChunkSection var2 = this.sections[var1];
        if (var2 == EMPTY_SECTION) {
            if (param1.isAir()) {
                return null;
            }

            var2 = new LevelChunkSection(SectionPos.blockToSectionCoord(var0));
            this.sections[var1] = var2;
        }

        boolean var3 = var2.isEmpty();
        int var4 = param0.getX() & 15;
        int var5 = var0 & 15;
        int var6 = param0.getZ() & 15;
        BlockState var7 = var2.setBlockState(var4, var5, var6, param1);
        if (var7 == param1) {
            return null;
        } else {
            Block var8 = param1.getBlock();
            this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(var4, var0, var6, param1);
            this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(var4, var0, var6, param1);
            this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(var4, var0, var6, param1);
            this.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(var4, var0, var6, param1);
            boolean var9 = var2.isEmpty();
            if (var3 != var9) {
                this.level.getChunkSource().getLightEngine().updateSectionStatus(param0, var9);
            }

            boolean var10 = var7.hasBlockEntity();
            if (!this.level.isClientSide) {
                var7.onRemove(this.level, param0, param1, param2);
            } else if (!var7.is(var8) && var10) {
                this.removeBlockEntity(param0);
            }

            if (!var2.getBlockState(var4, var5, var6).is(var8)) {
                return null;
            } else {
                if (!this.level.isClientSide) {
                    param1.onPlace(this.level, param0, var7, param2);
                }

                if (param1.hasBlockEntity()) {
                    BlockEntity var11 = this.getBlockEntity(param0, LevelChunk.EntityCreationType.CHECK);
                    if (var11 == null) {
                        var11 = ((EntityBlock)var8).newBlockEntity(param0, param1);
                        if (var11 != null) {
                            this.addAndRegisterBlockEntity(var11);
                        }
                    } else {
                        var11.setBlockState(param1);
                        this.updateBlockEntityTicker(var11);
                    }
                }

                this.unsaved = true;
                return var7;
            }
        }
    }

    @Deprecated
    @Override
    public void addEntity(Entity param0) {
    }

    @Override
    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        return this.heightmaps.get(param0).getFirstAvailable(param1 & 15, param2 & 15) - 1;
    }

    @Override
    public BlockPos getHeighestPosition(Heightmap.Types param0) {
        ChunkPos var0 = this.getPos();
        int var1 = this.getMinBuildHeight();
        BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

        for(int var3 = var0.getMinBlockX(); var3 <= var0.getMaxBlockX(); ++var3) {
            for(int var4 = var0.getMinBlockZ(); var4 <= var0.getMaxBlockZ(); ++var4) {
                int var5 = this.getHeight(param0, var3 & 15, var4 & 15);
                if (var5 > var1) {
                    var1 = var5;
                    var2.set(var3, var5, var4);
                }
            }
        }

        return var2.immutable();
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
            this.addGameEventListener(param0);
            this.updateBlockEntityTicker(param0);
        }

    }

    private boolean isInLevel() {
        return this.loaded || this.level.isClientSide();
    }

    boolean isTicking(BlockPos param0) {
        if (!this.level.getWorldBorder().isWithinBounds(param0)) {
            return false;
        } else if (!(this.level instanceof ServerLevel)) {
            return true;
        } else {
            return this.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING) && ((ServerLevel)this.level).areEntitiesLoaded(ChunkPos.asLong(param0));
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

    @Override
    public void setBlockEntityNbt(CompoundTag param0) {
        this.pendingBlockEntities.put(new BlockPos(param0.getInt("x"), param0.getInt("y"), param0.getInt("z")), param0);
    }

    @Nullable
    @Override
    public CompoundTag getBlockEntityNbtForSaving(BlockPos param0) {
        BlockEntity var0 = this.getBlockEntity(param0);
        if (var0 != null && !var0.isRemoved()) {
            CompoundTag var1 = var0.save(new CompoundTag());
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
                this.removeGameEventListener(var0);
                var0.setRemoved();
            }
        }

        this.removeBlockEntityTicker(param0);
    }

    private <T extends BlockEntity> void removeGameEventListener(T param0) {
        if (!this.level.isClientSide) {
            Block var0 = param0.getBlockState().getBlock();
            if (var0 instanceof EntityBlock) {
                GameEventListener var1 = ((EntityBlock)var0).getListener(this.level, param0);
                if (var1 != null) {
                    int var2 = SectionPos.blockToSectionCoord(param0.getBlockPos().getY());
                    GameEventDispatcher var3 = this.getEventDispatcher(var2);
                    var3.unregister(var1);
                    if (var3.isEmpty()) {
                        this.gameEventDispatcherSections.remove(var2);
                    }
                }
            }

        }
    }

    private void removeBlockEntityTicker(BlockPos param0) {
        LevelChunk.RebindableTickingBlockEntityWrapper var0 = this.tickersInLevel.remove(param0);
        if (var0 != null) {
            var0.rebind(NULL_TICKER);
        }

    }

    public void runPostLoad() {
        if (this.postLoad != null) {
            this.postLoad.accept(this);
            this.postLoad = null;
        }

    }

    public void markUnsaved() {
        this.unsaved = true;
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public ChunkPos getPos() {
        return this.chunkPos;
    }

    public void replaceWithPacketData(@Nullable ChunkBiomeContainer param0, FriendlyByteBuf param1, CompoundTag param2, BitSet param3) {
        boolean var0 = param0 != null;
        if (var0) {
            this.blockEntities.values().forEach(this::onBlockEntityRemove);
            this.blockEntities.clear();
        } else {
            this.blockEntities.values().removeIf(param1x -> {
                int var0x = this.getSectionIndex(param1x.getBlockPos().getY());
                if (param3.get(var0x)) {
                    param1x.setRemoved();
                    return true;
                } else {
                    return false;
                }
            });
        }

        for(int var1 = 0; var1 < this.sections.length; ++var1) {
            LevelChunkSection var2 = this.sections[var1];
            if (!param3.get(var1)) {
                if (var0 && var2 != EMPTY_SECTION) {
                    this.sections[var1] = EMPTY_SECTION;
                }
            } else {
                if (var2 == EMPTY_SECTION) {
                    var2 = new LevelChunkSection(this.getSectionYFromSectionIndex(var1));
                    this.sections[var1] = var2;
                }

                var2.read(param1);
            }
        }

        if (param0 != null) {
            this.biomes = param0;
        }

        for(Heightmap.Types var3 : Heightmap.Types.values()) {
            String var4 = var3.getSerializationKey();
            if (param2.contains(var4, 12)) {
                this.setHeightmap(var3, param2.getLongArray(var4));
            }
        }

    }

    private void onBlockEntityRemove(BlockEntity param0x) {
        param0x.setRemoved();
        this.tickersInLevel.remove(param0x.getBlockPos());
    }

    @Override
    public ChunkBiomeContainer getBiomes() {
        return this.biomes;
    }

    public void setLoaded(boolean param0) {
        this.loaded = param0;
    }

    public Level getLevel() {
        return this.level;
    }

    @Override
    public Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
        return Collections.unmodifiableSet(this.heightmaps.entrySet());
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    @Override
    public CompoundTag getBlockEntityNbt(BlockPos param0) {
        return this.pendingBlockEntities.get(param0);
    }

    @Override
    public Stream<BlockPos> getLights() {
        return StreamSupport.stream(
                BlockPos.betweenClosed(
                        this.chunkPos.getMinBlockX(),
                        this.getMinBuildHeight(),
                        this.chunkPos.getMinBlockZ(),
                        this.chunkPos.getMaxBlockX(),
                        this.getMaxBuildHeight() - 1,
                        this.chunkPos.getMaxBlockZ()
                    )
                    .spliterator(),
                false
            )
            .filter(param0 -> this.getBlockState(param0).getLightEmission() != 0);
    }

    @Override
    public TickList<Block> getBlockTicks() {
        return this.blockTicks;
    }

    @Override
    public TickList<Fluid> getLiquidTicks() {
        return this.liquidTicks;
    }

    @Override
    public void setUnsaved(boolean param0) {
        this.unsaved = param0;
    }

    @Override
    public boolean isUnsaved() {
        return this.unsaved;
    }

    @Nullable
    @Override
    public StructureStart<?> getStartForFeature(StructureFeature<?> param0) {
        return this.structureStarts.get(param0);
    }

    @Override
    public void setStartForFeature(StructureFeature<?> param0, StructureStart<?> param1) {
        this.structureStarts.put(param0, param1);
    }

    @Override
    public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
        return this.structureStarts;
    }

    @Override
    public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> param0) {
        this.structureStarts.clear();
        this.structureStarts.putAll(param0);
    }

    @Override
    public LongSet getReferencesForFeature(StructureFeature<?> param0) {
        return this.structuresRefences.computeIfAbsent(param0, param0x -> new LongOpenHashSet());
    }

    @Override
    public void addReferenceForFeature(StructureFeature<?> param0, long param1) {
        this.structuresRefences.computeIfAbsent(param0, param0x -> new LongOpenHashSet()).add(param1);
    }

    @Override
    public Map<StructureFeature<?>, LongSet> getAllReferences() {
        return this.structuresRefences;
    }

    @Override
    public void setAllReferences(Map<StructureFeature<?>, LongSet> param0) {
        this.structuresRefences.clear();
        this.structuresRefences.putAll(param0);
    }

    @Override
    public long getInhabitedTime() {
        return this.inhabitedTime;
    }

    @Override
    public void setInhabitedTime(long param0) {
        this.inhabitedTime = param0;
    }

    public void postProcessGeneration() {
        ChunkPos var0 = this.getPos();

        for(int var1 = 0; var1 < this.postProcessing.length; ++var1) {
            if (this.postProcessing[var1] != null) {
                for(Short var2 : this.postProcessing[var1]) {
                    BlockPos var3 = ProtoChunk.unpackOffsetCoordinates(var2, this.getSectionYFromSectionIndex(var1), var0);
                    BlockState var4 = this.getBlockState(var3);
                    BlockState var5 = Block.updateFromNeighbourShapes(var4, this.level, var3);
                    this.level.setBlock(var3, var5, 20);
                }

                this.postProcessing[var1].clear();
            }
        }

        this.unpackTicks();

        for(BlockPos var6 : ImmutableList.copyOf(this.pendingBlockEntities.keySet())) {
            this.getBlockEntity(var6);
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

    @Override
    public UpgradeData getUpgradeData() {
        return this.upgradeData;
    }

    @Override
    public ShortList[] getPostProcessing() {
        return this.postProcessing;
    }

    public void unpackTicks() {
        if (this.blockTicks instanceof ProtoTickList) {
            ((ProtoTickList)this.blockTicks).copyOut(this.level.getBlockTicks(), param0 -> this.getBlockState(param0).getBlock());
            this.blockTicks = EmptyTickList.empty();
        } else if (this.blockTicks instanceof ChunkTickList) {
            ((ChunkTickList)this.blockTicks).copyOut(this.level.getBlockTicks());
            this.blockTicks = EmptyTickList.empty();
        }

        if (this.liquidTicks instanceof ProtoTickList) {
            ((ProtoTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks(), param0 -> this.getFluidState(param0).getType());
            this.liquidTicks = EmptyTickList.empty();
        } else if (this.liquidTicks instanceof ChunkTickList) {
            ((ChunkTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks());
            this.liquidTicks = EmptyTickList.empty();
        }

    }

    public void packTicks(ServerLevel param0) {
        if (this.blockTicks == EmptyTickList.empty()) {
            this.blockTicks = new ChunkTickList<>(
                Registry.BLOCK::getKey, param0.getBlockTicks().fetchTicksInChunk(this.chunkPos, true, false), param0.getGameTime()
            );
            this.setUnsaved(true);
        }

        if (this.liquidTicks == EmptyTickList.empty()) {
            this.liquidTicks = new ChunkTickList<>(
                Registry.FLUID::getKey, param0.getLiquidTicks().fetchTicksInChunk(this.chunkPos, true, false), param0.getGameTime()
            );
            this.setUnsaved(true);
        }

    }

    @Override
    public int getMinBuildHeight() {
        return this.level.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    @Override
    public ChunkStatus getStatus() {
        return ChunkStatus.FULL;
    }

    public ChunkHolder.FullChunkStatus getFullStatus() {
        return this.fullStatus == null ? ChunkHolder.FullChunkStatus.BORDER : this.fullStatus.get();
    }

    public void setFullStatus(Supplier<ChunkHolder.FullChunkStatus> param0) {
        this.fullStatus = param0;
    }

    @Override
    public boolean isLightCorrect() {
        return this.isLightCorrect;
    }

    @Override
    public void setLightCorrect(boolean param0) {
        this.isLightCorrect = param0;
        this.setUnsaved(true);
    }

    public void invalidateAllBlockEntities() {
        this.blockEntities.values().forEach(this::onBlockEntityRemove);
    }

    public void registerAllBlockEntitiesAfterLevelLoad() {
        this.blockEntities.values().forEach(param0 -> {
            this.addGameEventListener(param0);
            this.updateBlockEntityTicker(param0);
        });
    }

    private <T extends BlockEntity> void addGameEventListener(T param0) {
        if (!this.level.isClientSide) {
            Block var0 = param0.getBlockState().getBlock();
            if (var0 instanceof EntityBlock) {
                GameEventListener var1 = ((EntityBlock)var0).getListener(this.level, param0);
                if (var1 != null) {
                    GameEventDispatcher var2 = this.getEventDispatcher(SectionPos.blockToSectionCoord(param0.getBlockPos().getY()));
                    var2.register(var1);
                }
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
                            LevelChunk.LOGGER.warn("Block entity {} @ {} state {} invalid for ticking:", this::getType, this::getPos, () -> var2);
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
            return this.ticker.toString() + " <wrapped>";
        }
    }
}
