package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.DecoratorNoiseDependant;

public class NoiseHeightmap32Decorator extends FeatureDecorator<DecoratorNoiseDependant> {
    public NoiseHeightmap32Decorator(Function<Dynamic<?>, ? extends DecoratorNoiseDependant> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, DecoratorNoiseDependant param3, BlockPos param4
    ) {
        double var0 = Biome.BIOME_INFO_NOISE.getValue((double)param4.getX() / 200.0, (double)param4.getZ() / 200.0, false);
        int var1 = var0 < param3.noiseLevel ? param3.belowNoise : param3.aboveNoise;
        return IntStream.range(0, var1).mapToObj(param3x -> {
            int var0x = param2.nextInt(16);
            int var1x = param2.nextInt(16);
            int var2x = param0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, param4.offset(var0x, 0, var1x)).getY() + 32;
            if (var2x <= 0) {
                return null;
            } else {
                int var3x = param2.nextInt(var2x);
                return param4.offset(var0x, var3x, var1x);
            }
        }).filter(Objects::nonNull);
    }
}
