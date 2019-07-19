package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.DecoratorFrequency;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class MagmaDecorator extends FeatureDecorator<DecoratorFrequency> {
    public MagmaDecorator(Function<Dynamic<?>, ? extends DecoratorFrequency> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, DecoratorFrequency param3, BlockPos param4
    ) {
        int var0 = param0.getSeaLevel() / 2 + 1;
        return IntStream.range(0, param3.count).mapToObj(param3x -> {
            int var0x = param2.nextInt(16);
            int var1x = var0 - 5 + param2.nextInt(10);
            int var2x = param2.nextInt(16);
            return param4.offset(var0x, var1x, var2x);
        });
    }
}
