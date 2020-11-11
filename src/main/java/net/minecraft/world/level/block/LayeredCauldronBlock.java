package net.minecraft.world.level.block;

import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class LayeredCauldronBlock extends AbstractCauldronBlock {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;
    public static final Predicate<Biome.Precipitation> RAIN = param0 -> param0 == Biome.Precipitation.RAIN;
    public static final Predicate<Biome.Precipitation> SNOW = param0 -> param0 == Biome.Precipitation.SNOW;
    private final Predicate<Biome.Precipitation> fillPredicate;

    public LayeredCauldronBlock(BlockBehaviour.Properties param0, Predicate<Biome.Precipitation> param1, Map<Item, CauldronInteraction> param2) {
        super(param0, param2);
        this.fillPredicate = param1;
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(1)));
    }

    @Override
    protected double getContentHeight(BlockState param0) {
        return (6.0 + (double)param0.getValue(LEVEL).intValue() * 3.0) / 16.0;
    }

    @Override
    public void entityInside(BlockState param0, Level param1, BlockPos param2, Entity param3) {
        if (!param1.isClientSide && param3.isOnFire() && this.isEntityInsideContent(param0, param2, param3)) {
            param3.clearFire();
            lowerFillLevel(param0, param1, param2);
        }

    }

    public static void lowerFillLevel(BlockState param0, Level param1, BlockPos param2) {
        int var0 = param0.getValue(LEVEL) - 1;
        param1.setBlockAndUpdate(param2, var0 == 0 ? Blocks.CAULDRON.defaultBlockState() : param0.setValue(LEVEL, Integer.valueOf(var0)));
    }

    @Override
    public void handlePrecipitation(BlockState param0, Level param1, BlockPos param2, Biome.Precipitation param3) {
        if (CauldronBlock.shouldHandlePrecipitation(param1) && param0.getValue(LEVEL) != 3 && this.fillPredicate.test(param3)) {
            param1.setBlockAndUpdate(param2, param0.cycle(LEVEL));
        }
    }

    @Override
    public int getAnalogOutputSignal(BlockState param0, Level param1, BlockPos param2) {
        return param0.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> param0) {
        param0.add(LEVEL);
    }
}
