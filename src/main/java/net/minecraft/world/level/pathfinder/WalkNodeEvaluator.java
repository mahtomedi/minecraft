package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WalkNodeEvaluator extends NodeEvaluator {
    protected float oldWaterCost;

    @Override
    public void prepare(PathNavigationRegion param0, Mob param1) {
        super.prepare(param0, param1);
        this.oldWaterCost = param1.getPathfindingMalus(BlockPathTypes.WATER);
    }

    @Override
    public void done() {
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        super.done();
    }

    @Override
    public Node getStart() {
        int var0;
        if (this.canFloat() && this.mob.isInWater()) {
            var0 = Mth.floor(this.mob.getY());
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos(this.mob.getX(), (double)var0, this.mob.getZ());

            for(BlockState var2 = this.level.getBlockState(var1);
                var2.getBlock() == Blocks.WATER || var2.getFluidState() == Fluids.WATER.getSource(false);
                var2 = this.level.getBlockState(var1)
            ) {
                var1.set(this.mob.getX(), (double)(++var0), this.mob.getZ());
            }

            --var0;
        } else if (this.mob.isOnGround()) {
            var0 = Mth.floor(this.mob.getY() + 0.5);
        } else {
            BlockPos var4 = this.mob.blockPosition();

            while(
                (this.level.getBlockState(var4).isAir() || this.level.getBlockState(var4).isPathfindable(this.level, var4, PathComputationType.LAND))
                    && var4.getY() > 0
            ) {
                var4 = var4.below();
            }

            var0 = var4.above().getY();
        }

        BlockPos var6 = this.mob.blockPosition();
        BlockPathTypes var7 = this.getBlockPathType(this.mob, var6.getX(), var0, var6.getZ());
        if (this.mob.getPathfindingMalus(var7) < 0.0F) {
            Set<BlockPos> var8 = Sets.newHashSet();
            var8.add(new BlockPos(this.mob.getBoundingBox().minX, (double)var0, this.mob.getBoundingBox().minZ));
            var8.add(new BlockPos(this.mob.getBoundingBox().minX, (double)var0, this.mob.getBoundingBox().maxZ));
            var8.add(new BlockPos(this.mob.getBoundingBox().maxX, (double)var0, this.mob.getBoundingBox().minZ));
            var8.add(new BlockPos(this.mob.getBoundingBox().maxX, (double)var0, this.mob.getBoundingBox().maxZ));

            for(BlockPos var9 : var8) {
                BlockPathTypes var10 = this.getBlockPathType(this.mob, var9);
                if (this.mob.getPathfindingMalus(var10) >= 0.0F) {
                    return this.getNode(var9.getX(), var9.getY(), var9.getZ());
                }
            }
        }

        return this.getNode(var6.getX(), var0, var6.getZ());
    }

    @Override
    public Target getGoal(double param0, double param1, double param2) {
        return new Target(this.getNode(Mth.floor(param0), Mth.floor(param1), Mth.floor(param2)));
    }

    @Override
    public int getNeighbors(Node[] param0, Node param1) {
        int var0 = 0;
        int var1 = 0;
        BlockPathTypes var2 = this.getBlockPathType(this.mob, param1.x, param1.y + 1, param1.z);
        if (this.mob.getPathfindingMalus(var2) >= 0.0F) {
            BlockPathTypes var3 = this.getBlockPathType(this.mob, param1.x, param1.y, param1.z);
            if (var3 == BlockPathTypes.STICKY_HONEY) {
                var1 = 0;
            } else {
                var1 = Mth.floor(Math.max(1.0F, this.mob.maxUpStep));
            }
        }

        double var4 = getFloorLevel(this.level, new BlockPos(param1.x, param1.y, param1.z));
        Node var5 = this.getLandNode(param1.x, param1.y, param1.z + 1, var1, var4, Direction.SOUTH);
        if (var5 != null && !var5.closed && var5.costMalus >= 0.0F) {
            param0[var0++] = var5;
        }

        Node var6 = this.getLandNode(param1.x - 1, param1.y, param1.z, var1, var4, Direction.WEST);
        if (var6 != null && !var6.closed && var6.costMalus >= 0.0F) {
            param0[var0++] = var6;
        }

        Node var7 = this.getLandNode(param1.x + 1, param1.y, param1.z, var1, var4, Direction.EAST);
        if (var7 != null && !var7.closed && var7.costMalus >= 0.0F) {
            param0[var0++] = var7;
        }

        Node var8 = this.getLandNode(param1.x, param1.y, param1.z - 1, var1, var4, Direction.NORTH);
        if (var8 != null && !var8.closed && var8.costMalus >= 0.0F) {
            param0[var0++] = var8;
        }

        Node var9 = this.getLandNode(param1.x - 1, param1.y, param1.z - 1, var1, var4, Direction.NORTH);
        if (this.isDiagonalValid(param1, var6, var8, var9)) {
            param0[var0++] = var9;
        }

        Node var10 = this.getLandNode(param1.x + 1, param1.y, param1.z - 1, var1, var4, Direction.NORTH);
        if (this.isDiagonalValid(param1, var7, var8, var10)) {
            param0[var0++] = var10;
        }

        Node var11 = this.getLandNode(param1.x - 1, param1.y, param1.z + 1, var1, var4, Direction.SOUTH);
        if (this.isDiagonalValid(param1, var6, var5, var11)) {
            param0[var0++] = var11;
        }

        Node var12 = this.getLandNode(param1.x + 1, param1.y, param1.z + 1, var1, var4, Direction.SOUTH);
        if (this.isDiagonalValid(param1, var7, var5, var12)) {
            param0[var0++] = var12;
        }

        return var0;
    }

    private boolean isDiagonalValid(Node param0, @Nullable Node param1, @Nullable Node param2, @Nullable Node param3) {
        if (param3 == null || param2 == null || param1 == null) {
            return false;
        } else if (param3.closed) {
            return false;
        } else if (param2.y <= param0.y && param1.y <= param0.y) {
            return param3.costMalus >= 0.0F && (param2.y < param0.y || param2.costMalus >= 0.0F) && (param1.y < param0.y || param1.costMalus >= 0.0F);
        } else {
            return false;
        }
    }

    public static double getFloorLevel(BlockGetter param0, BlockPos param1) {
        BlockPos var0 = param1.below();
        VoxelShape var1 = param0.getBlockState(var0).getCollisionShape(param0, var0);
        return (double)var0.getY() + (var1.isEmpty() ? 0.0 : var1.max(Direction.Axis.Y));
    }

    @Nullable
    private Node getLandNode(int param0, int param1, int param2, int param3, double param4, Direction param5) {
        Node var0 = null;
        BlockPos var1 = new BlockPos(param0, param1, param2);
        double var2 = getFloorLevel(this.level, var1);
        if (var2 - param4 > 1.125) {
            return null;
        } else {
            BlockPathTypes var3 = this.getBlockPathType(this.mob, param0, param1, param2);
            float var4 = this.mob.getPathfindingMalus(var3);
            double var5 = (double)this.mob.getBbWidth() / 2.0;
            if (var4 >= 0.0F) {
                var0 = this.getNode(param0, param1, param2);
                var0.type = var3;
                var0.costMalus = Math.max(var0.costMalus, var4);
            }

            if (var3 == BlockPathTypes.WALKABLE) {
                return var0;
            } else {
                if ((var0 == null || var0.costMalus < 0.0F) && param3 > 0 && var3 != BlockPathTypes.FENCE && var3 != BlockPathTypes.TRAPDOOR) {
                    var0 = this.getLandNode(param0, param1 + 1, param2, param3 - 1, param4, param5);
                    if (var0 != null && (var0.type == BlockPathTypes.OPEN || var0.type == BlockPathTypes.WALKABLE) && this.mob.getBbWidth() < 1.0F) {
                        double var6 = (double)(param0 - param5.getStepX()) + 0.5;
                        double var7 = (double)(param2 - param5.getStepZ()) + 0.5;
                        AABB var8 = new AABB(
                            var6 - var5,
                            getFloorLevel(this.level, new BlockPos(var6, (double)(param1 + 1), var7)) + 0.001,
                            var7 - var5,
                            var6 + var5,
                            (double)this.mob.getBbHeight() + getFloorLevel(this.level, new BlockPos(var0.x, var0.y, var0.z)) - 0.002,
                            var7 + var5
                        );
                        if (!this.level.noCollision(this.mob, var8)) {
                            var0 = null;
                        }
                    }
                }

                if (var3 == BlockPathTypes.WATER && !this.canFloat()) {
                    if (this.getBlockPathType(this.mob, param0, param1 - 1, param2) != BlockPathTypes.WATER) {
                        return var0;
                    }

                    while(param1 > 0) {
                        var3 = this.getBlockPathType(this.mob, param0, --param1, param2);
                        if (var3 != BlockPathTypes.WATER) {
                            return var0;
                        }

                        var0 = this.getNode(param0, param1, param2);
                        var0.type = var3;
                        var0.costMalus = Math.max(var0.costMalus, this.mob.getPathfindingMalus(var3));
                    }
                }

                if (var3 == BlockPathTypes.OPEN) {
                    AABB var9 = new AABB(
                        (double)param0 - var5 + 0.5,
                        (double)param1 + 0.001,
                        (double)param2 - var5 + 0.5,
                        (double)param0 + var5 + 0.5,
                        (double)((float)param1 + this.mob.getBbHeight()),
                        (double)param2 + var5 + 0.5
                    );
                    if (!this.level.noCollision(this.mob, var9)) {
                        return null;
                    }

                    if (this.mob.getBbWidth() >= 1.0F) {
                        BlockPathTypes var10 = this.getBlockPathType(this.mob, param0, param1 - 1, param2);
                        if (var10 == BlockPathTypes.BLOCKED) {
                            var0 = this.getNode(param0, param1, param2);
                            var0.type = BlockPathTypes.WALKABLE;
                            var0.costMalus = Math.max(var0.costMalus, var4);
                            return var0;
                        }
                    }

                    int var11 = 0;
                    int var12 = param1;

                    while(var3 == BlockPathTypes.OPEN) {
                        if (--param1 < 0) {
                            Node var13 = this.getNode(param0, var12, param2);
                            var13.type = BlockPathTypes.BLOCKED;
                            var13.costMalus = -1.0F;
                            return var13;
                        }

                        Node var14 = this.getNode(param0, param1, param2);
                        if (var11++ >= this.mob.getMaxFallDistance()) {
                            var14.type = BlockPathTypes.BLOCKED;
                            var14.costMalus = -1.0F;
                            return var14;
                        }

                        var3 = this.getBlockPathType(this.mob, param0, param1, param2);
                        var4 = this.mob.getPathfindingMalus(var3);
                        if (var3 != BlockPathTypes.OPEN && var4 >= 0.0F) {
                            var0 = var14;
                            var14.type = var3;
                            var14.costMalus = Math.max(var14.costMalus, var4);
                            break;
                        }

                        if (var4 < 0.0F) {
                            var14.type = BlockPathTypes.BLOCKED;
                            var14.costMalus = -1.0F;
                            return var14;
                        }
                    }
                }

                return var0;
            }
        }
    }

    @Override
    public BlockPathTypes getBlockPathType(
        BlockGetter param0, int param1, int param2, int param3, Mob param4, int param5, int param6, int param7, boolean param8, boolean param9
    ) {
        EnumSet<BlockPathTypes> var0 = EnumSet.noneOf(BlockPathTypes.class);
        BlockPathTypes var1 = BlockPathTypes.BLOCKED;
        double var2 = (double)param4.getBbWidth() / 2.0;
        BlockPos var3 = param4.blockPosition();
        var1 = this.getBlockPathTypes(param0, param1, param2, param3, param5, param6, param7, param8, param9, var0, var1, var3);
        if (var0.contains(BlockPathTypes.FENCE)) {
            return BlockPathTypes.FENCE;
        } else {
            BlockPathTypes var4 = BlockPathTypes.BLOCKED;

            for(BlockPathTypes var5 : var0) {
                if (param4.getPathfindingMalus(var5) < 0.0F) {
                    return var5;
                }

                if (param4.getPathfindingMalus(var5) >= param4.getPathfindingMalus(var4)) {
                    var4 = var5;
                }
            }

            return var1 == BlockPathTypes.OPEN && param4.getPathfindingMalus(var4) == 0.0F ? BlockPathTypes.OPEN : var4;
        }
    }

    public BlockPathTypes getBlockPathTypes(
        BlockGetter param0,
        int param1,
        int param2,
        int param3,
        int param4,
        int param5,
        int param6,
        boolean param7,
        boolean param8,
        EnumSet<BlockPathTypes> param9,
        BlockPathTypes param10,
        BlockPos param11
    ) {
        for(int var0 = 0; var0 < param4; ++var0) {
            for(int var1 = 0; var1 < param5; ++var1) {
                for(int var2 = 0; var2 < param6; ++var2) {
                    int var3 = var0 + param1;
                    int var4 = var1 + param2;
                    int var5 = var2 + param3;
                    BlockPathTypes var6 = this.getBlockPathType(param0, var3, var4, var5);
                    var6 = this.evaluateBlockPathType(param0, param7, param8, param11, var6);
                    if (var0 == 0 && var1 == 0 && var2 == 0) {
                        param10 = var6;
                    }

                    param9.add(var6);
                }
            }
        }

        return param10;
    }

    protected BlockPathTypes evaluateBlockPathType(BlockGetter param0, boolean param1, boolean param2, BlockPos param3, BlockPathTypes param4) {
        if (param4 == BlockPathTypes.DOOR_WOOD_CLOSED && param1 && param2) {
            param4 = BlockPathTypes.WALKABLE;
        }

        if (param4 == BlockPathTypes.DOOR_OPEN && !param2) {
            param4 = BlockPathTypes.BLOCKED;
        }

        if (param4 == BlockPathTypes.RAIL
            && !(param0.getBlockState(param3).getBlock() instanceof BaseRailBlock)
            && !(param0.getBlockState(param3.below()).getBlock() instanceof BaseRailBlock)) {
            param4 = BlockPathTypes.FENCE;
        }

        if (param4 == BlockPathTypes.LEAVES) {
            param4 = BlockPathTypes.BLOCKED;
        }

        return param4;
    }

    private BlockPathTypes getBlockPathType(Mob param0, BlockPos param1) {
        return this.getBlockPathType(param0, param1.getX(), param1.getY(), param1.getZ());
    }

    private BlockPathTypes getBlockPathType(Mob param0, int param1, int param2, int param3) {
        return this.getBlockPathType(
            this.level, param1, param2, param3, param0, this.entityWidth, this.entityHeight, this.entityDepth, this.canOpenDoors(), this.canPassDoors()
        );
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter param0, int param1, int param2, int param3) {
        return getBlockPathTypeStatic(param0, param1, param2, param3);
    }

    public static BlockPathTypes getBlockPathTypeStatic(BlockGetter param0, int param1, int param2, int param3) {
        BlockPathTypes var0 = getBlockPathTypeRaw(param0, param1, param2, param3);
        if (var0 == BlockPathTypes.OPEN && param2 >= 1) {
            Block var1 = param0.getBlockState(new BlockPos(param1, param2 - 1, param3)).getBlock();
            BlockPathTypes var2 = getBlockPathTypeRaw(param0, param1, param2 - 1, param3);
            var0 = var2 != BlockPathTypes.WALKABLE && var2 != BlockPathTypes.OPEN && var2 != BlockPathTypes.WATER && var2 != BlockPathTypes.LAVA
                ? BlockPathTypes.WALKABLE
                : BlockPathTypes.OPEN;
            if (var2 == BlockPathTypes.DAMAGE_FIRE || var1 == Blocks.MAGMA_BLOCK || var1 == Blocks.CAMPFIRE) {
                var0 = BlockPathTypes.DAMAGE_FIRE;
            }

            if (var2 == BlockPathTypes.DAMAGE_CACTUS) {
                var0 = BlockPathTypes.DAMAGE_CACTUS;
            }

            if (var2 == BlockPathTypes.DAMAGE_OTHER) {
                var0 = BlockPathTypes.DAMAGE_OTHER;
            }

            if (var2 == BlockPathTypes.STICKY_HONEY) {
                var0 = BlockPathTypes.STICKY_HONEY;
            }
        }

        if (var0 == BlockPathTypes.WALKABLE) {
            var0 = checkNeighbourBlocks(param0, param1, param2, param3, var0);
        }

        return var0;
    }

    public static BlockPathTypes checkNeighbourBlocks(BlockGetter param0, int param1, int param2, int param3, BlockPathTypes param4) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = -1; var1 <= 1; ++var1) {
            for(int var2 = -1; var2 <= 1; ++var2) {
                for(int var3 = -1; var3 <= 1; ++var3) {
                    if (var1 != 0 || var3 != 0) {
                        Block var4 = param0.getBlockState(var0.set(var1 + param1, var2 + param2, var3 + param3)).getBlock();
                        if (var4 == Blocks.CACTUS) {
                            param4 = BlockPathTypes.DANGER_CACTUS;
                        } else if (var4.is(BlockTags.FIRE) || var4 == Blocks.LAVA) {
                            param4 = BlockPathTypes.DANGER_FIRE;
                        } else if (var4 == Blocks.SWEET_BERRY_BUSH) {
                            param4 = BlockPathTypes.DANGER_OTHER;
                        }
                    }
                }
            }
        }

        return param4;
    }

    protected static BlockPathTypes getBlockPathTypeRaw(BlockGetter param0, int param1, int param2, int param3) {
        BlockPos var0 = new BlockPos(param1, param2, param3);
        BlockState var1 = param0.getBlockState(var0);
        Block var2 = var1.getBlock();
        Material var3 = var1.getMaterial();
        if (var1.isAir()) {
            return BlockPathTypes.OPEN;
        } else if (var2.is(BlockTags.TRAPDOORS) || var2 == Blocks.LILY_PAD) {
            return BlockPathTypes.TRAPDOOR;
        } else if (var1.is(BlockTags.FIRE)) {
            return BlockPathTypes.DAMAGE_FIRE;
        } else if (var2 == Blocks.CACTUS) {
            return BlockPathTypes.DAMAGE_CACTUS;
        } else if (var2 == Blocks.SWEET_BERRY_BUSH) {
            return BlockPathTypes.DAMAGE_OTHER;
        } else if (var2 == Blocks.HONEY_BLOCK) {
            return BlockPathTypes.STICKY_HONEY;
        } else if (var2 == Blocks.COCOA) {
            return BlockPathTypes.COCOA;
        } else if (DoorBlock.isWoodenDoor(var1) && !var1.getValue(DoorBlock.OPEN)) {
            return BlockPathTypes.DOOR_WOOD_CLOSED;
        } else if (var2 instanceof DoorBlock && var3 == Material.METAL && !var1.getValue(DoorBlock.OPEN)) {
            return BlockPathTypes.DOOR_IRON_CLOSED;
        } else if (var2 instanceof DoorBlock && var1.getValue(DoorBlock.OPEN)) {
            return BlockPathTypes.DOOR_OPEN;
        } else if (var2 instanceof BaseRailBlock) {
            return BlockPathTypes.RAIL;
        } else if (var2 instanceof LeavesBlock) {
            return BlockPathTypes.LEAVES;
        } else if (!var2.is(BlockTags.FENCES) && !var2.is(BlockTags.WALLS) && (!(var2 instanceof FenceGateBlock) || var1.getValue(FenceGateBlock.OPEN))) {
            FluidState var4 = param0.getFluidState(var0);
            if (var4.is(FluidTags.WATER)) {
                return BlockPathTypes.WATER;
            } else if (var4.is(FluidTags.LAVA)) {
                return BlockPathTypes.LAVA;
            } else {
                return var1.isPathfindable(param0, var0, PathComputationType.LAND) ? BlockPathTypes.OPEN : BlockPathTypes.BLOCKED;
            }
        } else {
            return BlockPathTypes.FENCE;
        }
    }
}
