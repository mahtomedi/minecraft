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
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;

public class NoiseHeightmapDoubleDecorator extends FeatureDecorator<NoiseDependantDecoratorConfiguration> {
    public NoiseHeightmapDoubleDecorator(
        Function<Dynamic<?>, ? extends NoiseDependantDecoratorConfiguration> param0, Function<Random, ? extends NoiseDependantDecoratorConfiguration> param1
    ) {
        super(param0, param1);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0,
        ChunkGenerator<? extends ChunkGeneratorSettings> param1,
        Random param2,
        NoiseDependantDecoratorConfiguration param3,
        BlockPos param4
    ) {
        double var0 = Biome.BIOME_INFO_NOISE.getValue((double)param4.getX() / 200.0, (double)param4.getZ() / 200.0, false);
        int var1 = var0 < param3.noiseLevel ? param3.belowNoise : param3.aboveNoise;
        return IntStream.range(0, var1).mapToObj(param3x -> {
            int var0x = param2.nextInt(16) + param4.getX();
            int var1x = param2.nextInt(16) + param4.getZ();
            int var2x = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var0x, var1x) * 2;
            return var2x <= 0 ? null : new BlockPos(var0x, param2.nextInt(var2x), var1x);
        }).filter(Objects::nonNull);
    }
}
