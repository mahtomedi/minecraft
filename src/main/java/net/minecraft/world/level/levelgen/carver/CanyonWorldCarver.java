package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.Dynamic;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.ProbabilityFeatureConfiguration;

public class CanyonWorldCarver extends WorldCarver<ProbabilityFeatureConfiguration> {
    private final float[] rs = new float[1024];

    public CanyonWorldCarver(Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration> param0) {
        super(param0, 256);
    }

    public boolean isStartChunk(Random param0, int param1, int param2, ProbabilityFeatureConfiguration param3) {
        return param0.nextFloat() <= param3.probability;
    }

    public boolean carve(
        ChunkAccess param0, Random param1, int param2, int param3, int param4, int param5, int param6, BitSet param7, ProbabilityFeatureConfiguration param8
    ) {
        int var0 = (this.getRange() * 2 - 1) * 16;
        double var1 = (double)(param3 * 16 + param1.nextInt(16));
        double var2 = (double)(param1.nextInt(param1.nextInt(40) + 8) + 20);
        double var3 = (double)(param4 * 16 + param1.nextInt(16));
        float var4 = param1.nextFloat() * (float) (Math.PI * 2);
        float var5 = (param1.nextFloat() - 0.5F) * 2.0F / 8.0F;
        double var6 = 3.0;
        float var7 = (param1.nextFloat() * 2.0F + param1.nextFloat()) * 2.0F;
        int var8 = var0 - param1.nextInt(var0 / 4);
        int var9 = 0;
        this.genCanyon(param0, param1.nextLong(), param2, param5, param6, var1, var2, var3, var7, var4, var5, 0, var8, 3.0, param7);
        return true;
    }

    private void genCanyon(
        ChunkAccess param0,
        long param1,
        int param2,
        int param3,
        int param4,
        double param5,
        double param6,
        double param7,
        float param8,
        float param9,
        float param10,
        int param11,
        int param12,
        double param13,
        BitSet param14
    ) {
        Random var0 = new Random(param1);
        float var1 = 1.0F;

        for(int var2 = 0; var2 < 256; ++var2) {
            if (var2 == 0 || var0.nextInt(3) == 0) {
                var1 = 1.0F + var0.nextFloat() * var0.nextFloat();
            }

            this.rs[var2] = var1 * var1;
        }

        float var3 = 0.0F;
        float var4 = 0.0F;

        for(int var5 = param11; var5 < param12; ++var5) {
            double var6 = 1.5 + (double)(Mth.sin((float)var5 * (float) Math.PI / (float)param12) * param8);
            double var7 = var6 * param13;
            var6 *= (double)var0.nextFloat() * 0.25 + 0.75;
            var7 *= (double)var0.nextFloat() * 0.25 + 0.75;
            float var8 = Mth.cos(param10);
            float var9 = Mth.sin(param10);
            param5 += (double)(Mth.cos(param9) * var8);
            param6 += (double)var9;
            param7 += (double)(Mth.sin(param9) * var8);
            param10 *= 0.7F;
            param10 += var4 * 0.05F;
            param9 += var3 * 0.05F;
            var4 *= 0.8F;
            var3 *= 0.5F;
            var4 += (var0.nextFloat() - var0.nextFloat()) * var0.nextFloat() * 2.0F;
            var3 += (var0.nextFloat() - var0.nextFloat()) * var0.nextFloat() * 4.0F;
            if (var0.nextInt(4) != 0) {
                if (!this.canReach(param3, param4, param5, param7, var5, param12, param8)) {
                    return;
                }

                this.carveSphere(param0, param1, param2, param3, param4, param5, param6, param7, var6, var7, param14);
            }
        }

    }

    @Override
    protected boolean skip(double param0, double param1, double param2, int param3) {
        return (param0 * param0 + param2 * param2) * (double)this.rs[param3 - 1] + param1 * param1 / 6.0 >= 1.0;
    }
}
