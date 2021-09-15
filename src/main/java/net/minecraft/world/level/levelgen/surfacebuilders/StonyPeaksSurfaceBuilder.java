package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class StonyPeaksSurfaceBuilder extends NoiseMaterialSurfaceBuilder {
    public StonyPeaksSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
        super(param0);
    }

    @Nullable
    @Override
    protected NoiseMaterialSurfaceBuilder.SteepMaterial getSteepMaterial() {
        return null;
    }

    @Override
    protected BlockState getTopMaterial(SurfaceBuilderBaseConfiguration param0, int param1, int param2) {
        return this.getMaterial(0.015, param1, param2, Blocks.STONE.defaultBlockState(), Blocks.CALCITE.defaultBlockState(), -0.0125, 0.0125);
    }

    @Override
    protected BlockState getMidMaterial(SurfaceBuilderBaseConfiguration param0, int param1, int param2) {
        return this.getMaterial(0.015, param1, param2, Blocks.STONE.defaultBlockState(), Blocks.CALCITE.defaultBlockState(), -0.0125, 0.0125);
    }
}
