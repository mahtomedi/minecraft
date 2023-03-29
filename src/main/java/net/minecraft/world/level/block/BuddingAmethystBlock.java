package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingAmethystBlock extends AmethystBlock {
    public static final int GROWTH_CHANCE = 5;
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingAmethystBlock(BlockBehaviour.Properties param0) {
        super(param0);
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        if (param3.nextInt(5) == 0) {
            Direction var0 = DIRECTIONS[param3.nextInt(DIRECTIONS.length)];
            BlockPos var1 = param2.relative(var0);
            BlockState var2 = param1.getBlockState(var1);
            Block var3 = null;
            if (canClusterGrowAtState(var2)) {
                var3 = Blocks.SMALL_AMETHYST_BUD;
            } else if (var2.is(Blocks.SMALL_AMETHYST_BUD) && var2.getValue(AmethystClusterBlock.FACING) == var0) {
                var3 = Blocks.MEDIUM_AMETHYST_BUD;
            } else if (var2.is(Blocks.MEDIUM_AMETHYST_BUD) && var2.getValue(AmethystClusterBlock.FACING) == var0) {
                var3 = Blocks.LARGE_AMETHYST_BUD;
            } else if (var2.is(Blocks.LARGE_AMETHYST_BUD) && var2.getValue(AmethystClusterBlock.FACING) == var0) {
                var3 = Blocks.AMETHYST_CLUSTER;
            }

            if (var3 != null) {
                BlockState var4 = var3.defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, var0)
                    .setValue(AmethystClusterBlock.WATERLOGGED, Boolean.valueOf(var2.getFluidState().getType() == Fluids.WATER));
                param1.setBlockAndUpdate(var1, var4);
            }

        }
    }

    public static boolean canClusterGrowAtState(BlockState param0) {
        return param0.isAir() || param0.is(Blocks.WATER) && param0.getFluidState().getAmount() == 8;
    }
}
