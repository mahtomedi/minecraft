package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class ConcretePowderBlock extends FallingBlock {
    private final BlockState concrete;

    public ConcretePowderBlock(Block param0, Block.Properties param1) {
        super(param1);
        this.concrete = param0.defaultBlockState();
    }

    @Override
    public void onLand(Level param0, BlockPos param1, BlockState param2, BlockState param3) {
        if (canSolidify(param3)) {
            param0.setBlock(param1, this.concrete, 3);
        }

    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockGetter var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        return !canSolidify(var0.getBlockState(var1)) && !touchesLiquid(var0, var1) ? super.getStateForPlacement(param0) : this.concrete;
    }

    private static boolean touchesLiquid(BlockGetter param0, BlockPos param1) {
        boolean var0 = false;
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos(param1);

        for(Direction var2 : Direction.values()) {
            BlockState var3 = param0.getBlockState(var1);
            if (var2 != Direction.DOWN || canSolidify(var3)) {
                var1.set(param1).move(var2);
                var3 = param0.getBlockState(var1);
                if (canSolidify(var3) && !var3.isFaceSturdy(param0, param1, var2.getOpposite())) {
                    var0 = true;
                    break;
                }
            }
        }

        return var0;
    }

    private static boolean canSolidify(BlockState param0) {
        return param0.getFluidState().is(FluidTags.WATER);
    }

    @Override
    public BlockState updateShape(BlockState param0, Direction param1, BlockState param2, LevelAccessor param3, BlockPos param4, BlockPos param5) {
        return touchesLiquid(param3, param4) ? this.concrete : super.updateShape(param0, param1, param2, param3, param4, param5);
    }
}
