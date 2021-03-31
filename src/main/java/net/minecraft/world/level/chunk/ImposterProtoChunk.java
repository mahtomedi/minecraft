package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.BitSet;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class ImposterProtoChunk extends ProtoChunk {
    private final LevelChunk wrapped;

    public ImposterProtoChunk(LevelChunk param0) {
        super(param0.getPos(), UpgradeData.EMPTY, param0);
        this.wrapped = param0;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        return this.wrapped.getBlockEntity(param0);
    }

    @Nullable
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

    @Nullable
    @Override
    public BlockState setBlockState(BlockPos param0, BlockState param1, boolean param2) {
        return null;
    }

    @Override
    public void setBlockEntity(BlockEntity param0) {
    }

    @Override
    public void addEntity(Entity param0) {
    }

    @Override
    public void setStatus(ChunkStatus param0) {
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
    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        return this.wrapped.getHeight(this.fixType(param0), param1, param2);
    }

    @Override
    public BlockPos getHeighestPosition(Heightmap.Types param0) {
        return this.wrapped.getHeighestPosition(this.fixType(param0));
    }

    @Override
    public ChunkPos getPos() {
        return this.wrapped.getPos();
    }

    @Nullable
    @Override
    public StructureStart<?> getStartForFeature(StructureFeature<?> param0) {
        return this.wrapped.getStartForFeature(param0);
    }

    @Override
    public void setStartForFeature(StructureFeature<?> param0, StructureStart<?> param1) {
    }

    @Override
    public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
        return this.wrapped.getAllStarts();
    }

    @Override
    public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> param0) {
    }

    @Override
    public LongSet getReferencesForFeature(StructureFeature<?> param0) {
        return this.wrapped.getReferencesForFeature(param0);
    }

    @Override
    public void addReferenceForFeature(StructureFeature<?> param0, long param1) {
    }

    @Override
    public Map<StructureFeature<?>, LongSet> getAllReferences() {
        return this.wrapped.getAllReferences();
    }

    @Override
    public void setAllReferences(Map<StructureFeature<?>, LongSet> param0) {
    }

    @Override
    public ChunkBiomeContainer getBiomes() {
        return this.wrapped.getBiomes();
    }

    @Override
    public void setUnsaved(boolean param0) {
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
    public void setBiomes(ChunkBiomeContainer param0) {
    }

    @Override
    public Stream<BlockPos> getLights() {
        return this.wrapped.getLights();
    }

    @Override
    public ProtoTickList<Block> getBlockTicks() {
        return new ProtoTickList<>(param0 -> param0.defaultBlockState().isAir(), this.getPos(), this);
    }

    @Override
    public ProtoTickList<Fluid> getLiquidTicks() {
        return new ProtoTickList<>(param0 -> param0 == Fluids.EMPTY, this.getPos(), this);
    }

    @Override
    public BitSet getCarvingMask(GenerationStep.Carving param0) {
        throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
    }

    @Override
    public BitSet getOrCreateCarvingMask(GenerationStep.Carving param0) {
        throw (UnsupportedOperationException)Util.pauseInIde(new UnsupportedOperationException("Meaningless in this context"));
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
}
