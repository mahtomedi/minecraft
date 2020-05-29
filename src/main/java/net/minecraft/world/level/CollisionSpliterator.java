package net.minecraft.world.level;

import java.util.Objects;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
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

public class CollisionSpliterator extends AbstractSpliterator<VoxelShape> {
    @Nullable
    private final Entity source;
    private final AABB box;
    private final CollisionContext context;
    private final Cursor3D cursor;
    private final BlockPos.MutableBlockPos pos;
    private final VoxelShape entityShape;
    private final CollisionGetter collisionGetter;
    private boolean needsBorderCheck;

    public CollisionSpliterator(CollisionGetter param0, @Nullable Entity param1, AABB param2) {
        super(Long.MAX_VALUE, 1280);
        this.context = param1 == null ? CollisionContext.empty() : CollisionContext.of(param1);
        this.pos = new BlockPos.MutableBlockPos();
        this.entityShape = Shapes.create(param2);
        this.collisionGetter = param0;
        this.needsBorderCheck = param1 != null;
        this.source = param1;
        this.box = param2;
        int var0 = Mth.floor(param2.minX - 1.0E-7) - 1;
        int var1 = Mth.floor(param2.maxX + 1.0E-7) + 1;
        int var2 = Mth.floor(param2.minY - 1.0E-7) - 1;
        int var3 = Mth.floor(param2.maxY + 1.0E-7) + 1;
        int var4 = Mth.floor(param2.minZ - 1.0E-7) - 1;
        int var5 = Mth.floor(param2.maxZ + 1.0E-7) + 1;
        this.cursor = new Cursor3D(var0, var2, var4, var1, var3, var5);
    }

    @Override
    public boolean tryAdvance(Consumer<? super VoxelShape> param0) {
        return this.needsBorderCheck && this.worldBorderCheck(param0) || this.collisionCheck(param0);
    }

    boolean collisionCheck(Consumer<? super VoxelShape> param0) {
        while(this.cursor.advance()) {
            int var0 = this.cursor.nextX();
            int var1 = this.cursor.nextY();
            int var2 = this.cursor.nextZ();
            int var3 = this.cursor.getNextType();
            if (var3 != 3) {
                BlockGetter var4 = this.getChunk(var0, var2);
                if (var4 != null) {
                    this.pos.set(var0, var1, var2);
                    BlockState var5 = var4.getBlockState(this.pos);
                    if ((var3 != 1 || var5.hasLargeCollisionShape()) && (var3 != 2 || var5.is(Blocks.MOVING_PISTON))) {
                        VoxelShape var6 = var5.getCollisionShape(this.collisionGetter, this.pos, this.context);
                        if (var6 == Shapes.block()) {
                            if (this.box.intersects((double)var0, (double)var1, (double)var2, (double)var0 + 1.0, (double)var1 + 1.0, (double)var2 + 1.0)) {
                                param0.accept(var6.move((double)var0, (double)var1, (double)var2));
                                return true;
                            }
                        } else {
                            VoxelShape var7 = var6.move((double)var0, (double)var1, (double)var2);
                            if (Shapes.joinIsNotEmpty(var7, this.entityShape, BooleanOp.AND)) {
                                param0.accept(var7);
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    @Nullable
    private BlockGetter getChunk(int param0, int param1) {
        int var0 = param0 >> 4;
        int var1 = param1 >> 4;
        return this.collisionGetter.getChunkForCollisions(var0, var1);
    }

    boolean worldBorderCheck(Consumer<? super VoxelShape> param0) {
        Objects.requireNonNull(this.source);
        this.needsBorderCheck = false;
        WorldBorder var0 = this.collisionGetter.getWorldBorder();
        boolean var1 = isBoxFullyWithinWorldBorder(var0, this.source.getBoundingBox().deflate(1.0E-7));
        boolean var2 = var1 && !isBoxFullyWithinWorldBorder(var0, this.source.getBoundingBox().inflate(1.0E-7));
        if (var2) {
            param0.accept(var0.getCollisionShape());
            return true;
        } else {
            return false;
        }
    }

    public static boolean isBoxFullyWithinWorldBorder(WorldBorder param0, AABB param1) {
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
