package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class EndGatewayPlacementDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
    public EndGatewayPlacementDecorator(Codec<NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, NoneDecoratorConfiguration param2, BlockPos param3) {
        if (param1.nextInt(700) == 0) {
            int var0 = param1.nextInt(16) + param3.getX();
            int var1 = param1.nextInt(16) + param3.getZ();
            int var2 = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var0, var1);
            if (var2 > param0.getMinBuildHeight()) {
                int var3 = var2 + 3 + param1.nextInt(7);
                return Stream.of(new BlockPos(var0, var3, var1));
            }
        }

        return Stream.empty();
    }
}
