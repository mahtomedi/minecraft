package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.configurations.BiasedRangeDecoratorConfiguration;

public class RangeVeryBiasedToBottomDecorator extends AbstractBiasedRangeDecorator {
    public RangeVeryBiasedToBottomDecorator(Codec<BiasedRangeDecoratorConfiguration> param0) {
        super(param0);
    }

    @Override
    protected int y(Random param0, int param1, int param2, int param3) {
        int var0 = Mth.nextInt(param0, param1 + param3, param2);
        int var1 = Mth.nextInt(param0, param1, var0 - 1);
        return Mth.nextInt(param0, param1, var1 - 1 + param3);
    }
}
