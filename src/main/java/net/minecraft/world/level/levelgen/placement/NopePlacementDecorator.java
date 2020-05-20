package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class NopePlacementDecorator extends SimpleFeatureDecorator<NoneDecoratorConfiguration> {
    public NopePlacementDecorator(Codec<NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> place(Random param0, NoneDecoratorConfiguration param1, BlockPos param2) {
        return Stream.of(param2);
    }
}
