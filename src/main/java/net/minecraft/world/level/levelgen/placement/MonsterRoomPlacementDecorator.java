package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class MonsterRoomPlacementDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
    public MonsterRoomPlacementDecorator(Codec<ChanceDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(LevelAccessor param0, ChunkGenerator param1, Random param2, ChanceDecoratorConfiguration param3, BlockPos param4) {
        int var0 = param3.chance;
        return IntStream.range(0, var0).mapToObj(param3x -> {
            int var0x = param2.nextInt(16) + param4.getX();
            int var1x = param2.nextInt(16) + param4.getZ();
            int var2x = param2.nextInt(param1.getGenDepth());
            return new BlockPos(var0x, var2x, var1x);
        });
    }
}
