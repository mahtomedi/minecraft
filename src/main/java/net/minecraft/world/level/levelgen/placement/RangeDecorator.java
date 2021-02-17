package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;

public class RangeDecorator extends AbstractRangeDecorator {
    public RangeDecorator(Codec<RangeDecoratorConfiguration> param0) {
        super(param0);
    }

    @Override
    protected int y(Random param0, int param1, int param2) {
        return Mth.nextInt(param0, param1, param2);
    }
}
