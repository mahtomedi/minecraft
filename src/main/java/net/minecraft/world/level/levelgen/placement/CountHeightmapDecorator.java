package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;

public class CountHeightmapDecorator extends FeatureDecorator<FrequencyDecoratorConfiguration> {
    public CountHeightmapDecorator(
        Function<Dynamic<?>, ? extends FrequencyDecoratorConfiguration> param0, Function<Random, ? extends FrequencyDecoratorConfiguration> param1
    ) {
        super(param0, param1);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, FrequencyDecoratorConfiguration param3, BlockPos param4
    ) {
        return IntStream.range(0, param3.count).mapToObj(param3x -> {
            int var0 = param2.nextInt(16) + param4.getX();
            int var1x = param2.nextInt(16) + param4.getZ();
            int var2x = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var0, var1x);
            return new BlockPos(var0, var2x, var1x);
        });
    }
}
