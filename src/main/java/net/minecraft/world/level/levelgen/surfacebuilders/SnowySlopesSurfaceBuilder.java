package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SnowySlopesSurfaceBuilder extends NoiseMaterialSurfaceBuilder {
    private final NoiseMaterialSurfaceBuilder.SteepMaterial steepMaterial = new NoiseMaterialSurfaceBuilder.SteepMaterial(
        Blocks.STONE.defaultBlockState(), true, false, false, true
    );

    public SnowySlopesSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
        super(param0);
    }

    @Nullable
    @Override
    protected NoiseMaterialSurfaceBuilder.SteepMaterial getSteepMaterial() {
        return this.steepMaterial;
    }

    @Override
    protected BlockState getTopMaterial(SurfaceBuilderBaseConfiguration param0, int param1, int param2) {
        return this.getMaterial(0.1, param1, param2, Blocks.SNOW_BLOCK.defaultBlockState(), Blocks.POWDER_SNOW.defaultBlockState(), 0.35, 0.6);
    }

    @Override
    protected BlockState getMidMaterial(SurfaceBuilderBaseConfiguration param0, int param1, int param2) {
        return this.getMaterial(0.1, param1, param2, Blocks.SNOW_BLOCK.defaultBlockState(), Blocks.POWDER_SNOW.defaultBlockState(), 0.45, 0.58);
    }
}
