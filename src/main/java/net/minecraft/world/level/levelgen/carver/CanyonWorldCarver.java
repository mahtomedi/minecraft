package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;

public class CanyonWorldCarver extends WorldCarver<CanyonCarverConfiguration> {
    public CanyonWorldCarver(Codec<CanyonCarverConfiguration> param0) {
        super(param0);
    }

    public boolean isStartChunk(CanyonCarverConfiguration param0, Random param1) {
        return param1.nextFloat() <= param0.probability;
    }

    public boolean carve(
        CarvingContext param0,
        CanyonCarverConfiguration param1,
        ChunkAccess param2,
        Function<BlockPos, Holder<Biome>> param3,
        Random param4,
        Aquifer param5,
        ChunkPos param6,
        CarvingMask param7
    ) {
        int var0 = (this.getRange() * 2 - 1) * 16;
        double var1 = (double)param6.getBlockX(param4.nextInt(16));
        int var2 = param1.y.sample(param4, param0);
        double var3 = (double)param6.getBlockZ(param4.nextInt(16));
        float var4 = param4.nextFloat() * (float) (Math.PI * 2);
        float var5 = param1.verticalRotation.sample(param4);
        double var6 = (double)param1.yScale.sample(param4);
        float var7 = param1.shape.thickness.sample(param4);
        int var8 = (int)((float)var0 * param1.shape.distanceFactor.sample(param4));
        int var9 = 0;
        this.doCarve(param0, param1, param2, param3, param4.nextLong(), param5, var1, (double)var2, var3, var7, var4, var5, 0, var8, var6, param7);
        return true;
    }

    private void doCarve(
        CarvingContext param0,
        CanyonCarverConfiguration param1,
        ChunkAccess param2,
        Function<BlockPos, Holder<Biome>> param3,
        long param4,
        Aquifer param5,
        double param6,
        double param7,
        double param8,
        float param9,
        float param10,
        float param11,
        int param12,
        int param13,
        double param14,
        CarvingMask param15
    ) {
        Random var0 = new Random(param4);
        float[] var1 = this.initWidthFactors(param0, param1, var0);
        float var2 = 0.0F;
        float var3 = 0.0F;

        for(int var4 = param12; var4 < param13; ++var4) {
            double var5 = 1.5 + (double)(Mth.sin((float)var4 * (float) Math.PI / (float)param13) * param9);
            double var6 = var5 * param14;
            var5 *= (double)param1.shape.horizontalRadiusFactor.sample(var0);
            var6 = this.updateVerticalRadius(param1, var0, var6, (float)param13, (float)var4);
            float var7 = Mth.cos(param11);
            float var8 = Mth.sin(param11);
            param6 += (double)(Mth.cos(param10) * var7);
            param7 += (double)var8;
            param8 += (double)(Mth.sin(param10) * var7);
            param11 *= 0.7F;
            param11 += var3 * 0.05F;
            param10 += var2 * 0.05F;
            var3 *= 0.8F;
            var2 *= 0.5F;
            var3 += (var0.nextFloat() - var0.nextFloat()) * var0.nextFloat() * 2.0F;
            var2 += (var0.nextFloat() - var0.nextFloat()) * var0.nextFloat() * 4.0F;
            if (var0.nextInt(4) != 0) {
                if (!canReach(param2.getPos(), param6, param8, var4, param13, param9)) {
                    return;
                }

                this.carveEllipsoid(
                    param0,
                    param1,
                    param2,
                    param3,
                    param5,
                    param6,
                    param7,
                    param8,
                    var5,
                    var6,
                    param15,
                    (param1x, param2x, param3x, param4x, param5x) -> this.shouldSkip(param1x, var1, param2x, param3x, param4x, param5x)
                );
            }
        }

    }

    private float[] initWidthFactors(CarvingContext param0, CanyonCarverConfiguration param1, Random param2) {
        int var0 = param0.getGenDepth();
        float[] var1 = new float[var0];
        float var2 = 1.0F;

        for(int var3 = 0; var3 < var0; ++var3) {
            if (var3 == 0 || param2.nextInt(param1.shape.widthSmoothness) == 0) {
                var2 = 1.0F + param2.nextFloat() * param2.nextFloat();
            }

            var1[var3] = var2 * var2;
        }

        return var1;
    }

    private double updateVerticalRadius(CanyonCarverConfiguration param0, Random param1, double param2, float param3, float param4) {
        float var0 = 1.0F - Mth.abs(0.5F - param4 / param3) * 2.0F;
        float var1 = param0.shape.verticalRadiusDefaultFactor + param0.shape.verticalRadiusCenterFactor * var0;
        return (double)var1 * param2 * (double)Mth.randomBetween(param1, 0.75F, 1.0F);
    }

    private boolean shouldSkip(CarvingContext param0, float[] param1, double param2, double param3, double param4, int param5) {
        int var0 = param5 - param0.getMinGenY();
        return (param2 * param2 + param4 * param4) * (double)param1[var0 - 1] + param3 * param3 / 6.0 >= 1.0;
    }
}
