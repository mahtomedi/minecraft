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

public class CountWithExtraChanceHeightmapDecorator extends FeatureDecorator<FrequencyWithExtraChanceDecoratorConfiguration> {
    public CountWithExtraChanceHeightmapDecorator(Function<Dynamic<?>, ? extends FrequencyWithExtraChanceDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0,
        ChunkGenerator<? extends ChunkGeneratorSettings> param1,
        Random param2,
        FrequencyWithExtraChanceDecoratorConfiguration param3,
        BlockPos param4
    ) {
        int var0 = param3.count;
        if (param2.nextFloat() < param3.extraChance) {
            var0 += param3.extraCount;
        }

        return IntStream.range(0, var0).mapToObj(param3x -> {
            int var0x = param2.nextInt(16) + param4.getX();
            int var1x = param2.nextInt(16) + param4.getZ();
            int var2x = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var0x, var1x);
            return new BlockPos(var0x, var2x, var1x);
        });
    }
}
