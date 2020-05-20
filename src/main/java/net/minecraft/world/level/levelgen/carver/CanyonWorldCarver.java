package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class CanyonWorldCarver extends WorldCarver<ProbabilityFeatureConfiguration> {
    private final float[] rs = new float[1024];

    public CanyonWorldCarver(Codec<ProbabilityFeatureConfiguration> param0) {
        super(param0, 256);
    }

    public boolean isStartChunk(Random param0, int param1, int param2, ProbabilityFeatureConfiguration param3) {
        return param0.nextFloat() <= param3.probability;
    }

    public boolean carve(
        ChunkAccess param0,
        Function<BlockPos, Biome> param1,
        Random param2,
        int param3,
        int param4,
        int param5,
        int param6,
        int param7,
        BitSet param8,
        ProbabilityFeatureConfiguration param9
    ) {
        int var0 = (this.getRange() * 2 - 1) * 16;
        double var1 = (double)(param4 * 16 + param2.nextInt(16));
        double var2 = (double)(param2.nextInt(param2.nextInt(40) + 8) + 20);
        double var3 = (double)(param5 * 16 + param2.nextInt(16));
        float var4 = param2.nextFloat() * (float) (Math.PI * 2);
        float var5 = (param2.nextFloat() - 0.5F) * 2.0F / 8.0F;
        double var6 = 3.0;
        float var7 = (param2.nextFloat() * 2.0F + param2.nextFloat()) * 2.0F;
        int var8 = var0 - param2.nextInt(var0 / 4);
        int var9 = 0;
        this.genCanyon(param0, param1, param2.nextLong(), param3, param6, param7, var1, var2, var3, var7, var4, var5, 0, var8, 3.0, param8);
        return true;
    }

    private void genCanyon(
        ChunkAccess param0,
        Function<BlockPos, Biome> param1,
        long param2,
        int param3,
        int param4,
        int param5,
        double param6,
        double param7,
        double param8,
        float param9,
        float param10,
        float param11,
        int param12,
        int param13,
        double param14,
        BitSet param15
    ) {
        Random var0 = new Random(param2);
        float var1 = 1.0F;

        for(int var2 = 0; var2 < 256; ++var2) {
            if (var2 == 0 || var0.nextInt(3) == 0) {
                var1 = 1.0F + var0.nextFloat() * var0.nextFloat();
            }

            this.rs[var2] = var1 * var1;
        }

        float var3 = 0.0F;
        float var4 = 0.0F;

        for(int var5 = param12; var5 < param13; ++var5) {
            double var6 = 1.5 + (double)(Mth.sin((float)var5 * (float) Math.PI / (float)param13) * param9);
            double var7 = var6 * param14;
            var6 *= (double)var0.nextFloat() * 0.25 + 0.75;
            var7 *= (double)var0.nextFloat() * 0.25 + 0.75;
            float var8 = Mth.cos(param11);
            float var9 = Mth.sin(param11);
            param6 += (double)(Mth.cos(param10) * var8);
            param7 += (double)var9;
            param8 += (double)(Mth.sin(param10) * var8);
            param11 *= 0.7F;
            param11 += var4 * 0.05F;
            param10 += var3 * 0.05F;
            var4 *= 0.8F;
            var3 *= 0.5F;
            var4 += (var0.nextFloat() - var0.nextFloat()) * var0.nextFloat() * 2.0F;
            var3 += (var0.nextFloat() - var0.nextFloat()) * var0.nextFloat() * 4.0F;
            if (var0.nextInt(4) != 0) {
                if (!this.canReach(param4, param5, param6, param8, var5, param13, param9)) {
                    return;
                }

                this.carveSphere(param0, param1, param2, param3, param4, param5, param6, param7, param8, var6, var7, param15);
            }
        }

    }

    @Override
    protected boolean skip(double param0, double param1, double param2, int param3) {
        return (param0 * param0 + param2 * param2) * (double)this.rs[param3 - 1] + param1 * param1 / 6.0 >= 1.0;
    }
}
