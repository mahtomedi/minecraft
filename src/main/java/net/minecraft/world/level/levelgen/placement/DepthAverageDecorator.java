package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;

public class DepthAverageDecorator extends VerticalDecorator<DepthAverageConfiguration> {
    public DepthAverageDecorator(Codec<DepthAverageConfiguration> param0) {
        super(param0);
    }

    protected int y(DecorationContext param0, Random param1, DepthAverageConfiguration param2, int param3) {
        int var0 = param2.spread();
        return param1.nextInt(var0) + param1.nextInt(var0) - var0 + param2.baseline().resolveY(param0);
    }
}
