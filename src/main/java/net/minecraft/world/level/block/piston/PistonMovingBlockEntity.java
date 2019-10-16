package net.minecraft.world.level.block.piston;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PistonMovingBlockEntity extends BlockEntity implements TickableBlockEntity {
    private BlockState movedState;
    private Direction direction;
    private boolean extending;
    private boolean isSourcePiston;
    private static final ThreadLocal<Direction> NOCLIP = ThreadLocal.withInitial(() -> null);
    private float progress;
    private float progressO;
    private long lastTicked;

    public PistonMovingBlockEntity() {
        super(BlockEntityType.PISTON);
    }

    public PistonMovingBlockEntity(BlockState param0, Direction param1, boolean param2, boolean param3) {
        this();
        this.movedState = param0;
        this.direction = param1;
        this.extending = param2;
        this.isSourcePiston = param3;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    public boolean isExtending() {
        return this.extending;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public boolean isSourcePiston() {
        return this.isSourcePiston;
    }

    public float getProgress(float param0) {
        if (param0 > 1.0F) {
            param0 = 1.0F;
        }

        return Mth.lerp(param0, this.progressO, this.progress);
    }

    @OnlyIn(Dist.CLIENT)
    public float getXOff(float param0) {
        return (float)this.direction.getStepX() * this.getExtendedProgress(this.getProgress(param0));
    }

    @OnlyIn(Dist.CLIENT)
    public float getYOff(float param0) {
        return (float)this.direction.getStepY() * this.getExtendedProgress(this.getProgress(param0));
    }

    @OnlyIn(Dist.CLIENT)
    public float getZOff(float param0) {
        return (float)this.direction.getStepZ() * this.getExtendedProgress(this.getProgress(param0));
    }

    private float getExtendedProgress(float param0) {
        return this.extending ? param0 - 1.0F : 1.0F - param0;
    }

    private BlockState getCollisionRelatedBlockState() {
        return !this.isExtending() && this.isSourcePiston() && this.movedState.getBlock() instanceof PistonBaseBlock
            ? Blocks.PISTON_HEAD
                .defaultBlockState()
                .setValue(PistonHeadBlock.TYPE, this.movedState.getBlock() == Blocks.STICKY_PISTON ? PistonType.STICKY : PistonType.DEFAULT)
                .setValue(PistonHeadBlock.FACING, this.movedState.getValue(PistonBaseBlock.FACING))
            : this.movedState;
    }

    private void moveCollidedEntities(float param0) {
        Direction var0 = this.getMovementDirection();
        double var1 = (double)(param0 - this.progress);
        VoxelShape var2 = this.getCollisionRelatedBlockState().getCollisionShape(this.level, this.getBlockPos());
        if (!var2.isEmpty()) {
            List<AABB> var3 = var2.toAabbs();
            AABB var4 = this.moveByPositionAndProgress(this.getMinMaxPiecesAABB(var3));
            List<Entity> var5 = this.level.getEntities(null, PistonMath.getMovementArea(var4, var0, var1).minmax(var4));
            if (!var5.isEmpty()) {
                boolean var6 = this.movedState.getBlock() == Blocks.SLIME_BLOCK;

                for(Entity var7 : var5) {
                    if (var7.getPistonPushReaction() != PushReaction.IGNORE) {
                        if (var6) {
                            Vec3 var8 = var7.getDeltaMovement();
                            double var9 = var8.x;
                            double var10 = var8.y;
                            double var11 = var8.z;
                            switch(var0.getAxis()) {
                                case X:
                                    var9 = (double)var0.getStepX();
                                    break;
                                case Y:
                                    var10 = (double)var0.getStepY();
                                    break;
                                case Z:
                                    var11 = (double)var0.getStepZ();
                            }

                            var7.setDeltaMovement(var9, var10, var11);
                        }

                        double var12 = 0.0;

                        for(AABB var13 : var3) {
                            AABB var14 = PistonMath.getMovementArea(this.moveByPositionAndProgress(var13), var0, var1);
                            AABB var15 = var7.getBoundingBox();
                            if (var14.intersects(var15)) {
                                var12 = Math.max(var12, getMovement(var14, var0, var15));
                                if (var12 >= var1) {
                                    break;
                                }
                            }
                        }

                        if (!(var12 <= 0.0)) {
                            var12 = Math.min(var12, var1) + 0.01;
                            moveEntityByPiston(var0, var7, var12, var0);
                            if (!this.extending && this.isSourcePiston) {
                                this.fixEntityWithinPistonBase(var7, var0, var1);
                            }
                        }
                    }
                }

            }
        }
    }

    private static void moveEntityByPiston(Direction param0, Entity param1, double param2, Direction param3) {
        NOCLIP.set(param0);
        param1.move(MoverType.PISTON, new Vec3(param2 * (double)param3.getStepX(), param2 * (double)param3.getStepY(), param2 * (double)param3.getStepZ()));
        NOCLIP.set(null);
    }

    private void moveStuckEntities(float param0) {
        if (this.isStickyForEntities()) {
            Direction var0 = this.getMovementDirection();
            if (var0.getAxis().isHorizontal()) {
                double var1 = this.movedState.getCollisionShape(this.level, this.worldPosition).max(Direction.Axis.Y);
                AABB var2 = this.moveByPositionAndProgress(new AABB(0.0, var1, 0.0, 1.0, 1.5000000999999998, 1.0));
                double var3 = (double)(param0 - this.progress);

                for(Entity var5 : this.level.getEntities((Entity)null, var2, param1 -> matchesStickyCritera(var2, param1))) {
                    moveEntityByPiston(var0, var5, var3, var0);
                }

            }
        }
    }

    private static boolean matchesStickyCritera(AABB param0, Entity param1) {
        return param1.getPistonPushReaction() == PushReaction.NORMAL
            && param1.onGround
            && param1.getX() >= param0.minX
            && param1.getX() <= param0.maxX
            && param1.getZ() >= param0.minZ
            && param1.getZ() <= param0.maxZ;
    }

    private boolean isStickyForEntities() {
        return this.movedState.getBlock() == Blocks.HONEY_BLOCK;
    }

    public Direction getMovementDirection() {
        return this.extending ? this.direction : this.direction.getOpposite();
    }

    private AABB getMinMaxPiecesAABB(List<AABB> param0) {
        double var0 = 0.0;
        double var1 = 0.0;
        double var2 = 0.0;
        double var3 = 1.0;
        double var4 = 1.0;
        double var5 = 1.0;

        for(AABB var6 : param0) {
            var0 = Math.min(var6.minX, var0);
            var1 = Math.min(var6.minY, var1);
            var2 = Math.min(var6.minZ, var2);
            var3 = Math.max(var6.maxX, var3);
            var4 = Math.max(var6.maxY, var4);
            var5 = Math.max(var6.maxZ, var5);
        }

        return new AABB(var0, var1, var2, var3, var4, var5);
    }

    private static double getMovement(AABB param0, Direction param1, AABB param2) {
        switch(param1) {
            case EAST:
                return param0.maxX - param2.minX;
            case WEST:
                return param2.maxX - param0.minX;
            case UP:
            default:
                return param0.maxY - param2.minY;
            case DOWN:
                return param2.maxY - param0.minY;
            case SOUTH:
                return param0.maxZ - param2.minZ;
            case NORTH:
                return param2.maxZ - param0.minZ;
        }
    }

    private AABB moveByPositionAndProgress(AABB param0) {
        double var0 = (double)this.getExtendedProgress(this.progress);
        return param0.move(
            (double)this.worldPosition.getX() + var0 * (double)this.direction.getStepX(),
            (double)this.worldPosition.getY() + var0 * (double)this.direction.getStepY(),
            (double)this.worldPosition.getZ() + var0 * (double)this.direction.getStepZ()
        );
    }

    private void fixEntityWithinPistonBase(Entity param0, Direction param1, double param2) {
        AABB var0 = param0.getBoundingBox();
        AABB var1 = Shapes.block().bounds().move(this.worldPosition);
        if (var0.intersects(var1)) {
            Direction var2 = param1.getOpposite();
            double var3 = getMovement(var1, var2, var0) + 0.01;
            double var4 = getMovement(var1, var2, var0.intersect(var1)) + 0.01;
            if (Math.abs(var3 - var4) < 0.01) {
                var3 = Math.min(var3, param2) + 0.01;
                moveEntityByPiston(param1, param0, var3, var2);
            }
        }

    }

    public BlockState getMovedState() {
        return this.movedState;
    }

    public void finalTick() {
        if (this.progressO < 1.0F && this.level != null) {
            this.progress = 1.0F;
            this.progressO = this.progress;
            this.level.removeBlockEntity(this.worldPosition);
            this.setRemoved();
            if (this.level.getBlockState(this.worldPosition).getBlock() == Blocks.MOVING_PISTON) {
                BlockState var0;
                if (this.isSourcePiston) {
                    var0 = Blocks.AIR.defaultBlockState();
                } else {
                    var0 = Block.updateFromNeighbourShapes(this.movedState, this.level, this.worldPosition);
                }

                this.level.setBlock(this.worldPosition, var0, 3);
                this.level.neighborChanged(this.worldPosition, var0.getBlock(), this.worldPosition);
            }
        }

    }

    @Override
    public void tick() {
        this.lastTicked = this.level.getGameTime();
        this.progressO = this.progress;
        if (this.progressO >= 1.0F) {
            this.level.removeBlockEntity(this.worldPosition);
            this.setRemoved();
            if (this.movedState != null && this.level.getBlockState(this.worldPosition).getBlock() == Blocks.MOVING_PISTON) {
                BlockState var0 = Block.updateFromNeighbourShapes(this.movedState, this.level, this.worldPosition);
                if (var0.isAir()) {
                    this.level.setBlock(this.worldPosition, this.movedState, 84);
                    Block.updateOrDestroy(this.movedState, var0, this.level, this.worldPosition, 3);
                } else {
                    if (var0.hasProperty(BlockStateProperties.WATERLOGGED) && var0.getValue(BlockStateProperties.WATERLOGGED)) {
                        var0 = var0.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false));
                    }

                    this.level.setBlock(this.worldPosition, var0, 67);
                    this.level.neighborChanged(this.worldPosition, var0.getBlock(), this.worldPosition);
                }
            }

        } else {
            float var1 = this.progress + 0.5F;
            this.moveCollidedEntities(var1);
            this.moveStuckEntities(var1);
            this.progress = var1;
            if (this.progress >= 1.0F) {
                this.progress = 1.0F;
            }

        }
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.movedState = NbtUtils.readBlockState(param0.getCompound("blockState"));
        this.direction = Direction.from3DDataValue(param0.getInt("facing"));
        this.progress = param0.getFloat("progress");
        this.progressO = this.progress;
        this.extending = param0.getBoolean("extending");
        this.isSourcePiston = param0.getBoolean("source");
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        param0.put("blockState", NbtUtils.writeBlockState(this.movedState));
        param0.putInt("facing", this.direction.get3DDataValue());
        param0.putFloat("progress", this.progressO);
        param0.putBoolean("extending", this.extending);
        param0.putBoolean("source", this.isSourcePiston);
        return param0;
    }

    public VoxelShape getCollisionShape(BlockGetter param0, BlockPos param1) {
        VoxelShape var0;
        if (!this.extending && this.isSourcePiston) {
            var0 = this.movedState.setValue(PistonBaseBlock.EXTENDED, Boolean.valueOf(true)).getCollisionShape(param0, param1);
        } else {
            var0 = Shapes.empty();
        }

        Direction var2 = NOCLIP.get();
        if ((double)this.progress < 1.0 && var2 == this.getMovementDirection()) {
            return var0;
        } else {
            BlockState var3;
            if (this.isSourcePiston()) {
                var3 = Blocks.PISTON_HEAD
                    .defaultBlockState()
                    .setValue(PistonHeadBlock.FACING, this.direction)
                    .setValue(PistonHeadBlock.SHORT, Boolean.valueOf(this.extending != 1.0F - this.progress < 4.0F));
            } else {
                var3 = this.movedState;
            }

            float var5 = this.getExtendedProgress(this.progress);
            double var6 = (double)((float)this.direction.getStepX() * var5);
            double var7 = (double)((float)this.direction.getStepY() * var5);
            double var8 = (double)((float)this.direction.getStepZ() * var5);
            return Shapes.or(var0, var3.getCollisionShape(param0, param1).move(var6, var7, var8));
        }
    }

    public long getLastTicked() {
        return this.lastTicked;
    }
}
