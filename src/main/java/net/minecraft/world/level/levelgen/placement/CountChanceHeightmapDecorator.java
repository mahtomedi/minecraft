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

public class CountChanceHeightmapDecorator extends FeatureDecorator<DecoratorFrequencyChance> {
    public CountChanceHeightmapDecorator(Function<Dynamic<?>, ? extends DecoratorFrequencyChance> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, DecoratorFrequencyChance param3, BlockPos param4
    ) {
        return IntStream.range(0, param3.count).filter(param2x -> param2.nextFloat() < param3.chance).mapToObj(param3x -> {
            int var0 = param2.nextInt(16);
            int var1x = param2.nextInt(16);
            return param0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, param4.offset(var0, 0, var1x));
        });
    }
}
