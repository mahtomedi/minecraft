package net.minecraft.world.level.chunk;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

public class EmptyLevelChunk extends LevelChunk {
    private static final Biome[] BIOMES = Util.make(new Biome[ChunkBiomeContainer.BIOMES_SIZE], param0 -> Arrays.fill(param0, Biomes.PLAINS));

    public EmptyLevelChunk(Level param0, ChunkPos param1) {
        super(param0, param1, new ChunkBiomeContainer(BIOMES));
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

    @Nullable
    @Override
    public LevelLightEngine getLightEngine() {
        return null;
    }

    @Override
    public int getLightEmission(BlockPos param0) {
        return 0;
    }

    @Override
    public void addEntity(Entity param0) {
    }

    @Override
    public void removeEntity(Entity param0) {
    }

    @Override
    public void removeEntity(Entity param0, int param1) {
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0, LevelChunk.EntityCreationType param1) {
        return null;
    }

    @Override
    public void addBlockEntity(BlockEntity param0) {
    }

    @Override
    public void setBlockEntity(BlockPos param0, BlockEntity param1) {
    }

    @Override
    public void removeBlockEntity(BlockPos param0) {
    }

    @Override
    public void markUnsaved() {
    }

    @Override
    public void getEntities(@Nullable Entity param0, AABB param1, List<Entity> param2, Predicate<? super Entity> param3) {
    }

    @Override
    public <T extends Entity> void getEntitiesOfClass(Class<? extends T> param0, AABB param1, List<T> param2, Predicate<? super T> param3) {
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
    public ChunkHolder.FullChunkStatus getFullStatus() {
        return ChunkHolder.FullChunkStatus.BORDER;
    }
}
