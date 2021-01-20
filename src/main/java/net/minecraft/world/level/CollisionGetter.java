package net.minecraft.world.level;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CollisionGetter extends BlockGetter {
    WorldBorder getWorldBorder();

    @Nullable
    BlockGetter getChunkForCollisions(int var1, int var2);

    default boolean isUnobstructed(@Nullable Entity param0, VoxelShape param1) {
        return true;
    }

    default boolean isUnobstructed(BlockState param0, BlockPos param1, CollisionContext param2) {
        VoxelShape var0 = param0.getCollisionShape(this, param1, param2);
        return var0.isEmpty() || this.isUnobstructed(null, var0.move((double)param1.getX(), (double)param1.getY(), (double)param1.getZ()));
    }

    default boolean isUnobstructed(Entity param0) {
        return this.isUnobstructed(param0, Shapes.create(param0.getBoundingBox()));
    }

    default boolean noCollision(AABB param0) {
        return this.noCollision(null, param0, param0x -> true);
    }

    default boolean noCollision(Entity param0) {
        return this.noCollision(param0, param0.getBoundingBox(), param0x -> true);
    }

    default boolean noCollision(Entity param0, AABB param1) {
        return this.noCollision(param0, param1, param0x -> true);
    }

    default boolean noCollision(@Nullable Entity param0, AABB param1, Predicate<Entity> param2) {
        return this.getCollisions(param0, param1, param2).allMatch(VoxelShape::isEmpty);
    }

    Stream<VoxelShape> getEntityCollisions(@Nullable Entity var1, AABB var2, Predicate<Entity> var3);

    default Stream<VoxelShape> getCollisions(@Nullable Entity param0, AABB param1, Predicate<Entity> param2) {
        return Stream.concat(this.getBlockCollisions(param0, param1), this.getEntityCollisions(param0, param1, param2));
    }

    default Stream<VoxelShape> getBlockCollisions(@Nullable Entity param0, AABB param1) {
        return StreamSupport.stream(new CollisionSpliterator(this, param0, param1), false);
    }

    default boolean noBlockCollision(@Nullable Entity param0, AABB param1, BiPredicate<BlockState, BlockPos> param2) {
        return this.getBlockCollisions(param0, param1, param2).allMatch(VoxelShape::isEmpty);
    }

    default Stream<VoxelShape> getBlockCollisions(@Nullable Entity param0, AABB param1, BiPredicate<BlockState, BlockPos> param2) {
        return StreamSupport.stream(new CollisionSpliterator(this, param0, param1, param2), false);
    }
}
