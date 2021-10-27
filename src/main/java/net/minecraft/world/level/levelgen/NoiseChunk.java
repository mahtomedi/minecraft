package net.minecraft.world.level.levelgen;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blending.Blender;

public class NoiseChunk {
    final int cellWidth;
    final int cellHeight;
    final int cellCountY;
    final int cellCountXZ;
    final int cellNoiseMinY;
    final int firstCellX;
    final int firstCellZ;
    private final int firstNoiseX;
    private final int firstNoiseZ;
    final List<NoiseChunk.NoiseInterpolator> interpolators;
    private final NoiseSampler.FlatNoiseData[][] noiseData;
    private final Long2ObjectMap<TerrainInfo> terrainInfo = new Long2ObjectOpenHashMap<>();
    private final Aquifer aquifer;
    private final NoiseChunk.BlockStateFiller baseNoise;
    private final NoiseChunk.BlockStateFiller oreVeins;
    private final Blender blender;

    public NoiseChunk(
        int param0,
        int param1,
        int param2,
        int param3,
        int param4,
        NoiseSampler param5,
        int param6,
        int param7,
        NoiseChunk.NoiseFiller param8,
        Supplier<NoiseGeneratorSettings> param9,
        Aquifer.FluidPicker param10,
        Blender param11
    ) {
        this.cellWidth = param0;
        this.cellHeight = param1;
        this.cellCountY = param3;
        this.cellCountXZ = param2;
        this.cellNoiseMinY = param4;
        this.firstCellX = Math.floorDiv(param6, param0);
        this.firstCellZ = Math.floorDiv(param7, param0);
        this.interpolators = Lists.newArrayList();
        this.firstNoiseX = QuartPos.fromBlock(param6);
        this.firstNoiseZ = QuartPos.fromBlock(param7);
        int var0 = QuartPos.fromBlock(param2 * param0);
        this.noiseData = new NoiseSampler.FlatNoiseData[var0 + 1][];
        this.blender = param11;

        for(int var1 = 0; var1 <= var0; ++var1) {
            int var2 = this.firstNoiseX + var1;
            this.noiseData[var1] = new NoiseSampler.FlatNoiseData[var0 + 1];

            for(int var3 = 0; var3 <= var0; ++var3) {
                int var4 = this.firstNoiseZ + var3;
                this.noiseData[var1][var3] = param5.noiseData(var2, var4, param11);
            }
        }

        this.aquifer = param5.createAquifer(this, param6, param7, param4, param3, param10, param9.get().isAquifersEnabled());
        this.baseNoise = param5.makeBaseNoiseFiller(this, param8, param9.get().isNoodleCavesEnabled());
        this.oreVeins = param5.makeOreVeinifier(this, param9.get().isOreVeinsEnabled());
    }

    public NoiseSampler.FlatNoiseData noiseData(int param0, int param1) {
        return this.noiseData[param0 - this.firstNoiseX][param1 - this.firstNoiseZ];
    }

    public TerrainInfo terrainInfoWide(NoiseSampler param0, int param1, int param2) {
        int var0 = param1 - this.firstNoiseX;
        int var1 = param2 - this.firstNoiseZ;
        int var2 = this.noiseData.length;
        return var0 >= 0 && var1 >= 0 && var0 < var2 && var1 < var2
            ? this.noiseData[var0][var1].terrainInfo()
            : this.terrainInfo
                .computeIfAbsent(
                    ChunkPos.asLong(param1, param2), param1x -> param0.noiseData(ChunkPos.getX(param1x), ChunkPos.getZ(param1x), this.blender).terrainInfo()
                );
    }

    public TerrainInfo terrainInfoInterpolated(int param0, int param1) {
        int var0 = QuartPos.fromBlock(param0) - this.firstNoiseX;
        int var1 = QuartPos.fromBlock(param1) - this.firstNoiseZ;
        TerrainInfo var2 = this.noiseData[var0][var1].terrainInfo();
        TerrainInfo var3 = this.noiseData[var0][var1 + 1].terrainInfo();
        TerrainInfo var4 = this.noiseData[var0 + 1][var1].terrainInfo();
        TerrainInfo var5 = this.noiseData[var0 + 1][var1 + 1].terrainInfo();
        double var6 = (double)Math.floorMod(param0, 4) / 4.0;
        double var7 = (double)Math.floorMod(param1, 4) / 4.0;
        double var8 = Mth.lerp2(var6, var7, var2.offset(), var4.offset(), var3.offset(), var5.offset());
        double var9 = Mth.lerp2(var6, var7, var2.factor(), var4.factor(), var3.factor(), var5.factor());
        double var10 = Mth.lerp2(var6, var7, var2.jaggedness(), var4.jaggedness(), var3.jaggedness(), var5.jaggedness());
        return new TerrainInfo(var8, var9, var10);
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
            for(int var0 = 0; var0 < NoiseChunk.this.cellCountXZ + 1; ++var0) {
                int var1 = NoiseChunk.this.firstCellZ + var0;

                for(int var2 = 0; var2 < NoiseChunk.this.cellCountY + 1; ++var2) {
                    int var3 = var2 + NoiseChunk.this.cellNoiseMinY;
                    int var4 = var3 * NoiseChunk.this.cellHeight;
                    double var5 = this.noiseFiller.calculateNoise(param1 * NoiseChunk.this.cellWidth, var4, var1 * NoiseChunk.this.cellWidth);
                    param0[var0][var2] = var5;
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
