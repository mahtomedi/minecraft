package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperStairBlock extends StairBlock implements WeatheringCopper {
    private final WeatheringCopper.WeatherState weatherState;
    private final Block changeTo;

    public WeatheringCopperStairBlock(BlockState param0, BlockBehaviour.Properties param1) {
        super(param0, param1);
        this.weatherState = WeatheringCopper.WeatherState.values()[WeatheringCopper.WeatherState.values().length - 1];
        this.changeTo = this;
    }

    public WeatheringCopperStairBlock(BlockState param0, BlockBehaviour.Properties param1, WeatheringCopper.WeatherState param2, Block param3) {
        super(param0, param1);
        this.weatherState = param2;
        this.changeTo = param3;
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        this.onRandomTick(param0, param1, param2, param3);
    }

    @Override
    public boolean isRandomlyTicking(BlockState param0) {
        return this.changeTo != this;
    }

    public WeatheringCopper.WeatherState getAge() {
        return this.weatherState;
    }

    @Override
    public BlockState getChangeTo(BlockState param0) {
        return this.changeTo
            .defaultBlockState()
            .setValue(FACING, param0.getValue(FACING))
            .setValue(HALF, param0.getValue(HALF))
            .setValue(SHAPE, param0.getValue(SHAPE))
            .setValue(WATERLOGGED, param0.getValue(WATERLOGGED));
    }
}
