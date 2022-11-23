package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class FlyNodeEvaluator extends WalkNodeEvaluator {
    private final Long2ObjectMap<BlockPathTypes> pathTypeByPosCache = new Long2ObjectOpenHashMap<>();
    private static final float SMALL_MOB_INFLATED_START_NODE_BOUNDING_BOX = 1.5F;
    private static final int MAX_START_NODE_CANDIDATES = 10;

    @Override
    public void prepare(PathNavigationRegion param0, Mob param1) {
        super.prepare(param0, param1);
        this.pathTypeByPosCache.clear();
        this.oldWaterCost = param1.getPathfindingMalus(BlockPathTypes.WATER);
    }

    @Override
    public void done() {
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        this.pathTypeByPosCache.clear();
        super.done();
    }

    @Override
    public Node getStart() {
        int var0;
        if (this.canFloat() && this.mob.isInWater()) {
            var0 = this.mob.getBlockY();
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos(this.mob.getX(), (double)var0, this.mob.getZ());

            for(BlockState var2 = this.level.getBlockState(var1); var2.is(Blocks.WATER); var2 = this.level.getBlockState(var1)) {
                var1.set(this.mob.getX(), (double)(++var0), this.mob.getZ());
            }
        } else {
            var0 = Mth.floor(this.mob.getY() + 0.5);
        }

        BlockPos var4 = new BlockPos(this.mob.getX(), (double)var0, this.mob.getZ());
        if (!this.canStartAt(var4)) {
            for(BlockPos var5 : this.iteratePathfindingStartNodeCandidatePositions(this.mob)) {
                if (this.canStartAt(var5)) {
                    return super.getStartNode(var5);
                }
            }
        }

        return super.getStartNode(var4);
    }

    @Override
    protected boolean canStartAt(BlockPos param0) {
        BlockPathTypes var0 = this.getBlockPathType(this.mob, param0);
        return this.mob.getPathfindingMalus(var0) >= 0.0F;
    }

    @Override
    public Target getGoal(double param0, double param1, double param2) {
        return this.getTargetFromNode(this.getNode(Mth.floor(param0), Mth.floor(param1), Mth.floor(param2)));
    }

    @Override
    public int getNeighbors(Node[] param0, Node param1) {
        int var0 = 0;
        Node var1 = this.findAcceptedNode(param1.x, param1.y, param1.z + 1);
        if (this.isOpen(var1)) {
            param0[var0++] = var1;
        }

        Node var2 = this.findAcceptedNode(param1.x - 1, param1.y, param1.z);
        if (this.isOpen(var2)) {
            param0[var0++] = var2;
        }

        Node var3 = this.findAcceptedNode(param1.x + 1, param1.y, param1.z);
        if (this.isOpen(var3)) {
            param0[var0++] = var3;
        }

        Node var4 = this.findAcceptedNode(param1.x, param1.y, param1.z - 1);
        if (this.isOpen(var4)) {
            param0[var0++] = var4;
        }

        Node var5 = this.findAcceptedNode(param1.x, param1.y + 1, param1.z);
        if (this.isOpen(var5)) {
            param0[var0++] = var5;
        }

        Node var6 = this.findAcceptedNode(param1.x, param1.y - 1, param1.z);
        if (this.isOpen(var6)) {
            param0[var0++] = var6;
        }

        Node var7 = this.findAcceptedNode(param1.x, param1.y + 1, param1.z + 1);
        if (this.isOpen(var7) && this.hasMalus(var1) && this.hasMalus(var5)) {
            param0[var0++] = var7;
        }

        Node var8 = this.findAcceptedNode(param1.x - 1, param1.y + 1, param1.z);
        if (this.isOpen(var8) && this.hasMalus(var2) && this.hasMalus(var5)) {
            param0[var0++] = var8;
        }

        Node var9 = this.findAcceptedNode(param1.x + 1, param1.y + 1, param1.z);
        if (this.isOpen(var9) && this.hasMalus(var3) && this.hasMalus(var5)) {
            param0[var0++] = var9;
        }

        Node var10 = this.findAcceptedNode(param1.x, param1.y + 1, param1.z - 1);
        if (this.isOpen(var10) && this.hasMalus(var4) && this.hasMalus(var5)) {
            param0[var0++] = var10;
        }

        Node var11 = this.findAcceptedNode(param1.x, param1.y - 1, param1.z + 1);
        if (this.isOpen(var11) && this.hasMalus(var1) && this.hasMalus(var6)) {
            param0[var0++] = var11;
        }

        Node var12 = this.findAcceptedNode(param1.x - 1, param1.y - 1, param1.z);
        if (this.isOpen(var12) && this.hasMalus(var2) && this.hasMalus(var6)) {
            param0[var0++] = var12;
        }

        Node var13 = this.findAcceptedNode(param1.x + 1, param1.y - 1, param1.z);
        if (this.isOpen(var13) && this.hasMalus(var3) && this.hasMalus(var6)) {
            param0[var0++] = var13;
        }

        Node var14 = this.findAcceptedNode(param1.x, param1.y - 1, param1.z - 1);
        if (this.isOpen(var14) && this.hasMalus(var4) && this.hasMalus(var6)) {
            param0[var0++] = var14;
        }

        Node var15 = this.findAcceptedNode(param1.x + 1, param1.y, param1.z - 1);
        if (this.isOpen(var15) && this.hasMalus(var4) && this.hasMalus(var3)) {
            param0[var0++] = var15;
        }

        Node var16 = this.findAcceptedNode(param1.x + 1, param1.y, param1.z + 1);
        if (this.isOpen(var16) && this.hasMalus(var1) && this.hasMalus(var3)) {
            param0[var0++] = var16;
        }

        Node var17 = this.findAcceptedNode(param1.x - 1, param1.y, param1.z - 1);
        if (this.isOpen(var17) && this.hasMalus(var4) && this.hasMalus(var2)) {
            param0[var0++] = var17;
        }

        Node var18 = this.findAcceptedNode(param1.x - 1, param1.y, param1.z + 1);
        if (this.isOpen(var18) && this.hasMalus(var1) && this.hasMalus(var2)) {
            param0[var0++] = var18;
        }

        Node var19 = this.findAcceptedNode(param1.x + 1, param1.y + 1, param1.z - 1);
        if (this.isOpen(var19)
            && this.hasMalus(var15)
            && this.hasMalus(var4)
            && this.hasMalus(var3)
            && this.hasMalus(var5)
            && this.hasMalus(var10)
            && this.hasMalus(var9)) {
            param0[var0++] = var19;
        }

        Node var20 = this.findAcceptedNode(param1.x + 1, param1.y + 1, param1.z + 1);
        if (this.isOpen(var20)
            && this.hasMalus(var16)
            && this.hasMalus(var1)
            && this.hasMalus(var3)
            && this.hasMalus(var5)
            && this.hasMalus(var7)
            && this.hasMalus(var9)) {
            param0[var0++] = var20;
        }

        Node var21 = this.findAcceptedNode(param1.x - 1, param1.y + 1, param1.z - 1);
        if (this.isOpen(var21)
            && this.hasMalus(var17)
            && this.hasMalus(var4)
            && this.hasMalus(var2)
            && this.hasMalus(var5)
            && this.hasMalus(var10)
            && this.hasMalus(var8)) {
            param0[var0++] = var21;
        }

        Node var22 = this.findAcceptedNode(param1.x - 1, param1.y + 1, param1.z + 1);
        if (this.isOpen(var22)
            && this.hasMalus(var18)
            && this.hasMalus(var1)
            && this.hasMalus(var2)
            && this.hasMalus(var5)
            && this.hasMalus(var7)
            && this.hasMalus(var8)) {
            param0[var0++] = var22;
        }

        Node var23 = this.findAcceptedNode(param1.x + 1, param1.y - 1, param1.z - 1);
        if (this.isOpen(var23)
            && this.hasMalus(var15)
            && this.hasMalus(var4)
            && this.hasMalus(var3)
            && this.hasMalus(var6)
            && this.hasMalus(var14)
            && this.hasMalus(var13)) {
            param0[var0++] = var23;
        }

        Node var24 = this.findAcceptedNode(param1.x + 1, param1.y - 1, param1.z + 1);
        if (this.isOpen(var24)
            && this.hasMalus(var16)
            && this.hasMalus(var1)
            && this.hasMalus(var3)
            && this.hasMalus(var6)
            && this.hasMalus(var11)
            && this.hasMalus(var13)) {
            param0[var0++] = var24;
        }

        Node var25 = this.findAcceptedNode(param1.x - 1, param1.y - 1, param1.z - 1);
        if (this.isOpen(var25)
            && this.hasMalus(var17)
            && this.hasMalus(var4)
            && this.hasMalus(var2)
            && this.hasMalus(var6)
            && this.hasMalus(var14)
            && this.hasMalus(var12)) {
            param0[var0++] = var25;
        }

        Node var26 = this.findAcceptedNode(param1.x - 1, param1.y - 1, param1.z + 1);
        if (this.isOpen(var26)
            && this.hasMalus(var18)
            && this.hasMalus(var1)
            && this.hasMalus(var2)
            && this.hasMalus(var6)
            && this.hasMalus(var11)
            && this.hasMalus(var12)) {
            param0[var0++] = var26;
        }

        return var0;
    }

    private boolean hasMalus(@Nullable Node param0) {
        return param0 != null && param0.costMalus >= 0.0F;
    }

    private boolean isOpen(@Nullable Node param0) {
        return param0 != null && !param0.closed;
    }

    @Nullable
    protected Node findAcceptedNode(int param0, int param1, int param2) {
        Node var0 = null;
        BlockPathTypes var1 = this.getCachedBlockPathType(param0, param1, param2);
        float var2 = this.mob.getPathfindingMalus(var1);
        if (var2 >= 0.0F) {
            var0 = this.getNode(param0, param1, param2);
            var0.type = var1;
            var0.costMalus = Math.max(var0.costMalus, var2);
            if (var1 == BlockPathTypes.WALKABLE) {
                ++var0.costMalus;
            }
        }

        return var0;
    }

    private BlockPathTypes getCachedBlockPathType(int param0, int param1, int param2) {
        return this.pathTypeByPosCache
            .computeIfAbsent(
                BlockPos.asLong(param0, param1, param2),
                param3 -> this.getBlockPathType(
                        this.level,
                        param0,
                        param1,
                        param2,
                        this.mob,
                        this.entityWidth,
                        this.entityHeight,
                        this.entityDepth,
                        this.canOpenDoors(),
                        this.canPassDoors()
                    )
            );
    }

    @Override
    public BlockPathTypes getBlockPathType(
        BlockGetter param0, int param1, int param2, int param3, Mob param4, int param5, int param6, int param7, boolean param8, boolean param9
    ) {
        EnumSet<BlockPathTypes> var0 = EnumSet.noneOf(BlockPathTypes.class);
        BlockPathTypes var1 = BlockPathTypes.BLOCKED;
        BlockPos var2 = param4.blockPosition();
        var1 = super.getBlockPathTypes(param0, param1, param2, param3, param5, param6, param7, param8, param9, var0, var1, var2);
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
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        BlockPathTypes var1 = getBlockPathTypeRaw(param0, var0.set(param1, param2, param3));
        if (var1 == BlockPathTypes.OPEN && param2 >= param0.getMinBuildHeight() + 1) {
            BlockPathTypes var2 = getBlockPathTypeRaw(param0, var0.set(param1, param2 - 1, param3));
            if (var2 == BlockPathTypes.DAMAGE_FIRE || var2 == BlockPathTypes.LAVA) {
                var1 = BlockPathTypes.DAMAGE_FIRE;
            } else if (var2 == BlockPathTypes.DAMAGE_CACTUS) {
                var1 = BlockPathTypes.DAMAGE_CACTUS;
            } else if (var2 == BlockPathTypes.DAMAGE_OTHER) {
                var1 = BlockPathTypes.DAMAGE_OTHER;
            } else if (var2 == BlockPathTypes.COCOA) {
                var1 = BlockPathTypes.COCOA;
            } else if (var2 == BlockPathTypes.FENCE) {
                if (!var0.equals(this.mob.blockPosition())) {
                    var1 = BlockPathTypes.FENCE;
                }
            } else {
                var1 = var2 != BlockPathTypes.WALKABLE && var2 != BlockPathTypes.OPEN && var2 != BlockPathTypes.WATER
                    ? BlockPathTypes.WALKABLE
                    : BlockPathTypes.OPEN;
            }
        }

        if (var1 == BlockPathTypes.WALKABLE || var1 == BlockPathTypes.OPEN) {
            var1 = checkNeighbourBlocks(param0, var0.set(param1, param2, param3), var1);
        }

        return var1;
    }

    private Iterable<BlockPos> iteratePathfindingStartNodeCandidatePositions(Mob param0) {
        float var0 = 1.0F;
        AABB var1 = param0.getBoundingBox();
        boolean var2 = var1.getSize() < 1.0;
        if (!var2) {
            return List.of(
                new BlockPos(var1.minX, (double)param0.getBlockY(), var1.minZ),
                new BlockPos(var1.minX, (double)param0.getBlockY(), var1.maxZ),
                new BlockPos(var1.maxX, (double)param0.getBlockY(), var1.minZ),
                new BlockPos(var1.maxX, (double)param0.getBlockY(), var1.maxZ)
            );
        } else {
            double var3 = Math.max(0.0, (1.5 - var1.getZsize()) / 2.0);
            double var4 = Math.max(0.0, (1.5 - var1.getXsize()) / 2.0);
            double var5 = Math.max(0.0, (1.5 - var1.getYsize()) / 2.0);
            AABB var6 = var1.inflate(var4, var5, var3);
            return BlockPos.randomBetweenClosed(
                param0.getRandom(),
                10,
                Mth.floor(var6.minX),
                Mth.floor(var6.minY),
                Mth.floor(var6.minZ),
                Mth.floor(var6.maxX),
                Mth.floor(var6.maxY),
                Mth.floor(var6.maxZ)
            );
        }
    }
}
