package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;

public class CaveWorldCarver extends WorldCarver<CaveCarverConfiguration> {
    public CaveWorldCarver(Codec<CaveCarverConfiguration> param0) {
        super(param0);
    }

    public boolean isStartChunk(CaveCarverConfiguration param0, Random param1) {
        return param1.nextFloat() <= param0.probability;
    }

    public boolean carve(
        CarvingContext param0,
        CaveCarverConfiguration param1,
        ChunkAccess param2,
        Function<BlockPos, Biome> param3,
        Random param4,
        Aquifer param5,
        ChunkPos param6,
        BitSet param7
    ) {
        int var0 = SectionPos.sectionToBlockCoord(this.getRange() * 2 - 1);
        int var1 = param4.nextInt(param4.nextInt(param4.nextInt(this.getCaveBound()) + 1) + 1);

        for(int var2 = 0; var2 < var1; ++var2) {
            double var3 = (double)param6.getBlockX(param4.nextInt(16));
            double var4 = (double)param1.y.sample(param4, param0);
            double var5 = (double)param6.getBlockZ(param4.nextInt(16));
            double var6 = (double)param1.horizontalRadiusMultiplier.sample(param4);
            double var7 = (double)param1.verticalRadiusMultiplier.sample(param4);
            double var8 = (double)param1.floorLevel.sample(param4);
            WorldCarver.CarveSkipChecker var9 = (param1x, param2x, param3x, param4x, param5x) -> shouldSkip(param2x, param3x, param4x, var8);
            int var10 = 1;
            if (param4.nextInt(4) == 0) {
                double var11 = (double)param1.yScale.sample(param4);
                float var12 = 1.0F + param4.nextFloat() * 6.0F;
                this.createRoom(param0, param1, param2, param3, param4.nextLong(), param5, var3, var4, var5, var12, var11, param7, var9);
                var10 += param4.nextInt(4);
            }

            for(int var13 = 0; var13 < var10; ++var13) {
                float var14 = param4.nextFloat() * (float) (Math.PI * 2);
                float var15 = (param4.nextFloat() - 0.5F) / 4.0F;
                float var16 = this.getThickness(param4);
                int var17 = var0 - param4.nextInt(var0 / 4);
                int var18 = 0;
                this.createTunnel(
                    param0,
                    param1,
                    param2,
                    param3,
                    param4.nextLong(),
                    param5,
                    var3,
                    var4,
                    var5,
                    var6,
                    var7,
                    var16,
                    var14,
                    var15,
                    0,
                    var17,
                    this.getYScale(),
                    param7,
                    var9
                );
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

    protected void createRoom(
        CarvingContext param0,
        CaveCarverConfiguration param1,
        ChunkAccess param2,
        Function<BlockPos, Biome> param3,
        long param4,
        Aquifer param5,
        double param6,
        double param7,
        double param8,
        float param9,
        double param10,
        BitSet param11,
        WorldCarver.CarveSkipChecker param12
    ) {
        double var0 = 1.5 + (double)(Mth.sin((float) (Math.PI / 2)) * param9);
        double var1 = var0 * param10;
        this.carveEllipsoid(param0, param1, param2, param3, param4, param5, param6 + 1.0, param7, param8, var0, var1, param11, param12);
    }

    protected void createTunnel(
        CarvingContext param0,
        CaveCarverConfiguration param1,
        ChunkAccess param2,
        Function<BlockPos, Biome> param3,
        long param4,
        Aquifer param5,
        double param6,
        double param7,
        double param8,
        double param9,
        double param10,
        float param11,
        float param12,
        float param13,
        int param14,
        int param15,
        double param16,
        BitSet param17,
        WorldCarver.CarveSkipChecker param18
    ) {
        Random var0 = new Random(param4);
        int var1 = var0.nextInt(param15 / 2) + param15 / 4;
        boolean var2 = var0.nextInt(6) == 0;
        float var3 = 0.0F;
        float var4 = 0.0F;

        for(int var5 = param14; var5 < param15; ++var5) {
            double var6 = 1.5 + (double)(Mth.sin((float) Math.PI * (float)var5 / (float)param15) * param11);
            double var7 = var6 * param16;
            float var8 = Mth.cos(param13);
            param6 += (double)(Mth.cos(param12) * var8);
            param7 += (double)Mth.sin(param13);
            param8 += (double)(Mth.sin(param12) * var8);
            param13 *= var2 ? 0.92F : 0.7F;
            param13 += var4 * 0.1F;
            param12 += var3 * 0.1F;
            var4 *= 0.9F;
            var3 *= 0.75F;
            var4 += (var0.nextFloat() - var0.nextFloat()) * var0.nextFloat() * 2.0F;
            var3 += (var0.nextFloat() - var0.nextFloat()) * var0.nextFloat() * 4.0F;
            if (var5 == var1 && param11 > 1.0F) {
                this.createTunnel(
                    param0,
                    param1,
                    param2,
                    param3,
                    var0.nextLong(),
                    param5,
                    param6,
                    param7,
                    param8,
                    param9,
                    param10,
                    var0.nextFloat() * 0.5F + 0.5F,
                    param12 - (float) (Math.PI / 2),
                    param13 / 3.0F,
                    var5,
                    param15,
                    1.0,
                    param17,
                    param18
                );
                this.createTunnel(
                    param0,
                    param1,
                    param2,
                    param3,
                    var0.nextLong(),
                    param5,
                    param6,
                    param7,
                    param8,
                    param9,
                    param10,
                    var0.nextFloat() * 0.5F + 0.5F,
                    param12 + (float) (Math.PI / 2),
                    param13 / 3.0F,
                    var5,
                    param15,
                    1.0,
                    param17,
                    param18
                );
                return;
            }

            if (var0.nextInt(4) != 0) {
                if (!canReach(param2.getPos(), param6, param8, var5, param15, param11)) {
                    return;
                }

                this.carveEllipsoid(param0, param1, param2, param3, param4, param5, param6, param7, param8, var6 * param9, var7 * param10, param17, param18);
            }
        }

    }

    private static boolean shouldSkip(double param0, double param1, double param2, double param3) {
        if (param1 <= param3) {
            return true;
        } else {
            return param0 * param0 + param1 * param1 + param2 * param2 >= 1.0;
        }
    }
}
