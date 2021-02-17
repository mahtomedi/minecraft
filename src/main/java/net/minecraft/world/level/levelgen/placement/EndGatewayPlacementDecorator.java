package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class EndGatewayPlacementDecorator extends VerticalDecorator<NoneDecoratorConfiguration> {
    public EndGatewayPlacementDecorator(Codec<NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    protected int y(DecorationContext param0, Random param1, NoneDecoratorConfiguration param2, int param3) {
        return param3 + 3 + param1.nextInt(7);
    }
}
