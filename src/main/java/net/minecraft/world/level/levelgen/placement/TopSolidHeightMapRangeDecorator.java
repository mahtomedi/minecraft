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

public class TopSolidHeightMapRangeDecorator extends FeatureDecorator<DecoratorRange> {
    public TopSolidHeightMapRangeDecorator(Function<Dynamic<?>, ? extends DecoratorRange> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, DecoratorRange param3, BlockPos param4
    ) {
        int var0 = param2.nextInt(param3.max - param3.min) + param3.min;
        return IntStream.range(0, var0).mapToObj(param3x -> {
            int var0x = param2.nextInt(16);
            int var1x = param2.nextInt(16);
            int var2x = param0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, param4.getX() + var0x, param4.getZ() + var1x);
            return new BlockPos(param4.getX() + var0x, var2x, param4.getZ() + var1x);
        });
    }
}
