package net.minecraft.world.level.levelgen.carver;

import com.mojang.datafixers.Dynamic;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.ProbabilityFeatureConfiguration;

public class CaveWorldCarver extends WorldCarver<ProbabilityFeatureConfiguration> {
    public CaveWorldCarver(Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration> param0, int param1) {
        super(param0, param1);
    }

    public boolean isStartChunk(Random param0, int param1, int param2, ProbabilityFeatureConfiguration param3) {
        return param0.nextFloat() <= param3.probability;
    }

    public boolean carve(
        ChunkAccess param0, Random param1, int param2, int param3, int param4, int param5, int param6, BitSet param7, ProbabilityFeatureConfiguration param8
    ) {
        int var0 = (this.getRange() * 2 - 1) * 16;
        int var1 = param1.nextInt(param1.nextInt(param1.nextInt(this.getCaveBound()) + 1) + 1);

        for(int var2 = 0; var2 < var1; ++var2) {
            double var3 = (double)(param3 * 16 + param1.nextInt(16));
            double var4 = (double)this.getCaveY(param1);
            double var5 = (double)(param4 * 16 + param1.nextInt(16));
            int var6 = 1;
            if (param1.nextInt(4) == 0) {
                double var7 = 0.5;
                float var8 = 1.0F + param1.nextFloat() * 6.0F;
                this.genRoom(param0, param1.nextLong(), param2, param5, param6, var3, var4, var5, var8, 0.5, param7);
                var6 += param1.nextInt(4);
            }

            for(int var9 = 0; var9 < var6; ++var9) {
                float var10 = param1.nextFloat() * (float) (Math.PI * 2);
                float var11 = (param1.nextFloat() - 0.5F) / 4.0F;
                float var12 = this.getThickness(param1);
                int var13 = var0 - param1.nextInt(var0 / 4);
                int var14 = 0;
                this.genTunnel(param0, param1.nextLong(), param2, param5, param6, var3, var4, var5, var12, var10, var11, 0, var13, this.getYScale(), param7);
            }
        }

        return true;
    }

    protected int getCaveBound() {
        return 15;
    }

    protected float getThickness(Random param0) {
        float var0 = param0.nextFloat() * 2.0F + param0.nextFloat();
        if (param0.nextInt(10) == 0) {
            var0 *= param0.nextFloat() * param0.nextFloat() * 3.0F + 1.0F;
        }

        return var0;
    }

    protected double getYScale() {
        return 1.0;
    }

    protected int getCaveY(Random param0) {
        return param0.nextInt(param0.nextInt(120) + 8);
    }

    protected void genRoom(
        ChunkAccess param0,
        long param1,
        int param2,
        int param3,
        int param4,
        double param5,
        double param6,
        double param7,
        float param8,
        double param9,
        BitSet param10
    ) {
        double var0 = 1.5 + (double)(Mth.sin((float) (Math.PI / 2)) * param8);
        double var1 = var0 * param9;
        this.carveSphere(param0, param1, param2, param3, param4, param5 + 1.0, param6, param7, var0, var1, param10);
    }

    protected void genTunnel(
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
        int var1 = var0.nextInt(param12 / 2) + param12 / 4;
        boolean var2 = var0.nextInt(6) == 0;
        float var3 = 0.0F;
        float var4 = 0.0F;

        for(int var5 = param11; var5 < param12; ++var5) {
            double var6 = 1.5 + (double)(Mth.sin((float) Math.PI * (float)var5 / (float)param12) * param8);
            double var7 = var6 * param13;
            float var8 = Mth.cos(param10);
            param5 += (double)(Mth.cos(param9) * var8);
            param6 += (double)Mth.sin(param10);
            param7 += (double)(Mth.sin(param9) * var8);
            param10 *= var2 ? 0.92F : 0.7F;
            param10 += var4 * 0.1F;
            param9 += var3 * 0.1F;
            var4 *= 0.9F;
            var3 *= 0.75F;
            var4 += (var0.nextFloat() - var0.nextFloat()) * var0.nextFloat() * 2.0F;
            var3 += (var0.nextFloat() - var0.nextFloat()) * var0.nextFloat() * 4.0F;
            if (var5 == var1 && param8 > 1.0F) {
                this.genTunnel(
                    param0,
                    var0.nextLong(),
                    param2,
                    param3,
                    param4,
                    param5,
                    param6,
                    param7,
                    var0.nextFloat() * 0.5F + 0.5F,
                    param9 - (float) (Math.PI / 2),
                    param10 / 3.0F,
                    var5,
                    param12,
                    1.0,
                    param14
                );
                this.genTunnel(
                    param0,
                    var0.nextLong(),
                    param2,
                    param3,
                    param4,
                    param5,
                    param6,
                    param7,
                    var0.nextFloat() * 0.5F + 0.5F,
                    param9 + (float) (Math.PI / 2),
                    param10 / 3.0F,
                    var5,
                    param12,
                    1.0,
                    param14
                );
                return;
            }

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
        return param1 <= -0.7 || param0 * param0 + param1 * param1 + param2 * param2 >= 1.0;
    }
}
