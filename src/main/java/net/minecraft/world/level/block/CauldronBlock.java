package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

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

    @Override
    protected boolean canReceiveStalactiteDrip(Fluid param0) {
        return true;
    }

    @Override
    protected void receiveStalactiteDrip(BlockState param0, Level param1, BlockPos param2, Fluid param3) {
        if (param3 == Fluids.WATER) {
            param1.setBlockAndUpdate(param2, Blocks.WATER_CAULDRON.defaultBlockState());
            param1.levelEvent(1047, param2, 0);
        } else if (param3 == Fluids.LAVA) {
            param1.setBlockAndUpdate(param2, Blocks.LAVA_CAULDRON.defaultBlockState());
            param1.levelEvent(1046, param2, 0);
        }

    }
}
