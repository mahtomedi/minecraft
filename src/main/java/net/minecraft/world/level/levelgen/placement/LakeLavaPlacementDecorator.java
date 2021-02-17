package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class LakeLavaPlacementDecorator extends RepeatingDecorator<ChanceDecoratorConfiguration> {
    public LakeLavaPlacementDecorator(Codec<ChanceDecoratorConfiguration> param0) {
        super(param0);
    }

    protected int count(Random param0, ChanceDecoratorConfiguration param1, BlockPos param2) {
        return param2.getY() >= 63 && param0.nextInt(10) != 0 ? 0 : 1;
    }
}
