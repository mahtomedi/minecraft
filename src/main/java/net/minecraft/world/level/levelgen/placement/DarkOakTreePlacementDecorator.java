package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class DarkOakTreePlacementDecorator extends EdgeDecorator<NoneDecoratorConfiguration> {
    public DarkOakTreePlacementDecorator(Codec<NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    protected Heightmap.Types type(NoneDecoratorConfiguration param0) {
        return Heightmap.Types.MOTION_BLOCKING;
    }

    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, NoneDecoratorConfiguration param2, BlockPos param3) {
        return IntStream.range(0, 16).mapToObj(param4 -> {
            int var0 = param4 / 4;
            int var1x = param4 % 4;
            int var2x = var0 * 4 + 1 + param1.nextInt(3) + param3.getX();
            int var3x = var1x * 4 + 1 + param1.nextInt(3) + param3.getZ();
            int var4x = param0.getHeight(this.type(param2), var2x, var3x);
            return new BlockPos(var2x, var4x, var3x);
        });
    }
}
