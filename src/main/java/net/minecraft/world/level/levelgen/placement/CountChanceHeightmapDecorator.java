package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;

public class CountChanceHeightmapDecorator extends FeatureDecorator<FrequencyChanceDecoratorConfiguration> {
    public CountChanceHeightmapDecorator(Function<Dynamic<?>, ? extends FrequencyChanceDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator param1, Random param2, FrequencyChanceDecoratorConfiguration param3, BlockPos param4
    ) {
        return IntStream.range(0, param3.count).filter(param2x -> param2.nextFloat() < param3.chance).mapToObj(param3x -> {
            int var0 = param2.nextInt(16) + param4.getX();
            int var1x = param2.nextInt(16) + param4.getZ();
            int var2x = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var0, var1x);
            return new BlockPos(var0, var2x, var1x);
        });
    }
}
