package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class LakeLavaPlacementDecorator extends FilterDecorator<ChanceDecoratorConfiguration> {
    public LakeLavaPlacementDecorator(Codec<ChanceDecoratorConfiguration> param0) {
        super(param0);
    }

    protected boolean shouldPlace(DecorationContext param0, Random param1, ChanceDecoratorConfiguration param2, BlockPos param3) {
        return param3.getY() < 63 || param1.nextInt(10) == 0;
    }
}
