package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

public class WaterDepthThresholdDecorator extends FeatureDecorator<WaterDepthThresholdConfiguration> {
    public WaterDepthThresholdDecorator(Codec<WaterDepthThresholdConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, WaterDepthThresholdConfiguration param2, BlockPos param3) {
        int var0 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR, param3.getX(), param3.getZ());
        int var1 = param0.getHeight(Heightmap.Types.WORLD_SURFACE, param3.getX(), param3.getZ());
        return var1 - var0 > param2.maxWaterDepth ? Stream.of() : Stream.of(param3);
    }
}
