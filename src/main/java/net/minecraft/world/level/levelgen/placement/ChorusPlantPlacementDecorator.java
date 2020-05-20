package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class ChorusPlantPlacementDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
    public ChorusPlantPlacementDecorator(Codec<NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(LevelAccessor param0, ChunkGenerator param1, Random param2, NoneDecoratorConfiguration param3, BlockPos param4) {
        int var0 = param2.nextInt(5);
        return IntStream.range(0, var0).mapToObj(param3x -> {
            int var0x = param2.nextInt(16) + param4.getX();
            int var1x = param2.nextInt(16) + param4.getZ();
            int var2x = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var0x, var1x);
            if (var2x > 0) {
                int var3x = var2x - 1;
                return new BlockPos(var0x, var3x, var1x);
            } else {
                return null;
            }
        }).filter(Objects::nonNull);
    }
}
