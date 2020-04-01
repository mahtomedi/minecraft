package net.minecraft.world.level.levelgen;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.levelgen.feature.Feature;

public class NetherLevelSource extends NoiseBasedChunkGenerator<NetherGeneratorSettings> {
    private final double[] yOffsets = this.makeYOffsets();

    public NetherLevelSource(LevelAccessor param0, BiomeSource param1, NetherGeneratorSettings param2) {
        super(param0, param1, 4, 8, 128, param2, false);
    }

    @Override
    protected void fillNoiseColumn(double[] param0, int param1, int param2) {
        double var0 = 684.412;
        double var1 = 2053.236;
        double var2 = 8.555150000000001;
        double var3 = 34.2206;
        int var4 = -10;
        int var5 = 3;
        this.fillNoiseColumn(param0, param1, param2, 684.412, 2053.236, 8.555150000000001, 34.2206, 3, -10);
    }

    @Override
    protected double[] getDepthAndScale(int param0, int param1) {
        return new double[]{0.0, 0.0};
    }

    @Override
    protected double getYOffset(double param0, double param1, int param2) {
        return this.yOffsets[param2];
    }

    private double[] makeYOffsets() {
        double[] var0 = new double[this.getNoiseSizeY()];

        for(int var1 = 0; var1 < this.getNoiseSizeY(); ++var1) {
            var0[var1] = Math.cos((double)var1 * Math.PI * 6.0 / (double)this.getNoiseSizeY()) * 2.0;
            double var2 = (double)var1;
            if (var1 > this.getNoiseSizeY() / 2) {
                var2 = (double)(this.getNoiseSizeY() - 1 - var1);
            }

            if (var2 < 4.0) {
                var2 = 4.0 - var2;
                var0[var1] -= var2 * var2 * var2 * 10.0;
            }
        }

        return var0;
    }

    @Override
    public List<Biome.SpawnerData> getMobsAt(MobCategory param0, BlockPos param1) {
        if (param0 == MobCategory.MONSTER) {
            if (Feature.NETHER_BRIDGE.isInsideFeature(this.level, param1)) {
                return Feature.NETHER_BRIDGE.getSpecialEnemies();
            }

            if (Feature.NETHER_BRIDGE.isInsideBoundingFeature(this.level, param1)
                && this.level.getBlockState(param1.below()).getBlock() == Blocks.NETHER_BRICKS) {
                return Feature.NETHER_BRIDGE.getSpecialEnemies();
            }
        }

        return super.getMobsAt(param0, param1);
    }

    @Override
    public int getSpawnHeight() {
        return this.level.getSeaLevel() + 1;
    }

    @Override
    public int getGenDepth() {
        return 128;
    }

    @Override
    public int getSeaLevel() {
        return 32;
    }

    @Override
    public int getBaseHeight(int param0, int param1, Heightmap.Types param2) {
        return this.getGenDepth() / 2;
    }

    @Override
    public ChunkGeneratorType<?, ?> getType() {
        return ChunkGeneratorType.CAVES;
    }
}
