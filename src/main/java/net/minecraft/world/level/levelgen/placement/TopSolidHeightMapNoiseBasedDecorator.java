package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class TopSolidHeightMapNoiseBasedDecorator extends FeatureDecorator<DecoratorNoiseCountFactor> {
    public TopSolidHeightMapNoiseBasedDecorator(Function<Dynamic<?>, ? extends DecoratorNoiseCountFactor> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, DecoratorNoiseCountFactor param3, BlockPos param4
    ) {
        double var0 = Biome.BIOME_INFO_NOISE.getValue((double)param4.getX() / param3.noiseFactor, (double)param4.getZ() / param3.noiseFactor, false);
        int var1 = (int)Math.ceil((var0 + param3.noiseOffset) * (double)param3.noiseToCountRatio);
        return IntStream.range(0, var1).mapToObj(param4x -> {
            int var0x = param2.nextInt(16);
            int var1x = param2.nextInt(16);
            int var2x = param0.getHeight(param3.heightmap, param4.getX() + var0x, param4.getZ() + var1x);
            return new BlockPos(param4.getX() + var0x, var2x, param4.getZ() + var1x);
        });
    }
}
