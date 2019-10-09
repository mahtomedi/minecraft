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

public class CountHeight64Decorator extends FeatureDecorator<FrequencyDecoratorConfiguration> {
    public CountHeight64Decorator(Function<Dynamic<?>, ? extends FrequencyDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, FrequencyDecoratorConfiguration param3, BlockPos param4
    ) {
        return IntStream.range(0, param3.count).mapToObj(param2x -> {
            int var0 = param2.nextInt(16) + param4.getX();
            int var1x = param2.nextInt(16) + param4.getZ();
            int var2x = 64;
            return new BlockPos(var0, 64, var1x);
        });
    }
}
