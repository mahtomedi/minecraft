package net.minecraft.world.level.levelgen;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.blending.Blender;

public class NoiseChunk {
    private final NoiseSampler sampler;
    final NoiseSettings noiseSettings;
    final int cellCountXZ;
    final int cellCountY;
    final int cellNoiseMinY;
    final int firstCellX;
    final int firstCellZ;
    private final int firstNoiseX;
    private final int firstNoiseZ;
    final List<NoiseChunk.NoiseInterpolator> interpolators;
    private final NoiseSampler.FlatNoiseData[][] noiseData;
    private final Long2IntMap preliminarySurfaceLevel = new Long2IntOpenHashMap();
    private final Aquifer aquifer;
    private final NoiseChunk.BlockStateFiller baseNoise;
    private final NoiseChunk.BlockStateFiller oreVeins;
    private final Blender blender;

    public static NoiseChunk forChunk(
        ChunkAccess param0,
        NoiseSampler param1,
        Supplier<NoiseChunk.NoiseFiller> param2,
        NoiseGeneratorSettings param3,
        Aquifer.FluidPicker param4,
        Blender param5
    ) {
        ChunkPos var0 = param0.getPos();
        NoiseSettings var1 = param3.noiseSettings();
        int var2 = Math.max(var1.minY(), param0.getMinBuildHeight());
        int var3 = Math.min(var1.minY() + var1.height(), param0.getMaxBuildHeight());
        int var4 = Mth.intFloorDiv(var2, var1.getCellHeight());
        int var5 = Mth.intFloorDiv(var3 - var2, var1.getCellHeight());
        return new NoiseChunk(16 / var1.getCellWidth(), var5, var4, param1, var0.getMinBlockX(), var0.getMinBlockZ(), param2.get(), param3, param4, param5);
    }

    public static NoiseChunk forColumn(
        int param0, int param1, int param2, int param3, NoiseSampler param4, NoiseGeneratorSettings param5, Aquifer.FluidPicker param6
    ) {
        return new NoiseChunk(1, param3, param2, param4, param0, param1, (param0x, param1x, param2x) -> 0.0, param5, param6, Blender.empty());
    }

    private NoiseChunk(
        int param0,
        int param1,
        int param2,
        NoiseSampler param3,
        int param4,
        int param5,
        NoiseChunk.NoiseFiller param6,
        NoiseGeneratorSettings param7,
        Aquifer.FluidPicker param8,
        Blender param9
    ) {
        this.noiseSettings = param7.noiseSettings();
        this.cellCountXZ = param0;
        this.cellCountY = param1;
        this.cellNoiseMinY = param2;
        this.sampler = param3;
        int var0 = this.noiseSettings.getCellWidth();
        this.firstCellX = Math.floorDiv(param4, var0);
        this.firstCellZ = Math.floorDiv(param5, var0);
        this.interpolators = Lists.newArrayList();
        this.firstNoiseX = QuartPos.fromBlock(param4);
        this.firstNoiseZ = QuartPos.fromBlock(param5);
        int var1 = QuartPos.fromBlock(param0 * var0);
        this.noiseData = new NoiseSampler.FlatNoiseData[var1 + 1][];
        this.blender = param9;

        for(int var2 = 0; var2 <= var1; ++var2) {
            int var3 = this.firstNoiseX + var2;
            this.noiseData[var2] = new NoiseSampler.FlatNoiseData[var1 + 1];

            for(int var4 = 0; var4 <= var1; ++var4) {
                int var5 = this.firstNoiseZ + var4;
                this.noiseData[var2][var4] = param3.noiseData(var3, var5, param9);
            }
        }

        this.aquifer = param3.createAquifer(this, param4, param5, param2, param1, param8, param7.isAquifersEnabled());
        this.baseNoise = param3.makeBaseNoiseFiller(this, param6, param7.isNoodleCavesEnabled());
        this.oreVeins = param3.makeOreVeinifier(this, param7.isOreVeinsEnabled());
    }

    public NoiseSampler.FlatNoiseData noiseData(int param0, int param1) {
        return this.noiseData[param0 - this.firstNoiseX][param1 - this.firstNoiseZ];
    }

    public int preliminarySurfaceLevel(int param0, int param1) {
        return this.preliminarySurfaceLevel
            .computeIfAbsent(ChunkPos.asLong(QuartPos.fromBlock(param0), QuartPos.fromBlock(param1)), this::computePreliminarySurfaceLevel);
    }

    private int computePreliminarySurfaceLevel(long param0x) {
        int var0 = ChunkPos.getX(param0x);
        int var1 = ChunkPos.getZ(param0x);
        int var2 = var0 - this.firstNoiseX;
        int var3 = var1 - this.firstNoiseZ;
        int var4 = this.noiseData.length;
        TerrainInfo var5;
        if (var2 >= 0 && var3 >= 0 && var2 < var4 && var3 < var4) {
            var5 = this.noiseData[var2][var3].terrainInfo();
        } else {
            var5 = this.sampler.noiseData(var0, var1, this.blender).terrainInfo();
        }

        return this.sampler.getPreliminarySurfaceLevel(QuartPos.toBlock(var0), QuartPos.toBlock(var1), var5);
    }

    protected NoiseChunk.NoiseInterpolator createNoiseInterpolator(NoiseChunk.NoiseFiller param0) {
        return new NoiseChunk.NoiseInterpolator(param0);
    }

