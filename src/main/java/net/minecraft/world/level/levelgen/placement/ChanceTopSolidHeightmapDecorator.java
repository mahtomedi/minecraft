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

public class ChanceTopSolidHeightmapDecorator extends FeatureDecorator<DecoratorChance> {
    public ChanceTopSolidHeightmapDecorator(Function<Dynamic<?>, ? extends DecoratorChance> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, DecoratorChance param3, BlockPos param4
    ) {
        if (param2.nextFloat() < 1.0F / (float)param3.chance) {
            int var0 = param2.nextInt(16);
            int var1 = param2.nextInt(16);
            int var2 = param0.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, param4.getX() + var0, param4.getZ() + var1);
            return Stream.of(new BlockPos(param4.getX() + var0, var2, param4.getZ() + var1));
        } else {
            return Stream.empty();
        }
    }
}
