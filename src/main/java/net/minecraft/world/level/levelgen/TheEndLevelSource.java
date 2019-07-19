package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;

public class TheEndLevelSource extends NoiseBasedChunkGenerator<TheEndGeneratorSettings> {
    private final BlockPos dimensionSpawnPosition;

    public TheEndLevelSource(LevelAccessor param0, BiomeSource param1, TheEndGeneratorSettings param2) {
        super(param0, param1, 8, 4, 128, param2, true);
        this.dimensionSpawnPosition = param2.getSpawnPosition();
    }

    @Override
    protected void fillNoiseColumn(double[] param0, int param1, int param2) {
        double var0 = 1368.824;
        double var1 = 684.412;
        double var2 = 17.110300000000002;
        double var3 = 4.277575000000001;
        int var4 = 64;
        int var5 = -3000;
        this.fillNoiseColumn(param0, param1, param2, 1368.824, 684.412, 17.110300000000002, 4.277575000000001, 64, -3000);
    }

    @Override
    protected double[] getDepthAndScale(int param0, int param1) {
        return new double[]{(double)this.biomeSource.getHeightValue(param0, param1), 0.0};
    }

    @Override
    protected double getYOffset(double param0, double param1, int param2) {
        return 8.0 - param0;
    }

    @Override
    protected double getTopSlideStart() {
        return (double)((int)super.getTopSlideStart() / 2);
    }

    @Override
    protected double getBottomSlideStart() {
        return 8.0;
    }

    @Override
    public int getSpawnHeight() {
        return 50;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }
}
