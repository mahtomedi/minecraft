package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;

public class CountDecorator extends RepeatingDecorator<CountConfiguration> {
    public CountDecorator(Codec<CountConfiguration> param0) {
        super(param0);
    }

    protected int count(Random param0, CountConfiguration param1, BlockPos param2) {
        return param1.count().sample(param0);
    }
}
