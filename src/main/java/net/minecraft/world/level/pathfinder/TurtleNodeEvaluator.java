package net.minecraft.world.level.pathfinder;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TurtleNodeEvaluator extends WalkNodeEvaluator {
    private float oldWalkableCost;
    private float oldWaterBorderCost;

    @Override
    public void prepare(PathNavigationRegion param0, Mob param1) {
        super.prepare(param0, param1);
        param1.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.oldWalkableCost = param1.getPathfindingMalus(BlockPathTypes.WALKABLE);
        param1.setPathfindingMalus(BlockPathTypes.WALKABLE, 6.0F);
        this.oldWaterBorderCost = param1.getPathfindingMalus(BlockPathTypes.WATER_BORDER);
        param1.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 4.0F);
    }

    @Override
    public void done() {
        this.mob.setPathfindingMalus(BlockPathTypes.WALKABLE, this.oldWalkableCost);
        this.mob.setPathfindingMalus(BlockPathTypes.WATER_BORDER, this.oldWaterBorderCost);
        super.done();
    }

    @Override
    public Node getStart() {
        return this.getNode(
            Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5), Mth.floor(this.mob.getBoundingBox().minZ)
        );
    }

    @Override
    public Target getGoal(double param0, double param1, double param2) {
        return new Target(this.getNode(Mth.floor(param0), Mth.floor(param1 + 0.5), Mth.floor(param2)));
    }

    @Override
    public int getNeighbors(Node[] param0, Node param1) {
        int var0 = 0;
        int var1 = 1;
        BlockPos var2 = new BlockPos(param1.x, param1.y, param1.z);
        double var3 = this.inWaterDependentPosHeight(var2);
        Node var4 = this.getAcceptedNode(param1.x, param1.y, param1.z + 1, 1, var3);
        Node var5 = this.getAcceptedNode(param1.x - 1, param1.y, param1.z, 1, var3);
        Node var6 = this.getAcceptedNode(param1.x + 1, param1.y, param1.z, 1, var3);
        Node var7 = this.getAcceptedNode(param1.x, param1.y, param1.z - 1, 1, var3);
        Node var8 = this.getAcceptedNode(param1.x, param1.y + 1, param1.z, 0, var3);
        Node var9 = this.getAcceptedNode(param1.x, param1.y - 1, param1.z, 1, var3);
        if (var4 != null && !var4.closed) {
            param0[var0++] = var4;
        }

        if (var5 != null && !var5.closed) {
            param0[var0++] = var5;
        }

        if (var6 != null && !var6.closed) {
            param0[var0++] = var6;
        }

        if (var7 != null && !var7.closed) {
            param0[var0++] = var7;
        }

        if (var8 != null && !var8.closed) {
            param0[var0++] = var8;
        }

        if (var9 != null && !var9.closed) {
            param0[var0++] = var9;
        }

        boolean var10 = var7 == null || var7.type == BlockPathTypes.OPEN || var7.costMalus != 0.0F;
        boolean var11 = var4 == null || var4.type == BlockPathTypes.OPEN || var4.costMalus != 0.0F;
        boolean var12 = var6 == null || var6.type == BlockPathTypes.OPEN || var6.costMalus != 0.0F;
        boolean var13 = var5 == null || var5.type == BlockPathTypes.OPEN || var5.costMalus != 0.0F;
        if (var10 && var13) {
            Node var14 = this.getAcceptedNode(param1.x - 1, param1.y, param1.z - 1, 1, var3);
            if (var14 != null && !var14.closed) {
                param0[var0++] = var14;
            }
        }

        if (var10 && var12) {
            Node var15 = this.getAcceptedNode(param1.x + 1, param1.y, param1.z - 1, 1, var3);
            if (var15 != null && !var15.closed) {
                param0[var0++] = var15;
            }
        }

        if (var11 && var13) {
            Node var16 = this.getAcceptedNode(param1.x - 1, param1.y, param1.z + 1, 1, var3);
            if (var16 != null && !var16.closed) {
                param0[var0++] = var16;
            }
        }

        if (var11 && var12) {
            Node var17 = this.getAcceptedNode(param1.x + 1, param1.y, param1.z + 1, 1, var3);
            if (var17 != null && !var17.closed) {
                param0[var0++] = var17;
            }
        }

        return var0;
    }

    private double inWaterDependentPosHeight(BlockPos param0) {
        if (!this.mob.isInWater()) {
            BlockPos var0 = param0.below();
            VoxelShape var1 = this.level.getBlockState(var0).getCollisionShape(this.level, var0);
            return (double)var0.getY() + (var1.isEmpty() ? 0.0 : var1.max(Direction.Axis.Y));
        } else {
            return (double)param0.getY() + 0.5;
        }
    }

    @Nullable
    private Node getAcceptedNode(int param0, int param1, int param2, int param3, double param4) {
        Node var0 = null;
        BlockPos var1 = new BlockPos(param0, param1, param2);
        double var2 = this.inWaterDependentPosHeight(var1);
        if (var2 - param4 > 1.125) {
            return null;
        } else {
            BlockPathTypes var3 = this.getBlockPathType(
                this.level, param0, param1, param2, this.mob, this.entityWidth, this.entityHeight, this.entityDepth, false, false
            );
            float var4 = this.mob.getPathfindingMalus(var3);
            double var5 = (double)this.mob.getBbWidth() / 2.0;
            if (var4 >= 0.0F) {
                var0 = this.getNode(param0, param1, param2);
                var0.type = var3;
                var0.costMalus = Math.max(var0.costMalus, var4);
            }

            if (var3 != BlockPathTypes.WATER && var3 != BlockPathTypes.WALKABLE) {
                if (var0 == null && param3 > 0 && var3 != BlockPathTypes.FENCE && var3 != BlockPathTypes.TRAPDOOR) {
                    var0 = this.getAcceptedNode(param0, param1 + 1, param2, param3 - 1, param4);
                }

                if (var3 == BlockPathTypes.OPEN) {
                    AABB var6 = new AABB(
                        (double)param0 - var5 + 0.5,
                        (double)param1 + 0.001,
                        (double)param2 - var5 + 0.5,
                        (double)param0 + var5 + 0.5,
                        (double)((float)param1 + this.mob.getBbHeight()),
                        (double)param2 + var5 + 0.5
                    );
                    if (!this.mob.level.noCollision(this.mob, var6)) {
                        return null;
                    }

                    BlockPathTypes var7 = this.getBlockPathType(
                        this.level, param0, param1 - 1, param2, this.mob, this.entityWidth, this.entityHeight, this.entityDepth, false, false
                    );
                    if (var7 == BlockPathTypes.BLOCKED) {
                        var0 = this.getNode(param0, param1, param2);
                        var0.type = BlockPathTypes.WALKABLE;
                        var0.costMalus = Math.max(var0.costMalus, var4);
                        return var0;
                    }

                    if (var7 == BlockPathTypes.WATER) {
                        var0 = this.getNode(param0, param1, param2);
                        var0.type = BlockPathTypes.WATER;
                        var0.costMalus = Math.max(var0.costMalus, var4);
                        return var0;
                    }

                    int var8 = 0;

                    while(param1 > 0 && var3 == BlockPathTypes.OPEN) {
                        --param1;
                        if (var8++ >= this.mob.getMaxFallDistance()) {
                            return null;
                        }

                        var3 = this.getBlockPathType(
                            this.level, param0, param1, param2, this.mob, this.entityWidth, this.entityHeight, this.entityDepth, false, false
                        );
                        var4 = this.mob.getPathfindingMalus(var3);
                        if (var3 != BlockPathTypes.OPEN && var4 >= 0.0F) {
                            var0 = this.getNode(param0, param1, param2);
                            var0.type = var3;
                            var0.costMalus = Math.max(var0.costMalus, var4);
                            break;
                        }

                        if (var4 < 0.0F) {
                            return null;
                        }
                    }
                }

                return var0;
            } else {
                if (param1 < this.mob.level.getSeaLevel() - 10 && var0 != null) {
                    ++var0.costMalus;
                }

                return var0;
            }
        }
    }

    @Override
    protected BlockPathTypes evaluateBlockPathType(BlockGetter param0, boolean param1, boolean param2, BlockPos param3, BlockPathTypes param4) {
        if (param4 == BlockPathTypes.RAIL
            && !(param0.getBlockState(param3).getBlock() instanceof BaseRailBlock)
            && !(param0.getBlockState(param3.below()).getBlock() instanceof BaseRailBlock)) {
            param4 = BlockPathTypes.FENCE;
        }

        if (param4 == BlockPathTypes.DOOR_OPEN || param4 == BlockPathTypes.DOOR_WOOD_CLOSED || param4 == BlockPathTypes.DOOR_IRON_CLOSED) {
            param4 = BlockPathTypes.BLOCKED;
        }

        if (param4 == BlockPathTypes.LEAVES) {
            param4 = BlockPathTypes.BLOCKED;
        }

        return param4;
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter param0, int param1, int param2, int param3) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        BlockPathTypes var1 = getBlockPathTypeRaw(param0, var0.set(param1, param2, param3));
        if (var1 == BlockPathTypes.WATER) {
            for(Direction var2 : Direction.values()) {
                BlockPathTypes var3 = getBlockPathTypeRaw(param0, var0.set(param1, param2, param3).move(var2));
                if (var3 == BlockPathTypes.BLOCKED) {
                    return BlockPathTypes.WATER_BORDER;
                }
            }

            return BlockPathTypes.WATER;
        } else {
            if (var1 == BlockPathTypes.OPEN && param2 >= 1) {
                Block var4 = param0.getBlockState(new BlockPos(param1, param2 - 1, param3)).getBlock();
                BlockPathTypes var5 = getBlockPathTypeRaw(param0, var0.set(param1, param2 - 1, param3));
                if (var5 != BlockPathTypes.WALKABLE && var5 != BlockPathTypes.OPEN && var5 != BlockPathTypes.LAVA) {
                    var1 = BlockPathTypes.WALKABLE;
                } else {
                    var1 = BlockPathTypes.OPEN;
                }

                if (var5 == BlockPathTypes.DAMAGE_FIRE || var4 == Blocks.MAGMA_BLOCK || var4.is(BlockTags.CAMPFIRES)) {
                    var1 = BlockPathTypes.DAMAGE_FIRE;
                }

                if (var5 == BlockPathTypes.DAMAGE_CACTUS) {
                    var1 = BlockPathTypes.DAMAGE_CACTUS;
                }

                if (var5 == BlockPathTypes.DAMAGE_OTHER) {
                    var1 = BlockPathTypes.DAMAGE_OTHER;
                }
            }

            if (var1 == BlockPathTypes.WALKABLE) {
                var1 = checkNeighbourBlocks(param0, var0.set(param1, param2, param3), var1);
            }

            return var1;
        }
    }
}
