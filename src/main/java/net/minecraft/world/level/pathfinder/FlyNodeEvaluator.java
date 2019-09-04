package net.minecraft.world.level.pathfinder;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class FlyNodeEvaluator extends WalkNodeEvaluator {
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
            var0 = Mth.floor(this.mob.getBoundingBox().minY);
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos(this.mob.x, (double)var0, this.mob.z);

            for(Block var2 = this.level.getBlockState(var1).getBlock(); var2 == Blocks.WATER; var2 = this.level.getBlockState(var1).getBlock()) {
                var1.set(this.mob.x, (double)(++var0), this.mob.z);
            }
        } else {
            var0 = Mth.floor(this.mob.getBoundingBox().minY + 0.5);
        }

        BlockPos var4 = new BlockPos(this.mob);
        BlockPathTypes var5 = this.getBlockPathType(this.mob, var4.getX(), var0, var4.getZ());
        if (this.mob.getPathfindingMalus(var5) < 0.0F) {
            Set<BlockPos> var6 = Sets.newHashSet();
            var6.add(new BlockPos(this.mob.getBoundingBox().minX, (double)var0, this.mob.getBoundingBox().minZ));
            var6.add(new BlockPos(this.mob.getBoundingBox().minX, (double)var0, this.mob.getBoundingBox().maxZ));
            var6.add(new BlockPos(this.mob.getBoundingBox().maxX, (double)var0, this.mob.getBoundingBox().minZ));
            var6.add(new BlockPos(this.mob.getBoundingBox().maxX, (double)var0, this.mob.getBoundingBox().maxZ));

            for(BlockPos var7 : var6) {
                BlockPathTypes var8 = this.getBlockPathType(this.mob, var7);
                if (this.mob.getPathfindingMalus(var8) >= 0.0F) {
                    return super.getNode(var7.getX(), var7.getY(), var7.getZ());
                }
            }
        }

        return super.getNode(var4.getX(), var0, var4.getZ());
    }

    @Override
    public Target getGoal(double param0, double param1, double param2) {
        return new Target(super.getNode(Mth.floor(param0), Mth.floor(param1), Mth.floor(param2)));
    }

    @Override
    public int getNeighbors(Node[] param0, Node param1) {
        int var0 = 0;
        Node var1 = this.getNode(param1.x, param1.y, param1.z + 1);
        if (var1 != null && !var1.closed) {
            param0[var0++] = var1;
        }

        Node var2 = this.getNode(param1.x - 1, param1.y, param1.z);
        if (var2 != null && !var2.closed) {
            param0[var0++] = var2;
        }

        Node var3 = this.getNode(param1.x + 1, param1.y, param1.z);
        if (var3 != null && !var3.closed) {
            param0[var0++] = var3;
        }

        Node var4 = this.getNode(param1.x, param1.y, param1.z - 1);
        if (var4 != null && !var4.closed) {
            param0[var0++] = var4;
        }

        Node var5 = this.getNode(param1.x, param1.y + 1, param1.z);
        if (var5 != null && !var5.closed) {
            param0[var0++] = var5;
        }

        Node var6 = this.getNode(param1.x, param1.y - 1, param1.z);
        if (var6 != null && !var6.closed) {
            param0[var0++] = var6;
        }

        Node var7 = this.getNode(param1.x + 1, param1.y, param1.z - 1);
        if (var7 != null && !var7.closed && var4 != null && var4.costMalus >= 0.0F && var3 != null && var3.costMalus >= 0.0F) {
            param0[var0++] = var7;
        }

        Node var8 = this.getNode(param1.x + 1, param1.y, param1.z + 1);
        if (var8 != null && !var8.closed && var1 != null && var1.costMalus >= 0.0F && var3 != null && var3.costMalus >= 0.0F) {
            param0[var0++] = var8;
        }

        Node var9 = this.getNode(param1.x - 1, param1.y, param1.z - 1);
        if (var9 != null && !var9.closed && var4 != null && var4.costMalus >= 0.0F && var2 != null && var2.costMalus >= 0.0F) {
            param0[var0++] = var9;
        }

        Node var10 = this.getNode(param1.x - 1, param1.y, param1.z + 1);
        if (var10 != null && !var10.closed && var1 != null && var1.costMalus >= 0.0F && var2 != null && var2.costMalus >= 0.0F) {
            param0[var0++] = var10;
        }

        Node var11 = this.getNode(param1.x + 1, param1.y + 1, param1.z - 1);
        if (var11 != null && !var11.closed && var7 != null && var7.costMalus >= 0.0F && var5 != null && var5.costMalus >= 0.0F) {
            param0[var0++] = var11;
        }

        Node var12 = this.getNode(param1.x + 1, param1.y + 1, param1.z + 1);
        if (var12 != null && !var12.closed && var8 != null && var8.costMalus >= 0.0F && var5 != null && var5.costMalus >= 0.0F) {
            param0[var0++] = var12;
        }

        Node var13 = this.getNode(param1.x - 1, param1.y + 1, param1.z - 1);
        if (var13 != null && !var13.closed && var9 != null && var9.costMalus >= 0.0F && var5 != null && var5.costMalus >= 0.0F) {
            param0[var0++] = var13;
        }

        Node var14 = this.getNode(param1.x - 1, param1.y + 1, param1.z + 1);
        if (var14 != null && !var14.closed && var10 != null && var10.costMalus >= 0.0F && var5 != null && var5.costMalus >= 0.0F) {
            param0[var0++] = var14;
        }

        Node var15 = this.getNode(param1.x + 1, param1.y - 1, param1.z - 1);
        if (var15 != null && !var15.closed && var7 != null && var7.costMalus >= 0.0F && var6 != null && var6.costMalus >= 0.0F) {
            param0[var0++] = var15;
        }

        Node var16 = this.getNode(param1.x + 1, param1.y - 1, param1.z + 1);
        if (var16 != null && !var16.closed && var8 != null && var8.costMalus >= 0.0F && var6 != null && var6.costMalus >= 0.0F) {
            param0[var0++] = var16;
        }

        Node var17 = this.getNode(param1.x - 1, param1.y - 1, param1.z - 1);
        if (var17 != null && !var17.closed && var9 != null && var9.costMalus >= 0.0F && var6 != null && var6.costMalus >= 0.0F) {
            param0[var0++] = var17;
        }

        Node var18 = this.getNode(param1.x - 1, param1.y - 1, param1.z + 1);
        if (var18 != null && !var18.closed && var10 != null && var10.costMalus >= 0.0F && var6 != null && var6.costMalus >= 0.0F) {
            param0[var0++] = var18;
        }

        return var0;
    }

    @Nullable
    @Override
    protected Node getNode(int param0, int param1, int param2) {
        Node var0 = null;
        BlockPathTypes var1 = this.getBlockPathType(this.mob, param0, param1, param2);
        float var2 = this.mob.getPathfindingMalus(var1);
        if (var2 >= 0.0F) {
            var0 = super.getNode(param0, param1, param2);
            var0.type = var1;
            var0.costMalus = Math.max(var0.costMalus, var2);
            if (var1 == BlockPathTypes.WALKABLE) {
                ++var0.costMalus;
            }
        }

        return var1 != BlockPathTypes.OPEN && var1 != BlockPathTypes.WALKABLE ? var0 : var0;
    }

    @Override
    public BlockPathTypes getBlockPathType(
        BlockGetter param0, int param1, int param2, int param3, Mob param4, int param5, int param6, int param7, boolean param8, boolean param9
    ) {
        EnumSet<BlockPathTypes> var0 = EnumSet.noneOf(BlockPathTypes.class);
        BlockPathTypes var1 = BlockPathTypes.BLOCKED;
        BlockPos var2 = new BlockPos(param4);
        var1 = this.getBlockPathTypes(param0, param1, param2, param3, param5, param6, param7, param8, param9, var0, var1, var2);
        if (var0.contains(BlockPathTypes.FENCE)) {
            return BlockPathTypes.FENCE;
        } else {
            BlockPathTypes var3 = BlockPathTypes.BLOCKED;

            for(BlockPathTypes var4 : var0) {
                if (param4.getPathfindingMalus(var4) < 0.0F) {
                    return var4;
                }

                if (param4.getPathfindingMalus(var4) >= param4.getPathfindingMalus(var3)) {
                    var3 = var4;
                }
            }

            return var1 == BlockPathTypes.OPEN && param4.getPathfindingMalus(var3) == 0.0F ? BlockPathTypes.OPEN : var3;
        }
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter param0, int param1, int param2, int param3) {
        BlockPathTypes var0 = this.getBlockPathTypeRaw(param0, param1, param2, param3);
        if (var0 == BlockPathTypes.OPEN && param2 >= 1) {
            Block var1 = param0.getBlockState(new BlockPos(param1, param2 - 1, param3)).getBlock();
            BlockPathTypes var2 = this.getBlockPathTypeRaw(param0, param1, param2 - 1, param3);
            if (var2 == BlockPathTypes.DAMAGE_FIRE || var1 == Blocks.MAGMA_BLOCK || var2 == BlockPathTypes.LAVA || var1 == Blocks.CAMPFIRE) {
                var0 = BlockPathTypes.DAMAGE_FIRE;
            } else if (var2 == BlockPathTypes.DAMAGE_CACTUS) {
                var0 = BlockPathTypes.DAMAGE_CACTUS;
            } else if (var2 == BlockPathTypes.DAMAGE_OTHER) {
                var0 = BlockPathTypes.DAMAGE_OTHER;
            } else {
                var0 = var2 != BlockPathTypes.WALKABLE && var2 != BlockPathTypes.OPEN && var2 != BlockPathTypes.WATER
                    ? BlockPathTypes.WALKABLE
                    : BlockPathTypes.OPEN;
            }
        }

        return this.checkNeighbourBlocks(param0, param1, param2, param3, var0);
    }

    private BlockPathTypes getBlockPathType(Mob param0, BlockPos param1) {
        return this.getBlockPathType(param0, param1.getX(), param1.getY(), param1.getZ());
    }

    private BlockPathTypes getBlockPathType(Mob param0, int param1, int param2, int param3) {
        return this.getBlockPathType(
            this.level, param1, param2, param3, param0, this.entityWidth, this.entityHeight, this.entityDepth, this.canOpenDoors(), this.canPassDoors()
        );
    }
}
