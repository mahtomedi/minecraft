package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperStairBlock extends StairBlock implements WeatheringCopper {
    public static final MapCodec<WeatheringCopperStairBlock> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(ChangeOverTimeBlock::getAge),
                    BlockState.CODEC.fieldOf("base_state").forGetter(param0x -> param0x.baseState),
                    propertiesCodec()
                )
                .apply(param0, WeatheringCopperStairBlock::new)
    );
    private final WeatheringCopper.WeatherState weatherState;

    @Override
    public MapCodec<WeatheringCopperStairBlock> codec() {
        return CODEC;
    }

    public WeatheringCopperStairBlock(WeatheringCopper.WeatherState param0, BlockState param1, BlockBehaviour.Properties param2) {
        super(param1, param2);
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
