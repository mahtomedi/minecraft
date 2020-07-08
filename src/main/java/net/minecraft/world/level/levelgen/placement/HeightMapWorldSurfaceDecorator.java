package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class HeightMapWorldSurfaceDecorator extends BaseHeightmapDecorator<NoneDecoratorConfiguration> {
    public HeightMapWorldSurfaceDecorator(Codec<NoneDecoratorConfiguration> param0) {
        super(param0);
    }

    protected Heightmap.Types type(NoneDecoratorConfiguration param0) {
        return Heightmap.Types.WORLD_SURFACE_WG;
    }
}
