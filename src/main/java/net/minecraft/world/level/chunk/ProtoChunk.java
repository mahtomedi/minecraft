package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.TickContainerAccess;

public class ProtoChunk extends ChunkAccess {
    @Nullable
    private volatile LevelLightEngine lightEngine;
    private volatile ChunkStatus status = ChunkStatus.EMPTY;
    private final List<CompoundTag> entities = Lists.newArrayList();
    private final List<BlockPos> lights = Lists.newArrayList();
    private final Map<GenerationStep.Carving, CarvingMask> carvingMasks = new Object2ObjectArrayMap<>();
    @Nullable
    private BelowZeroRetrogen belowZeroRetrogen;
    private final ProtoChunkTicks<Block> blockTicks;
    private final ProtoChunkTicks<Fluid> fluidTicks;

    public ProtoChunk(ChunkPos param0, UpgradeData param1, LevelHeightAccessor param2, Registry<Biome> param3, @Nullable BlendingData param4) {
        this(param0, param1, null, new ProtoChunkTicks<>(), new ProtoChunkTicks<>(), param2, param3, param4);
    }

    public ProtoChunk(
        ChunkPos param0,
        UpgradeData param1,
        @Nullable LevelChunkSection[] param2,
        ProtoChunkTicks<Block> param3,
        ProtoChunkTicks<Fluid> param4,
        LevelHeightAccessor param5,
        Registry<Biome> param6,
        @Nullable BlendingData param7
    ) {
        super(param0, param1, param5, param6, 0L, param2, param7);
        this.blockTicks = param3;
        this.fluidTicks = param4;
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
    public BlockState getBlockState(BlockPos param0) {
        int var0 = param0.getY();
        if (this.isOutsideBuildHeight(var0)) {
            return Blocks.VOID_AIR.defaultBlockState();
        } else {
            LevelChunkSection var1 = this.getSection(this.getSectionIndex(var0));
            return var1.hasOnlyAir() ? Blocks.AIR.defaultBlockState() : var1.getBlockState(param0.getX() & 15, var0 & 15, param0.getZ() & 15);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        int var0 = param0.getY();
        if (this.isOutsideBuildHeight(var0)) {
            return Fluids.EMPTY.defaultFluidState();
        } else {
            LevelChunkSection var1 = this.getSection(this.getSectionIndex(var0));
            return var1.hasOnlyAir() ? Fluids.EMPTY.defaultFluidState() : var1.getFluidState(param0.getX() & 15, var0 & 15, param0.getZ() & 15);
        }
    }

    @Override
    public Stream<BlockPos> getLights() {
        return this.lights.stream();
    }

    public ShortList[] getPackedLights() {
        ShortList[] var0 = new ShortList[this.getSectionsCount()];

        for(BlockPos var1 : this.lights) {
            ChunkAccess.getOrCreateOffsetList(var0, this.getSectionIndex(var1.getY())).add(packOffsetCoordinates(var1));
        }

        return var0;
    }

    public void addLight(short param0, int param1) {
        this.addLight(unpackOffsetCoordinates(param0, this.getSectionYFromSectionIndex(param1), this.chunkPos));
    }

    public void addLight(BlockPos param0) {
        this.lights.add(param0.immutable());
    }

    @Nullable
    @Override
    public BlockState setBlockState(BlockPos param0, BlockState param1, boolean param2) {
        int var0 = param0.getX();
        int var1 = param0.getY();
        int var2 = param0.getZ();
        if (var1 >= this.getMinBuildHeight() && var1 < this.getMaxBuildHeight()) {
            int var3 = this.getSectionIndex(var1);
            if (this.sections[var3].hasOnlyAir() && param1.is(Blocks.AIR)) {
                return param1;
            } else {
                if (param1.getLightEmission() > 0) {
                    this.lights.add(new BlockPos((var0 & 15) + this.getPos().getMinBlockX(), var1, (var2 & 15) + this.getPos().getMinBlockZ()));
                }

                LevelChunkSection var4 = this.getSection(var3);
                BlockState var5 = var4.setBlockState(var0 & 15, var1 & 15, var2 & 15, param1);
                if (this.status.isOrAfter(ChunkStatus.FEATURES)
                    && param1 != var5
                    && (
                        param1.getLightBlock(this, param0) != var5.getLightBlock(this, param0)
                            || param1.getLightEmission() != var5.getLightEmission()
                            || param1.useShapeForLightOcclusion()
                            || var5.useShapeForLightOcclusion()
                    )) {
                    this.lightEngine.checkBlock(param0);
                }

                EnumSet<Heightmap.Types> var6 = this.getStatus().heightmapsAfter();
                EnumSet<Heightmap.Types> var7 = null;

                for(Heightmap.Types var8 : var6) {
                    Heightmap var9 = this.heightmaps.get(var8);
                    if (var9 == null) {
                        if (var7 == null) {
                            var7 = EnumSet.noneOf(Heightmap.Types.class);
                        }

                        var7.add(var8);
                    }
                }

                if (var7 != null) {
                    Heightmap.primeHeightmaps(this, var7);
                }

                for(Heightmap.Types var10 : var6) {
                    this.heightmaps.get(var10).update(var0 & 15, var1, var2 & 15, param1);
                }

                return var5;
            }
        } else {
            return Blocks.VOID_AIR.defaultBlockState();
        }
    }

    @Override
    public void setBlockEntity(BlockEntity param0) {
        this.blockEntities.put(param0.getBlockPos(), param0);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        return this.blockEntities.get(param0);
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    public void addEntity(CompoundTag param0) {
        this.entities.add(param0);
    }

    @Override
    public void addEntity(Entity param0) {
        if (!param0.isPassenger()) {
            CompoundTag var0 = new CompoundTag();
            param0.save(var0);
            this.addEntity(var0);
        }
    }

    @Override
    public void setStartForFeature(StructureFeature<?> param0, StructureStart<?> param1) {
        BelowZeroRetrogen var0 = this.getBelowZeroRetrogen();
        if (var0 != null && param1.isValid()) {
            BoundingBox var1 = param1.getBoundingBox();
            LevelHeightAccessor var2 = this.getHeightAccessorForGeneration();
            if (var1.minY() < var2.getMinBuildHeight() || var1.maxY() >= var2.getMaxBuildHeight()) {
                return;
            }
        }

        super.setStartForFeature(param0, param1);
    }

    public List<CompoundTag> getEntities() {
        return this.entities;
    }

    @Override
    public ChunkStatus getStatus() {
        return this.status;
    }

    public void setStatus(ChunkStatus param0) {
        this.status = param0;
        if (this.belowZeroRetrogen != null && param0.isOrAfter(this.belowZeroRetrogen.targetStatus())) {
            this.setBelowZeroRetrogen(null);
        }

        this.setUnsaved(true);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int param0, int param1, int param2) {
        if (!this.getStatus().isOrAfter(ChunkStatus.BIOMES)
            && (this.belowZeroRetrogen == null || !this.belowZeroRetrogen.targetStatus().isOrAfter(ChunkStatus.BIOMES))) {
            throw new IllegalStateException("Asking for biomes before we have biomes");
        } else {
            return super.getNoiseBiome(param0, param1, param2);
        }
    }

    public static short packOffsetCoordinates(BlockPos param0) {
        int var0 = param0.getX();
        int var1 = param0.getY();
        int var2 = param0.getZ();
        int var3 = var0 & 15;
        int var4 = var1 & 15;
        int var5 = var2 & 15;
        return (short)(var3 | var4 << 4 | var5 << 8);
    }

    public static BlockPos unpackOffsetCoordinates(short param0, int param1, ChunkPos param2) {
        int var0 = SectionPos.sectionToBlockCoord(param2.x, param0 & 15);
        int var1 = SectionPos.sectionToBlockCoord(param1, param0 >>> 4 & 15);
        int var2 = SectionPos.sectionToBlockCoord(param2.z, param0 >>> 8 & 15);
        return new BlockPos(var0, var1, var2);
    }

    @Override
    public void markPosForPostprocessing(BlockPos param0) {
        if (!this.isOutsideBuildHeight(param0)) {
            ChunkAccess.getOrCreateOffsetList(this.postProcessing, this.getSectionIndex(param0.getY())).add(packOffsetCoordinates(param0));
        }

    }

    @Override
    public void addPackedPostProcess(short param0, int param1) {
        ChunkAccess.getOrCreateOffsetList(this.postProcessing, param1).add(param0);
    }

    public Map<BlockPos, CompoundTag> getBlockEntityNbts() {
        return Collections.unmodifiableMap(this.pendingBlockEntities);
    }

    @Nullable
    @Override
    public CompoundTag getBlockEntityNbtForSaving(BlockPos param0) {
        BlockEntity var0 = this.getBlockEntity(param0);
        return var0 != null ? var0.saveWithFullMetadata() : this.pendingBlockEntities.get(param0);
    }

    @Override
    public void removeBlockEntity(BlockPos param0) {
        this.blockEntities.remove(param0);
        this.pendingBlockEntities.remove(param0);
    }

    @Nullable
    public CarvingMask getCarvingMask(GenerationStep.Carving param0) {
        return this.carvingMasks.get(param0);
    }

    public CarvingMask getOrCreateCarvingMask(GenerationStep.Carving param0) {
        return this.carvingMasks.computeIfAbsent(param0, param0x -> new CarvingMask(this.getHeight(), this.getMinBuildHeight()));
    }

    public void setCarvingMask(GenerationStep.Carving param0, CarvingMask param1) {
        this.carvingMasks.put(param0, param1);
    }

    public void setLightEngine(LevelLightEngine param0) {
        this.lightEngine = param0;
    }

    public void setBelowZeroRetrogen(@Nullable BelowZeroRetrogen param0) {
        this.belowZeroRetrogen = param0;
    }

    @Nullable
    @Override
    public BelowZeroRetrogen getBelowZeroRetrogen() {
        return this.belowZeroRetrogen;
    }

    private static <T> LevelChunkTicks<T> unpackTicks(ProtoChunkTicks<T> param0) {
        return new LevelChunkTicks<>(param0.scheduledTicks());
    }

    public LevelChunkTicks<Block> unpackBlockTicks() {
        return unpackTicks(this.blockTicks);
    }

    public LevelChunkTicks<Fluid> unpackFluidTicks() {
        return unpackTicks(this.fluidTicks);
    }

    @Override
    public LevelHeightAccessor getHeightAccessorForGeneration() {
        return (LevelHeightAccessor)(this.isUpgrading() ? BelowZeroRetrogen.UPGRADE_HEIGHT_ACCESSOR : this);
    }
}
