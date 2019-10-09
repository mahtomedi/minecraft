package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;

public class CountHeighmapDoubleDecorator extends FeatureDecorator<FrequencyDecoratorConfiguration> {
    public CountHeighmapDoubleDecorator(Function<Dynamic<?>, ? extends FrequencyDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, FrequencyDecoratorConfiguration param3, BlockPos param4
    ) {
        return IntStream.range(0, param3.count).mapToObj(param3x -> {
            int var0 = param2.nextInt(16) + param4.getX();
            int var1x = param2.nextInt(16) + param4.getZ();
            int var2x = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var0, var1x) * 2;
            return var2x <= 0 ? null : new BlockPos(var0, param2.nextInt(var2x), var1x);
        }).filter(Objects::nonNull);
    }
}
