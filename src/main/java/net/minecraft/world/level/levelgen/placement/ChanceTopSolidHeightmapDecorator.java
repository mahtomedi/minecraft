package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;

public class ChanceTopSolidHeightmapDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
    public ChanceTopSolidHeightmapDecorator(Codec<ChanceDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(LevelAccessor param0, ChunkGenerator param1, Random param2, ChanceDecoratorConfiguration param3, BlockPos param4) {
        if (param2.nextFloat() < 1.0F / (float)param3.chance) {
            int var0 = param2.nextInt(16) + param4.getX();
            int var1 = param2.nextInt(16) + param4.getZ();
            int var2 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, var0, var1);
            return Stream.of(new BlockPos(var0, var2, var1));
        } else {
            return Stream.empty();
        }
    }
}