    public Blender getBlender() {
        return this.blender;
    }

    public void initializeForFirstCellX() {
        this.interpolators.forEach(param0 -> param0.initializeForFirstCellX());
    }

    public void advanceCellX(int param0) {
        this.interpolators.forEach(param1 -> param1.advanceCellX(param0));
    }

    public void selectCellYZ(int param0, int param1) {
        this.interpolators.forEach(param2 -> param2.selectCellYZ(param0, param1));
    }

    public void updateForY(double param0) {
        this.interpolators.forEach(param1 -> param1.updateForY(param0));
    }

    public void updateForX(double param0) {
        this.interpolators.forEach(param1 -> param1.updateForX(param0));
    }

    public void updateForZ(double param0) {
        this.interpolators.forEach(param1 -> param1.updateForZ(param0));
    }

    public void swapSlices() {
        this.interpolators.forEach(NoiseChunk.NoiseInterpolator::swapSlices);
    }

    public Aquifer aquifer() {
        return this.aquifer;
    }

    @Nullable
    protected BlockState updateNoiseAndGenerateBaseState(int param0, int param1, int param2) {
        return this.baseNoise.calculate(param0, param1, param2);
    }

    @Nullable
    protected BlockState oreVeinify(int param0, int param1, int param2) {
        return this.oreVeins.calculate(param0, param1, param2);
    }

    @FunctionalInterface
    public interface BlockStateFiller {
        @Nullable
        BlockState calculate(int var1, int var2, int var3);
    }

    @FunctionalInterface
    public interface InterpolatableNoise {
        NoiseChunk.Sampler instantiate(NoiseChunk var1);
    }

    @FunctionalInterface
    public interface NoiseFiller {
        double calculateNoise(int var1, int var2, int var3);
    }

    public class NoiseInterpolator implements NoiseChunk.Sampler {
        private double[][] slice0;
        private double[][] slice1;
        private final NoiseChunk.NoiseFiller noiseFiller;
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
        private double value;

        NoiseInterpolator(NoiseChunk.NoiseFiller param1) {
            this.noiseFiller = param1;
            this.slice0 = this.allocateSlice(NoiseChunk.this.cellCountY, NoiseChunk.this.cellCountXZ);
            this.slice1 = this.allocateSlice(NoiseChunk.this.cellCountY, NoiseChunk.this.cellCountXZ);
            NoiseChunk.this.interpolators.add(this);
        }

        private double[][] allocateSlice(int param0, int param1) {
            int var0 = param1 + 1;
            int var1 = param0 + 1;
            double[][] var2 = new double[var0][var1];

            for(int var3 = 0; var3 < var0; ++var3) {
                var2[var3] = new double[var1];
            }

            return var2;
        }

        void initializeForFirstCellX() {
            this.fillSlice(this.slice0, NoiseChunk.this.firstCellX);
        }

        void advanceCellX(int param0) {
            this.fillSlice(this.slice1, NoiseChunk.this.firstCellX + param0 + 1);
        }

        private void fillSlice(double[][] param0, int param1) {
            int var0 = NoiseChunk.this.noiseSettings.getCellWidth();
            int var1 = NoiseChunk.this.noiseSettings.getCellHeight();

            for(int var2 = 0; var2 < NoiseChunk.this.cellCountXZ + 1; ++var2) {
                int var3 = NoiseChunk.this.firstCellZ + var2;

                for(int var4 = 0; var4 < NoiseChunk.this.cellCountY + 1; ++var4) {
                    int var5 = var4 + NoiseChunk.this.cellNoiseMinY;
                    int var6 = var5 * var1;
                    double var7 = this.noiseFiller.calculateNoise(param1 * var0, var6, var3 * var0);
                    param0[var2][var4] = var7;
                }
            }

        }

        void selectCellYZ(int param0, int param1) {
            this.noise000 = this.slice0[param1][param0];
            this.noise001 = this.slice0[param1 + 1][param0];
            this.noise100 = this.slice1[param1][param0];
            this.noise101 = this.slice1[param1 + 1][param0];
            this.noise010 = this.slice0[param1][param0 + 1];
            this.noise011 = this.slice0[param1 + 1][param0 + 1];
            this.noise110 = this.slice1[param1][param0 + 1];
            this.noise111 = this.slice1[param1 + 1][param0 + 1];
        }

        void updateForY(double param0) {
            this.valueXZ00 = Mth.lerp(param0, this.noise000, this.noise010);
            this.valueXZ10 = Mth.lerp(param0, this.noise100, this.noise110);
            this.valueXZ01 = Mth.lerp(param0, this.noise001, this.noise011);
            this.valueXZ11 = Mth.lerp(param0, this.noise101, this.noise111);
        }

        void updateForX(double param0) {
            this.valueZ0 = Mth.lerp(param0, this.valueXZ00, this.valueXZ10);
            this.valueZ1 = Mth.lerp(param0, this.valueXZ01, this.valueXZ11);
        }

        void updateForZ(double param0) {
            this.value = Mth.lerp(param0, this.valueZ0, this.valueZ1);
        }

        @Override
        public double sample() {
            return this.value;
        }

        private void swapSlices() {
            double[][] var0 = this.slice0;
            this.slice0 = this.slice1;
            this.slice1 = var0;
        }
    }

    @FunctionalInterface
    public interface Sampler {
        double sample();
    }
}
