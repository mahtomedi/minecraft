package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class CarvingMaskDecorator extends FeatureDecorator<CarvingMaskDecoratorConfiguration> {
    public CarvingMaskDecorator(Codec<CarvingMaskDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, CarvingMaskDecoratorConfiguration param2, BlockPos param3) {
        ChunkPos var0 = new ChunkPos(param3);
        BitSet var1 = param0.getCarvingMask(var0, param2.step);
        return IntStream.range(0, var1.length()).filter(var1::get).mapToObj(param1x -> {
            int var0x = param1x & 15;
            int var1x = param1x >> 4 & 15;
            int var2x = param1x >> 8;
            return new BlockPos(var0.getMinBlockX() + var0x, var2x, var0.getMinBlockZ() + var1x);
        });
    }
}
