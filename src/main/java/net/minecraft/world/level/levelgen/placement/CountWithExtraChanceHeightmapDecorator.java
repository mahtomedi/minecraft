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

public class CountWithExtraChanceHeightmapDecorator extends FeatureDecorator<DecoratorFrequencyWithExtraChance> {
    public CountWithExtraChanceHeightmapDecorator(Function<Dynamic<?>, ? extends DecoratorFrequencyWithExtraChance> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, DecoratorFrequencyWithExtraChance param3, BlockPos param4
    ) {
        int var0 = param3.count;
        if (param2.nextFloat() < param3.extraChance) {
            var0 += param3.extraCount;
        }

        return IntStream.range(0, var0).mapToObj(param3x -> {
            int var0x = param2.nextInt(16);
            int var1x = param2.nextInt(16);
            return param0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, param4.offset(var0x, 0, var1x));
        });
    }
}
