package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public abstract class FilterDecorator<DC extends DecoratorConfiguration> extends FeatureDecorator<DC> {
    public FilterDecorator(Codec<DC> param0) {
        super(param0);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, DC param2, BlockPos param3) {
        return this.shouldPlace(param0, param1, param2, param3) ? Stream.of(param3) : Stream.of();
    }

    protected abstract boolean shouldPlace(DecorationContext var1, Random var2, DC var3, BlockPos var4);
}
