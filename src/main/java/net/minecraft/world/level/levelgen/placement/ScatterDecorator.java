package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.configurations.ScatterDecoratorConfiguration;

public class ScatterDecorator extends FeatureDecorator<ScatterDecoratorConfiguration> {
    public ScatterDecorator(Codec<ScatterDecoratorConfiguration> param0) {
        super(param0);
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, ScatterDecoratorConfiguration param2, BlockPos param3) {
        int var0 = param3.getX() + param2.xzSpread.sample(param1);
        int var1 = param3.getY() + param2.ySpread.sample(param1);
        int var2 = param3.getZ() + param2.xzSpread.sample(param1);
        BlockPos var3 = new BlockPos(var0, var1, var2);
        ChunkPos var4 = new ChunkPos(var3);
        ChunkPos var5 = new ChunkPos(param3);
        int var6 = Mth.abs(var4.x - var5.x);
        int var7 = Mth.abs(var4.z - var5.z);
        return var6 <= 1 && var7 <= 1 ? Stream.of(new BlockPos(var0, var1, var2)) : Stream.empty();
    }
}
