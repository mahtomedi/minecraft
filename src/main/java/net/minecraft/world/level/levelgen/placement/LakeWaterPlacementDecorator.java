package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class LakeWaterPlacementDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
    public LakeWaterPlacementDecorator(
        Function<Dynamic<?>, ? extends ChanceDecoratorConfiguration> param0, Function<Random, ? extends ChanceDecoratorConfiguration> param1
    ) {
        super(param0, param1);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, ChanceDecoratorConfiguration param3, BlockPos param4
    ) {
        if (param2.nextInt(param3.chance) == 0) {
            int var0 = param2.nextInt(16) + param4.getX();
            int var1 = param2.nextInt(16) + param4.getZ();
            int var2 = param2.nextInt(param1.getGenDepth());
            return Stream.of(new BlockPos(var0, var2, var1));
        } else {
            return Stream.empty();
        }
    }
}
