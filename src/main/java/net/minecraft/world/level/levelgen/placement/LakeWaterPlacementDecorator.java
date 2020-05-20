package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class LakeWaterPlacementDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
    public LakeWaterPlacementDecorator(Codec<ChanceDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(LevelAccessor param0, ChunkGenerator param1, Random param2, ChanceDecoratorConfiguration param3, BlockPos param4) {
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
