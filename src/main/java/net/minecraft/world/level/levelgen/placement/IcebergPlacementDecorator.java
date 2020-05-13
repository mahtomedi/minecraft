package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;

public class IcebergPlacementDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
    public IcebergPlacementDecorator(Function<Dynamic<?>, ? extends ChanceDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(LevelAccessor param0, ChunkGenerator param1, Random param2, ChanceDecoratorConfiguration param3, BlockPos param4) {
        if (param2.nextFloat() < 1.0F / (float)param3.chance) {
            int var0 = param2.nextInt(8) + 4 + param4.getX();
            int var1 = param2.nextInt(8) + 4 + param4.getZ();
            int var2 = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var0, var1);
            return Stream.of(new BlockPos(var0, var2, var1));
        } else {
            return Stream.empty();
        }
    }
}
