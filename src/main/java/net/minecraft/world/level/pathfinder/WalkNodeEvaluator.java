package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
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
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WalkNodeEvaluator extends NodeEvaluator {
    public static final double SPACE_BETWEEN_WALL_POSTS = 0.5;
    private static final double DEFAULT_MOB_JUMP_HEIGHT = 1.125;
    protected float oldWaterCost;
    private final Long2ObjectMap<BlockPathTypes> pathTypesByPosCache = new Long2ObjectOpenHashMap<>();
    private final Object2BooleanMap<AABB> collisionCache = new Object2BooleanOpenHashMap<>();

    @Override
    public void prepare(PathNavigationRegion param0, Mob param1) {
        super.prepare(param0, param1);
        this.oldWaterCost = param1.getPathfindingMalus(BlockPathTypes.WATER);
    }

    @Override
    public void done() {
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
        this.pathTypesByPosCache.clear();
        this.collisionCache.clear();
        super.done();
    }

    @Override
    public Node getStart() {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        int var1 = this.mob.getBlockY();
        BlockState var2 = this.level.getBlockState(var0.set(this.mob.getX(), (double)var1, this.mob.getZ()));
        if (!this.mob.canStandOnFluid(var2.getFluidState())) {
            if (this.canFloat() && this.mob.isInWater()) {
                while(true) {
                    if (!var2.is(Blocks.WATER) && var2.getFluidState() != Fluids.WATER.getSource(false)) {
                        --var1;
                        break;
                    }

                    var2 = this.level.getBlockState(var0.set(this.mob.getX(), (double)(++var1), this.mob.getZ()));
                }
            } else if (this.mob.isOnGround()) {
                var1 = Mth.floor(this.mob.getY() + 0.5);
            } else {
                BlockPos var3 = this.mob.blockPosition();

                while(
                    (this.level.getBlockState(var3).isAir() || this.level.getBlockState(var3).isPathfindable(this.level, var3, PathComputationType.LAND))
                        && var3.getY() > this.mob.level.getMinBuildHeight()
                ) {
                    var3 = var3.below();
                }

                var1 = var3.above().getY();
            }
        } else {
            while(this.mob.canStandOnFluid(var2.getFluidState())) {
                var2 = this.level.getBlockState(var0.set(this.mob.getX(), (double)(++var1), this.mob.getZ()));
            }

            --var1;
        }

        BlockPos var4 = this.mob.blockPosition();
        if (!this.canStartAt(var0.set(var4.getX(), var1, var4.getZ()))) {
            AABB var5 = this.mob.getBoundingBox();
            if (this.canStartAt(var0.set(var5.minX, (double)var1, var5.minZ))
                || this.canStartAt(var0.set(var5.minX, (double)var1, var5.maxZ))
                || this.canStartAt(var0.set(var5.maxX, (double)var1, var5.minZ))
                || this.canStartAt(var0.set(var5.maxX, (double)var1, var5.maxZ))) {
                return this.getStartNode(var0);
            }
        }

        return this.getStartNode(new BlockPos(var4.getX(), var1, var4.getZ()));
    }

    protected Node getStartNode(BlockPos param0) {
        Node var0 = this.getNode(param0);
        var0.type = this.getBlockPathType(this.mob, var0.asBlockPos());
        var0.costMalus = this.mob.getPathfindingMalus(var0.type);
        return var0;
    }

    protected boolean canStartAt(BlockPos param0) {
        BlockPathTypes var0 = this.getBlockPathType(this.mob, param0);
        return var0 != BlockPathTypes.OPEN && this.mob.getPathfindingMalus(var0) >= 0.0F;
    }

    @Override
    public Target getGoal(double param0, double param1, double param2) {
        return this.getTargetFromNode(this.getNode(Mth.floor(param0), Mth.floor(param1), Mth.floor(param2)));
    }

    @Override
    public int getNeighbors(Node[] param0, Node param1) {
        int var0 = 0;
        int var1 = 0;
        BlockPathTypes var2 = this.getCachedBlockType(this.mob, param1.x, param1.y + 1, param1.z);
        BlockPathTypes var3 = this.getCachedBlockType(this.mob, param1.x, param1.y, param1.z);
        if (this.mob.getPathfindingMalus(var2) >= 0.0F && var3 != BlockPathTypes.STICKY_HONEY) {
            var1 = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
        }

        double var4 = this.getFloorLevel(new BlockPos(param1.x, param1.y, param1.z));
        Node var5 = this.findAcceptedNode(param1.x, param1.y, param1.z + 1, var1, var4, Direction.SOUTH, var3);
        if (this.isNeighborValid(var5, param1)) {
            param0[var0++] = var5;
        }

        Node var6 = this.findAcceptedNode(param1.x - 1, param1.y, param1.z, var1, var4, Direction.WEST, var3);
        if (this.isNeighborValid(var6, param1)) {
            param0[var0++] = var6;
        }

        Node var7 = this.findAcceptedNode(param1.x + 1, param1.y, param1.z, var1, var4, Direction.EAST, var3);
        if (this.isNeighborValid(var7, param1)) {
            param0[var0++] = var7;
        }

        Node var8 = this.findAcceptedNode(param1.x, param1.y, param1.z - 1, var1, var4, Direction.NORTH, var3);
        if (this.isNeighborValid(var8, param1)) {
            param0[var0++] = var8;
        }

        Node var9 = this.findAcceptedNode(param1.x - 1, param1.y, param1.z - 1, var1, var4, Direction.NORTH, var3);
        if (this.isDiagonalValid(param1, var6, var8, var9)) {
            param0[var0++] = var9;
        }

        Node var10 = this.findAcceptedNode(param1.x + 1, param1.y, param1.z - 1, var1, var4, Direction.NORTH, var3);
        if (this.isDiagonalValid(param1, var7, var8, var10)) {
            param0[var0++] = var10;
        }

        Node var11 = this.findAcceptedNode(param1.x - 1, param1.y, param1.z + 1, var1, var4, Direction.SOUTH, var3);
        if (this.isDiagonalValid(param1, var6, var5, var11)) {
            param0[var0++] = var11;
        }

        Node var12 = this.findAcceptedNode(param1.x + 1, param1.y, param1.z + 1, var1, var4, Direction.SOUTH, var3);
        if (this.isDiagonalValid(param1, var7, var5, var12)) {
            param0[var0++] = var12;
        }

        return var0;
    }

    protected boolean isNeighborValid(@Nullable Node param0, Node param1) {
        return param0 != null && !param0.closed && (param0.costMalus >= 0.0F || param1.costMalus < 0.0F);
    }

    protected boolean isDiagonalValid(Node param0, @Nullable Node param1, @Nullable Node param2, @Nullable Node param3) {
        if (param3 == null || param2 == null || param1 == null) {
            return false;
        } else if (param3.closed) {
            return false;
        } else if (param2.y > param0.y || param1.y > param0.y) {
            return false;
        } else if (param1.type != BlockPathTypes.WALKABLE_DOOR && param2.type != BlockPathTypes.WALKABLE_DOOR && param3.type != BlockPathTypes.WALKABLE_DOOR) {
            boolean var0 = param2.type == BlockPathTypes.FENCE && param1.type == BlockPathTypes.FENCE && (double)this.mob.getBbWidth() < 0.5;
            return param3.costMalus >= 0.0F
                && (param2.y < param0.y || param2.costMalus >= 0.0F || var0)
                && (param1.y < param0.y || param1.costMalus >= 0.0F || var0);
        } else {
            return false;
        }
    }

    private static boolean doesBlockHavePartialCollision(BlockPathTypes param0) {
        return param0 == BlockPathTypes.FENCE || param0 == BlockPathTypes.DOOR_WOOD_CLOSED || param0 == BlockPathTypes.DOOR_IRON_CLOSED;
    }

    private boolean canReachWithoutCollision(Node param0) {
        AABB var0 = this.mob.getBoundingBox();
        Vec3 var1 = new Vec3(
            (double)param0.x - this.mob.getX() + var0.getXsize() / 2.0,
            (double)param0.y - this.mob.getY() + var0.getYsize() / 2.0,
            (double)param0.z - this.mob.getZ() + var0.getZsize() / 2.0
        );
        int var2 = Mth.ceil(var1.length() / var0.getSize());
        var1 = var1.scale((double)(1.0F / (float)var2));

        for(int var3 = 1; var3 <= var2; ++var3) {
            var0 = var0.move(var1);
            if (this.hasCollisions(var0)) {
                return false;
            }
        }

        return true;
    }

    protected double getFloorLevel(BlockPos param0) {
        return (this.canFloat() || this.isAmphibious()) && this.level.getFluidState(param0).is(FluidTags.WATER)
            ? (double)param0.getY() + 0.5
            : getFloorLevel(this.level, param0);
    }

    public static double getFloorLevel(BlockGetter param0, BlockPos param1) {
        BlockPos var0 = param1.below();
        VoxelShape var1 = param0.getBlockState(var0).getCollisionShape(param0, var0);
        return (double)var0.getY() + (var1.isEmpty() ? 0.0 : var1.max(Direction.Axis.Y));
    }

    protected boolean isAmphibious() {
        return false;
    }

    @Nullable
    protected Node findAcceptedNode(int param0, int param1, int param2, int param3, double param4, Direction param5, BlockPathTypes param6) {
        Node var0 = null;
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
        double var2 = this.getFloorLevel(var1.set(param0, param1, param2));
        if (var2 - param4 > this.getMobJumpHeight()) {
            return null;
        } else {
            BlockPathTypes var3 = this.getCachedBlockType(this.mob, param0, param1, param2);
            float var4 = this.mob.getPathfindingMalus(var3);
            double var5 = (double)this.mob.getBbWidth() / 2.0;
            if (var4 >= 0.0F) {
                var0 = this.getNodeAndUpdateCostToMax(param0, param1, param2, var3, var4);
            }

            if (doesBlockHavePartialCollision(param6) && var0 != null && var0.costMalus >= 0.0F && !this.canReachWithoutCollision(var0)) {
                var0 = null;
            }

            if (var3 != BlockPathTypes.WALKABLE && (!this.isAmphibious() || var3 != BlockPathTypes.WATER)) {
                if ((var0 == null || var0.costMalus < 0.0F)
                    && param3 > 0
                    && (var3 != BlockPathTypes.FENCE || this.canWalkOverFences())
                    && var3 != BlockPathTypes.UNPASSABLE_RAIL
                    && var3 != BlockPathTypes.TRAPDOOR
                    && var3 != BlockPathTypes.POWDER_SNOW) {
                    var0 = this.findAcceptedNode(param0, param1 + 1, param2, param3 - 1, param4, param5, param6);
                    if (var0 != null && (var0.type == BlockPathTypes.OPEN || var0.type == BlockPathTypes.WALKABLE) && this.mob.getBbWidth() < 1.0F) {
                        double var6 = (double)(param0 - param5.getStepX()) + 0.5;
                        double var7 = (double)(param2 - param5.getStepZ()) + 0.5;
                        AABB var8 = new AABB(
                            var6 - var5,
                            this.getFloorLevel(var1.set(var6, (double)(param1 + 1), var7)) + 0.001,
                            var7 - var5,
                            var6 + var5,
                            (double)this.mob.getBbHeight() + this.getFloorLevel(var1.set((double)var0.x, (double)var0.y, (double)var0.z)) - 0.002,
                            var7 + var5
                        );
                        if (this.hasCollisions(var8)) {
                            var0 = null;
                        }
                    }
                }

                if (!this.isAmphibious() && var3 == BlockPathTypes.WATER && !this.canFloat()) {
                    if (this.getCachedBlockType(this.mob, param0, param1 - 1, param2) != BlockPathTypes.WATER) {
                        return var0;
                    }

                    while(param1 > this.mob.level.getMinBuildHeight()) {
                        var3 = this.getCachedBlockType(this.mob, param0, --param1, param2);
                        if (var3 != BlockPathTypes.WATER) {
                            return var0;
                        }

                        var0 = this.getNodeAndUpdateCostToMax(param0, param1, param2, var3, this.mob.getPathfindingMalus(var3));
                    }
                }

                if (var3 == BlockPathTypes.OPEN) {
                    int var9 = 0;
                    int var10 = param1;

                    while(var3 == BlockPathTypes.OPEN) {
                        if (--param1 < this.mob.level.getMinBuildHeight()) {
                            return this.getBlockedNode(param0, var10, param2);
                        }

                        if (var9++ >= this.mob.getMaxFallDistance()) {
                            return this.getBlockedNode(param0, param1, param2);
                        }

                        var3 = this.getCachedBlockType(this.mob, param0, param1, param2);
                        var4 = this.mob.getPathfindingMalus(var3);
                        if (var3 != BlockPathTypes.OPEN && var4 >= 0.0F) {
                            var0 = this.getNodeAndUpdateCostToMax(param0, param1, param2, var3, var4);
                            break;
                        }

                        if (var4 < 0.0F) {
                            return this.getBlockedNode(param0, param1, param2);
                        }
                    }
                }

                if (doesBlockHavePartialCollision(var3) && var0 == null) {
                    var0 = this.getNode(param0, param1, param2);
                    var0.closed = true;
                    var0.type = var3;
                    var0.costMalus = var3.getMalus();
                }

                return var0;
            } else {
                return var0;
            }
        }
    }

    private double getMobJumpHeight() {
        return Math.max(1.125, (double)this.mob.maxUpStep());
    }

    private Node getNodeAndUpdateCostToMax(int param0, int param1, int param2, BlockPathTypes param3, float param4) {
        Node var0 = this.getNode(param0, param1, param2);
        var0.type = param3;
        var0.costMalus = Math.max(var0.costMalus, param4);
        return var0;
    }

    private Node getBlockedNode(int param0, int param1, int param2) {
        Node var0 = this.getNode(param0, param1, param2);
        var0.type = BlockPathTypes.BLOCKED;
        var0.costMalus = -1.0F;
        return var0;
    }

    private boolean hasCollisions(AABB param0) {
        return this.collisionCache.computeIfAbsent(param0, param1 -> !this.level.noCollision(this.mob, param0));
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter param0, int param1, int param2, int param3, Mob param4) {
        EnumSet<BlockPathTypes> var0 = EnumSet.noneOf(BlockPathTypes.class);
        BlockPathTypes var1 = BlockPathTypes.BLOCKED;
        var1 = this.getBlockPathTypes(param0, param1, param2, param3, var0, var1, param4.blockPosition());
        if (var0.contains(BlockPathTypes.FENCE)) {
            return BlockPathTypes.FENCE;
        } else if (var0.contains(BlockPathTypes.UNPASSABLE_RAIL)) {
            return BlockPathTypes.UNPASSABLE_RAIL;
        } else {
            BlockPathTypes var2 = BlockPathTypes.BLOCKED;

            for(BlockPathTypes var3 : var0) {
                if (param4.getPathfindingMalus(var3) < 0.0F) {
                    return var3;
                }

                if (param4.getPathfindingMalus(var3) >= param4.getPathfindingMalus(var2)) {
                    var2 = var3;
                }
            }

            return var1 == BlockPathTypes.OPEN && param4.getPathfindingMalus(var2) == 0.0F && this.entityWidth <= 1 ? BlockPathTypes.OPEN : var2;
        }
    }

    public BlockPathTypes getBlockPathTypes(
        BlockGetter param0, int param1, int param2, int param3, EnumSet<BlockPathTypes> param4, BlockPathTypes param5, BlockPos param6
    ) {
        for(int var0 = 0; var0 < this.entityWidth; ++var0) {
            for(int var1 = 0; var1 < this.entityHeight; ++var1) {
                for(int var2 = 0; var2 < this.entityDepth; ++var2) {
                    int var3 = var0 + param1;
                    int var4 = var1 + param2;
                    int var5 = var2 + param3;
                    BlockPathTypes var6 = this.getBlockPathType(param0, var3, var4, var5);
                    var6 = this.evaluateBlockPathType(param0, param6, var6);
                    if (var0 == 0 && var1 == 0 && var2 == 0) {
                        param5 = var6;
                    }

                    param4.add(var6);
                }
            }
        }

        return param5;
    }

    protected BlockPathTypes evaluateBlockPathType(BlockGetter param0, BlockPos param1, BlockPathTypes param2) {
        boolean var0 = this.canPassDoors();
        if (param2 == BlockPathTypes.DOOR_WOOD_CLOSED && this.canOpenDoors() && var0) {
            param2 = BlockPathTypes.WALKABLE_DOOR;
        }

        if (param2 == BlockPathTypes.DOOR_OPEN && !var0) {
            param2 = BlockPathTypes.BLOCKED;
        }

        if (param2 == BlockPathTypes.RAIL
            && !(param0.getBlockState(param1).getBlock() instanceof BaseRailBlock)
            && !(param0.getBlockState(param1.below()).getBlock() instanceof BaseRailBlock)) {
            param2 = BlockPathTypes.UNPASSABLE_RAIL;
        }

        return param2;
    }

    protected BlockPathTypes getBlockPathType(Mob param0, BlockPos param1) {
        return this.getCachedBlockType(param0, param1.getX(), param1.getY(), param1.getZ());
    }

    protected BlockPathTypes getCachedBlockType(Mob param0, int param1, int param2, int param3) {
        return this.pathTypesByPosCache
            .computeIfAbsent(BlockPos.asLong(param1, param2, param3), param4 -> this.getBlockPathType(this.level, param1, param2, param3, param0));
    }

    @Override
    public BlockPathTypes getBlockPathType(BlockGetter param0, int param1, int param2, int param3) {
        return getBlockPathTypeStatic(param0, new BlockPos.MutableBlockPos(param1, param2, param3));
    }

    public static BlockPathTypes getBlockPathTypeStatic(BlockGetter param0, BlockPos.MutableBlockPos param1) {
        int var0 = param1.getX();
        int var1 = param1.getY();
        int var2 = param1.getZ();
        BlockPathTypes var3 = getBlockPathTypeRaw(param0, param1);
        if (var3 == BlockPathTypes.OPEN && var1 >= param0.getMinBuildHeight() + 1) {
            BlockPathTypes var4 = getBlockPathTypeRaw(param0, param1.set(var0, var1 - 1, var2));
            var3 = var4 != BlockPathTypes.WALKABLE && var4 != BlockPathTypes.OPEN && var4 != BlockPathTypes.WATER && var4 != BlockPathTypes.LAVA
                ? BlockPathTypes.WALKABLE
                : BlockPathTypes.OPEN;
            if (var4 == BlockPathTypes.DAMAGE_FIRE) {
                var3 = BlockPathTypes.DAMAGE_FIRE;
            }

            if (var4 == BlockPathTypes.DAMAGE_OTHER) {
                var3 = BlockPathTypes.DAMAGE_OTHER;
            }

            if (var4 == BlockPathTypes.STICKY_HONEY) {
                var3 = BlockPathTypes.STICKY_HONEY;
            }

            if (var4 == BlockPathTypes.POWDER_SNOW) {
                var3 = BlockPathTypes.DANGER_POWDER_SNOW;
            }

            if (var4 == BlockPathTypes.DAMAGE_CAUTIOUS) {
                var3 = BlockPathTypes.DAMAGE_CAUTIOUS;
            }
        }

        if (var3 == BlockPathTypes.WALKABLE) {
            var3 = checkNeighbourBlocks(param0, param1.set(var0, var1, var2), var3);
        }

        return var3;
    }

    public static BlockPathTypes checkNeighbourBlocks(BlockGetter param0, BlockPos.MutableBlockPos param1, BlockPathTypes param2) {
        int var0 = param1.getX();
        int var1 = param1.getY();
        int var2 = param1.getZ();

        for(int var3 = -1; var3 <= 1; ++var3) {
            for(int var4 = -1; var4 <= 1; ++var4) {
                for(int var5 = -1; var5 <= 1; ++var5) {
                    if (var3 != 0 || var5 != 0) {
                        param1.set(var0 + var3, var1 + var4, var2 + var5);
                        BlockState var6 = param0.getBlockState(param1);
                        if (var6.is(Blocks.CACTUS) || var6.is(Blocks.SWEET_BERRY_BUSH)) {
                            return BlockPathTypes.DANGER_OTHER;
                        }

                        if (isBurningBlock(var6)) {
                            return BlockPathTypes.DANGER_FIRE;
                        }

                        if (param0.getFluidState(param1).is(FluidTags.WATER)) {
                            return BlockPathTypes.WATER_BORDER;
                        }

                        if (var6.is(Blocks.WITHER_ROSE) || var6.is(Blocks.POINTED_DRIPSTONE)) {
                            return BlockPathTypes.DAMAGE_CAUTIOUS;
                        }
                    }
                }
            }
        }

        return param2;
    }

    protected static BlockPathTypes getBlockPathTypeRaw(BlockGetter param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        Block var1 = var0.getBlock();
        Material var2 = var0.getMaterial();
        if (var0.isAir()) {
            return BlockPathTypes.OPEN;
        } else if (var0.is(BlockTags.TRAPDOORS) || var0.is(Blocks.LILY_PAD) || var0.is(Blocks.BIG_DRIPLEAF)) {
            return BlockPathTypes.TRAPDOOR;
        } else if (var0.is(Blocks.POWDER_SNOW)) {
            return BlockPathTypes.POWDER_SNOW;
        } else if (var0.is(Blocks.CACTUS) || var0.is(Blocks.SWEET_BERRY_BUSH)) {
            return BlockPathTypes.DAMAGE_OTHER;
        } else if (var0.is(Blocks.HONEY_BLOCK)) {
            return BlockPathTypes.STICKY_HONEY;
        } else if (var0.is(Blocks.COCOA)) {
            return BlockPathTypes.COCOA;
        } else if (!var0.is(Blocks.WITHER_ROSE) && !var0.is(Blocks.POINTED_DRIPSTONE)) {
            FluidState var3 = param0.getFluidState(param1);
            if (var3.is(FluidTags.LAVA)) {
                return BlockPathTypes.LAVA;
            } else if (isBurningBlock(var0)) {
                return BlockPathTypes.DAMAGE_FIRE;
            } else if (DoorBlock.isWoodenDoor(var0) && !var0.getValue(DoorBlock.OPEN)) {
                return BlockPathTypes.DOOR_WOOD_CLOSED;
            } else if (var1 instanceof DoorBlock && var2 == Material.METAL && !var0.getValue(DoorBlock.OPEN)) {
                return BlockPathTypes.DOOR_IRON_CLOSED;
            } else if (var1 instanceof DoorBlock && var0.getValue(DoorBlock.OPEN)) {
                return BlockPathTypes.DOOR_OPEN;
            } else if (var1 instanceof BaseRailBlock) {
                return BlockPathTypes.RAIL;
            } else if (var1 instanceof LeavesBlock) {
                return BlockPathTypes.LEAVES;
            } else if (!var0.is(BlockTags.FENCES) && !var0.is(BlockTags.WALLS) && (!(var1 instanceof FenceGateBlock) || var0.getValue(FenceGateBlock.OPEN))) {
                if (!var0.isPathfindable(param0, param1, PathComputationType.LAND)) {
                    return BlockPathTypes.BLOCKED;
                } else {
                    return var3.is(FluidTags.WATER) ? BlockPathTypes.WATER : BlockPathTypes.OPEN;
                }
            } else {
                return BlockPathTypes.FENCE;
            }
        } else {
            return BlockPathTypes.DAMAGE_CAUTIOUS;
        }
    }

    public static boolean isBurningBlock(BlockState param0) {
        return param0.is(BlockTags.FIRE)
            || param0.is(Blocks.LAVA)
            || param0.is(Blocks.MAGMA_BLOCK)
            || CampfireBlock.isLitCampfire(param0)
            || param0.is(Blocks.LAVA_CAULDRON);
    }
}
