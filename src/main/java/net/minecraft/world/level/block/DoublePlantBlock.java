package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;

public class DoublePlantBlock extends BushBlock {
    public static final MapCodec<DoublePlantBlock> CODEC = simpleCodec(DoublePlantBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    @Override
    public MapCodec<? extends DoublePlantBlock> codec() {
        return CODEC;
    }

    public DoublePlantBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        DoubleBlockHalf var0 = param0.getValue(HALF);
        if (param1.getAxis() != Direction.Axis.Y
            || var0 == DoubleBlockHalf.LOWER != (param1 == Direction.UP)
            || param2.is(this) && param2.getValue(HALF) != var0) {
            return var0 == DoubleBlockHalf.LOWER && param1 == Direction.DOWN && !param0.canSurvive(param3, param4)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(param0, param1, param2, param3, param4, param5);
        } else {
            return Blocks.AIR.defaultBlockState();
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockPos var0 = param0.getClickedPos();
        Level var1 = param0.getLevel();
        return var0.getY() < var1.getMaxBuildHeight() - 1 && var1.getBlockState(var0.above()).canBeReplaced(param0) ? super.getStateForPlacement(param0) : null;
    }

    @Override
    public void setPlacedBy(Level param0, BlockPos param1, BlockState param2, LivingEntity param3, ItemStack param4) {
        BlockPos var0 = param1.above();
        param0.setBlock(var0, copyWaterloggedFrom(param0, var0, this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER)), 3);
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        if (param0.getValue(HALF) != DoubleBlockHalf.UPPER) {
            return super.canSurvive(param0, param1, param2);
        } else {
            BlockState var0 = param1.getBlockState(param2.below());
            return var0.is(this) && var0.getValue(HALF) == DoubleBlockHalf.LOWER;
        }
    }

    public static void placeAt(LevelAccessor param0, BlockState param1, BlockPos param2, int param3) {
        BlockPos var0 = param2.above();
        param0.setBlock(param2, copyWaterloggedFrom(param0, param2, param1.setValue(HALF, DoubleBlockHalf.LOWER)), param3);
        param0.setBlock(var0, copyWaterloggedFrom(param0, var0, param1.setValue(HALF, DoubleBlockHalf.UPPER)), param3);
    }

    public static BlockState copyWaterloggedFrom(LevelReader param0, BlockPos param1, BlockState param2) {
        return param2.hasProperty(BlockStateProperties.WATERLOGGED)
            ? param2.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(param0.isWaterAt(param1)))
            : param2;
    }

    @Override
    public BlockState playerWillDestroy(Level param0, BlockPos param1, BlockState param2, Player param3) {
        if (!param0.isClientSide) {
            if (param3.isCreative()) {
                preventCreativeDropFromBottomPart(param0, param1, param2, param3);
            } else {
                dropResources(param2, param0, param1, null, param3, param3.getMainHandItem());
            }
        }

        return super.playerWillDestroy(param0, param1, param2, param3);
    }

    @Override
    public void playerDestroy(Level param0, Player param1, BlockPos param2, BlockState param3, @Nullable BlockEntity param4, ItemStack param5) {
        super.playerDestroy(param0, param1, param2, Blocks.AIR.defaultBlockState(), param4, param5);
    }

    protected static void preventCreativeDropFromBottomPart(Level param0, BlockPos param1, BlockState param2, Player param3) {
        DoubleBlockHalf var0 = param2.getValue(HALF);
        if (var0 == DoubleBlockHalf.UPPER) {
            BlockPos var1 = param1.below();
            BlockState var2 = param0.getBlockState(var1);
            if (var2.is(param2.getBlock()) && var2.getValue(HALF) == DoubleBlockHalf.LOWER) {
                BlockState var3 = var2.getFluidState().is(Fluids.WATER) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                param0.setBlock(var1, var3, 35);
                param0.levelEvent(param3, 2001, var1, Block.getId(var2));
            }
        }

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(HALF);
    }

    @Override
    public long getSeed(BlockState param0, BlockPos param1) {
        return Mth.getSeed(param1.getX(), param1.below(param0.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1).getY(), param1.getZ());
    }
}
