package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public abstract class BaseHeightmapDecorator<DC extends DecoratorConfiguration> extends EdgeDecorator<DC> {
    public BaseHeightmapDecorator(Codec<DC> param0) {
        super(param0);
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, DC param2, BlockPos param3) {
        int var0 = param3.getX();
        int var1 = param3.getZ();
        int var2 = param0.getHeight(this.type(param2), var0, var1);
        return var2 > 0 ? Stream.of(new BlockPos(var0, var2, var1)) : Stream.of();
    }
}