package net.minecraft.world.level.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class KelpBlock extends Block implements LiquidBlockContainer {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_25;
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 9.0, 16.0);

    protected KelpBlock(Block.Properties param0) {
        super(param0);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
    }

    @Override
    public VoxelShape getShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        FluidState var0 = param0.getLevel().getFluidState(param0.getClickedPos());
        return var0.is(FluidTags.WATER) && var0.getAmount() == 8 ? this.getStateForPlacement(param0.getLevel()) : null;
    }

    public BlockState getStateForPlacement(LevelAccessor param0) {
        return this.defaultBlockState().setValue(AGE, Integer.valueOf(param0.getRandom().nextInt(25)));
    }

    @Override
    public BlockLayer getRenderLayer() {
        return BlockLayer.CUTOUT;
    }

    @Override
    public FluidState getFluidState(BlockState param0) {
        return Fluids.WATER.getSource(false);
    }

    @Override
    public void tick(BlockState param0, Level param1, BlockPos param2, Random param3) {
        if (!param0.canSurvive(param1, param2)) {
            param1.destroyBlock(param2, true);
        } else {
            BlockPos var0 = param2.above();
            BlockState var1 = param1.getBlockState(var0);
            if (var1.getBlock() == Blocks.WATER && param0.getValue(AGE) < 25 && param3.nextDouble() < 0.14) {
                param1.setBlockAndUpdate(var0, param0.cycle(AGE));
            }

        }
    }

    @Override
    public boolean canSurvive(BlockState param0, LevelReader param1, BlockPos param2) {
        BlockPos var0 = param2.below();
        BlockState var1 = param1.getBlockState(var0);
        Block var2 = var1.getBlock();
        if (var2 == Blocks.MAGMA_BLOCK) {
            return false;
        } else {
            return var2 == this || var2 == Blocks.KELP_PLANT || var1.isFaceSturdy(param1, var0, Direction.UP);
        }
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (!param0.canSurvive(param3, param4)) {
            if (param1 == Direction.DOWN) {
                return Blocks.AIR.defaultBlockState();
            }

            param3.getBlockTicks().scheduleTick(param4, this, 1);
        }

        if (param1 == Direction.UP && param2.getBlock() == this) {
            return Blocks.KELP_PLANT.defaultBlockState();
        } else {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
            return super.updateShape(param0, param1, param2, param3, param4, param5);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(AGE);
    }

    @Override
    public boolean canPlaceLiquid(BlockGetter param0, BlockPos param1, BlockState param2, Fluid param3) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor param0, BlockPos param1, BlockState param2, FluidState param3) {
        return false;
    }
}
