package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;

public class ChanceHeightmapDecorator extends FeatureDecorator<DecoratorChance> {
    public ChanceHeightmapDecorator(Function<Dynamic<?>, ? extends DecoratorChance> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, DecoratorChance param3, BlockPos param4
    ) {
        if (param2.nextFloat() < 1.0F / (float)param3.chance) {
            int var0 = param2.nextInt(16);
            int var1 = param2.nextInt(16);
            BlockPos var2 = param0.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, param4.offset(var0, 0, var1));
            return Stream.of(var2);
        } else {
            return Stream.empty();
        }
    }
}
