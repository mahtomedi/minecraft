package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class ChanceDecorator extends RepeatingDecorator<ChanceDecoratorConfiguration> {
    public ChanceDecorator(Codec<ChanceDecoratorConfiguration> param0) {
        super(param0);
    }

    protected int count(Random param0, ChanceDecoratorConfiguration param1, BlockPos param2) {
        return param0.nextFloat() < 1.0F / (float)param1.chance ? 1 : 0;
    }
}
