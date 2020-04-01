package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class CarvingMaskDecorator extends FeatureDecorator<CarvingMaskDecoratorConfiguration> {
    public CarvingMaskDecorator(
        Function<Dynamic<?>, ? extends CarvingMaskDecoratorConfiguration> param0, Function<Random, ? extends CarvingMaskDecoratorConfiguration> param1
    ) {
        super(param0, param1);
    }

    public Stream<BlockPos> getPositions(
        LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, CarvingMaskDecoratorConfiguration param3, BlockPos param4
    ) {
        ChunkAccess var0 = param0.getChunk(param4);
        ChunkPos var1 = var0.getPos();
        BitSet var2 = var0.getCarvingMask(param3.step);
        return IntStream.range(0, var2.length()).filter(param3x -> var2.get(param3x) && param2.nextFloat() < param3.probability).mapToObj(param1x -> {
            int var0x = param1x & 15;
            int var1x = param1x >> 4 & 15;
            int var2x = param1x >> 8;
            return new BlockPos(var1.getMinBlockX() + var0x, var2x, var1.getMinBlockZ() + var1x);
        });
    }
}
