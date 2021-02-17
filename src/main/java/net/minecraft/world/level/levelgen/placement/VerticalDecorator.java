package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public abstract class VerticalDecorator<DC extends DecoratorConfiguration> extends FeatureDecorator<DC> {
    public VerticalDecorator(Codec<DC> param0) {
        super(param0);
    }

    protected abstract int y(DecorationContext var1, Random var2, DC var3, int var4);

    @Override
    public final Stream<BlockPos> getPositions(DecorationContext param0, Random param1, DC param2, BlockPos param3) {
        return Stream.of(new BlockPos(param3.getX(), this.y(param0, param1, param2, param3.getY()), param3.getZ()));
    }
}
