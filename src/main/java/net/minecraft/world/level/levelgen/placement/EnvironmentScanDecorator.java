package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

public class EnvironmentScanDecorator extends FeatureDecorator<EnvironmentScanConfiguration> {
    public EnvironmentScanDecorator(Codec<EnvironmentScanConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, EnvironmentScanConfiguration param2, BlockPos param3) {
        BlockPos.MutableBlockPos var0 = param3.mutable();
        WorldGenLevel var1 = param0.getLevel();

        for(int var2 = 0; var2 < param2.maxSteps() && !var1.isOutsideBuildHeight(var0.getY()); ++var2) {
            if (param2.targetCondition().test(var1, var0)) {
                return Stream.of(var0);
            }

            var0.move(param2.directionOfSearch());
        }

        return Stream.of();
    }
}
