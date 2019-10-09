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
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class EndGatewayPlacementDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
    public EndGatewayPlacementDecorator(Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, NoneDecoratorConfiguration param3, BlockPos param4
    ) {
        if (param2.nextInt(700) == 0) {
            int var0 = param2.nextInt(16) + param4.getX();
            int var1 = param2.nextInt(16) + param4.getZ();
            int var2 = param0.getHeight(Heightmap.Types.MOTION_BLOCKING, var0, var1);
            if (var2 > 0) {
                int var3 = var2 + 3 + param2.nextInt(7);
                return Stream.of(new BlockPos(var0, var3, var1));
            }
        }

        return Stream.empty();
    }
}
