package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.TickContainerAccess;

public class ImposterProtoChunk extends ProtoChunk {
    private final LevelChunk wrapped;
    private final boolean allowWrites;

    public ImposterProtoChunk(LevelChunk param0, boolean param1) {
        super(
            param0.getPos(),
            UpgradeData.EMPTY,
            param0.levelHeightAccessor,
            param0.getLevel().registryAccess().registryOrThrow(Registries.BIOME),
            param0.getBlendingData()
        );
        this.wrapped = param0;
        this.allowWrites = param1;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        return this.wrapped.getBlockEntity(param0);
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        return this.wrapped.getBlockState(param0);
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        return this.wrapped.getFluidState(param0);
    }

    @Override
    public int getMaxLightLevel() {
        return this.wrapped.getMaxLightLevel();
    }

    @Override
    public LevelChunkSection getSection(int param0) {
        return this.allowWrites ? this.wrapped.getSection(param0) : super.getSection(param0);
    }

    @Nullable
    @Override
    public BlockState setBlockState(BlockPos param0, BlockState param1, boolean param2) {
        return this.allowWrites ? this.wrapped.setBlockState(param0, param1, param2) : null;
    }

    @Override
    public void setBlockEntity(BlockEntity param0) {
        if (this.allowWrites) {
            this.wrapped.setBlockEntity(param0);
        }

    }

    @Override
    public void addEntity(Entity param0) {
        if (this.allowWrites) {
            this.wrapped.addEntity(param0);
        }

    }

    @Override
    public void setStatus(ChunkStatus param0) {
        if (this.allowWrites) {
            super.setStatus(param0);
        }

    }

    @Override
    public LevelChunkSection[] getSections() {
        return this.wrapped.getSections();
    }

    @Override
    public void setHeightmap(Heightmap.Types param0, long[] param1) {
    }

    private Heightmap.Types fixType(Heightmap.Types param0) {
        if (param0 == Heightmap.Types.WORLD_SURFACE_WG) {
            return Heightmap.Types.WORLD_SURFACE;
        } else {
            return param0 == Heightmap.Types.OCEAN_FLOOR_WG ? Heightmap.Types.OCEAN_FLOOR : param0;
        }
    }

    @Override
    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types param0) {
        return this.wrapped.getOrCreateHeightmapUnprimed(param0);
    }

    @Override
    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        return this.wrapped.getHeight(this.fixType(param0), param1, param2);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int param0, int param1, int param2) {
        return this.wrapped.getNoiseBiome(param0, param1, param2);
    }

    @Override
    public ChunkPos getPos() {
        return this.wrapped.getPos();
    }

    @Nullable
    @Override
    public StructureStart getStartForStructure(Structure param0) {
        return this.wrapped.getStartForStructure(param0);
    }

    @Override
    public void setStartForStructure(Structure param0, StructureStart param1) {
    }

    @Override
    public Map<Structure, StructureStart> getAllStarts() {
        return this.wrapped.getAllStarts();
    }

    @Override
    public void setAllStarts(Map<Structure, StructureStart> param0) {
    }

    @Override
    public LongSet getReferencesForStructure(Structure param0) {
        return this.wrapped.getReferencesForStructure(param0);
    }

    @Override
    public void addReferenceForStructure(Structure param0, long param1) {
    }

    @Override
    public Map<Structure, LongSet> getAllReferences() {
        return this.wrapped.getAllReferences();
    }

    @Override
    public void setAllReferences(Map<Structure, LongSet> param0) {
    }

    @Override
    public void setUnsaved(boolean param0) {
        this.wrapped.setUnsaved(param0);
    }

    @Override
    public boolean isUnsaved() {
        return false;
    }

    @Override
    public ChunkStatus getStatus() {
        return this.wrapped.getStatus();
    }

    @Override
    public void removeBlockEntity(BlockPos param0) {
    }

    @Override
    public void markPosForPostprocessing(BlockPos param0) {
    }

    @Override
    public void setBlockEntityNbt(CompoundTag param0) {
    }

    @Nullable
    @Override
    public CompoundTag getBlockEntityNbt(BlockPos param0) {
        return this.wrapped.getBlockEntityNbt(param0);
    }

    @Nullable
    @Override
    public CompoundTag getBlockEntityNbtForSaving(BlockPos param0) {
        return this.wrapped.getBlockEntityNbtForSaving(param0);
    }

    @Override
    public Stream<BlockPos> getLights() {
        return this.wrapped.getLights();
    }

    @Override
    public TickContainerAccess<Block> getBlockTicks() {
        return this.allowWrites ? this.wrapped.getBlockTicks() : BlackholeTickAccess.emptyContainer();
    }

    @Override
    public TickContainerAccess<Fluid> getFluidTicks() {
        return this.allowWrites ? this.wrapped.getFluidTicks() : BlackholeTickAccess.emptyContainer();
    }

    @Override
    public ChunkAccess.TicksToSave getTicksForSerialization() {
        return this.wrapped.getTicksForSerialization();
    }

    @Nullable
    @Override
    public BlendingData getBlendingData() {
        return this.wrapped.getBlendingData();
    }

    @Override
    public void setBlendingData(BlendingData param0) {
        this.wrapped.setBlendingData(param0);
    }

    @Override
    public CarvingMask getCarvingMask(GenerationStep.Carving param0) {
        if (this.allowWrites) {
            return super.getCarvingMask(param0);
        } else {
            throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
        }
    }

    @Override
    public CarvingMask getOrCreateCarvingMask(GenerationStep.Carving param0) {
        if (this.allowWrites) {
            return super.getOrCreateCarvingMask(param0);
        } else {
            throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
        }
    }

    public LevelChunk getWrapped() {
        return this.wrapped;
    }

    @Override
    public boolean isLightCorrect() {
        return this.wrapped.isLightCorrect();
    }

    @Override
    public void setLightCorrect(boolean param0) {
        this.wrapped.setLightCorrect(param0);
    }

    @Override
    public void fillBiomesFromNoise(BiomeResolver param0, Climate.Sampler param1) {
        if (this.allowWrites) {
            this.wrapped.fillBiomesFromNoise(param0, param1);
        }

    }
}
