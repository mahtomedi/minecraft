package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperGrateBlock extends WaterloggedTransparentBlock implements WeatheringCopper {
    public static final MapCodec<WeatheringCopperGrateBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(WeatheringCopperGrateBlock::getAge), propertiesCodec())
                .apply(param0, WeatheringCopperGrateBlock::new)
    );
    private final WeatheringCopper.WeatherState weatherState;

    @Override
    protected MapCodec<WeatheringCopperGrateBlock> codec() {
        return CODEC;
    }

    protected WeatheringCopperGrateBlock(WeatheringCopper.WeatherState param0, BlockBehaviour.Properties param1) {
        super(param1);
        this.weatherState = param0;
    }

    @Override
    public void randomTick(BlockState param0, ServerLevel param1, BlockPos param2, RandomSource param3) {
        this.changeOverTime(param0, param1, param2, param3);
    }

    @Override
    public boolean isRandomlyTicking(BlockState param0) {
        return WeatheringCopper.getNext(param0.getBlock()).isPresent();
    }

    public WeatheringCopper.WeatherState getAge() {
        return this.weatherState;
    }
}
