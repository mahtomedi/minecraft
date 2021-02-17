package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class CountWithExtraChanceDecorator extends RepeatingDecorator<FrequencyWithExtraChanceDecoratorConfiguration> {
    public CountWithExtraChanceDecorator(Codec<FrequencyWithExtraChanceDecoratorConfiguration> param0) {
        super(param0);
    }

    protected int count(Random param0, FrequencyWithExtraChanceDecoratorConfiguration param1, BlockPos param2) {
        return param1.count + (param0.nextFloat() < param1.extraChance ? param1.extraCount : 0);
    }
}
