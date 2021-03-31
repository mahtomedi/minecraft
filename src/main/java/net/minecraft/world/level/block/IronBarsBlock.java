package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IronBarsBlock extends CrossCollisionBlock {
    protected IronBarsBlock(BlockBehaviour.Properties param0) {
        super(1.0F, 1.0F, 16.0F, 16.0F, 16.0F, param0);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(NORTH, Boolean.valueOf(false))
                .setValue(EAST, Boolean.valueOf(false))
                .setValue(SOUTH, Boolean.valueOf(false))
                .setValue(WEST, Boolean.valueOf(false))
                .setValue(WATERLOGGED, Boolean.valueOf(false))
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockGetter var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        FluidState var2 = param0.getLevel().getFluidState(param0.getClickedPos());
        BlockPos var3 = var1.north();
        BlockPos var4 = var1.south();
        BlockPos var5 = var1.west();
        BlockPos var6 = var1.east();
        BlockState var7 = var0.getBlockState(var3);
        BlockState var8 = var0.getBlockState(var4);
        BlockState var9 = var0.getBlockState(var5);
        BlockState var10 = var0.getBlockState(var6);
        return this.defaultBlockState()
            .setValue(NORTH, Boolean.valueOf(this.attachsTo(var7, var7.isFaceSturdy(var0, var3, Direction.SOUTH))))
            .setValue(SOUTH, Boolean.valueOf(this.attachsTo(var8, var8.isFaceSturdy(var0, var4, Direction.NORTH))))
            .setValue(WEST, Boolean.valueOf(this.attachsTo(var9, var9.isFaceSturdy(var0, var5, Direction.EAST))))
            .setValue(EAST, Boolean.valueOf(this.attachsTo(var10, var10.isFaceSturdy(var0, var6, Direction.WEST))))
            .setValue(WATERLOGGED, Boolean.valueOf(var2.getType() == Fluids.WATER));
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        if (param0.getValue(WATERLOGGED)) {
            param3.getLiquidTicks().scheduleTick(param4, Fluids.WATER, Fluids.WATER.getTickDelay(param3));
        }

        return param1.getAxis().isHorizontal()
            ? param0.setValue(
                PROPERTY_BY_DIRECTION.get(param1), Boolean.valueOf(this.attachsTo(param2, param2.isFaceSturdy(param3, param5, param1.getOpposite())))
            )
            : super.updateShape(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public VoxelShape getVisualShape(BlockState param0, BlockGetter param1, BlockPos param2, CollisionContext param3) {
        return Shapes.empty();
    }

    @Override
    public boolean skipRendering(BlockState param0, BlockState param1, Direction param2) {
        if (param1.is(this)) {
            if (!param2.getAxis().isHorizontal()) {
                return true;
            }

            if (param0.getValue(PROPERTY_BY_DIRECTION.get(param2)) && param1.getValue(PROPERTY_BY_DIRECTION.get(param2.getOpposite()))) {
                return true;
            }
        }

        return super.skipRendering(param0, param1, param2);
    }

    public final boolean attachsTo(BlockState param0, boolean param1) {
        return !isExceptionForConnection(param0) && param1 || param0.getBlock() instanceof IronBarsBlock || param0.is(BlockTags.WALLS);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }
}
