package net.minecraft.world.level;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PathNavigationRegion implements BlockGetter, CollisionGetter {
    protected final int centerX;
    protected final int centerZ;
    protected final ChunkAccess[][] chunks;
    protected boolean allEmpty;
    protected final Level level;

    public PathNavigationRegion(Level param0, BlockPos param1, BlockPos param2) {
        this.level = param0;
        this.centerX = SectionPos.blockToSectionCoord(param1.getX());
        this.centerZ = SectionPos.blockToSectionCoord(param1.getZ());
        int var0 = SectionPos.blockToSectionCoord(param2.getX());
        int var1 = SectionPos.blockToSectionCoord(param2.getZ());
        this.chunks = new ChunkAccess[var0 - this.centerX + 1][var1 - this.centerZ + 1];
        ChunkSource var2 = param0.getChunkSource();
        this.allEmpty = true;

        for(int var3 = this.centerX; var3 <= var0; ++var3) {
            for(int var4 = this.centerZ; var4 <= var1; ++var4) {
                this.chunks[var3 - this.centerX][var4 - this.centerZ] = var2.getChunkNow(var3, var4);
            }
        }

        for(int var5 = SectionPos.blockToSectionCoord(param1.getX()); var5 <= SectionPos.blockToSectionCoord(param2.getX()); ++var5) {
            for(int var6 = SectionPos.blockToSectionCoord(param1.getZ()); var6 <= SectionPos.blockToSectionCoord(param2.getZ()); ++var6) {
                ChunkAccess var7 = this.chunks[var5 - this.centerX][var6 - this.centerZ];
                if (var7 != null && !var7.isYSpaceEmpty(param1.getY(), param2.getY())) {
                    this.allEmpty = false;
                    return;
                }
            }
        }

    }

    private ChunkAccess getChunk(BlockPos param0) {
        return this.getChunk(SectionPos.blockToSectionCoord(param0.getX()), SectionPos.blockToSectionCoord(param0.getZ()));
    }

    private ChunkAccess getChunk(int param0, int param1) {
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
    public WorldBorder getWorldBorder() {
        return this.level.getWorldBorder();
    }

    @Override
    public BlockGetter getChunkForCollisions(int param0, int param1) {
        return this.getChunk(param0, param1);
    }

    @Override
    public List<VoxelShape> getEntityCollisions(@Nullable Entity param0, AABB param1) {
        return List.of();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        ChunkAccess var0 = this.getChunk(param0);
        return var0.getBlockEntity(param0);
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        if (this.isOutsideBuildHeight(param0)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            ChunkAccess var0 = this.getChunk(param0);
            return var0.getBlockState(param0);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        if (this.isOutsideBuildHeight(param0)) {
            return Fluids.EMPTY.defaultFluidState();
        } else {
            ChunkAccess var0 = this.getChunk(param0);
            return var0.getFluidState(param0);
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

    public ProfilerFiller getProfiler() {
        return this.level.getProfiler();
    }
}
