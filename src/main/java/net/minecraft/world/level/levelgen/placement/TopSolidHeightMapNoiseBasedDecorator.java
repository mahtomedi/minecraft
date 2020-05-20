package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class TopSolidHeightMapNoiseBasedDecorator extends FeatureDecorator<NoiseCountFactorDecoratorConfiguration> {
    public TopSolidHeightMapNoiseBasedDecorator(Codec<NoiseCountFactorDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator param1, Random param2, NoiseCountFactorDecoratorConfiguration param3, BlockPos param4
    ) {
        double var0 = Biome.BIOME_INFO_NOISE.getValue((double)param4.getX() / param3.noiseFactor, (double)param4.getZ() / param3.noiseFactor, false);
        int var1 = (int)Math.ceil((var0 + param3.noiseOffset) * (double)param3.noiseToCountRatio);
        return IntStream.range(0, var1).mapToObj(param4x -> {
            int var0x = param2.nextInt(16) + param4.getX();
            int var1x = param2.nextInt(16) + param4.getZ();
            int var2x = param0.getHeight(param3.heightmap, var0x, var1x);
            return new BlockPos(var0x, var2x, var1x);
        });
    }
}
