package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;

public class MagmaDecorator extends FeatureDecorator<FrequencyDecoratorConfiguration> {
    public MagmaDecorator(Function<Dynamic<?>, ? extends FrequencyDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(LevelAccessor param0, ChunkGenerator param1, Random param2, FrequencyDecoratorConfiguration param3, BlockPos param4) {
        int var0 = param0.getSeaLevel() / 2 + 1;
        return IntStream.range(0, param3.count).mapToObj(param3x -> {
            int var0x = param2.nextInt(16) + param4.getX();
            int var1x = param2.nextInt(16) + param4.getZ();
            int var2x = var0 - 5 + param2.nextInt(10);
            return new BlockPos(var0x, var2x, var1x);
        });
    }
}
