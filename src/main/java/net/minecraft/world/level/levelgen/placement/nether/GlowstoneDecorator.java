package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.placement.RepeatingDecorator;

public class GlowstoneDecorator extends RepeatingDecorator<CountConfiguration> {
    public GlowstoneDecorator(Codec<CountConfiguration> param0) {
        super(param0);
    }

    protected int count(Random param0, CountConfiguration param1, BlockPos param2) {
        return param0.nextInt(param0.nextInt(param1.count().sample(param0)) + 1);
    }
}
