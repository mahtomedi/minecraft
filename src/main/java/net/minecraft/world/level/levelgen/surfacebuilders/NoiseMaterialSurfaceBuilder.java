package net.minecraft.world.level.levelgen.surfacebuilders;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public abstract class NoiseMaterialSurfaceBuilder extends DefaultSurfaceBuilder {
    private long seed;
    protected NormalNoise surfaceNoise;

    public NoiseMaterialSurfaceBuilder(Codec<SurfaceBuilderBaseConfiguration> param0) {
        super(param0);
    }

    @Override
    public void apply(
        Random param0,
        BlockColumn param1,
        Biome param2,
        int param3,
        int param4,
        int param5,
        double param6,
        BlockState param7,
        BlockState param8,
        int param9,
        int param10,
        long param11,
        SurfaceBuilderBaseConfiguration param12
    ) {
        BlockState var0;
        BlockState var1;
        if (this.getSteepMaterial() != null && this.isSteepTerrain(param1, param3, param4, this.getSteepMaterial())) {
            var0 = this.getSteepMaterial().getState();
            var1 = this.getSteepMaterial().getState();
        } else {
            var0 = this.getTopMaterial(param12, param3, param4);
            var1 = this.getMidMaterial(param12, param3, param4);
        }

        this.apply(param0, param1, param2, param3, param4, param5, param6, param7, param8, var0, var1, param12.getUnderwaterMaterial(), param9, param10);
    }

    protected BlockState getMaterial(double param0, int param1, int param2, BlockState param3, BlockState param4, double param5, double param6) {
        double var0 = this.surfaceNoise.getValue((double)param1 * param0, 0.0, (double)param2 * param0);
        BlockState var1;
        if (var0 >= param5 && var0 <= param6) {
            var1 = param4;
        } else {
            var1 = param3;
        }

        return var1;
    }

    @Override
    public void initNoise(long param0) {
        if (this.seed != param0) {
            WorldgenRandom var0 = new WorldgenRandom(param0);
            this.surfaceNoise = NormalNoise.create(var0, -3, 1.0, 1.0, 1.0, 1.0);
        }

        this.seed = param0;
    }

    public boolean isSteepTerrain(BlockColumn param0, int param1, int param2, NoiseMaterialSurfaceBuilder.SteepMaterial param3) {
        return false;
    }

    @Nullable
    protected abstract NoiseMaterialSurfaceBuilder.SteepMaterial getSteepMaterial();

    protected abstract BlockState getTopMaterial(SurfaceBuilderBaseConfiguration var1, int var2, int var3);

    protected abstract BlockState getMidMaterial(SurfaceBuilderBaseConfiguration var1, int var2, int var3);

    public static class SteepMaterial {
        private final BlockState state;
        private final boolean northSlopes;
        private final boolean southSlopes;
        private final boolean westSlopes;
        private final boolean eastSlopes;

        public SteepMaterial(BlockState param0, boolean param1, boolean param2, boolean param3, boolean param4) {
            this.state = param0;
            this.northSlopes = param1;
            this.southSlopes = param2;
            this.westSlopes = param3;
            this.eastSlopes = param4;
        }

        public BlockState getState() {
            return this.state;
        }

        public boolean hasNorthSlopes() {
            return this.northSlopes;
        }

        public boolean hasSouthSlopes() {
            return this.southSlopes;
        }

        public boolean hasWestSlopes() {
            return this.westSlopes;
        }

        public boolean hasEastSlopes() {
            return this.eastSlopes;
        }
    }
}
