package net.minecraft.world.level.levelgen;

import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TheEndLevelSource extends NoiseBasedChunkGenerator<NoiseGeneratorSettings> {
    private final NoiseGeneratorSettings settings;

    public TheEndLevelSource(BiomeSource param0, long param1, NoiseGeneratorSettings param2) {
        super(param0, param1, param2, 8, 4, 128, true);
        this.settings = param2;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ChunkGenerator withSeed(long param0) {
        return new TheEndLevelSource(this.biomeSource.withSeed(param0), param0, this.settings);
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
