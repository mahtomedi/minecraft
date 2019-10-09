package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class LakeLavaPlacementDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
    public LakeLavaPlacementDecorator(Function<Dynamic<?>, ? extends ChanceDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, ChanceDecoratorConfiguration param3, BlockPos param4
    ) {
        if (param2.nextInt(param3.chance / 10) == 0) {
            int var0 = param2.nextInt(16) + param4.getX();
            int var1 = param2.nextInt(16) + param4.getZ();
            int var2 = param2.nextInt(param2.nextInt(param1.getGenDepth() - 8) + 8);
            if (var2 < param0.getSeaLevel() || param2.nextInt(param3.chance / 8) == 0) {
                return Stream.of(new BlockPos(var0, var2, var1));
            }
        }

        return Stream.empty();
    }
}
