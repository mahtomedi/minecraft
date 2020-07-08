package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public abstract class SimpleFeatureDecorator<DC extends DecoratorConfiguration> extends FeatureDecorator<DC> {
    public SimpleFeatureDecorator(Codec<DC> param0) {
        super(param0);
    }

    @Override
    public final Stream<BlockPos> getPositions(DecorationContext param0, Random param1, DC param2, BlockPos param3) {
        return this.place(param1, param2, param3);
    }

    protected abstract Stream<BlockPos> place(Random var1, DC var2, BlockPos var3);
}
