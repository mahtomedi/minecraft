package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.HeightmapConfiguration;

public class HeightmapDoubleDecorator extends FeatureDecorator<HeightmapConfiguration> {
    public HeightmapDoubleDecorator(Codec<HeightmapConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, HeightmapConfiguration param2, BlockPos param3) {
        int var0 = param3.getX();
        int var1 = param3.getZ();
        int var2 = param0.getHeight(param2.heightmap, var0, var1);
        return var2 == param0.getMinBuildHeight()
            ? Stream.of()
            : Stream.of(new BlockPos(var0, param0.getMinBuildHeight() + param1.nextInt((var2 - param0.getMinBuildHeight()) * 2), var1));
    }
}
