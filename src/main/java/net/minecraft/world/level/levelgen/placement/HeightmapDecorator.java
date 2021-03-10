package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.configurations.HeightmapConfiguration;

public class HeightmapDecorator extends FeatureDecorator<HeightmapConfiguration> {
    public HeightmapDecorator(Codec<HeightmapConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, HeightmapConfiguration param2, BlockPos param3) {
        int var0 = param3.getX();
        int var1 = param3.getZ();
        int var2 = param0.getHeight(param2.heightmap, var0, var1);
        return var2 > param0.getMinBuildHeight() ? Stream.of(new BlockPos(var0, var2, var1)) : Stream.of();
    }
}
