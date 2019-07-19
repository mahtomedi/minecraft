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

public class CountHeighmapDoubleDecorator extends FeatureDecorator<DecoratorFrequency> {
    public CountHeighmapDoubleDecorator(Function<Dynamic<?>, ? extends DecoratorFrequency> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, DecoratorFrequency param3, BlockPos param4
    ) {
        return IntStream.range(0, param3.count).mapToObj(param3x -> {
            int var0 = param2.nextInt(16);
            int var1x = param2.nextInt(16);
            int var2x = param0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, param4.offset(var0, 0, var1x)).getY() * 2;
            if (var2x <= 0) {
                return null;
            } else {
                int var3x = param2.nextInt(var2x);
                return param4.offset(var0, var3x, var1x);
            }
        }).filter(Objects::nonNull);
    }
}
