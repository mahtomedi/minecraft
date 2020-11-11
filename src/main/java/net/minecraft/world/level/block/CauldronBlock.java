package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CauldronBlock extends AbstractCauldronBlock {
    public CauldronBlock(BlockBehaviour.Properties param0) {
        super(param0, CauldronInteraction.EMPTY);
    }

    protected static boolean shouldHandlePrecipitation(Level param0) {
        return param0.random.nextInt(20) == 1;
    }

    @Override
    public void handlePrecipitation(BlockState param0, Level param1, BlockPos param2, Biome.Precipitation param3) {
        if (shouldHandlePrecipitation(param1)) {
            if (param3 == Biome.Precipitation.RAIN) {
                param1.setBlockAndUpdate(param2, Blocks.WATER_CAULDRON.defaultBlockState());
            } else if (param3 == Biome.Precipitation.SNOW) {
                param1.setBlockAndUpdate(param2, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState());
            }

        }
    }
}
