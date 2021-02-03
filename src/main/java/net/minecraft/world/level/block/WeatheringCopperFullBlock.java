package net.minecraft.world.level.block;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperFullBlock extends Block implements WeatheringCopper {
    private final WeatheringCopper.WeatherState weatherState;
    private final Block changeTo;

    public WeatheringCopperFullBlock(BlockBehaviour.Properties param0) {
        super(param0);
        this.weatherState = WeatheringCopper.WeatherState.values()[WeatheringCopper.WeatherState.values().length - 1];
        this.changeTo = this;
    }

    public WeatheringCopperFullBlock(BlockBehaviour.Properties param0, WeatheringCopper.WeatherState param1, Block param2) {
        super(param0);
        this.weatherState = param1;
        this.changeTo = param2;
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
        return this.changeTo.defaultBlockState();
    }
}
