package net.minecraft.world.level.levelgen;

import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

public class NoiseInterpolator {
    private double[][] slice0;
    private double[][] slice1;
    private final int cellCountY;
    private final int cellCountZ;
    private final int cellNoiseMinY;
    private final NoiseInterpolator.NoiseColumnFiller noiseColumnFiller;
    private double noise000;
    private double noise001;
    private double noise100;
    private double noise101;
    private double noise010;
    private double noise011;
    private double noise110;
    private double noise111;
    private double valueXZ00;
    private double valueXZ10;
    private double valueXZ01;
    private double valueXZ11;
    private double valueZ0;
    private double valueZ1;
    private final int firstCellXInChunk;
    private final int firstCellZInChunk;

    public NoiseInterpolator(int param0, int param1, int param2, ChunkPos param3, int param4, NoiseInterpolator.NoiseColumnFiller param5) {
        this.cellCountY = param1;
        this.cellCountZ = param2;
        this.cellNoiseMinY = param4;
        this.noiseColumnFiller = param5;
        this.slice0 = allocateSlice(param1, param2);
        this.slice1 = allocateSlice(param1, param2);
        this.firstCellXInChunk = param3.x * param0;
        this.firstCellZInChunk = param3.z * param2;
    }

    private static double[][] allocateSlice(int param0, int param1) {
        int var0 = param1 + 1;
        int var1 = param0 + 1;
        double[][] var2 = new double[var0][var1];

        for(int var3 = 0; var3 < var0; ++var3) {
            var2[var3] = new double[var1];
        }

        return var2;
    }

    public void initializeForFirstCellX() {
        this.fillSlice(this.slice0, this.firstCellXInChunk);
    }

    public void advanceCellX(int param0) {
        this.fillSlice(this.slice1, this.firstCellXInChunk + param0 + 1);
    }

    private void fillSlice(double[][] param0, int param1) {
        for(int var0 = 0; var0 < this.cellCountZ + 1; ++var0) {
            int var1 = this.firstCellZInChunk + var0;
            this.noiseColumnFiller.fillNoiseColumn(param0[var0], param1, var1, this.cellNoiseMinY, this.cellCountY);
        }

    }

    public void selectCellYZ(int param0, int param1) {
        this.noise000 = this.slice0[param1][param0];
        this.noise001 = this.slice0[param1 + 1][param0];
        this.noise100 = this.slice1[param1][param0];
        this.noise101 = this.slice1[param1 + 1][param0];
        this.noise010 = this.slice0[param1][param0 + 1];
        this.noise011 = this.slice0[param1 + 1][param0 + 1];
        this.noise110 = this.slice1[param1][param0 + 1];
        this.noise111 = this.slice1[param1 + 1][param0 + 1];
    }

    public void updateForY(double param0) {
        this.valueXZ00 = Mth.lerp(param0, this.noise000, this.noise010);
        this.valueXZ10 = Mth.lerp(param0, this.noise100, this.noise110);
        this.valueXZ01 = Mth.lerp(param0, this.noise001, this.noise011);
        this.valueXZ11 = Mth.lerp(param0, this.noise101, this.noise111);
    }

    public void updateForX(double param0) {
        this.valueZ0 = Mth.lerp(param0, this.valueXZ00, this.valueXZ10);
        this.valueZ1 = Mth.lerp(param0, this.valueXZ01, this.valueXZ11);
    }

    public double calculateValue(double param0) {
        return Mth.lerp(param0, this.valueZ0, this.valueZ1);
    }

    public void swapSlices() {
        double[][] var0 = this.slice0;
        this.slice0 = this.slice1;
        this.slice1 = var0;
    }

    @FunctionalInterface
    public interface NoiseColumnFiller {
        void fillNoiseColumn(double[] var1, int var2, int var3, int var4, int var5);
    }
}
