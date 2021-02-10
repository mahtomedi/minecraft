package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.NoiseUtils;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class Cavifier {
    private final int minCellY;
    private final NormalNoise layerNoiseSource;
    private final NormalNoise pillarNoiseSource;
    private final NormalNoise pillarRarenessModulator;
    private final NormalNoise pillarThicknessModulator;
    private final NormalNoise spaghetti2dNoiseSource;
    private final NormalNoise spaghetti2dElevationModulator;
    private final NormalNoise spaghetti2dRarityModulator;
    private final NormalNoise spaghetti2dThicknessModulator;
    private final NormalNoise spaghetti3dNoiseSource1;
    private final NormalNoise spaghetti3dNoiseSource2;
    private final NormalNoise spaghetti3dRarityModulator;
    private final NormalNoise spaghetti3dThicknessModulator;
    private final NormalNoise spaghettiRoughnessNoise;
    private final NormalNoise spaghettiRoughnessModulator;
    private final NormalNoise caveEntranceNoiseSource;

    public Cavifier(RandomSource param0, int param1) {
        this.minCellY = param1;
        this.pillarNoiseSource = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -7, 1.0, 1.0);
        this.pillarRarenessModulator = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -8, 1.0);
        this.pillarThicknessModulator = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -8, 1.0);
        this.spaghetti2dNoiseSource = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -7, 1.0);
        this.spaghetti2dElevationModulator = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -8, 1.0);
        this.spaghetti2dRarityModulator = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -11, 1.0);
        this.spaghetti2dThicknessModulator = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -11, 1.0);
        this.spaghetti3dNoiseSource1 = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -7, 1.0);
        this.spaghetti3dNoiseSource2 = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -7, 1.0);
        this.spaghetti3dRarityModulator = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -11, 1.0);
        this.spaghetti3dThicknessModulator = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -8, 1.0);
        this.spaghettiRoughnessNoise = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -5, 1.0);
        this.spaghettiRoughnessModulator = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -8, 1.0);
        this.caveEntranceNoiseSource = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -8, 1.0, 1.0, 1.0);
        this.layerNoiseSource = NormalNoise.create(new SimpleRandomSource(param0.nextLong()), -8, 1.0);
    }

    public double cavify(int param0, int param1, int param2, double param3, double param4) {
        boolean var0 = param4 >= 375.0;
        double var1 = this.spaghettiRoughness(param0, param1, param2);
        double var2 = this.getSpaghetti3d(param0, param1, param2);
        if (var0) {
            double var3 = param3 / 128.0;
            double var4 = Mth.clamp(var3 + 0.2, -1.0, 1.0);
            double var5 = this.getLayerizedCaverns(param0, param1, param2);
            double var6 = this.getSpaghetti2d(param0, param1, param2);
            double var7 = var4 + var5;
            double var8 = Math.min(var7, Math.min(var2, var6) + var1);
            double var9 = Math.max(var8, this.getPillars(param0, param1, param2));
            return 128.0 * Mth.clamp(var9, -1.0, 1.0);
        } else {
            return Math.min(param4, (var2 + var1) * 128.0);
        }
    }

    private double getPillars(int param0, int param1, int param2) {
        double var0 = 0.0;
        double var1 = 2.0;
        double var2 = NoiseUtils.sampleNoiseAndMapToRange(this.pillarRarenessModulator, (double)param0, (double)param1, (double)param2, 0.0, 2.0);
        int var3 = 0;
        int var4 = 1;
        double var5 = NoiseUtils.sampleNoiseAndMapToRange(this.pillarThicknessModulator, (double)param0, (double)param1, (double)param2, 0.0, 1.0);
        var5 = Math.pow(var5, 3.0);
        double var6 = 25.0;
        double var7 = 0.3;
        double var8 = this.pillarNoiseSource.getValue((double)param0 * 25.0, (double)param1 * 0.3, (double)param2 * 25.0);
        var8 = var5 * (var8 * 2.0 - var2);
        return var8 > 0.02 ? var8 : Double.NEGATIVE_INFINITY;
    }

    private double getLayerizedCaverns(int param0, int param1, int param2) {
        double var0 = this.layerNoiseSource.getValue((double)param0, (double)(param1 * 8), (double)param2);
        return Mth.square(var0) * 4.0;
    }

    private double getSpaghetti3d(int param0, int param1, int param2) {
        double var0 = this.spaghetti3dRarityModulator.getValue((double)(param0 * 2), (double)param1, (double)(param2 * 2));
        double var1 = this.getQuantizedSpaghettiRarity(var0);
        double var2 = 0.065;
        double var3 = 0.09;
        double var4 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti3dThicknessModulator, (double)param0, (double)param1, (double)param2, 0.065, 0.09);
        double var5 = sampleWithRarity(this.spaghetti3dNoiseSource1, (double)param0, (double)param1, (double)param2, var1);
        double var6 = Math.abs(var1 * var5) - var4;
        double var7 = sampleWithRarity(this.spaghetti3dNoiseSource2, (double)param0, (double)param1, (double)param2, var1);
        double var8 = Math.abs(var1 * var7) - var4;
        return clampToUnit(Math.max(var6, var8));
    }

    private double getSpaghetti2d(int param0, int param1, int param2) {
        double var0 = this.spaghetti2dRarityModulator.getValue((double)(param0 * 2), (double)param1, (double)(param2 * 2));
        double var1 = this.getQuantizedSpaghettiRarity(var0);
        double var2 = 0.6;
        double var3 = 1.3;
        double var4 = NoiseUtils.sampleNoiseAndMapToRange(
            this.spaghetti2dThicknessModulator, (double)(param0 * 2), (double)param1, (double)(param2 * 2), 0.6, 1.3
        );
        double var5 = sampleWithRarity(this.spaghetti2dNoiseSource, (double)param0, (double)param1, (double)param2, var1);
        double var6 = 0.083;
        double var7 = Math.abs(var1 * var5) - 0.083 * var4;
        int var8 = this.minCellY;
        int var9 = 8;
        double var10 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghetti2dElevationModulator, (double)param0, 0.0, (double)param2, (double)var8, 8.0);
        double var11 = Math.abs(var10 - (double)param1 / 8.0) - 1.0 * var4;
        var11 = var11 * var11 * var11;
        return clampToUnit(Math.max(var11, var7));
    }

    private double spaghettiRoughness(int param0, int param1, int param2) {
        double var0 = NoiseUtils.sampleNoiseAndMapToRange(this.spaghettiRoughnessModulator, (double)param0, (double)param1, (double)param2, 0.0, 0.1);
        return (0.4 - Math.abs(this.spaghettiRoughnessNoise.getValue((double)param0, (double)param1, (double)param2))) * var0;
    }

    private double getQuantizedSpaghettiRarity(double param0) {
        if (param0 < -0.75) {
            return 0.5;
        } else if (param0 < -0.5) {
            return 0.75;
        } else if (param0 < 0.5) {
            return 1.0;
        } else {
            return param0 < 0.75 ? 2.0 : 3.0;
        }
    }

    private static double clampToUnit(double param0) {
        return Mth.clamp(param0, -1.0, 1.0);
    }

    private static double sampleWithRarity(NormalNoise param0, double param1, double param2, double param3, double param4) {
        return param0.getValue(param1 / param4, param2 / param4, param3 / param4);
    }
}
