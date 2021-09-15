package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class StoneShoreSurfaceBuilder extends NoiseMaterialSurfaceBuilder {
    public StoneShoreSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
        super(param0);
    }

    @Nullable
    @Override
    protected NoiseMaterialSurfaceBuilder.SteepMaterial getSteepMaterial() {
        return null;
    }

    @Override
    protected BlockState getTopMaterial(SurfaceBuilderBaseConfiguration param0, int param1, int param2) {
        return this.getMaterial(0.03175, param1, param2, Blocks.STONE.defaultBlockState(), Blocks.GRAVEL.defaultBlockState(), -0.05, 0.05);
    }

    @Override
    protected BlockState getMidMaterial(SurfaceBuilderBaseConfiguration param0, int param1, int param2) {
        return this.getMaterial(0.03175, param1, param2, Blocks.STONE.defaultBlockState(), Blocks.GRAVEL.defaultBlockState(), -0.05, 0.05);
    }
}
