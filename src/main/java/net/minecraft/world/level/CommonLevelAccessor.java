package net.minecraft.world.level;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CommonLevelAccessor extends EntityGetter, LevelReader, LevelSimulatedRW {
    @Override
    default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos param0, BlockEntityType<T> param1) {
        return LevelReader.super.getBlockEntity(param0, param1);
    }

    @Override
    default List<VoxelShape> getEntityCollisions(@Nullable Entity param0, AABB param1) {
        return EntityGetter.super.getEntityCollisions(param0, param1);
    }

    @Override
    default boolean isUnobstructed(@Nullable Entity param0, VoxelShape param1) {
        return EntityGetter.super.isUnobstructed(param0, param1);
    }

    @Override
    default BlockPos getHeightmapPos(Heightmap.Types param0, BlockPos param1) {
        return LevelReader.super.getHeightmapPos(param0, param1);
    }

    RegistryAccess registryAccess();
}
