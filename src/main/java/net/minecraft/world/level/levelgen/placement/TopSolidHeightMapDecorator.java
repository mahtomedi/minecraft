package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class TopSolidHeightMapDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
    public TopSolidHeightMapDecorator(Codec<NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(LevelAccessor param0, ChunkGenerator param1, Random param2, NoneDecoratorConfiguration param3, BlockPos param4) {
        int var0 = param2.nextInt(16) + param4.getX();
        int var1 = param2.nextInt(16) + param4.getZ();
        int var2 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, var0, var1);
        return Stream.of(new BlockPos(var0, var2, var1));
    }
}
