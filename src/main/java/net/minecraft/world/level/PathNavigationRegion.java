package net.minecraft.world.level;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PathNavigationRegion implements LevelReader {
    protected final int centerX;
    protected final int centerZ;
    protected final ChunkAccess[][] chunks;
    protected boolean allEmpty;
    protected final Level level;

    public PathNavigationRegion(Level param0, BlockPos param1, BlockPos param2) {
        this.level = param0;
        this.centerX = param1.getX() >> 4;
        this.centerZ = param1.getZ() >> 4;
        int var0 = param2.getX() >> 4;
        int var1 = param2.getZ() >> 4;
        this.chunks = new ChunkAccess[var0 - this.centerX + 1][var1 - this.centerZ + 1];
        this.allEmpty = true;

        for(int var2 = this.centerX; var2 <= var0; ++var2) {
            for(int var3 = this.centerZ; var3 <= var1; ++var3) {
                this.chunks[var2 - this.centerX][var3 - this.centerZ] = param0.getChunk(var2, var3, ChunkStatus.FULL, false);
            }
        }

        for(int var4 = param1.getX() >> 4; var4 <= param2.getX() >> 4; ++var4) {
            for(int var5 = param1.getZ() >> 4; var5 <= param2.getZ() >> 4; ++var5) {
                ChunkAccess var6 = this.chunks[var4 - this.centerX][var5 - this.centerZ];
                if (var6 != null && !var6.isYSpaceEmpty(param1.getY(), param2.getY())) {
                    this.allEmpty = false;
                    return;
                }
            }
        }

    }

    @Override
    public int getRawBrightness(BlockPos param0, int param1) {
        return this.level.getRawBrightness(param0, param1);
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int param0, int param1, ChunkStatus param2, boolean param3) {
        int var0 = param0 - this.centerX;
        int var1 = param1 - this.centerZ;
        if (var0 >= 0 && var0 < this.chunks.length && var1 >= 0 && var1 < this.chunks[var0].length) {
            ChunkAccess var2 = this.chunks[var0][var1];
            return (ChunkAccess)(var2 != null ? var2 : new EmptyLevelChunk(this.level, new ChunkPos(param0, param1)));
        } else {
            return new EmptyLevelChunk(this.level, new ChunkPos(param0, param1));
        }
    }

    @Override
    public boolean hasChunk(int param0, int param1) {
        int var0 = param0 - this.centerX;
        int var1 = param1 - this.centerZ;
        return var0 >= 0 && var0 < this.chunks.length && var1 >= 0 && var1 < this.chunks[var0].length;
    }

    @Override
    public BlockPos getHeightmapPos(Heightmap.Types param0, BlockPos param1) {
        return this.level.getHeightmapPos(param0, param1);
    }

    @Override
    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        return this.level.getHeight(param0, param1, param2);
    }

    @Override
    public int getSkyDarken() {
        return this.level.getSkyDarken();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.level.getWorldBorder();
    }

    @Override
    public boolean isUnobstructed(@Nullable Entity param0, VoxelShape param1) {
        return true;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public int getSeaLevel() {
        return this.level.getSeaLevel();
    }

    @Override
    public Dimension getDimension() {
        return this.level.getDimension();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        ChunkAccess var0 = this.getChunk(param0);
        return var0.getBlockEntity(param0);
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        if (Level.isOutsideBuildHeight(param0)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            ChunkAccess var0 = this.getChunk(param0);
            return var0.getBlockState(param0);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        if (Level.isOutsideBuildHeight(param0)) {
            return Fluids.EMPTY.defaultFluidState();
        } else {
            ChunkAccess var0 = this.getChunk(param0);
            return var0.getFluidState(param0);
        }
    }

    @Override
    public Biome getBiome(BlockPos param0) {
        ChunkAccess var0 = this.getChunk(param0);
        return var0.getBiome(param0);
    }

    @Override
    public int getBrightness(LightLayer param0, BlockPos param1) {
        return this.level.getBrightness(param0, param1);
    }
}
