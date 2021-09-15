package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SnowcappedPeaksSurfaceBuilder extends NoiseMaterialSurfaceBuilder {
    private final NoiseMaterialSurfaceBuilder.SteepMaterial steepMaterial = new NoiseMaterialSurfaceBuilder.SteepMaterial(
        Blocks.PACKED_ICE.defaultBlockState(), true, false, false, true
    );

    public SnowcappedPeaksSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
        super(param0);
    }

    @Nullable
    @Override
    protected NoiseMaterialSurfaceBuilder.SteepMaterial getSteepMaterial() {
        return this.steepMaterial;
    }

    @Override
    protected BlockState getTopMaterial(SurfaceBuilderBaseConfiguration param0, int param1, int param2) {
        BlockState var0 = this.getMaterial(0.5, param1, param2, Blocks.SNOW_BLOCK.defaultBlockState(), Blocks.ICE.defaultBlockState(), 0.0, 0.025);
        return this.getMaterial(0.0625, param1, param2, var0, Blocks.PACKED_ICE.defaultBlockState(), 0.0, 0.2);
    }

    @Override
    protected BlockState getMidMaterial(SurfaceBuilderBaseConfiguration param0, int param1, int param2) {
        BlockState var0 = this.getMaterial(0.5, param1, param2, Blocks.SNOW_BLOCK.defaultBlockState(), Blocks.ICE.defaultBlockState(), -0.0625, 0.025);
        return this.getMaterial(0.0625, param1, param2, var0, Blocks.PACKED_ICE.defaultBlockState(), -0.5, 0.2);
    }
}
