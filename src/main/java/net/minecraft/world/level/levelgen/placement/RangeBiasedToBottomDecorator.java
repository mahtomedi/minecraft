package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.configurations.BiasedRangeDecoratorConfiguration;

public class RangeBiasedToBottomDecorator extends AbstractBiasedRangeDecorator {
    public RangeBiasedToBottomDecorator(Codec<BiasedRangeDecoratorConfiguration> param0) {
        super(param0);
    }

    @Override
    protected int y(Random param0, int param1, int param2, int param3) {
        int var0 = Mth.nextInt(param0, param1 + param3, param2);
        return Mth.nextInt(param0, param1, var0 - 1);
    }
}
