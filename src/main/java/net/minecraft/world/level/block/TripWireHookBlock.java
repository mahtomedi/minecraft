package net.minecraft.world.level.block;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TripWireHookBlock extends Block {
    public static final MapCodec<TripWireHookBlock> CODEC = simpleCodec(TripWireHookBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    protected static final int WIRE_DIST_MIN = 1;
    protected static final int WIRE_DIST_MAX = 42;
    private static final int RECHECK_PERIOD = 10;
    protected static final int AABB_OFFSET = 3;
    protected static final VoxelShape NORTH_AABB = Block.box(5.0, 0.0, 10.0, 11.0, 10.0, 16.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(5.0, 0.0, 0.0, 11.0, 10.0, 6.0);
    protected static final VoxelShape WEST_AABB = Block.box(10.0, 0.0, 5.0, 16.0, 10.0, 11.0);
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 5.0, 6.0, 10.0, 11.0);

    @Override
    public MapCodec<TripWireHookBlock> codec() {
        return CODEC;
    }

    public TripWireHookBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(
            this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(ATTACHED, Boolean.valueOf(false))
        );
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        switch((Direction)param0.getValue(FACING)) {
            case EAST:
            default:
                return EAST_AABB;
            case WEST:
                return WEST_AABB;
            case SOUTH:
                return SOUTH_AABB;
            case NORTH:
                return NORTH_AABB;
        }
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        Direction var0 = param0.getValue(FACING);
        BlockPos var1 = param2.relative(var0.getOpposite());
        BlockState var2 = param1.getBlockState(var1);
        return var0.getAxis().isHorizontal() && var2.isFaceSturdy(param1, var1, var0);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return param1.getOpposite() == param0.getValue(FACING) && !param0.canSurvive(param3, param4)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockState var0 = this.defaultBlockState().setValue(POWERED, Boolean.valueOf(false)).setValue(ATTACHED, Boolean.valueOf(false));
        LevelReader var1 = param0.getLevel();
        BlockPos var2 = param0.getClickedPos();
        Direction[] var3 = param0.getNearestLookingDirections();

        for(Direction var4 : var3) {
            if (var4.getAxis().isHorizontal()) {
                Direction var5 = var4.getOpposite();
                var0 = var0.setValue(FACING, var5);
                if (var0.canSurvive(var1, var2)) {
                    return var0;
                }
            }
        }

        return null;
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        calculateState(param0, param1, param2, false, false, -1, null);
    }

    public static void calculateState(Level param0, BlockPos param1, BlockState param2, boolean param3, boolean param4, int param5, @Nullable BlockState param6) {
        Optional<Direction> var0 = param2.getOptionalValue(FACING);
        if (var0.isPresent()) {
            Direction var1 = var0.get();
            boolean var2 = param2.getOptionalValue(ATTACHED).orElse(false);
            boolean var3 = param2.getOptionalValue(POWERED).orElse(false);
            Block var4 = param2.getBlock();
            boolean var5 = !param3;
            boolean var6 = false;
            int var7 = 0;
            BlockState[] var8 = new BlockState[42];

            for(int var9 = 1; var9 < 42; ++var9) {
                BlockPos var10 = param1.relative(var1, var9);
                BlockState var11 = param0.getBlockState(var10);
                if (var11.is(Blocks.TRIPWIRE_HOOK)) {
                    if (var11.getValue(FACING) == var1.getOpposite()) {
                        var7 = var9;
                    }
                    break;
                }

                if (!var11.is(Blocks.TRIPWIRE) && var9 != param5) {
                    var8[var9] = null;
                    var5 = false;
                } else {
                    if (var9 == param5) {
                        var11 = MoreObjects.firstNonNull(param6, var11);
                    }

                    boolean var12 = !var11.getValue(TripWireBlock.DISARMED);
                    boolean var13 = var11.getValue(TripWireBlock.POWERED);
                    var6 |= var12 && var13;
                    var8[var9] = var11;
                    if (var9 == param5) {
                        param0.scheduleTick(param1, var4, 10);
                        var5 &= var12;
                    }
                }
            }

            var5 &= var7 > 1;
            var6 &= var5;
            BlockState var14 = var4.defaultBlockState().trySetValue(ATTACHED, Boolean.valueOf(var5)).trySetValue(POWERED, Boolean.valueOf(var6));
            if (var7 > 0) {
                BlockPos var15 = param1.relative(var1, var7);
                Direction var16 = var1.getOpposite();
                param0.setBlock(var15, var14.setValue(FACING, var16), 3);
                notifyNeighbors(var4, param0, var15, var16);
                emitState(param0, var15, var5, var6, var2, var3);
            }

            emitState(param0, param1, var5, var6, var2, var3);
            if (!param3) {
                param0.setBlock(param1, var14.setValue(FACING, var1), 3);
                if (param4) {
                    notifyNeighbors(var4, param0, param1, var1);
                }
            }

            if (var2 != var5) {
                for(int var17 = 1; var17 < var7; ++var17) {
                    BlockPos var18 = param1.relative(var1, var17);
                    BlockState var19 = var8[var17];
                    if (var19 != null) {
                        param0.setBlock(var18, var19.trySetValue(ATTACHED, Boolean.valueOf(var5)), 3);
                        if (!param0.getBlockState(var18).isAir()) {
                        }
                    }
                }
            }

        }
    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        calculateState(param1, param2, param0, false, true, -1, null);
    }

    private static void emitState(Level param0, BlockPos param1, boolean param2, boolean param3, boolean param4, boolean param5) {
        if (param3 && !param5) {
            param0.playSound(null, param1, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.4F, 0.6F);
            param0.gameEvent(null, GameEvent.BLOCK_ACTIVATE, param1);
        } else if (!param3 && param5) {
            param0.playSound(null, param1, SoundEvents.TRIPWIRE_CLICK_OFF, SoundSource.BLOCKS, 0.4F, 0.5F);
            param0.gameEvent(null, GameEvent.BLOCK_DEACTIVATE, param1);
        } else if (param2 && !param4) {
            param0.playSound(null, param1, SoundEvents.TRIPWIRE_ATTACH, SoundSource.BLOCKS, 0.4F, 0.7F);
            param0.gameEvent(null, GameEvent.BLOCK_ATTACH, param1);
        } else if (!param2 && param4) {
            param0.playSound(null, param1, SoundEvents.TRIPWIRE_DETACH, SoundSource.BLOCKS, 0.4F, 1.2F / (param0.random.nextFloat() * 0.2F + 0.9F));
            param0.gameEvent(null, GameEvent.BLOCK_DETACH, param1);
        }

    }

    private static void notifyNeighbors(Block param0, Level param1, BlockPos param2, Direction param3) {
        param1.updateNeighborsAt(param2, param0);
        param1.updateNeighborsAt(param2.relative(param3.getOpposite()), param0);
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param4 && !param0.is(param3.getBlock())) {
            boolean var0 = param0.getValue(ATTACHED);
            boolean var1 = param0.getValue(POWERED);
            if (var0 || var1) {
                calculateState(param1, param2, param0, true, false, -1, null);
            }

            if (var1) {
                param1.updateNeighborsAt(param2, this);
                param1.updateNeighborsAt(param2.relative(param0.getValue(FACING).getOpposite()), this);
            }

            super.onRemove(param0, param1, param2, param3, param4);
        }
    }

    @Override
    public int getSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        return param0.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState param0, BlockGetter param1, BlockPos param2, Direction param3) {
        if (!param0.getValue(POWERED)) {
            return 0;
        } else {
            return param0.getValue(FACING) == param3 ? 15 : 0;
        }
    }

    @Override
    public boolean isSignalSource(BlockState param0) {
        return true;
    }

    @Override
    public BlockState rotate(BlockState param0, Rotation param1) {
        return param0.setValue(FACING, param1.rotate(param0.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState param0, Mirror param1) {
        return param0.rotate(param1.getRotation(param0.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(FACING, POWERED, ATTACHED);
    }
}
