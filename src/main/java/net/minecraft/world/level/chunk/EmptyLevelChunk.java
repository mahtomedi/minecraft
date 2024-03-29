package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class EmptyLevelChunk extends LevelChunk {
    private final Holder<Biome> biome;

    public EmptyLevelChunk(Level param0, ChunkPos param1, Holder<Biome> param2) {
        super(param0, param1);
        this.biome = param2;
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        return Blocks.VOID_AIR.defaultBlockState();
    }

    @Nullable
    @Override
    public BlockState setBlockState(BlockPos param0, BlockState param1, boolean param2) {
        return null;
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public int getLightEmission(BlockPos param0) {
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0, LevelChunk.EntityCreationType param1) {
        return null;
    }

    @Override
    public void addAndRegisterBlockEntity(BlockEntity param0) {
    }

    @Override
    public void setBlockEntity(BlockEntity param0) {
    }

    @Override
    public void removeBlockEntity(BlockPos param0) {
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean isYSpaceEmpty(int param0, int param1) {
        return true;
    }

    @Override
    public FullChunkStatus getFullStatus() {
        return FullChunkStatus.FULL;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int param0, int param1, int param2) {
        return this.biome;
    }
}
