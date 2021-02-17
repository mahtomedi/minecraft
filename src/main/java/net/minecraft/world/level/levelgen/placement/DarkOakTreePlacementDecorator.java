package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class DarkOakTreePlacementDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
    public DarkOakTreePlacementDecorator(Codec<NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, NoneDecoratorConfiguration param2, BlockPos param3) {
        return IntStream.range(0, 16).mapToObj(param2x -> {
            int var0 = param2x / 4;
            int var1x = param2x % 4;
            int var2x = var0 * 4 + 1 + param1.nextInt(3) + param3.getX();
            int var3x = var1x * 4 + 1 + param1.nextInt(3) + param3.getZ();
            return new BlockPos(var2x, param3.getY(), var3x);
        });
    }
}
