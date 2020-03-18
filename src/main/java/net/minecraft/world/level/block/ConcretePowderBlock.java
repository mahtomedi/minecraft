package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ConcretePowderBlock extends FallingBlock {
    private final BlockState concrete;

    public ConcretePowderBlock(Block param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.concrete = param0.defaultBlockState();
    }

    @Override
    public void onLand(Level param0, BlockPos param1, BlockState param2, BlockState param3, FallingBlockEntity param4) {
        if (shouldSolidify(param0, param1, param3)) {
            param0.setBlock(param1, this.concrete, 3);
        }

    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext param0) {
        BlockGetter var0 = param0.getLevel();
        BlockPos var1 = param0.getClickedPos();
        BlockState var2 = var0.getBlockState(var1);
        return shouldSolidify(var0, var1, var2) ? this.concrete : super.getStateForPlacement(param0);
    }

    private static boolean shouldSolidify(BlockGetter param0, BlockPos param1, BlockState param2) {
        return canSolidify(param2) || touchesLiquid(param0, param1);
    }

    private static boolean touchesLiquid(BlockGetter param0, BlockPos param1) {
        boolean var0 = false;
        BlockPos.MutableBlockPos var1 = param1.mutable();

        for(Direction var2 : Direction.values()) {
            BlockState var3 = param0.getBlockState(var1);
            if (var2 != Direction.DOWN || canSolidify(var3)) {
                var1.setWithOffset(param1, var2);
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getDustColor(BlockState param0, BlockGetter param1, BlockPos param2) {
        return param0.getMapColor(param1, param2).col;
    }
}
