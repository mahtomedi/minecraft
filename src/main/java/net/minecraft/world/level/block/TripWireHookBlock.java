package net.minecraft.world.level.block;

import com.google.common.base.MoreObjects;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TripWireHookBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty ATTACHED = BlockStateProperties.ATTACHED;
    protected static final VoxelShape NORTH_AABB = Block.box(5.0, 0.0, 10.0, 11.0, 10.0, 16.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(5.0, 0.0, 0.0, 11.0, 10.0, 6.0);
    protected static final VoxelShape WEST_AABB = Block.box(10.0, 0.0, 5.0, 16.0, 10.0, 11.0);
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 0.0, 5.0, 6.0, 10.0, 11.0);

    public TripWireHookBlock(Block.Properties param0) {
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
        this.calculateState(param0, param1, param2, false, false, -1, null);
    }

    public void calculateState(Level param0, BlockPos param1, BlockState param2, boolean param3, boolean param4, int param5, @Nullable BlockState param6) {
        Direction var0 = param2.getValue(FACING);
        boolean var1 = param2.getValue(ATTACHED);
        boolean var2 = param2.getValue(POWERED);
        boolean var3 = !param3;
        boolean var4 = false;
        int var5 = 0;
        BlockState[] var6 = new BlockState[42];

        for(int var7 = 1; var7 < 42; ++var7) {
            BlockPos var8 = param1.relative(var0, var7);
            BlockState var9 = param0.getBlockState(var8);
            if (var9.getBlock() == Blocks.TRIPWIRE_HOOK) {
                if (var9.getValue(FACING) == var0.getOpposite()) {
                    var5 = var7;
                }
                break;
            }

            if (var9.getBlock() != Blocks.TRIPWIRE && var7 != param5) {
                var6[var7] = null;
                var3 = false;
            } else {
                if (var7 == param5) {
                    var9 = MoreObjects.firstNonNull(param6, var9);
                }

                boolean var10 = !var9.getValue(TripWireBlock.DISARMED);
                boolean var11 = var9.getValue(TripWireBlock.POWERED);
                var4 |= var10 && var11;
                var6[var7] = var9;
                if (var7 == param5) {
                    param0.getBlockTicks().scheduleTick(param1, this, this.getTickDelay(param0));
                    var3 &= var10;
                }
            }
        }

        var3 &= var5 > 1;
        var4 &= var3;
        BlockState var12 = this.defaultBlockState().setValue(ATTACHED, Boolean.valueOf(var3)).setValue(POWERED, Boolean.valueOf(var4));
        if (var5 > 0) {
            BlockPos var13 = param1.relative(var0, var5);
            Direction var14 = var0.getOpposite();
            param0.setBlock(var13, var12.setValue(FACING, var14), 3);
            this.notifyNeighbors(param0, var13, var14);
            this.playSound(param0, var13, var3, var4, var1, var2);
        }

        this.playSound(param0, param1, var3, var4, var1, var2);
        if (!param3) {
            param0.setBlock(param1, var12.setValue(FACING, var0), 3);
            if (param4) {
                this.notifyNeighbors(param0, param1, var0);
            }
        }

        if (var1 != var3) {
            for(int var15 = 1; var15 < var5; ++var15) {
                BlockPos var16 = param1.relative(var0, var15);
                BlockState var17 = var6[var15];
                if (var17 != null) {
                    param0.setBlock(var16, var17.setValue(ATTACHED, Boolean.valueOf(var3)), 3);
                    if (!param0.getBlockState(var16).isAir()) {
                    }
                }
            }
        }

    }

    @Override
    public void tick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        this.calculateState(param1, param2, param0, false, true, -1, null);
    }

    private void playSound(Level param0, BlockPos param1, boolean param2, boolean param3, boolean param4, boolean param5) {
        if (param3 && !param5) {
            param0.playSound(null, param1, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 0.4F, 0.6F);
        } else if (!param3 && param5) {
            param0.playSound(null, param1, SoundEvents.TRIPWIRE_CLICK_OFF, SoundSource.BLOCKS, 0.4F, 0.5F);
        } else if (param2 && !param4) {
            param0.playSound(null, param1, SoundEvents.TRIPWIRE_ATTACH, SoundSource.BLOCKS, 0.4F, 0.7F);
        } else if (!param2 && param4) {
            param0.playSound(null, param1, SoundEvents.TRIPWIRE_DETACH, SoundSource.BLOCKS, 0.4F, 1.2F / (param0.random.nextFloat() * 0.2F + 0.9F));
        }

    }

    private void notifyNeighbors(Level param0, BlockPos param1, Direction param2) {
        param0.updateNeighborsAt(param1, this);
        param0.updateNeighborsAt(param1.relative(param2.getOpposite()), this);
    }

    @Override
    public void onRemove(BlockState param0, Level param1, BlockPos param2, BlockState param3, boolean param4) {
        if (!param4 && param0.getBlock() != param3.getBlock()) {
            boolean var0 = param0.getValue(ATTACHED);
            boolean var1 = param0.getValue(POWERED);
            if (var0 || var1) {
                this.calculateState(param1, param2, param0, true, false, -1, null);
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
