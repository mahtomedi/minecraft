package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class MonsterRoomPlacementDecorator extends FeatureDecorator<MonsterRoomPlacementConfiguration> {
    public MonsterRoomPlacementDecorator(Function<Dynamic<?>, ? extends MonsterRoomPlacementConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, MonsterRoomPlacementConfiguration param3, BlockPos param4
    ) {
        int var0 = param3.chance;
        return IntStream.range(0, var0).mapToObj(param3x -> {
            int var0x = param2.nextInt(16);
            int var1x = param2.nextInt(param1.getGenDepth());
            int var2x = param2.nextInt(16);
            return param4.offset(var0x, var1x, var2x);
        });
    }
}
