package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class CauldronBlock extends AbstractCauldronBlock {
    private static final float RAIN_FILL_CHANCE = 0.05F;
    private static final float POWDER_SNOW_FILL_CHANCE = 0.1F;

    public CauldronBlock(BlockBehaviour.Properties param0) {
        super(param0, CauldronInteraction.EMPTY);
    }

    @Override
    public boolean isFull(BlockState param0) {
        return false;
    }

    protected static boolean shouldHandlePrecipitation(Level param0, Biome.Precipitation param1) {
        if (param1 == Biome.Precipitation.RAIN) {
            return param0.getRandom().nextFloat() < 0.05F;
        } else if (param1 == Biome.Precipitation.SNOW) {
            return param0.getRandom().nextFloat() < 0.1F;
        } else {
            return false;
        }
    }

    @Override
    public void handlePrecipitation(BlockState param0, Level param1, BlockPos param2, Biome.Precipitation param3) {
        if (shouldHandlePrecipitation(param1, param3)) {
            if (param3 == Biome.Precipitation.RAIN) {
                param1.setBlockAndUpdate(param2, Blocks.WATER_CAULDRON.defaultBlockState());
                param1.gameEvent(null, GameEvent.BLOCK_CHANGE, param2);
            } else if (param3 == Biome.Precipitation.SNOW) {
                param1.setBlockAndUpdate(param2, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState());
                param1.gameEvent(null, GameEvent.BLOCK_CHANGE, param2);
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
            BlockState var0 = Blocks.WATER_CAULDRON.defaultBlockState();
            param1.setBlockAndUpdate(param2, var0);
            param1.gameEvent(GameEvent.BLOCK_CHANGE, param2, GameEvent.Context.of(var0));
            param1.levelEvent(1047, param2, 0);
        } else if (param3 == Fluids.LAVA) {
            BlockState var1 = Blocks.LAVA_CAULDRON.defaultBlockState();
            param1.setBlockAndUpdate(param2, var1);
            param1.gameEvent(GameEvent.BLOCK_CHANGE, param2, GameEvent.Context.of(var1));
            param1.levelEvent(1046, param2, 0);
        }

    }
}
