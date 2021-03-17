package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperStairBlock extends StairBlock implements WeatheringCopper {
    private final WeatheringCopper.WeatherState weatherState;

    public WeatheringCopperStairBlock(WeatheringCopper.WeatherState param0, BlockState param1, BlockBehaviour.Properties param2) {
        super(param1, param2);
        this.weatherState = param0;
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, Random param3) {
        this.onRandomTick(param0, param1, param2, param3);
    }

    @Override
    public boolean isRandomlyTicking(BlockState param0) {
        return WeatheringCopper.getNext(param0.getBlock()).isPresent();
    }

    public WeatheringCopper.WeatherState getAge() {
        return this.weatherState;
    }
}
