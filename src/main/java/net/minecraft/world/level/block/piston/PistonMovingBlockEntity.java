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
            List<Entity> var5 = this.level.getEntities(null, this.getMovementArea(var4, var0, var1).minmax(var4));
            if (!var5.isEmpty()) {
                boolean var6 = this.movedState.getBlock() == Blocks.SLIME_BLOCK;

                for(int var7 = 0; var7 < var5.size(); ++var7) {
                    Entity var8 = var5.get(var7);
                    if (var8.getPistonPushReaction() != PushReaction.IGNORE) {
                        if (var6) {
                            Vec3 var9 = var8.getDeltaMovement();
                            double var10 = var9.x;
                            double var11 = var9.y;
                            double var12 = var9.z;
                            switch(var0.getAxis()) {
                                case X:
                                    var10 = (double)var0.getStepX();
                                    break;
                                case Y:
                                    var11 = (double)var0.getStepY();
                                    break;
                                case Z:
                                    var12 = (double)var0.getStepZ();
                            }

                            var8.setDeltaMovement(var10, var11, var12);
                        }

                        double var13 = 0.0;

                        for(int var14 = 0; var14 < var3.size(); ++var14) {
                            AABB var15 = this.getMovementArea(this.moveByPositionAndProgress(var3.get(var14)), var0, var1);
                            AABB var16 = var8.getBoundingBox();
                            if (var15.intersects(var16)) {
                                var13 = Math.max(var13, this.getMovement(var15, var0, var16));
                                if (var13 >= var1) {
                                    break;
                                }
                            }
                        }

                        if (!(var13 <= 0.0)) {
                            var13 = Math.min(var13, var1) + 0.01;
                            NOCLIP.set(var0);
                            var8.move(
                                MoverType.PISTON, new Vec3(var13 * (double)var0.getStepX(), var13 * (double)var0.getStepY(), var13 * (double)var0.getStepZ())
                            );
                            NOCLIP.set(null);
                            if (!this.extending && this.isSourcePiston) {
                                this.fixEntityWithinPistonBase(var8, var0, var1);
                            }
                        }
                    }
                }

            }
        }
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

    private double getMovement(AABB param0, Direction param1, AABB param2) {
        switch(param1.getAxis()) {
            case X:
                return getDeltaX(param0, param1, param2);
            case Y:
            default:
                return getDeltaY(param0, param1, param2);
            case Z:
                return getDeltaZ(param0, param1, param2);
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

    private AABB getMovementArea(AABB param0, Direction param1, double param2) {
        double var0 = param2 * (double)param1.getAxisDirection().getStep();
        double var1 = Math.min(var0, 0.0);
        double var2 = Math.max(var0, 0.0);
        switch(param1) {
            case WEST:
                return new AABB(param0.minX + var1, param0.minY, param0.minZ, param0.minX + var2, param0.maxY, param0.maxZ);
            case EAST:
                return new AABB(param0.maxX + var1, param0.minY, param0.minZ, param0.maxX + var2, param0.maxY, param0.maxZ);
            case DOWN:
                return new AABB(param0.minX, param0.minY + var1, param0.minZ, param0.maxX, param0.minY + var2, param0.maxZ);
            case UP:
            default:
                return new AABB(param0.minX, param0.maxY + var1, param0.minZ, param0.maxX, param0.maxY + var2, param0.maxZ);
            case NORTH:
                return new AABB(param0.minX, param0.minY, param0.minZ + var1, param0.maxX, param0.maxY, param0.minZ + var2);
            case SOUTH:
                return new AABB(param0.minX, param0.minY, param0.maxZ + var1, param0.maxX, param0.maxY, param0.maxZ + var2);
        }
    }

    private void fixEntityWithinPistonBase(Entity param0, Direction param1, double param2) {
        AABB var0 = param0.getBoundingBox();
        AABB var1 = Shapes.block().bounds().move(this.worldPosition);
        if (var0.intersects(var1)) {
            Direction var2 = param1.getOpposite();
            double var3 = this.getMovement(var1, var2, var0) + 0.01;
            double var4 = this.getMovement(var1, var2, var0.intersect(var1)) + 0.01;
            if (Math.abs(var3 - var4) < 0.01) {
                var3 = Math.min(var3, param2) + 0.01;
                NOCLIP.set(param1);
                param0.move(MoverType.PISTON, new Vec3(var3 * (double)var2.getStepX(), var3 * (double)var2.getStepY(), var3 * (double)var2.getStepZ()));
                NOCLIP.set(null);
            }
        }

    }

    private static double getDeltaX(AABB param0, Direction param1, AABB param2) {
        return param1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? param0.maxX - param2.minX : param2.maxX - param0.minX;
    }

    private static double getDeltaY(AABB param0, Direction param1, AABB param2) {
        return param1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? param0.maxY - param2.minY : param2.maxY - param0.minY;
    }

    private static double getDeltaZ(AABB param0, Direction param1, AABB param2) {
        return param1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? param0.maxZ - param2.minZ : param2.maxZ - param0.minZ;
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
