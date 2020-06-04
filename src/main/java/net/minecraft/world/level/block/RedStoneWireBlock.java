package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.math.Vector3f;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
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
    private static final VoxelShape SHAPE_DOT = Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 13.0);
    private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(
        ImmutableMap.of(
            Direction.NORTH,
            Block.box(3.0, 0.0, 0.0, 13.0, 1.0, 13.0),
            Direction.SOUTH,
            Block.box(3.0, 0.0, 3.0, 13.0, 1.0, 16.0),
            Direction.EAST,
            Block.box(3.0, 0.0, 3.0, 16.0, 1.0, 13.0),
            Direction.WEST,
            Block.box(0.0, 0.0, 3.0, 13.0, 1.0, 13.0)
        )
    );
    private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(
        ImmutableMap.of(
            Direction.NORTH,
            Shapes.or(SHAPES_FLOOR.get(Direction.NORTH), Block.box(3.0, 0.0, 0.0, 13.0, 16.0, 1.0)),
            Direction.SOUTH,
            Shapes.or(SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3.0, 0.0, 15.0, 13.0, 16.0, 16.0)),
            Direction.EAST,
            Shapes.or(SHAPES_FLOOR.get(Direction.EAST), Block.box(15.0, 0.0, 3.0, 16.0, 16.0, 13.0)),
            Direction.WEST,
            Shapes.or(SHAPES_FLOOR.get(Direction.WEST), Block.box(0.0, 0.0, 3.0, 1.0, 16.0, 13.0))
        )
    );
    private final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();
    private static final Vector3f[] COLORS = new Vector3f[16];
    private final BlockState dotState;
    private boolean shouldSignal = true;

    public RedStoneWireBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(NORTH, RedstoneSide.SIDE)
                .setValue(EAST, RedstoneSide.SIDE)
                .setValue(SOUTH, RedstoneSide.SIDE)
                .setValue(WEST, RedstoneSide.SIDE)
                .setValue(POWER, Integer.valueOf(0))
        );
        this.dotState = this.defaultBlockState()
            .setValue(NORTH, RedstoneSide.NONE)
            .setValue(EAST, RedstoneSide.NONE)
            .setValue(SOUTH, RedstoneSide.NONE)
            .setValue(WEST, RedstoneSide.NONE);

        for(BlockState var0 : this.getStateDefinition().getPossibleStates()) {
            if (var0.getValue(POWER) == 0) {
                this.SHAPES_CACHE.put(var0, this.calculateShape(var0));
            }
        }

    }

    private VoxelShape calculateShape(BlockState param0) {
        VoxelShape var0 = SHAPE_DOT;

        for(Direction var1 : Direction.Plane.HORIZONTAL) {
            RedstoneSide var2 = param0.getValue(PROPERTY_BY_DIRECTION.get(var1));
            if (var2 == RedstoneSide.SIDE) {
                var0 = Shapes.or(var0, SHAPES_FLOOR.get(var1));
            } else if (var2 == RedstoneSide.UP) {
                var0 = Shapes.or(var0, SHAPES_UP.get(var1));
            }
        }

        return var0;
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return this.SHAPES_CACHE.get(param0.setValue(POWER, Integer.valueOf(0)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        return this.getConnectionState(param0.getLevel(), this.defaultBlockState(), param0.getClickedPos());
    }

    private BlockState getConnectionState(BlockGetter param0, BlockState param1, BlockPos param2) {
        boolean var0 = isDot(param1);
        param1 = this.getMissingConnections(param0, this.dotState.setValue(POWER, param1.getValue(POWER)), param2);
        boolean var1 = param1.getValue(NORTH).isConnected();
        boolean var2 = param1.getValue(SOUTH).isConnected();
        boolean var3 = param1.getValue(EAST).isConnected();
        boolean var4 = param1.getValue(WEST).isConnected();
        boolean var5 = !var1 && !var2;
        boolean var6 = !var3 && !var4;
        if (var0 && isDot(param1)) {
            return param1;
        } else {
            if (!var4 && var5) {
                param1 = param1.setValue(WEST, RedstoneSide.SIDE);
            }

            if (!var3 && var5) {
                param1 = param1.setValue(EAST, RedstoneSide.SIDE);
            }

            if (!var1 && var6) {
                param1 = param1.setValue(NORTH, RedstoneSide.SIDE);
            }

            if (!var2 && var6) {
                param1 = param1.setValue(SOUTH, RedstoneSide.SIDE);
            }

            return param1;
        }
    }

    private BlockState getMissingConnections(BlockGetter param0, BlockState param1, BlockPos param2) {
        boolean var0 = !param0.getBlockState(param2.above()).isRedstoneConductor(param0, param2);

        for(Direction var1 : Direction.Plane.HORIZONTAL) {
            if (!param1.getValue(PROPERTY_BY_DIRECTION.get(var1)).isConnected()) {
                RedstoneSide var2 = this.getConnectingSide(param0, param2, var1, var0);
                param1 = param1.setValue(PROPERTY_BY_DIRECTION.get(var1), var2);
            }
        }

        return param1;
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param1 == Direction.DOWN) {
            return param0;
        } else if (param1 == Direction.UP) {
            return this.getConnectionState(param3, param0, param4);
        } else {
            RedstoneSide var0 = this.getConnectingSide(param3, param4, param1);
            return var0.isConnected() == param0.getValue(PROPERTY_BY_DIRECTION.get(param1)).isConnected() && !isCross(param0)
                ? param0.setValue(PROPERTY_BY_DIRECTION.get(param1), var0)
                : this.getConnectionState(
                    param3, this.defaultBlockState().setValue(POWER, param0.getValue(POWER)).setValue(PROPERTY_BY_DIRECTION.get(param1), var0), param4
                );
        }
    }

    private static boolean isCross(BlockState param0) {
        return param0.getValue(NORTH).isConnected()
            && param0.getValue(SOUTH).isConnected()
            && param0.getValue(EAST).isConnected()
            && param0.getValue(WEST).isConnected();
    }

    private static boolean isDot(BlockState param0) {
        return !param0.getValue(NORTH).isConnected()
            && !param0.getValue(SOUTH).isConnected()
            && !param0.getValue(EAST).isConnected()
            && !param0.getValue(WEST).isConnected();
    }

    @Override
    public void updateIndirectNeighbourShapes(BlockState param0, LevelAccessor param1, BlockPos param2, int param3) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(Direction var1 : Direction.Plane.HORIZONTAL) {
            RedstoneSide var2 = param0.getValue(PROPERTY_BY_DIRECTION.get(var1));
            if (var2 != RedstoneSide.NONE && !param1.getBlockState(var0.setWithOffset(param2, var1)).is(this)) {
                var0.move(Direction.DOWN);
                BlockState var3 = param1.getBlockState(var0);
                if (!var3.is(Blocks.OBSERVER)) {
                    BlockPos var4 = var0.relative(var1.getOpposite());
                    BlockState var5 = var3.updateShape(var1.getOpposite(), param1.getBlockState(var4), param1, var0, var4);
                    updateOrDestroy(var3, var5, param1, var0, param3);
                }

                var0.setWithOffset(param2, var1).move(Direction.UP);
                BlockState var6 = param1.getBlockState(var0);
                if (!var6.is(Blocks.OBSERVER)) {
                    BlockPos var7 = var0.relative(var1.getOpposite());
                    BlockState var8 = var6.updateShape(var1.getOpposite(), param1.getBlockState(var7), param1, var0, var7);
                    updateOrDestroy(var6, var8, param1, var0, param3);
                }
            }
        }

    }

    private RedstoneSide getConnectingSide(BlockGetter param0, BlockPos param1, Direction param2) {
        return this.getConnectingSide(param0, param1, param2, !param0.getBlockState(param1.above()).isRedstoneConductor(param0, param1));
    }

    private RedstoneSide getConnectingSide(BlockGetter param0, BlockPos param1, Direction param2, boolean param3) {
        BlockPos var0 = param1.relative(param2);
        BlockState var1 = param0.getBlockState(var0);
        if (param3) {
            boolean var2 = this.canSurviveOn(param0, var0, var1);
            if (var2 && shouldConnectTo(param0.getBlockState(var0.above()))) {
                if (var1.isFaceSturdy(param0, var0, param2.getOpposite())) {
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
        return this.canSurviveOn(param1, var0, var1);
    }

    private boolean canSurviveOn(BlockGetter param0, BlockPos param1, BlockState param2) {
        return param2.isFaceSturdy(param0, param1, Direction.UP) || param2.is(Blocks.HOPPER);
    }

    private void updatePowerStrength(Level param0, BlockPos param1, BlockState param2) {
        int var0 = this.calculateTargetStrength(param0, param1);
        if (param2.getValue(POWER) != var0) {
            if (param0.getBlockState(param1) == param2) {
                param0.setBlock(param1, param2.setValue(POWER, Integer.valueOf(var0)), 2);
            }

            Set<BlockPos> var1 = Sets.newHashSet();
            var1.add(param1);

            for(Direction var2 : Direction.values()) {
                var1.add(param1.relative(var2));
            }

            for(BlockPos var3 : var1) {
                param0.updateNeighborsAt(var3, this);
            }
        }

    }

    private int calculateTargetStrength(Level param0, BlockPos param1) {
        this.shouldSignal = false;
        int var0 = param0.getBestNeighborSignal(param1);
        this.shouldSignal = true;
        int var1 = 0;
        if (var0 < 15) {
            for(Direction var2 : Direction.Plane.HORIZONTAL) {
                BlockPos var3 = param1.relative(var2);
                BlockState var4 = param0.getBlockState(var3);
                var1 = Math.max(var1, this.getWireSignal(var4));
                BlockPos var5 = param1.above();
                if (var4.isRedstoneConductor(param0, var3) && !param0.getBlockState(var5).isRedstoneConductor(param0, var5)) {
                    var1 = Math.max(var1, this.getWireSignal(param0.getBlockState(var3.above())));
                } else if (!var4.isRedstoneConductor(param0, var3)) {
                    var1 = Math.max(var1, this.getWireSignal(param0.getBlockState(var3.below())));
                }
            }
        }

        return Math.max(var0, var1 - 1);
    }

    private int getWireSignal(BlockState param0) {
        return param0.is(this) ? param0.getValue(POWER) : 0;
    }

    private void checkCornerChangeAt(Level param0, BlockPos param1) {
        if (param0.getBlockState(param1).is(this)) {
            param0.updateNeighborsAt(param1, this);

            for(Direction var0 : Direction.values()) {
                param0.updateNeighborsAt(param1.relative(var0), this);
            }

        }
    }

    @Override
    public void onPlace(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param3.is(param0.getBlock()) && !param1.isClientSide) {
            param1.setBlock(param2, this.getConnectionState(param1, this.defaultBlockState(), param2), 2);
            this.updatePowerStrength(param1, param2, param0);

            for(Direction var0 : Direction.Plane.VERTICAL) {
                param1.updateNeighborsAt(param2.relative(var0), this);
            }

            this.updateNeighborsOfNeighboringWires(param1, param2);
        }
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param4 && !param0.is(param3.getBlock())) {
            super.onRemove(param0, param1, param2, param3, param4);
            if (!param1.isClientSide) {
                for(Direction var0 : Direction.values()) {
                    param1.updateNeighborsAt(param2.relative(var0), this);
                }

                this.updatePowerStrength(param1, param2, param0);
                this.updateNeighborsOfNeighboringWires(param1, param2);
            }
        }
    }

    private void updateNeighborsOfNeighboringWires(Level param0, BlockPos param1) {
        for(Direction var0 : Direction.Plane.HORIZONTAL) {
            this.checkCornerChangeAt(param0, param1.relative(var0));
        }

        for(Direction var1 : Direction.Plane.HORIZONTAL) {
            BlockPos var2 = param1.relative(var1);
            if (param0.getBlockState(var2).isRedstoneConductor(param0, var2)) {
                this.checkCornerChangeAt(param0, var2.above());
            } else {
                this.checkCornerChangeAt(param0, var2.below());
            }
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
        if (this.shouldSignal && param3 != Direction.DOWN) {
            int var0 = param0.getValue(POWER);
            if (var0 == 0) {
                return 0;
            } else {
                return param3 != Direction.UP
                        && !this.getConnectionState(param1, param0, param2).getValue(PROPERTY_BY_DIRECTION.get(param3.getOpposite())).isConnected()
                    ? 0
                    : var0;
            }
        } else {
            return 0;
        }
    }

    protected static boolean shouldConnectTo(BlockState param0) {
        return shouldConnectTo(param0, null);
    }

    protected static boolean shouldConnectTo(BlockState param0, @Nullable Direction param1) {
        if (param0.is(Blocks.REDSTONE_WIRE)) {
            return true;
        } else if (param0.is(Blocks.REPEATER)) {
            Direction var0 = param0.getValue(RepeaterBlock.FACING);
            return var0 == param1 || var0.getOpposite() == param1;
        } else if (param0.is(Blocks.OBSERVER)) {
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
    public static int getColorForPower(int param0) {
        Vector3f var0 = COLORS[param0];
        return Mth.color(var0.x(), var0.y(), var0.z());
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnParticlesAlongLine(
        Level param0, Random param1, BlockPos param2, Vector3f param3, Direction param4, Direction param5, float param6, float param7
    ) {
        float var0 = param7 - param6;
        if (!(param1.nextFloat() >= 0.2F * var0)) {
            float var1 = 0.4375F;
            float var2 = param6 + var0 * param1.nextFloat();
            double var3 = 0.5 + (double)(0.4375F * (float)param4.getStepX()) + (double)(var2 * (float)param5.getStepX());
            double var4 = 0.5 + (double)(0.4375F * (float)param4.getStepY()) + (double)(var2 * (float)param5.getStepY());
            double var5 = 0.5 + (double)(0.4375F * (float)param4.getStepZ()) + (double)(var2 * (float)param5.getStepZ());
            param0.addParticle(
                new DustParticleOptions(param3.x(), param3.y(), param3.z(), 1.0F),
                (double)param2.getX() + var3,
                (double)param2.getY() + var4,
                (double)param2.getZ() + var5,
                0.0,
                0.0,
                0.0
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        int var0 = param0.getValue(POWER);
        if (var0 != 0) {
            for(Direction var1 : Direction.Plane.HORIZONTAL) {
                RedstoneSide var2 = param0.getValue(PROPERTY_BY_DIRECTION.get(var1));
                switch(var2) {
                    case UP:
                        this.spawnParticlesAlongLine(param1, param3, param2, COLORS[var0], var1, Direction.UP, -0.5F, 0.5F);
                    case SIDE:
                        this.spawnParticlesAlongLine(param1, param3, param2, COLORS[var0], Direction.DOWN, var1, 0.0F, 0.5F);
                        break;
                    case NONE:
                    default:
                        this.spawnParticlesAlongLine(param1, param3, param2, COLORS[var0], Direction.DOWN, var1, 0.0F, 0.3F);
                }
            }

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

    @Override
    public InteractionResult use(BlockState param0, Level param1, BlockPos param2, Player param3, InteractionHand param4, BlockHitResult param5) {
        if (!param3.abilities.mayBuild) {
            return InteractionResult.PASS;
        } else {
            if (isCross(param0) || isDot(param0)) {
                BlockState var0 = isCross(param0) ? this.dotState : this.defaultBlockState();
                var0 = var0.setValue(POWER, param0.getValue(POWER));
                var0 = this.getConnectionState(param1, var0, param2);
                if (var0 != param0) {
                    param1.setBlock(param2, var0, 3);
                    this.updatesOnShapeChange(param1, param2, param0, var0);
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.PASS;
        }
    }

    private void updatesOnShapeChange(Level param0, BlockPos param1, BlockState param2, BlockState param3) {
        for(Direction var0 : Direction.Plane.HORIZONTAL) {
            BlockPos var1 = param1.relative(var0);
            if (param2.getValue(PROPERTY_BY_DIRECTION.get(var0)).isConnected() != param3.getValue(PROPERTY_BY_DIRECTION.get(var0)).isConnected()
                && param0.getBlockState(var1).isRedstoneConductor(param0, var1)) {
                param0.updateNeighborsAtExceptFromFacing(var1, param3.getBlock(), var0.getOpposite());
            }
        }

    }

    static {
        for(int var0 = 0; var0 <= 15; ++var0) {
            float var1 = (float)var0 / 15.0F;
            float var2 = var1 * 0.6F + (var1 > 0.0F ? 0.4F : 0.3F);
            float var3 = Mth.clamp(var1 * var1 * 0.7F - 0.5F, 0.0F, 1.0F);
            float var4 = Mth.clamp(var1 * var1 * 0.6F - 0.7F, 0.0F, 1.0F);
            COLORS[var0] = new Vector3f(var2, var3, var4);
        }

    }
}
