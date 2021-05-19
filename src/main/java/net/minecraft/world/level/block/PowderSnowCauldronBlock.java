package net.minecraft.world.level.block;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PowderSnowCauldronBlock extends LayeredCauldronBlock {
    public PowderSnowCauldronBlock(BlockBehaviour.Properties param0, Predicate<Biome.Precipitation> param1, Map<Item, CauldronInteraction> param2) {
        super(param0, param1, param2);
    }

    @Override
    protected void handleEntityOnFireInside(BlockState param0, Level param1, BlockPos param2) {
        lowerFillLevel(Blocks.WATER_CAULDRON.defaultBlockState().setValue(LEVEL, param0.getValue(LEVEL)), param1, param2);
    }
}
