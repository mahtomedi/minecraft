package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class HeightmapDoubleDecorator<DC extends DecoratorConfiguration> extends EdgeDecorator<DC> {
    public HeightmapDoubleDecorator(Codec<DC> param0) {
        super(param0);
    }

    @Override
    protected Heightmap.Types type(DC param0) {
        return Heightmap.Types.MOTION_BLOCKING;
    }

    @Override
    public Stream<BlockPos> getPositions(DecorationContext param0, Random param1, DC param2, BlockPos param3) {
        int var0 = param3.getX();
        int var1 = param3.getZ();
        int var2 = param0.getHeight(this.type(param2), var0, var1);
        return var2 == 0 ? Stream.of() : Stream.of(new BlockPos(var0, param1.nextInt(var2 * 2), var1));
    }
}
