package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedStoneWireBlock extends Block {
    public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
    public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(
        ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST)
    );
    protected static final VoxelShape[] SHAPE_BY_INDEX = new VoxelShape[]{
        Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0),
        Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 16.0),
        Block.box(0.0, 0.0, 3.0, 13.0, 1.0, 13.0),
        Block.box(0.0, 0.0, 3.0, 13.0, 1.0, 16.0),
        Block.box(3.0, 0.0, 0.0, 13.0, 1.0, 13.0),
        Block.box(3.0, 0.0, 0.0, 13.0, 1.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 13.0, 1.0, 13.0),
        Block.box(0.0, 0.0, 0.0, 13.0, 1.0, 16.0),
        Block.box(3.0, 0.0, 3.0, 16.0, 1.0, 13.0),
        Block.box(3.0, 0.0, 3.0, 16.0, 1.0, 16.0),
        Block.box(0.0, 0.0, 3.0, 16.0, 1.0, 13.0),
        Block.box(0.0, 0.0, 3.0, 16.0, 1.0, 16.0),
        Block.box(3.0, 0.0, 0.0, 16.0, 1.0, 13.0),
        Block.box(3.0, 0.0, 0.0, 16.0, 1.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 13.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0)
    };
    private boolean shouldSignal = true;
    private final Set<BlockPos> toUpdate = Sets.newHashSet();

    public RedStoneWireBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(NORTH, RedstoneSide.NONE)
                .setValue(EAST, RedstoneSide.NONE)
                .setValue(SOUTH, RedstoneSide.NONE)
                .setValue(WEST, RedstoneSide.NONE)
                .setValue(POWER, Integer.valueOf(0))
        );
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE_BY_INDEX[getAABBIndex(param0)];
    }

    private static int getAABBIndex(BlockState param0) {
        int var0 = 0;
        boolean var1 = param0.getValue(NORTH) != RedstoneSide.NONE;
        boolean var2 = param0.getValue(EAST) != RedstoneSide.NONE;
        boolean var3 = param0.getValue(SOUTH) != RedstoneSide.NONE;
        boolean var4 = param0.getValue(WEST) != RedstoneSide.NONE;
        if (var1 || var3 && !var1 && !var2 && !var4) {
            var0 |= 1 << Direction.NORTH.get2DDataValue();
        }

        if (var2 || var4 && !var1 && !var2 && !var3) {
            var0 |= 1 << Direction.EAST.get2DDataValue();
        }

        if (var3 || var1 && !var2 && !var3 && !var4) {
            var0 |= 1 << Direction.SOUTH.get2DDataValue();
        }

        if (var4 || var2 && !var1 && !var3 && !var4) {
            var0 |= 1 << Direction.WEST.get2DDataValue();
        }

        return var0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockGetter var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        return this.defaultBlockState()
            .setValue(WEST, this.getConnectingSide(var0, var1, Direction.WEST))
            .setValue(EAST, this.getConnectingSide(var0, var1, Direction.EAST))
            .setValue(NORTH, this.getConnectingSide(var0, var1, Direction.NORTH))
            .setValue(SOUTH, this.getConnectingSide(var0, var1, Direction.SOUTH));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == Direction.DOWN) {
            return param0;
        } else {
            return param1 == Direction.UP
                ? param0.setValue(WEST, this.getConnectingSide(param3, param4, Direction.WEST))
                    .setValue(EAST, this.getConnectingSide(param3, param4, Direction.EAST))
                    .setValue(NORTH, this.getConnectingSide(param3, param4, Direction.NORTH))
                    .setValue(SOUTH, this.getConnectingSide(param3, param4, Direction.SOUTH))
                : param0.setValue(PROPERTY_BY_DIRECTION.get(param1), this.getConnectingSide(param3, param4, param1));
        }
    }

    @Override
    public void updateIndirectNeighbourShapes(BlockState param0, LevelAccessor param1, BlockPos param2, int param3) {
        try (BlockPos.PooledMutableBlockPos var0 = BlockPos.PooledMutableBlockPos.acquire()) {
            for(Direction var1 : Direction.Plane.HORIZONTAL) {
                RedstoneSide var2 = param0.getValue(PROPERTY_BY_DIRECTION.get(var1));
                if (var2 != RedstoneSide.NONE && param1.getBlockState(var0.set(param2).move(var1)).getBlock() != this) {
                    var0.move(Direction.DOWN);
                    BlockState var3 = param1.getBlockState(var0);
                    if (var3.getBlock() != Blocks.OBSERVER) {
                        BlockPos var4 = var0.relative(var1.getOpposite());
                        BlockState var5 = var3.updateShape(var1.getOpposite(), param1.getBlockState(var4), param1, var0, var4);
                        updateOrDestroy(var3, var5, param1, var0, param3);
                    }

                    var0.set(param2).move(var1).move(Direction.UP);
                    BlockState var6 = param1.getBlockState(var0);
                    if (var6.getBlock() != Blocks.OBSERVER) {
                        BlockPos var7 = var0.relative(var1.getOpposite());
                        BlockState var8 = var6.updateShape(var1.getOpposite(), param1.getBlockState(var7), param1, var0, var7);
                        updateOrDestroy(var6, var8, param1, var0, param3);
                    }
                }
            }
        }

    }

    private RedstoneSide getConnectingSide(BlockGetter param0, BlockPos param1, Direction param2) {
        BlockPos var0 = param1.relative(param2);
        BlockState var1 = param0.getBlockState(var0);
        BlockPos var2 = param1.above();
        BlockState var3 = param0.getBlockState(var2);
        if (!var3.isRedstoneConductor(param0, var2)) {
            boolean var4 = var1.isFaceSturdy(param0, var0, Direction.UP) || var1.getBlock() == Blocks.HOPPER;
            if (var4 && shouldConnectTo(param0.getBlockState(var0.above()))) {
                if (var1.isCollisionShapeFullBlock(param0, var0)) {
                    return RedstoneSide.UP;
                }

                return RedstoneSide.SIDE;
            }
        }

        return !shouldConnectTo(var1, param2) && (var1.isRedstoneConductor(param0, var0) || !shouldConnectTo(param0.getBlockState(var0.below())))
            ? RedstoneSide.NONE
            : RedstoneSide.SIDE;
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.below();
        BlockState var1 = param1.getBlockState(var0);
        return var1.isFaceSturdy(param1, var0, Direction.UP) || var1.getBlock() == Blocks.HOPPER;
    }

    private BlockState updatePowerStrength(Level param0, BlockPos param1, BlockState param2) {
        param2 = this.updatePowerStrengthImpl(param0, param1, param2);
        List<BlockPos> var0 = Lists.newArrayList(this.toUpdate);
        this.toUpdate.clear();

        for(BlockPos var1 : var0) {
            param0.updateNeighborsAt(var1, this);
        }

        return param2;
    }

    private BlockState updatePowerStrengthImpl(Level param0, BlockPos param1, BlockState param2) {
        BlockState var0 = param2;
        int var1 = param2.getValue(POWER);
        this.shouldSignal = false;
        int var2 = param0.getBestNeighborSignal(param1);
        this.shouldSignal = true;
        int var3 = 0;
        if (var2 < 15) {
            for(Direction var4 : Direction.Plane.HORIZONTAL) {
                BlockPos var5 = param1.relative(var4);
                BlockState var6 = param0.getBlockState(var5);
                var3 = this.checkTarget(var3, var6);
                BlockPos var7 = param1.above();
                if (var6.isRedstoneConductor(param0, var5) && !param0.getBlockState(var7).isRedstoneConductor(param0, var7)) {
                    var3 = this.checkTarget(var3, param0.getBlockState(var5.above()));
                } else if (!var6.isRedstoneConductor(param0, var5)) {
                    var3 = this.checkTarget(var3, param0.getBlockState(var5.below()));
                }
            }
        }

        int var8 = var3 - 1;
        if (var2 > var8) {
            var8 = var2;
        }

        if (var1 != var8) {
            param2 = param2.setValue(POWER, Integer.valueOf(var8));
            if (param0.getBlockState(param1) == var0) {
                param0.setBlock(param1, param2, 2);
            }

            this.toUpdate.add(param1);

            for(Direction var9 : Direction.values()) {
                this.toUpdate.add(param1.relative(var9));
            }
        }

        return param2;
    }

    private void checkCornerChangeAt(Level param0, BlockPos param1) {
        if (param0.getBlockState(param1).getBlock() == this) {
            param0.updateNeighborsAt(param1, this);

            for(Direction var0 : Direction.values()) {
                param0.updateNeighborsAt(param1.relative(var0), this);
            }

        }
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (param3.getBlock() != param0.getBlock() && !param1.isClientSide) {
            this.updatePowerStrength(param1, param2, param0);

            for(Direction var0 : Direction.Plane.VERTICAL) {
                param1.updateNeighborsAt(param2.relative(var0), this);
            }

            for(Direction var1 : Direction.Plane.HORIZONTAL) {
                this.checkCornerChangeAt(param1, param2.relative(var1));
            }

            for(Direction var2 : Direction.Plane.HORIZONTAL) {
                BlockPos var3 = param2.relative(var2);
                if (param1.getBlockState(var3).isRedstoneConductor(param1, var3)) {
                    this.checkCornerChangeAt(param1, var3.above());
                } else {
                    this.checkCornerChangeAt(param1, var3.below());
                }
            }

        }
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param4 && param0.getBlock() != param3.getBlock()) {
            super.onRemove(param0, param1, param2, param3, param4);
            if (!param1.isClientSide) {
                for(Direction var0 : Direction.values()) {
                    param1.updateNeighborsAt(param2.relative(var0), this);
                }

                this.updatePowerStrength(param1, param2, param0);

                for(Direction var1 : Direction.Plane.HORIZONTAL) {
                    this.checkCornerChangeAt(param1, param2.relative(var1));
                }

                for(Direction var2 : Direction.Plane.HORIZONTAL) {
                    BlockPos var3 = param2.relative(var2);
                    if (param1.getBlockState(var3).isRedstoneConductor(param1, var3)) {
                        this.checkCornerChangeAt(param1, var3.above());
                    } else {
                        this.checkCornerChangeAt(param1, var3.below());
                    }
                }

            }
        }
    }

    private int checkTarget(int param0, BlockState param1) {
        if (param1.getBlock() != this) {
            return param0;
        } else {
            int var0 = param1.getValue(POWER);
            return var0 > param0 ? var0 : param0;
        }
    }

    @Override
    public void neighborChanged(BlockState param0, Level param1, BlockPos param2, Block param3, BlockPos param4, boolean param5) {
        if (!param1.isClientSide) {
            if (param0.canSurvive(param1, param2)) {
                this.updatePowerStrength(param1, param2, param0);
            } else {
                dropResources(param0, param1, param2);
                param1.removeBlock(param2, false);
            }

        }
    }

    @Override
    public int getDirectSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return !this.shouldSignal ? 0 : param0.getSignal(param1, param2, param3);
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        if (!this.shouldSignal) {
            return 0;
        } else {
            int var0 = param0.getValue(POWER);
            if (var0 == 0) {
                return 0;
            } else if (param3 == Direction.UP) {
                return var0;
            } else {
                EnumSet<Direction> var1 = EnumSet.noneOf(Direction.class);

                for(Direction var2 : Direction.Plane.HORIZONTAL) {
                    if (this.isPowerSourceAt(param1, param2, var2)) {
                        var1.add(var2);
                    }
                }

                if (param3.getAxis().isHorizontal() && var1.isEmpty()) {
                    return var0;
                } else {
                    return var1.contains(param3) && !var1.contains(param3.getCounterClockWise()) && !var1.contains(param3.getClockWise()) ? var0 : 0;
                }
            }
        }
    }

    private boolean isPowerSourceAt(BlockGetter param0, BlockPos param1, Direction param2) {
        BlockPos var0 = param1.relative(param2);
        BlockState var1 = param0.getBlockState(var0);
        boolean var2 = var1.isRedstoneConductor(param0, var0);
        BlockPos var3 = param1.above();
        boolean var4 = param0.getBlockState(var3).isRedstoneConductor(param0, var3);
        if (!var4 && var2 && shouldConnectTo(param0, var0.above())) {
            return true;
        } else if (shouldConnectTo(var1, param2)) {
            return true;
        } else if (var1.getBlock() == Blocks.REPEATER && var1.getValue(DiodeBlock.POWERED) && var1.getValue(DiodeBlock.FACING) == param2) {
            return true;
        } else {
            return !var2 && shouldConnectTo(param0, var0.below());
        }
    }

    protected static boolean shouldConnectTo(BlockGetter param0, BlockPos param1) {
        return shouldConnectTo(param0.getBlockState(param1));
    }

    protected static boolean shouldConnectTo(BlockState param0) {
        return shouldConnectTo(param0, null);
    }

    protected static boolean shouldConnectTo(BlockState param0, @Nullable Direction param1) {
        Block var0 = param0.getBlock();
        if (var0 == Blocks.REDSTONE_WIRE) {
            return true;
        } else if (param0.getBlock() == Blocks.REPEATER) {
            Direction var1 = param0.getValue(RepeaterBlock.FACING);
            return var1 == param1 || var1.getOpposite() == param1;
        } else if (Blocks.OBSERVER == param0.getBlock()) {
            return param1 == param0.getValue(ObserverBlock.FACING);
        } else {
            return param0.isSignalSource() && param1 != null;
        }
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return this.shouldSignal;
    }

    @OnlyIn(Dist.CLIENT)
    public static int getColorForData(int param0) {
        float var0 = (float)param0 / 15.0F;
        float var1 = var0 * 0.6F + 0.4F;
        if (param0 == 0) {
            var1 = 0.3F;
        }

        float var2 = var0 * var0 * 0.7F - 0.5F;
        float var3 = var0 * var0 * 0.6F - 0.7F;
        if (var2 < 0.0F) {
            var2 = 0.0F;
        }

        if (var3 < 0.0F) {
            var3 = 0.0F;
        }

        int var4 = Mth.clamp((int)(var1 * 255.0F), 0, 255);
        int var5 = Mth.clamp((int)(var2 * 255.0F), 0, 255);
        int var6 = Mth.clamp((int)(var3 * 255.0F), 0, 255);
        return 0xFF000000 | var4 << 16 | var5 << 8 | var6;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        int var0 = param0.getValue(POWER);
        if (var0 != 0) {
            double var1 = (double)param2.getX() + 0.5 + ((double)param3.nextFloat() - 0.5) * 0.2;
            double var2 = (double)((float)param2.getY() + 0.0625F);
            double var3 = (double)param2.getZ() + 0.5 + ((double)param3.nextFloat() - 0.5) * 0.2;
            float var4 = (float)var0 / 15.0F;
            float var5 = var4 * 0.6F + 0.4F;
            float var6 = Math.max(0.0F, var4 * var4 * 0.7F - 0.5F);
            float var7 = Math.max(0.0F, var4 * var4 * 0.6F - 0.7F);
            param1.addParticle(new DustParticleOptions(var5, var6, var7, 1.0F), var1, var2, var3, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        switch(param1) {
            case CLOCKWISE_180:
                return param0.setValue(NORTH, param0.getValue(SOUTH))
                    .setValue(EAST, param0.getValue(WEST))
                    .setValue(SOUTH, param0.getValue(NORTH))
                    .setValue(WEST, param0.getValue(EAST));
            case COUNTERCLOCKWISE_90:
                return param0.setValue(NORTH, param0.getValue(EAST))
                    .setValue(EAST, param0.getValue(SOUTH))
                    .setValue(SOUTH, param0.getValue(WEST))
                    .setValue(WEST, param0.getValue(NORTH));
            case CLOCKWISE_90:
                return param0.setValue(NORTH, param0.getValue(WEST))
                    .setValue(EAST, param0.getValue(NORTH))
                    .setValue(SOUTH, param0.getValue(EAST))
                    .setValue(WEST, param0.getValue(SOUTH));
            default:
                return param0;
        }
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        switch(param1) {
            case LEFT_RIGHT:
                return param0.setValue(NORTH, param0.getValue(SOUTH)).setValue(SOUTH, param0.getValue(NORTH));
            case FRONT_BACK:
                return param0.setValue(EAST, param0.getValue(WEST)).setValue(WEST, param0.getValue(EAST));
            default:
                return super.mirror(param0, param1);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(NORTH, EAST, SOUTH, WEST, POWER);
    }
}
