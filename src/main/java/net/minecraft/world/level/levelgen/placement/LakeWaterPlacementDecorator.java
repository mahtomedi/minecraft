package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class LakeWaterPlacementDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
    public LakeWaterPlacementDecorator(Codec<ChanceDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, ChanceDecoratorConfiguration param2, BlockPos param3) {
        if (param1.nextInt(param2.chance) == 0) {
            int var0 = param1.nextInt(16) + param3.getX();
            int var1 = param1.nextInt(16) + param3.getZ();
            int var2 = param1.nextInt(param0.getGenDepth());
            return Stream.of(new BlockPos(var0, var2, var1));
        } else {
            return Stream.empty();
        }
    }
}
