package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;

public class RangeDecorator extends VerticalDecorator<RangeDecoratorConfiguration> {
    public RangeDecorator(Codec<RangeDecoratorConfiguration> param0) {
        super(param0);
    }

    protected int y(DecorationContext param0, Random param1, RangeDecoratorConfiguration param2, int param3) {
        return param2.height.sample(param1, param0);
    }
}
