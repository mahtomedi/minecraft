package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CauldronBlock extends AbstractCauldronBlock {
    public CauldronBlock(BlockBehaviour.Properties param0) {
        super(param0, CauldronInteraction.EMPTY);
    }

    protected static boolean shouldHandleRain(Level param0, BlockPos param1) {
        if (param0.random.nextInt(20) != 1) {
            return false;
        } else {
            return param0.getBiome(param1).getTemperature(param1) >= 0.15F;
        }
    }

    @Override
    public void handleRain(BlockState param0, Level param1, BlockPos param2) {
        if (shouldHandleRain(param1, param2)) {
            param1.setBlockAndUpdate(param2, Blocks.WATER_CAULDRON.defaultBlockState());
        }
    }
}
