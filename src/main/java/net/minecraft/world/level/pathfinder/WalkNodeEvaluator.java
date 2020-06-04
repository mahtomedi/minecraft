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
        int var1 = Mth.floor(this.mob.getY());
        BlockState var2 = this.level.getBlockState(var0.set(this.mob.getX(), (double)var1, this.mob.getZ()));
        if (!this.mob.canStandOnFluid(var2.getFluidState().getType())) {
            if (this.canFloat() && this.mob.isInWater()) {
                while(true) {
                    if (var2.getBlock() != Blocks.WATER && var2.getFluidState() != Fluids.WATER.getSource(false)) {
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
                        && var3.getY() > 0
                ) {
                    var3 = var3.below();
                }

                var1 = var3.above().getY();
            }
        } else {
            while(this.mob.canStandOnFluid(var2.getFluidState().getType())) {
                var2 = this.level.getBlockState(var0.set(this.mob.getX(), (double)(++var1), this.mob.getZ()));
            }

            --var1;
        }

        BlockPos var4 = this.mob.blockPosition();
        BlockPathTypes var5 = this.getCachedBlockType(this.mob, var4.getX(), var1, var4.getZ());
        if (this.mob.getPathfindingMalus(var5) < 0.0F) {
            AABB var6 = this.mob.getBoundingBox();
            if (this.hasPositiveMalus(var0.set(var6.minX, (double)var1, var6.minZ))
                || this.hasPositiveMalus(var0.set(var6.minX, (double)var1, var6.maxZ))
                || this.hasPositiveMalus(var0.set(var6.maxX, (double)var1, var6.minZ))
                || this.hasPositiveMalus(var0.set(var6.maxX, (double)var1, var6.maxZ))) {
                Node var7 = this.getNode(var0);
                var7.type = this.getBlockPathType(this.mob, var7.asBlockPos());
                var7.costMalus = this.mob.getPathfindingMalus(var7.type);
                return var7;
            }
        }

        Node var8 = this.getNode(var4.getX(), var1, var4.getZ());
        var8.type = this.getBlockPathType(this.mob, var8.asBlockPos());
        var8.costMalus = this.mob.getPathfindingMalus(var8.type);
        return var8;
    }

    private boolean hasPositiveMalus(BlockPos param0) {
        BlockPathTypes var0 = this.getBlockPathType(this.mob, param0);
        return this.mob.getPathfindingMalus(var0) >= 0.0F;
    }

    @Override
    public Target getGoal(double param0, double param1, double param2) {
        return new Target(this.getNode(Mth.floor(param0), Mth.floor(param1), Mth.floor(param2)));
    }

    @Override
    public int getNeighbors(Node[] param0, Node param1) {
        int var0 = 0;
        int var1 = 0;
        BlockPathTypes var2 = this.getCachedBlockType(this.mob, param1.x, param1.y + 1, param1.z);
        BlockPathTypes var3 = this.getCachedBlockType(this.mob, param1.x, param1.y, param1.z);
        if (this.mob.getPathfindingMalus(var2) >= 0.0F && var3 != BlockPathTypes.STICKY_HONEY) {
            var1 = Mth.floor(Math.max(1.0F, this.mob.maxUpStep));
        }

        double var4 = getFloorLevel(this.level, new BlockPos(param1.x, param1.y, param1.z));
        Node var5 = this.getLandNode(param1.x, param1.y, param1.z + 1, var1, var4, Direction.SOUTH, var3);
        if (this.isNeighborValid(var5, param1)) {
            param0[var0++] = var5;
        }

        Node var6 = this.getLandNode(param1.x - 1, param1.y, param1.z, var1, var4, Direction.WEST, var3);
        if (this.isNeighborValid(var6, param1)) {
            param0[var0++] = var6;
        }

        Node var7 = this.getLandNode(param1.x + 1, param1.y, param1.z, var1, var4, Direction.EAST, var3);
        if (this.isNeighborValid(var7, param1)) {
            param0[var0++] = var7;
        }

        Node var8 = this.getLandNode(param1.x, param1.y, param1.z - 1, var1, var4, Direction.NORTH, var3);
        if (this.isNeighborValid(var8, param1)) {
            param0[var0++] = var8;
        }

        Node var9 = this.getLandNode(param1.x - 1, param1.y, param1.z - 1, var1, var4, Direction.NORTH, var3);
        if (this.isDiagonalValid(param1, var6, var8, var9)) {
            param0[var0++] = var9;
        }

        Node var10 = this.getLandNode(param1.x + 1, param1.y, param1.z - 1, var1, var4, Direction.NORTH, var3);
        if (this.isDiagonalValid(param1, var7, var8, var10)) {
            param0[var0++] = var10;
        }

        Node var11 = this.getLandNode(param1.x - 1, param1.y, param1.z + 1, var1, var4, Direction.SOUTH, var3);
        if (this.isDiagonalValid(param1, var6, var5, var11)) {
            param0[var0++] = var11;
        }

        Node var12 = this.getLandNode(param1.x + 1, param1.y, param1.z + 1, var1, var4, Direction.SOUTH, var3);
        if (this.isDiagonalValid(param1, var7, var5, var12)) {
            param0[var0++] = var12;
        }

        return var0;
    }

    private boolean isNeighborValid(Node param0, Node param1) {
        return param0 != null && !param0.closed && (param0.costMalus >= 0.0F || param1.costMalus < 0.0F);
    }

    private boolean isDiagonalValid(Node param0, @Nullable Node param1, @Nullable Node param2, @Nullable Node param3) {
        if (param3 == null || param2 == null || param1 == null) {
            return false;
        } else if (param3.closed) {
            return false;
        } else if (param2.y <= param0.y && param1.y <= param0.y) {
            boolean var0 = param2.type == BlockPathTypes.FENCE && param1.type == BlockPathTypes.FENCE && (double)this.mob.getBbWidth() < 0.5;
            return param3.costMalus >= 0.0F
                && (param2.y < param0.y || param2.costMalus >= 0.0F || var0)
                && (param1.y < param0.y || param1.costMalus >= 0.0F || var0);
        } else {
            return false;
        }
    }

    private boolean canReachWithoutCollision(Node param0) {
        Vec3 var0 = new Vec3((double)param0.x - this.mob.getX(), (double)param0.y - this.mob.getY(), (double)param0.z - this.mob.getZ());
        AABB var1 = this.mob.getBoundingBox();
        int var2 = Mth.ceil(var0.length() / var1.getSize());
        var0 = var0.scale((double)(1.0F / (float)var2));

        for(int var3 = 1; var3 <= var2; ++var3) {
            var1 = var1.move(var0);
            if (this.hasCollisions(var1)) {
                return false;
            }
        }

        return true;
    }

    public static double getFloorLevel(BlockGetter param0, BlockPos param1) {
        BlockPos var0 = param1.below();
        VoxelShape var1 = param0.getBlockState(var0).getCollisionShape(param0, var0);
        return (double)var0.getY() + (var1.isEmpty() ? 0.0 : var1.max(Direction.Axis.Y));
    }

    @Nullable
    private Node getLandNode(int param0, int param1, int param2, int param3, double param4, Direction param5, BlockPathTypes param6) {
        Node var0 = null;
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
        double var2 = getFloorLevel(this.level, var1.set(param0, param1, param2));
        if (var2 - param4 > 1.125) {
            return null;
        } else {
            BlockPathTypes var3 = this.getCachedBlockType(this.mob, param0, param1, param2);
            float var4 = this.mob.getPathfindingMalus(var3);
            double var5 = (double)this.mob.getBbWidth() / 2.0;
            if (var4 >= 0.0F) {
                var0 = this.getNode(param0, param1, param2);
                var0.type = var3;
                var0.costMalus = Math.max(var0.costMalus, var4);
            }

            if (param6 == BlockPathTypes.FENCE && var0 != null && var0.costMalus >= 0.0F && !this.canReachWithoutCollision(var0)) {
                var0 = null;
            }

            if (var3 == BlockPathTypes.WALKABLE) {
                return var0;
            } else {
                if ((var0 == null || var0.costMalus < 0.0F)
                    && param3 > 0
                    && var3 != BlockPathTypes.FENCE
                    && var3 != BlockPathTypes.UNPASSABLE_RAIL
                    && var3 != BlockPathTypes.TRAPDOOR) {
                    var0 = this.getLandNode(param0, param1 + 1, param2, param3 - 1, param4, param5, param6);
                    if (var0 != null && (var0.type == BlockPathTypes.OPEN || var0.type == BlockPathTypes.WALKABLE) && this.mob.getBbWidth() < 1.0F) {
                        double var6 = (double)(param0 - param5.getStepX()) + 0.5;
                        double var7 = (double)(param2 - param5.getStepZ()) + 0.5;
                        AABB var8 = new AABB(
                            var6 - var5,
                            getFloorLevel(this.level, var1.set(var6, (double)(param1 + 1), var7)) + 0.001,
                            var7 - var5,
                            var6 + var5,
                            (double)this.mob.getBbHeight() + getFloorLevel(this.level, var1.set((double)var0.x, (double)var0.y, (double)var0.z)) - 0.002,
                            var7 + var5
                        );
                        if (this.hasCollisions(var8)) {
                            var0 = null;
                        }
                    }
                }

                if (var3 == BlockPathTypes.WATER && !this.canFloat()) {
                    if (this.getCachedBlockType(this.mob, param0, param1 - 1, param2) != BlockPathTypes.WATER) {
                        return var0;
                    }

                    while(param1 > 0) {
                        var3 = this.getCachedBlockType(this.mob, param0, --param1, param2);
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
                    if (this.hasCollisions(var9)) {
                        return null;
                    }

                    if (this.mob.getBbWidth() >= 1.0F) {
                        BlockPathTypes var10 = this.getCachedBlockType(this.mob, param0, param1 - 1, param2);
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

                        var3 = this.getCachedBlockType(this.mob, param0, param1, param2);
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

                if (var3 == BlockPathTypes.FENCE) {
                    var0 = this.getNode(param0, param1, param2);
                    var0.closed = true;
                    var0.type = var3;
                    var0.costMalus = var3.getMalus();
                }

                return var0;
            }
        }
    }

    private boolean hasCollisions(AABB param0) {
        return this.collisionCache.computeIfAbsent(param0, param1 -> !this.level.noCollision(this.mob, param0));
    }

    @Override
    public BlockPathTypes getBlockPathType(
        BlockGetter param0, int param1, int param2, int param3, Mob param4, int param5, int param6, int param7, boolean param8, boolean param9
    ) {
        EnumSet<BlockPathTypes> var0 = EnumSet.noneOf(BlockPathTypes.class);
        BlockPathTypes var1 = BlockPathTypes.BLOCKED;
        BlockPos var2 = param4.blockPosition();
        var1 = this.getBlockPathTypes(param0, param1, param2, param3, param5, param6, param7, param8, param9, var0, var1, var2);
        if (var0.contains(BlockPathTypes.FENCE)) {
            return BlockPathTypes.FENCE;
        } else if (var0.contains(BlockPathTypes.UNPASSABLE_RAIL)) {
            return BlockPathTypes.UNPASSABLE_RAIL;
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
            param4 = BlockPathTypes.UNPASSABLE_RAIL;
        }

        if (param4 == BlockPathTypes.LEAVES) {
            param4 = BlockPathTypes.BLOCKED;
        }

        return param4;
    }

    private BlockPathTypes getBlockPathType(Mob param0, BlockPos param1) {
        return this.getCachedBlockType(param0, param1.getX(), param1.getY(), param1.getZ());
    }

    private BlockPathTypes getCachedBlockType(Mob param0, int param1, int param2, int param3) {
        return this.pathTypesByPosCache
            .computeIfAbsent(
                BlockPos.asLong(param1, param2, param3),
                param4 -> this.getBlockPathType(
                        this.level,
                        param1,
                        param2,
                        param3,
                        param0,
                        this.entityWidth,
                        this.entityHeight,
                        this.entityDepth,
                        this.canOpenDoors(),
                        this.canPassDoors()
                    )
            );
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
        if (var3 == BlockPathTypes.OPEN && var1 >= 1) {
            BlockPathTypes var4 = getBlockPathTypeRaw(param0, param1.set(var0, var1 - 1, var2));
            var3 = var4 != BlockPathTypes.WALKABLE && var4 != BlockPathTypes.OPEN && var4 != BlockPathTypes.WATER && var4 != BlockPathTypes.LAVA
                ? BlockPathTypes.WALKABLE
                : BlockPathTypes.OPEN;
            if (var4 == BlockPathTypes.DAMAGE_FIRE) {
                var3 = BlockPathTypes.DAMAGE_FIRE;
            }

            if (var4 == BlockPathTypes.DAMAGE_CACTUS) {
                var3 = BlockPathTypes.DAMAGE_CACTUS;
            }

            if (var4 == BlockPathTypes.DAMAGE_OTHER) {
                var3 = BlockPathTypes.DAMAGE_OTHER;
            }

            if (var4 == BlockPathTypes.STICKY_HONEY) {
                var3 = BlockPathTypes.STICKY_HONEY;
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
                        if (var6.is(Blocks.CACTUS)) {
                            return BlockPathTypes.DANGER_CACTUS;
                        }

                        if (var6.is(Blocks.SWEET_BERRY_BUSH)) {
                            return BlockPathTypes.DANGER_OTHER;
                        }

                        if (isBurningBlock(var6)) {
                            return BlockPathTypes.DANGER_FIRE;
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
        } else if (var0.is(BlockTags.TRAPDOORS) || var0.is(Blocks.LILY_PAD)) {
            return BlockPathTypes.TRAPDOOR;
        } else if (var0.is(Blocks.CACTUS)) {
            return BlockPathTypes.DAMAGE_CACTUS;
        } else if (var0.is(Blocks.SWEET_BERRY_BUSH)) {
            return BlockPathTypes.DAMAGE_OTHER;
        } else if (var0.is(Blocks.HONEY_BLOCK)) {
            return BlockPathTypes.STICKY_HONEY;
        } else if (var0.is(Blocks.COCOA)) {
            return BlockPathTypes.COCOA;
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
        } else if (!var1.is(BlockTags.FENCES) && !var1.is(BlockTags.WALLS) && (!(var1 instanceof FenceGateBlock) || var0.getValue(FenceGateBlock.OPEN))) {
            if (!var0.isPathfindable(param0, param1, PathComputationType.LAND)) {
                return BlockPathTypes.BLOCKED;
            } else {
                FluidState var3 = param0.getFluidState(param1);
                if (var3.is(FluidTags.WATER)) {
                    return BlockPathTypes.WATER;
                } else {
                    return var3.is(FluidTags.LAVA) ? BlockPathTypes.LAVA : BlockPathTypes.OPEN;
                }
            }
        } else {
            return BlockPathTypes.FENCE;
        }
    }

    private static boolean isBurningBlock(BlockState param0) {
        return param0.is(BlockTags.FIRE) || param0.is(Blocks.LAVA) || param0.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(param0);
    }
}
