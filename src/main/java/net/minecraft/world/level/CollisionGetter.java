package net.minecraft.world.level;

import com.google.common.collect.Streams;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
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

    default Stream<VoxelShape> getEntityCollisions(@Nullable Entity param0, AABB param1, Predicate<Entity> param2) {
        return Stream.empty();
    }

    default Stream<VoxelShape> getCollisions(@Nullable Entity param0, AABB param1, Predicate<Entity> param2) {
        return Streams.concat(this.getBlockCollisions(param0, param1), this.getEntityCollisions(param0, param1, param2));
    }

    default Stream<VoxelShape> getBlockCollisions(@Nullable final Entity param0, final AABB param1) {
        int var0 = Mth.floor(param1.minX - 1.0E-7) - 1;
        int var1 = Mth.floor(param1.maxX + 1.0E-7) + 1;
        int var2 = Mth.floor(param1.minY - 1.0E-7) - 1;
        int var3 = Mth.floor(param1.maxY + 1.0E-7) + 1;
        int var4 = Mth.floor(param1.minZ - 1.0E-7) - 1;
        int var5 = Mth.floor(param1.maxZ + 1.0E-7) + 1;
        final CollisionContext var6 = param0 == null ? CollisionContext.empty() : CollisionContext.of(param0);
        final Cursor3D var7 = new Cursor3D(var0, var2, var4, var1, var3, var5);
        final BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();
        final VoxelShape var9 = Shapes.create(param1);
        return StreamSupport.stream(
            new AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, 1280) {
                boolean skipWorldBorderCheck = param0 == null;
    
                @Override
                public boolean tryAdvance(Consumer<? super VoxelShape> param0x) {
                    if (!this.skipWorldBorderCheck) {
                        this.skipWorldBorderCheck = true;
                        WorldBorder var0 = CollisionGetter.this.getWorldBorder();
                        boolean var1 = CollisionGetter.isBoxFullyWithinWorldBorder(var0, param0.getBoundingBox().deflate(1.0E-7));
                        boolean var2 = var1 && !CollisionGetter.isBoxFullyWithinWorldBorder(var0, param0.getBoundingBox().inflate(1.0E-7));
                        if (var2) {
                            param0.accept(var0.getCollisionShape());
                            return true;
                        }
                    }
    
                    while(var7.advance()) {
                        int var3 = var7.nextX();
                        int var4 = var7.nextY();
                        int var5 = var7.nextZ();
                        int var6 = var7.getNextType();
                        if (var6 != 3) {
                            int var7 = var3 >> 4;
                            int var8 = var5 >> 4;
                            BlockGetter var9 = CollisionGetter.this.getChunkForCollisions(var7, var8);
                            if (var9 != null) {
                                var8.set(var3, var4, var5);
                                BlockState var10 = var9.getBlockState(var8);
                                if ((var6 != 1 || var10.hasLargeCollisionShape()) && (var6 != 2 || var10.getBlock() == Blocks.MOVING_PISTON)) {
                                    VoxelShape var11 = var10.getCollisionShape(CollisionGetter.this, var8, var6);
                                    if (var11 == Shapes.block()) {
                                        if (param1.intersects(
                                            (double)var3, (double)var4, (double)var5, (double)var3 + 1.0, (double)var4 + 1.0, (double)var5 + 1.0
                                        )) {
                                            param0.accept(var11.move((double)var3, (double)var4, (double)var5));
                                            return true;
                                        }
                                    } else {
                                        VoxelShape var12 = var11.move((double)var3, (double)var4, (double)var5);
                                        if (Shapes.joinIsNotEmpty(var12, var9, BooleanOp.AND)) {
                                            param0.accept(var12);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
    
                    return false;
                }
            },
            false
        );
    }

    static boolean isBoxFullyWithinWorldBorder(WorldBorder param0, AABB param1) {
        double var0 = (double)Mth.floor(param0.getMinX());
        double var1 = (double)Mth.floor(param0.getMinZ());
        double var2 = (double)Mth.ceil(param0.getMaxX());
        double var3 = (double)Mth.ceil(param0.getMaxZ());
        return param1.minX > var0
            && param1.minX < var2
            && param1.minZ > var1
            && param1.minZ < var3
            && param1.maxX > var0
            && param1.maxX < var2
            && param1.maxZ > var1
            && param1.maxZ < var3;
    }
}
