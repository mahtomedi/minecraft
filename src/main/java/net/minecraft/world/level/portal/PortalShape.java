package net.minecraft.world.level.portal;

import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortalShape {
    private static final int MIN_WIDTH = 2;
    public static final int MAX_WIDTH = 21;
    private static final int MIN_HEIGHT = 3;
    public static final int MAX_HEIGHT = 21;
    private static final BlockBehaviour.StatePredicate FRAME = (param0, param1, param2) -> param0.is(Blocks.OBSIDIAN);
    private static final float SAFE_TRAVEL_MAX_ENTITY_XY = 4.0F;
    private static final double SAFE_TRAVEL_MAX_VERTICAL_DELTA = 1.0;
    private final LevelAccessor level;
    private final Direction.Axis axis;
    private final Direction rightDir;
    private int numPortalBlocks;
    @Nullable
    private BlockPos bottomLeft;
    private int height;
    private final int width;

    public static Optional<PortalShape> findEmptyPortalShape(LevelAccessor param0, BlockPos param1, Direction.Axis param2) {
        return findPortalShape(param0, param1, param0x -> param0x.isValid() && param0x.numPortalBlocks == 0, param2);
    }

    public static Optional<PortalShape> findPortalShape(LevelAccessor param0, BlockPos param1, Predicate<PortalShape> param2, Direction.Axis param3) {
        Optional<PortalShape> var0 = Optional.of(new PortalShape(param0, param1, param3)).filter(param2);
        if (var0.isPresent()) {
            return var0;
        } else {
            Direction.Axis var1 = param3 == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
            return Optional.of(new PortalShape(param0, param1, var1)).filter(param2);
        }
    }

    public PortalShape(LevelAccessor param0, BlockPos param1, Direction.Axis param2) {
        this.level = param0;
        this.axis = param2;
        this.rightDir = param2 == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.bottomLeft = this.calculateBottomLeft(param1);
        if (this.bottomLeft == null) {
            this.bottomLeft = param1;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = this.calculateWidth();
            if (this.width > 0) {
                this.height = this.calculateHeight();
            }
        }

    }

    @Nullable
    private BlockPos calculateBottomLeft(BlockPos param0) {
        int var0 = Math.max(this.level.getMinBuildHeight(), param0.getY() - 21);

        while(param0.getY() > var0 && isEmpty(this.level.getBlockState(param0.below()))) {
            param0 = param0.below();
        }

        Direction var1 = this.rightDir.getOpposite();
        int var2 = this.getDistanceUntilEdgeAboveFrame(param0, var1) - 1;
        return var2 < 0 ? null : param0.relative(var1, var2);
    }

    private int calculateWidth() {
        int var0 = this.getDistanceUntilEdgeAboveFrame(this.bottomLeft, this.rightDir);
        return var0 >= 2 && var0 <= 21 ? var0 : 0;
    }

    private int getDistanceUntilEdgeAboveFrame(BlockPos param0, Direction param1) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = 0; var1 <= 21; ++var1) {
            var0.set(param0).move(param1, var1);
            BlockState var2 = this.level.getBlockState(var0);
            if (!isEmpty(var2)) {
                if (FRAME.test(var2, this.level, var0)) {
                    return var1;
                }
                break;
            }

            BlockState var3 = this.level.getBlockState(var0.move(Direction.DOWN));
            if (!FRAME.test(var3, this.level, var0)) {
                break;
            }
        }

        return 0;
    }

    private int calculateHeight() {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();
        int var1 = this.getDistanceUntilTop(var0);
        return var1 >= 3 && var1 <= 21 && this.hasTopFrame(var0, var1) ? var1 : 0;
    }

    private boolean hasTopFrame(BlockPos.MutableBlockPos param0, int param1) {
        for(int var0 = 0; var0 < this.width; ++var0) {
            BlockPos.MutableBlockPos var1 = param0.set(this.bottomLeft).move(Direction.UP, param1).move(this.rightDir, var0);
            if (!FRAME.test(this.level.getBlockState(var1), this.level, var1)) {
                return false;
            }
        }

        return true;
    }

    private int getDistanceUntilTop(BlockPos.MutableBlockPos param0) {
        for(int var0 = 0; var0 < 21; ++var0) {
            param0.set(this.bottomLeft).move(Direction.UP, var0).move(this.rightDir, -1);
            if (!FRAME.test(this.level.getBlockState(param0), this.level, param0)) {
                return var0;
            }

            param0.set(this.bottomLeft).move(Direction.UP, var0).move(this.rightDir, this.width);
            if (!FRAME.test(this.level.getBlockState(param0), this.level, param0)) {
                return var0;
            }

            for(int var1 = 0; var1 < this.width; ++var1) {
                param0.set(this.bottomLeft).move(Direction.UP, var0).move(this.rightDir, var1);
                BlockState var2 = this.level.getBlockState(param0);
                if (!isEmpty(var2)) {
                    return var0;
                }

                if (var2.is(Blocks.NETHER_PORTAL)) {
                    ++this.numPortalBlocks;
                }
            }
        }

        return 21;
    }

    private static boolean isEmpty(BlockState param0) {
        return param0.isAir() || param0.is(BlockTags.FIRE) || param0.is(Blocks.NETHER_PORTAL);
    }

    public boolean isValid() {
        return this.bottomLeft != null && this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
    }

    public void createPortalBlocks() {
        BlockState var0 = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis);
        BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1))
            .forEach(param1 -> this.level.setBlock(param1, var0, 18));
    }

    public boolean isComplete() {
        return this.isValid() && this.numPortalBlocks == this.width * this.height;
    }

    public static Vec3 getRelativePosition(BlockUtil.FoundRectangle param0, Direction.Axis param1, Vec3 param2, EntityDimensions param3) {
        double var0 = (double)param0.axis1Size - (double)param3.width;
        double var1 = (double)param0.axis2Size - (double)param3.height;
        BlockPos var2 = param0.minCorner;
        double var4;
        if (var0 > 0.0) {
            float var3 = (float)var2.get(param1) + param3.width / 2.0F;
            var4 = Mth.clamp(Mth.inverseLerp(param2.get(param1) - (double)var3, 0.0, var0), 0.0, 1.0);
        } else {
            var4 = 0.5;
        }

        double var7;
        if (var1 > 0.0) {
            Direction.Axis var6 = Direction.Axis.Y;
            var7 = Mth.clamp(Mth.inverseLerp(param2.get(var6) - (double)var2.get(var6), 0.0, var1), 0.0, 1.0);
        } else {
            var7 = 0.0;
        }

        Direction.Axis var9 = param1 == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        double var10 = param2.get(var9) - ((double)var2.get(var9) + 0.5);
        return new Vec3(var4, var7, var10);
    }

    public static PortalInfo createPortalInfo(
        ServerLevel param0, BlockUtil.FoundRectangle param1, Direction.Axis param2, Vec3 param3, Entity param4, Vec3 param5, float param6, float param7
    ) {
        BlockPos var0 = param1.minCorner;
        BlockState var1 = param0.getBlockState(var0);
        Direction.Axis var2 = var1.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);
        double var3 = (double)param1.axis1Size;
        double var4 = (double)param1.axis2Size;
        EntityDimensions var5 = param4.getDimensions(param4.getPose());
        int var6 = param2 == var2 ? 0 : 90;
        Vec3 var7 = param2 == var2 ? param5 : new Vec3(param5.z, param5.y, -param5.x);
        double var8 = (double)var5.width / 2.0 + (var3 - (double)var5.width) * param3.x();
        double var9 = (var4 - (double)var5.height) * param3.y();
        double var10 = 0.5 + param3.z();
        boolean var11 = var2 == Direction.Axis.X;
        Vec3 var12 = new Vec3((double)var0.getX() + (var11 ? var8 : var10), (double)var0.getY() + var9, (double)var0.getZ() + (var11 ? var10 : var8));
        Vec3 var13 = findCollisionFreePosition(var12, param0, param4, var5);
        return new PortalInfo(var13, var7, param6 + (float)var6, param7);
    }

    private static Vec3 findCollisionFreePosition(Vec3 param0, ServerLevel param1, Entity param2, EntityDimensions param3) {
        if (!(param3.width > 4.0F) && !(param3.height > 4.0F)) {
            double var0 = (double)param3.height / 2.0;
            Vec3 var1 = param0.add(0.0, var0, 0.0);
            VoxelShape var2 = Shapes.create(AABB.ofSize(var1, (double)param3.width, 0.0, (double)param3.width).expandTowards(0.0, 1.0, 0.0).inflate(1.0E-6));
            Optional<Vec3> var3 = param1.findFreePosition(param2, var2, var1, (double)param3.width, (double)param3.height, (double)param3.width);
            Optional<Vec3> var4 = var3.map(param1x -> param1x.subtract(0.0, var0, 0.0));
            return var4.orElse(param0);
        } else {
            return param0;
        }
    }
}
