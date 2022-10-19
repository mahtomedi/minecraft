package net.minecraft.world.level.block.piston;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonMovingBlockEntity extends BlockEntity {
    private static final int TICKS_TO_EXTEND = 2;
    private static final double PUSH_OFFSET = 0.01;
    public static final double TICK_MOVEMENT = 0.51;
    private BlockState movedState = Blocks.AIR.defaultBlockState();
    private Direction direction;
    private boolean extending;
    private boolean isSourcePiston;
    private static final ThreadLocal<Direction> NOCLIP = ThreadLocal.withInitial(() -> null);
    private float progress;
    private float progressO;
    private long lastTicked;
    private int deathTicks;

    public PistonMovingBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.PISTON, param0, param1);
    }

    public PistonMovingBlockEntity(BlockPos param0, BlockState param1, BlockState param2, Direction param3, boolean param4, boolean param5) {
        this(param0, param1);
        this.movedState = param2;
        this.direction = param3;
        this.extending = param4;
        this.isSourcePiston = param5;
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
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

    public float getXOff(float param0) {
        return (float)this.direction.getStepX() * this.getExtendedProgress(this.getProgress(param0));
    }

    public float getYOff(float param0) {
        return (float)this.direction.getStepY() * this.getExtendedProgress(this.getProgress(param0));
    }

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
                .setValue(PistonHeadBlock.SHORT, Boolean.valueOf(this.progress > 0.25F))
                .setValue(PistonHeadBlock.TYPE, this.movedState.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT)
                .setValue(PistonHeadBlock.FACING, this.movedState.getValue(PistonBaseBlock.FACING))
            : this.movedState;
    }

    private static void moveCollidedEntities(Level param0, BlockPos param1, float param2, PistonMovingBlockEntity param3) {
        Direction var0 = param3.getMovementDirection();
        double var1 = (double)(param2 - param3.progress);
        VoxelShape var2 = param3.getCollisionRelatedBlockState().getCollisionShape(param0, param1);
        if (!var2.isEmpty()) {
            AABB var3 = moveByPositionAndProgress(param1, var2.bounds(), param3);
            List<Entity> var4 = param0.getEntities(null, PistonMath.getMovementArea(var3, var0, var1).minmax(var3));
            if (!var4.isEmpty()) {
                List<AABB> var5 = var2.toAabbs();
                boolean var6 = param3.movedState.is(Blocks.SLIME_BLOCK);
                Iterator var12 = var4.iterator();

                while(true) {
                    Entity var7;
                    while(true) {
                        if (!var12.hasNext()) {
                            return;
                        }

                        var7 = (Entity)var12.next();
                        if (var7.getPistonPushReaction() != PushReaction.IGNORE) {
                            if (!var6) {
                                break;
                            }

                            if (!(var7 instanceof ServerPlayer)) {
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
                                break;
                            }
                        }
                    }

                    double var12x = 0.0;

                    for(AABB var13 : var5) {
                        AABB var14 = PistonMath.getMovementArea(moveByPositionAndProgress(param1, var13, param3), var0, var1);
                        AABB var15 = var7.getBoundingBox();
                        if (var14.intersects(var15)) {
                            var12x = Math.max(var12x, getMovement(var14, var0, var15));
                            if (var12x >= var1) {
                                break;
                            }
                        }
                    }

                    if (!(var12x <= 0.0)) {
                        var12x = Math.min(var12x, var1) + 0.01;
                        moveEntityByPiston(var0, var7, var12x, var0);
                        if (!param3.extending && param3.isSourcePiston) {
                            fixEntityWithinPistonBase(param1, var7, var0, var1);
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

    private static void moveStuckEntities(Level param0, BlockPos param1, float param2, PistonMovingBlockEntity param3) {
        if (param3.isStickyForEntities()) {
            Direction var0 = param3.getMovementDirection();
            if (var0.getAxis().isHorizontal()) {
                double var1 = param3.movedState.getCollisionShape(param0, param1).max(Direction.Axis.Y);
                AABB var2 = moveByPositionAndProgress(param1, new AABB(0.0, var1, 0.0, 1.0, 1.5000000999999998, 1.0), param3);
                double var3 = (double)(param2 - param3.progress);

                for(Entity var5 : param0.getEntities((Entity)null, var2, param1x -> matchesStickyCritera(var2, param1x))) {
                    moveEntityByPiston(var0, var5, var3, var0);
                }

            }
        }
    }

    private static boolean matchesStickyCritera(AABB param0, Entity param1) {
        return param1.getPistonPushReaction() == PushReaction.NORMAL
            && param1.isOnGround()
            && param1.getX() >= param0.minX
            && param1.getX() <= param0.maxX
            && param1.getZ() >= param0.minZ
            && param1.getZ() <= param0.maxZ;
    }

    private boolean isStickyForEntities() {
        return this.movedState.is(Blocks.HONEY_BLOCK);
    }

    public Direction getMovementDirection() {
        return this.extending ? this.direction : this.direction.getOpposite();
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

    private static AABB moveByPositionAndProgress(BlockPos param0, AABB param1, PistonMovingBlockEntity param2) {
        double var0 = (double)param2.getExtendedProgress(param2.progress);
        return param1.move(
            (double)param0.getX() + var0 * (double)param2.direction.getStepX(),
            (double)param0.getY() + var0 * (double)param2.direction.getStepY(),
            (double)param0.getZ() + var0 * (double)param2.direction.getStepZ()
        );
    }

    private static void fixEntityWithinPistonBase(BlockPos param0, Entity param1, Direction param2, double param3) {
        AABB var0 = param1.getBoundingBox();
        AABB var1 = Shapes.block().bounds().move(param0);
        if (var0.intersects(var1)) {
            Direction var2 = param2.getOpposite();
            double var3 = getMovement(var1, var2, var0) + 0.01;
            double var4 = getMovement(var1, var2, var0.intersect(var1)) + 0.01;
            if (Math.abs(var3 - var4) < 0.01) {
                var3 = Math.min(var3, param3) + 0.01;
                moveEntityByPiston(param2, param1, var3, var2);
            }
        }

    }

    public BlockState getMovedState() {
        return this.movedState;
    }

    public void finalTick() {
        if (this.level != null && (this.progressO < 1.0F || this.level.isClientSide)) {
            this.progress = 1.0F;
            this.progressO = this.progress;
            this.level.removeBlockEntity(this.worldPosition);
            this.setRemoved();
            if (this.level.getBlockState(this.worldPosition).is(Blocks.MOVING_PISTON)) {
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

    public static void tick(Level param0, BlockPos param1, BlockState param2, PistonMovingBlockEntity param3) {
        param3.lastTicked = param0.getGameTime();
        param3.progressO = param3.progress;
        if (param3.progressO >= 1.0F) {
            if (param0.isClientSide && param3.deathTicks < 5) {
                ++param3.deathTicks;
            } else {
                param0.removeBlockEntity(param1);
                param3.setRemoved();
                if (param0.getBlockState(param1).is(Blocks.MOVING_PISTON)) {
                    BlockState var0 = Block.updateFromNeighbourShapes(param3.movedState, param0, param1);
                    if (var0.isAir()) {
                        param0.setBlock(param1, param3.movedState, 84);
                        Block.updateOrDestroy(param3.movedState, var0, param0, param1, 3);
                    } else {
                        if (var0.hasProperty(BlockStateProperties.WATERLOGGED) && var0.getValue(BlockStateProperties.WATERLOGGED)) {
                            var0 = var0.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(false));
                        }

                        param0.setBlock(param1, var0, 67);
                        param0.neighborChanged(param1, var0.getBlock(), param1);
                    }
                }

            }
        } else {
            float var1 = param3.progress + 0.5F;
            moveCollidedEntities(param0, param1, var1, param3);
            moveStuckEntities(param0, param1, var1, param3);
            param3.progress = var1;
            if (param3.progress >= 1.0F) {
                param3.progress = 1.0F;
            }

        }
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        HolderLookup<Block> var0 = (HolderLookup<Block>)(this.level != null
            ? this.level.holderLookup(Registry.BLOCK_REGISTRY)
            : HolderLookup.forRegistry(Registry.BLOCK));
        this.movedState = NbtUtils.readBlockState(var0, param0.getCompound("blockState"));
        this.direction = Direction.from3DDataValue(param0.getInt("facing"));
        this.progress = param0.getFloat("progress");
        this.progressO = this.progress;
        this.extending = param0.getBoolean("extending");
        this.isSourcePiston = param0.getBoolean("source");
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        param0.put("blockState", NbtUtils.writeBlockState(this.movedState));
        param0.putInt("facing", this.direction.get3DDataValue());
        param0.putFloat("progress", this.progressO);
        param0.putBoolean("extending", this.extending);
        param0.putBoolean("source", this.isSourcePiston);
    }

    public VoxelShape getCollisionShape(BlockGetter param0, BlockPos param1) {
        VoxelShape var0;
        if (!this.extending && this.isSourcePiston && this.movedState.getBlock() instanceof PistonBaseBlock) {
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
                    .setValue(PistonHeadBlock.SHORT, Boolean.valueOf(this.extending != 1.0F - this.progress < 0.25F));
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

    @Override
    public void setLevel(Level param0) {
        super.setLevel(param0);
        if (param0.holderLookup(Registry.BLOCK_REGISTRY).get(this.movedState.getBlock().builtInRegistryHolder().key()).isEmpty()) {
            this.movedState = Blocks.AIR.defaultBlockState();
        }

    }
}
