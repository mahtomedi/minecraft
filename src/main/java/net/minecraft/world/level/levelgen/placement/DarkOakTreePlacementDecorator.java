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
import net.minecraft.world.level.levelgen.feature.NoneDecoratorConfiguration;

public class DarkOakTreePlacementDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
    public DarkOakTreePlacementDecorator(Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, NoneDecoratorConfiguration param3, BlockPos param4
    ) {
        return IntStream.range(0, 16).mapToObj(param3x -> {
            int var0 = param3x / 4;
            int var1x = param3x % 4;
            int var2x = var0 * 4 + 1 + param2.nextInt(3);
            int var3x = var1x * 4 + 1 + param2.nextInt(3);
            return param0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, param4.offset(var2x, 0, var3x));
        });
    }
}
