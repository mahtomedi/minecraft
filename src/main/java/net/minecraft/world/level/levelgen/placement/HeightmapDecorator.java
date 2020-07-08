package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;

public class HeightmapDecorator<DC extends DecoratorConfiguration> extends BaseHeightmapDecorator<DC> {
    public HeightmapDecorator(Codec<DC> param0) {
        super(param0);
    }

    @Override
    protected Heightmap.Types type(DC param0) {
        return Heightmap.Types.MOTION_BLOCKING;
    }
}
