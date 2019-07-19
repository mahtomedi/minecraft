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

public class CountTopSolidDecorator extends FeatureDecorator<DecoratorFrequency> {
    public CountTopSolidDecorator(Function<Dynamic<?>, ? extends DecoratorFrequency> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, DecoratorFrequency param3, BlockPos param4
    ) {
        return IntStream.range(0, param3.count).mapToObj(param3x -> {
            int var0 = param2.nextInt(16) + param4.getX();
            int var1x = param2.nextInt(16) + param4.getZ();
            return new BlockPos(var0, param0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, var0, var1x), var1x);
        });
    }
}
