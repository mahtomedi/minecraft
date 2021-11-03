package net.minecraft.world.level;

import com.google.common.collect.AbstractIterator;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockCollisions extends AbstractIterator<VoxelShape> {
    private final AABB box;
    private final CollisionContext context;
    private final Cursor3D cursor;
    private final BlockPos.MutableBlockPos pos;
    private final VoxelShape entityShape;
    private final CollisionGetter collisionGetter;
    private final boolean onlySuffocatingBlocks;
    @Nullable
    private BlockGetter cachedBlockGetter;
    private long cachedBlockGetterPos;

    public BlockCollisions(CollisionGetter param0, @Nullable Entity param1, AABB param2) {
        this(param0, param1, param2, false);
    }

    public BlockCollisions(CollisionGetter param0, @Nullable Entity param1, AABB param2, boolean param3) {
        this.context = param1 == null ? CollisionContext.empty() : CollisionContext.of(param1);
        this.pos = new BlockPos.MutableBlockPos();
        this.entityShape = Shapes.create(param2);
        this.collisionGetter = param0;
        this.box = param2;
        this.onlySuffocatingBlocks = param3;
        int var0 = Mth.floor(param2.minX - 1.0E-7) - 1;
        int var1 = Mth.floor(param2.maxX + 1.0E-7) + 1;
        int var2 = Mth.floor(param2.minY - 1.0E-7) - 1;
        int var3 = Mth.floor(param2.maxY + 1.0E-7) + 1;
        int var4 = Mth.floor(param2.minZ - 1.0E-7) - 1;
        int var5 = Mth.floor(param2.maxZ + 1.0E-7) + 1;
        this.cursor = new Cursor3D(var0, var2, var4, var1, var3, var5);
    }

    @Nullable
    private BlockGetter getChunk(int param0, int param1) {
        int var0 = SectionPos.blockToSectionCoord(param0);
        int var1 = SectionPos.blockToSectionCoord(param1);
        long var2 = ChunkPos.asLong(var0, var1);
        if (this.cachedBlockGetter != null && this.cachedBlockGetterPos == var2) {
            return this.cachedBlockGetter;
        } else {
            BlockGetter var3 = this.collisionGetter.getChunkForCollisions(var0, var1);
            this.cachedBlockGetter = var3;
            this.cachedBlockGetterPos = var2;
            return var3;
        }
    }

    protected VoxelShape computeNext() {
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
                    if ((!this.onlySuffocatingBlocks || var5.isSuffocating(var4, this.pos))
                        && (var3 != 1 || var5.hasLargeCollisionShape())
                        && (var3 != 2 || var5.is(Blocks.MOVING_PISTON))) {
                        VoxelShape var6 = var5.getCollisionShape(this.collisionGetter, this.pos, this.context);
                        if (var6 == Shapes.block()) {
                            if (this.box.intersects((double)var0, (double)var1, (double)var2, (double)var0 + 1.0, (double)var1 + 1.0, (double)var2 + 1.0)) {
                                return var6.move((double)var0, (double)var1, (double)var2);
                            }
                        } else {
                            VoxelShape var7 = var6.move((double)var0, (double)var1, (double)var2);
                            if (Shapes.joinIsNotEmpty(var7, this.entityShape, BooleanOp.AND)) {
                                return var7;
                            }
                        }
                    }
                }
            }
        }

        return this.endOfData();
    }
}
