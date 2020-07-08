package net.minecraft.world.level;

import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CommonLevelAccessor extends EntityGetter, LevelReader, LevelSimulatedRW {
    @Override
    default Stream<VoxelShape> getEntityCollisions(@Nullable Entity param0, AABB param1, Predicate<Entity> param2) {
        return EntityGetter.super.getEntityCollisions(param0, param1, param2);
    }

    @Override
    default boolean isUnobstructed(@Nullable Entity param0, VoxelShape param1) {
        return EntityGetter.super.isUnobstructed(param0, param1);
    }

    @Override
    default BlockPos getHeightmapPos(Heightmap.Types param0, BlockPos param1) {
        return LevelReader.super.getHeightmapPos(param0, param1);
    }
}