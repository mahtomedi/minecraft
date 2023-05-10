package net.minecraft.world.level;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
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
        return this.noCollision(null, param0);
    }

    default boolean noCollision(Entity param0) {
        return this.noCollision(param0, param0.getBoundingBox());
    }

    default boolean noCollision(@Nullable Entity param0, AABB param1) {
        for(VoxelShape var0 : this.getBlockCollisions(param0, param1)) {
            if (!var0.isEmpty()) {
                return false;
            }
        }

        if (!this.getEntityCollisions(param0, param1).isEmpty()) {
            return false;
        } else if (param0 == null) {
            return true;
        } else {
            VoxelShape var1 = this.borderCollision(param0, param1);
            return var1 == null || !Shapes.joinIsNotEmpty(var1, Shapes.create(param1), BooleanOp.AND);
        }
    }

    List<VoxelShape> getEntityCollisions(@Nullable Entity var1, AABB var2);

    default Iterable<VoxelShape> getCollisions(@Nullable Entity param0, AABB param1) {
        List<VoxelShape> var0 = this.getEntityCollisions(param0, param1);
        Iterable<VoxelShape> var1 = this.getBlockCollisions(param0, param1);
        return var0.isEmpty() ? var1 : Iterables.concat(var0, var1);
    }

    default Iterable<VoxelShape> getBlockCollisions(@Nullable Entity param0, AABB param1) {
        return () -> new BlockCollisions<>(this, param0, param1, false, (param0x, param1x) -> param1x);
    }

    @Nullable
    private VoxelShape borderCollision(Entity param0, AABB param1) {
        WorldBorder var0 = this.getWorldBorder();
        return var0.isInsideCloseToBorder(param0, param1) ? var0.getCollisionShape() : null;
    }

    default boolean collidesWithSuffocatingBlock(@Nullable Entity param0, AABB param1) {
        BlockCollisions<VoxelShape> var0 = new BlockCollisions<>(this, param0, param1, true, (param0x, param1x) -> param1x);

        while(var0.hasNext()) {
            if (!var0.next().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    default Optional<BlockPos> findSupportingBlock(Entity param0, AABB param1) {
        BlockPos var0 = null;
        double var1 = Double.MAX_VALUE;
        BlockCollisions<BlockPos> var2 = new BlockCollisions<>(this, param0, param1, false, (param0x, param1x) -> param0x);

        while(var2.hasNext()) {
            BlockPos var3 = var2.next();
            double var4 = var3.distToCenterSqr(param0.position());
            if (var4 < var1 || var4 == var1 && (var0 == null || var0.compareTo(var3) < 0)) {
                var0 = var3.immutable();
                var1 = var4;
            }
        }

        return Optional.ofNullable(var0);
    }

    default Optional<Vec3> findFreePosition(@Nullable Entity param0, VoxelShape param1, Vec3 param2, double param3, double param4, double param5) {
        if (param1.isEmpty()) {
            return Optional.empty();
        } else {
            AABB var0 = param1.bounds().inflate(param3, param4, param5);
            VoxelShape var1 = StreamSupport.stream(this.getBlockCollisions(param0, var0).spliterator(), false)
                .filter(param0x -> this.getWorldBorder() == null || this.getWorldBorder().isWithinBounds(param0x.bounds()))
                .flatMap(param0x -> param0x.toAabbs().stream())
                .map(param3x -> param3x.inflate(param3 / 2.0, param4 / 2.0, param5 / 2.0))
                .map(Shapes::create)
                .reduce(Shapes.empty(), Shapes::or);
            VoxelShape var2 = Shapes.join(param1, var1, BooleanOp.ONLY_FIRST);
            return var2.closestPointTo(param2);
        }
    }
}
