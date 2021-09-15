package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;

public class SurfaceRelativeThresholdDecorator extends FeatureDecorator<SurfaceRelativeThresholdConfiguration> {
    public SurfaceRelativeThresholdDecorator(Codec<SurfaceRelativeThresholdConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, SurfaceRelativeThresholdConfiguration param2, BlockPos param3) {
        long var0 = (long)param0.getHeight(param2.heightmap, param3.getX(), param3.getZ());
        long var1 = var0 + (long)param2.minInclusive;
        long var2 = var0 + (long)param2.maxInclusive;
        return (long)param3.getY() >= var1 && (long)param3.getY() <= var2 ? Stream.of(param3) : Stream.of();
    }
}
