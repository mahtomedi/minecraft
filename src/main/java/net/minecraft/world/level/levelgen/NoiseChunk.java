package net.minecraft.world.level.levelgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.material.MaterialRuleList;

public class NoiseChunk implements DensityFunction.ContextProvider, DensityFunction.FunctionContext {
    private final NoiseSettings noiseSettings;
    final int cellCountXZ;
    final int cellCountY;
    final int cellNoiseMinY;
    private final int firstCellX;
    private final int firstCellZ;
    final int firstNoiseX;
    final int firstNoiseZ;
    final List<NoiseChunk.NoiseInterpolator> interpolators;
    final List<NoiseChunk.CacheAllInCell> cellCaches;
    private final Map<DensityFunction, DensityFunction> wrapped = new HashMap<>();
    private final Long2IntMap preliminarySurfaceLevel = new Long2IntOpenHashMap();
    private final Aquifer aquifer;
    private final DensityFunction initialDensityNoJaggedness;
    private final NoiseChunk.BlockStateFiller blockStateRule;
    private final Blender blender;
    private final NoiseChunk.FlatCache blendAlpha;
    private final NoiseChunk.FlatCache blendOffset;
    private long lastBlendingDataPos = ChunkPos.INVALID_CHUNK_POS;
    private Blender.BlendingOutput lastBlendingOutput = new Blender.BlendingOutput(1.0, 0.0);
    final int noiseSizeXZ;
    final int cellWidth;
    final int cellHeight;
    boolean interpolating;
    boolean fillingCell;
    private int cellStartBlockX;
    int cellStartBlockY;
    private int cellStartBlockZ;
    int inCellX;
    int inCellY;
    int inCellZ;
    long interpolationCounter;
    long arrayInterpolationCounter;
    int arrayIndex;
    private final DensityFunction.ContextProvider sliceFillingContextProvider = new DensityFunction.ContextProvider() {
        @Override
        public DensityFunction.FunctionContext forIndex(int param0) {
            NoiseChunk.this.cellStartBlockY = (param0 + NoiseChunk.this.cellNoiseMinY) * NoiseChunk.this.cellHeight;
            ++NoiseChunk.this.interpolationCounter;
            NoiseChunk.this.inCellY = 0;
            NoiseChunk.this.arrayIndex = param0;
            return NoiseChunk.this;
        }

        @Override
        public void fillAllDirectly(double[] param0, DensityFunction param1) {
            for(int var0 = 0; var0 < NoiseChunk.this.cellCountY + 1; ++var0) {
                NoiseChunk.this.cellStartBlockY = (var0 + NoiseChunk.this.cellNoiseMinY) * NoiseChunk.this.cellHeight;
                ++NoiseChunk.this.interpolationCounter;
                NoiseChunk.this.inCellY = 0;
                NoiseChunk.this.arrayIndex = var0;
                param0[var0] = param1.compute(NoiseChunk.this);
            }

        }
    };

    public static NoiseChunk forChunk(
        ChunkAccess param0, NoiseRouter param1, Supplier<DensityFunction> param2, NoiseGeneratorSettings param3, Aquifer.FluidPicker param4, Blender param5
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
        int param0, int param1, int param2, int param3, NoiseRouter param4, NoiseGeneratorSettings param5, Aquifer.FluidPicker param6
    ) {
        return new NoiseChunk(1, param3, param2, param4, param0, param1, DensityFunctions.zero(), param5, param6, Blender.empty());
    }

    private NoiseChunk(
        int param0,
        int param1,
        int param2,
        NoiseRouter param3,
        int param4,
        int param5,
        DensityFunction param6,
        NoiseGeneratorSettings param7,
        Aquifer.FluidPicker param8,
        Blender param9
    ) {
        this.noiseSettings = param7.noiseSettings();
        this.cellCountXZ = param0;
        this.cellCountY = param1;
        this.cellNoiseMinY = param2;
        this.cellWidth = this.noiseSettings.getCellWidth();
        this.cellHeight = this.noiseSettings.getCellHeight();
        this.firstCellX = Math.floorDiv(param4, this.cellWidth);
        this.firstCellZ = Math.floorDiv(param5, this.cellWidth);
        this.interpolators = Lists.newArrayList();
        this.cellCaches = Lists.newArrayList();
        this.firstNoiseX = QuartPos.fromBlock(param4);
        this.firstNoiseZ = QuartPos.fromBlock(param5);
        this.noiseSizeXZ = QuartPos.fromBlock(param0 * this.cellWidth);
        this.blender = param9;
        this.blendAlpha = new NoiseChunk.FlatCache(new NoiseChunk.BlendAlpha(), false);
        this.blendOffset = new NoiseChunk.FlatCache(new NoiseChunk.BlendOffset(), false);

        for(int var0 = 0; var0 <= this.noiseSizeXZ; ++var0) {
            int var1 = this.firstNoiseX + var0;
            int var2 = QuartPos.toBlock(var1);

            for(int var3 = 0; var3 <= this.noiseSizeXZ; ++var3) {
                int var4 = this.firstNoiseZ + var3;
                int var5 = QuartPos.toBlock(var4);
                Blender.BlendingOutput var6 = param9.blendOffsetAndFactor(var2, var5);
                this.blendAlpha.values[var0][var3] = var6.alpha();
                this.blendOffset.values[var0][var3] = var6.blendingOffset();
            }
        }

        if (!param7.isAquifersEnabled()) {
            this.aquifer = Aquifer.createDisabled(param8);
        } else {
            int var7 = SectionPos.blockToSectionCoord(param4);
            int var8 = SectionPos.blockToSectionCoord(param5);
            this.aquifer = Aquifer.create(
                this,
                new ChunkPos(var7, var8),
                param3.barrierNoise(),
                param3.fluidLevelFloodednessNoise(),
                param3.fluidLevelSpreadNoise(),
                param3.lavaNoise(),
                param3.aquiferPositionalRandomFactory(),
                param2 * this.cellHeight,
                param1 * this.cellHeight,
                param8
            );
        }

        Builder<NoiseChunk.BlockStateFiller> var9 = ImmutableList.builder();
        DensityFunction var10 = DensityFunctions.cacheAllInCell(DensityFunctions.add(param3.fullNoise(), param6)).mapAll(this::wrap);
        var9.add(param1x -> this.aquifer.computeSubstance(param1x, var10.compute(param1x)));
        if (param7.isOreVeinsEnabled()) {
            var9.add(
                OreVeinifier.create(
                    param3.veinToggle().mapAll(this::wrap),
                    param3.veinRidged().mapAll(this::wrap),
                    param3.veinGap().mapAll(this::wrap),
                    param3.oreVeinsPositionalRandomFactory()
                )
            );
        }

        this.blockStateRule = new MaterialRuleList(var9.build());
        this.initialDensityNoJaggedness = param3.initialDensityNoJaggedness().mapAll(this::wrap);
    }

    protected Climate.Sampler cachedClimateSampler(NoiseRouter param0) {
        return new Climate.Sampler(
            param0.temperature().mapAll(this::wrap),
            param0.humidity().mapAll(this::wrap),
            param0.continentalness().mapAll(this::wrap),
            param0.erosion().mapAll(this::wrap),
            param0.depth().mapAll(this::wrap),
            param0.weirdness().mapAll(this::wrap),
            param0.spawnTarget()
        );
    }

    @Nullable
    protected BlockState getInterpolatedState() {
        return this.blockStateRule.calculate(this);
    }

    @Override
    public int blockX() {
        return this.cellStartBlockX + this.inCellX;
    }

    @Override
    public int blockY() {
        return this.cellStartBlockY + this.inCellY;
    }

    @Override
    public int blockZ() {
        return this.cellStartBlockZ + this.inCellZ;
    }

    public int preliminarySurfaceLevel(int param0, int param1) {
        return this.preliminarySurfaceLevel
            .computeIfAbsent(ChunkPos.asLong(QuartPos.fromBlock(param0), QuartPos.fromBlock(param1)), this::computePreliminarySurfaceLevel);
    }

    private int computePreliminarySurfaceLevel(long param0x) {
        int var0 = ChunkPos.getX(param0x);
        int var1 = ChunkPos.getZ(param0x);
        return (int)NoiseRouterData.computePreliminarySurfaceLevelScanning(
            this.noiseSettings, this.initialDensityNoJaggedness, QuartPos.toBlock(var0), QuartPos.toBlock(var1)
        );
    }

    @Override
    public Blender getBlender() {
        return this.blender;
    }

    private void fillSlice(boolean param0, int param1) {
        this.cellStartBlockX = param1 * this.cellWidth;
        this.inCellX = 0;

        for(int var0 = 0; var0 < this.cellCountXZ + 1; ++var0) {
            int var1 = this.firstCellZ + var0;
            this.cellStartBlockZ = var1 * this.cellWidth;
            this.inCellZ = 0;
            ++this.arrayInterpolationCounter;

            for(NoiseChunk.NoiseInterpolator var2 : this.interpolators) {
                double[] var3 = (param0 ? var2.slice0 : var2.slice1)[var0];
                var2.fillArray(var3, this.sliceFillingContextProvider);
            }
        }

        ++this.arrayInterpolationCounter;
    }

    public void initializeForFirstCellX() {
        if (this.interpolating) {
            throw new IllegalStateException("Staring interpolation twice");
        } else {
            this.interpolating = true;
            this.interpolationCounter = 0L;
            this.fillSlice(true, this.firstCellX);
        }
    }

    public void advanceCellX(int param0) {
        this.fillSlice(false, this.firstCellX + param0 + 1);
        this.cellStartBlockX = (this.firstCellX + param0) * this.cellWidth;
    }

    public NoiseChunk forIndex(int param0) {
        int var0 = Math.floorMod(param0, this.cellWidth);
        int var1 = Math.floorDiv(param0, this.cellWidth);
        int var2 = Math.floorMod(var1, this.cellWidth);
        int var3 = this.cellHeight - 1 - Math.floorDiv(var1, this.cellWidth);
        this.inCellX = var2;
        this.inCellY = var3;
        this.inCellZ = var0;
        this.arrayIndex = param0;
        return this;
    }

    @Override
    public void fillAllDirectly(double[] param0, DensityFunction param1) {
        this.arrayIndex = 0;

        for(int var0 = this.cellHeight - 1; var0 >= 0; --var0) {
            this.inCellY = var0;

            for(int var1 = 0; var1 < this.cellWidth; ++var1) {
                this.inCellX = var1;

                for(int var2 = 0; var2 < this.cellWidth; ++var2) {
                    this.inCellZ = var2;
                    param0[this.arrayIndex++] = param1.compute(this);
                }
            }
        }

    }

    public void selectCellYZ(int param0, int param1) {
        this.interpolators.forEach(param2 -> param2.selectCellYZ(param0, param1));
        this.fillingCell = true;
        this.cellStartBlockY = (param0 + this.cellNoiseMinY) * this.cellHeight;
        this.cellStartBlockZ = (this.firstCellZ + param1) * this.cellWidth;
        ++this.arrayInterpolationCounter;

        for(NoiseChunk.CacheAllInCell var0 : this.cellCaches) {
            var0.noiseFiller.fillArray(var0.values, this);
        }

        ++this.arrayInterpolationCounter;
        this.fillingCell = false;
    }

    public void updateForY(int param0, double param1) {
        this.inCellY = param0 - this.cellStartBlockY;
        this.interpolators.forEach(param1x -> param1x.updateForY(param1));
    }

    public void updateForX(int param0, double param1) {
        this.inCellX = param0 - this.cellStartBlockX;
        this.interpolators.forEach(param1x -> param1x.updateForX(param1));
    }

    public void updateForZ(int param0, double param1) {
        this.inCellZ = param0 - this.cellStartBlockZ;
        ++this.interpolationCounter;
        this.interpolators.forEach(param1x -> param1x.updateForZ(param1));
    }

    public void stopInterpolation() {
        if (!this.interpolating) {
            throw new IllegalStateException("Staring interpolation twice");
        } else {
            this.interpolating = false;
        }
    }

    public void swapSlices() {
        this.interpolators.forEach(NoiseChunk.NoiseInterpolator::swapSlices);
    }

    public Aquifer aquifer() {
        return this.aquifer;
    }

    Blender.BlendingOutput getOrComputeBlendingOutput(int param0, int param1) {
        long var0 = ChunkPos.asLong(param0, param1);
        if (this.lastBlendingDataPos == var0) {
            return this.lastBlendingOutput;
        } else {
            this.lastBlendingDataPos = var0;
            Blender.BlendingOutput var1 = this.blender.blendOffsetAndFactor(param0, param1);
            this.lastBlendingOutput = var1;
            return var1;
        }
    }

    protected DensityFunction wrap(DensityFunction param0x) {
        return this.wrapped.computeIfAbsent(param0x, this::wrapNew);
    }

    private DensityFunction wrapNew(DensityFunction param0x) {
        if (param0x instanceof DensityFunctions.Marker var0x) {
            return (DensityFunction)(switch(var0x.type()) {
                case Interpolated -> new NoiseChunk.NoiseInterpolator(var0x.function());
                case FlatCache -> new NoiseChunk.FlatCache(var0x.function(), true);
                case Cache2D -> new NoiseChunk.Cache2D(var0x.function());
                case CacheOnce -> new NoiseChunk.CacheOnce(var0x.function());
                case CacheAllInCell -> new NoiseChunk.CacheAllInCell(var0x.function());
            });
        } else {
            if (this.blender != Blender.empty()) {
                if (param0x == DensityFunctions.BlendAlpha.INSTANCE) {
                    return this.blendAlpha;
                }

                if (param0x == DensityFunctions.BlendOffset.INSTANCE) {
                    return this.blendOffset;
                }
            }

            return param0x;
        }
    }

    class BlendAlpha implements NoiseChunk.NoiseChunkDensityFunction {
        @Override
        public DensityFunction wrapped() {
            return DensityFunctions.BlendAlpha.INSTANCE;
        }

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return NoiseChunk.this.getOrComputeBlendingOutput(param0.blockX(), param0.blockZ()).alpha();
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            param1.fillAllDirectly(param0, this);
        }

        @Override
        public double minValue() {
            return 0.0;
        }

        @Override
        public double maxValue() {
            return 1.0;
        }
    }

    class BlendOffset implements NoiseChunk.NoiseChunkDensityFunction {
        @Override
        public DensityFunction wrapped() {
            return DensityFunctions.BlendOffset.INSTANCE;
        }

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            return NoiseChunk.this.getOrComputeBlendingOutput(param0.blockX(), param0.blockZ()).blendingOffset();
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            param1.fillAllDirectly(param0, this);
        }

        @Override
        public double minValue() {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        public double maxValue() {
            return Double.POSITIVE_INFINITY;
        }
    }

    @FunctionalInterface
    public interface BlockStateFiller {
        @Nullable
        BlockState calculate(DensityFunction.FunctionContext var1);
    }

    static class Cache2D implements NoiseChunk.NoiseChunkDensityFunction {
        private final DensityFunction function;
        private long lastPos2D = ChunkPos.INVALID_CHUNK_POS;
        private double lastValue;

        Cache2D(DensityFunction param0) {
            this.function = param0;
        }

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            int var0 = param0.blockX();
            int var1 = param0.blockZ();
            long var2 = ChunkPos.asLong(var0, var1);
            if (this.lastPos2D == var2) {
                return this.lastValue;
            } else {
                this.lastPos2D = var2;
                double var3 = this.function.compute(param0);
                this.lastValue = var3;
                return var3;
            }
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            this.function.fillArray(param0, param1);
        }

        @Override
        public DensityFunction wrapped() {
            return this.function;
        }
    }

    class CacheAllInCell implements NoiseChunk.NoiseChunkDensityFunction {
        final DensityFunction noiseFiller;
        final double[] values;

        CacheAllInCell(DensityFunction param0) {
            this.noiseFiller = param0;
            this.values = new double[NoiseChunk.this.cellWidth * NoiseChunk.this.cellWidth * NoiseChunk.this.cellHeight];
            NoiseChunk.this.cellCaches.add(this);
        }

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            if (param0 != NoiseChunk.this) {
                return this.noiseFiller.compute(param0);
            } else if (!NoiseChunk.this.interpolating) {
                throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
            } else {
                int var0 = NoiseChunk.this.inCellX;
                int var1 = NoiseChunk.this.inCellY;
                int var2 = NoiseChunk.this.inCellZ;
                return var0 >= 0
                        && var1 >= 0
                        && var2 >= 0
                        && var0 < NoiseChunk.this.cellWidth
                        && var1 < NoiseChunk.this.cellHeight
                        && var2 < NoiseChunk.this.cellWidth
                    ? this.values[((NoiseChunk.this.cellHeight - 1 - var1) * NoiseChunk.this.cellWidth + var0) * NoiseChunk.this.cellWidth + var2]
                    : this.noiseFiller.compute(param0);
            }
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            param1.fillAllDirectly(param0, this);
        }

        @Override
        public DensityFunction wrapped() {
            return this.noiseFiller;
        }
    }

    class CacheOnce implements NoiseChunk.NoiseChunkDensityFunction {
        private final DensityFunction function;
        private long lastCounter;
        private long lastArrayCounter;
        private double lastValue;
        @Nullable
        private double[] lastArray;

        CacheOnce(DensityFunction param0) {
            this.function = param0;
        }

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            if (param0 != NoiseChunk.this) {
                return this.function.compute(param0);
            } else if (this.lastArray != null && this.lastArrayCounter == NoiseChunk.this.arrayInterpolationCounter) {
                return this.lastArray[NoiseChunk.this.arrayIndex];
            } else if (this.lastCounter == NoiseChunk.this.interpolationCounter) {
                return this.lastValue;
            } else {
                this.lastCounter = NoiseChunk.this.interpolationCounter;
                double var0 = this.function.compute(param0);
                this.lastValue = var0;
                return var0;
            }
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            if (this.lastArray != null && this.lastArrayCounter == NoiseChunk.this.arrayInterpolationCounter) {
                System.arraycopy(this.lastArray, 0, param0, 0, param0.length);
            } else {
                this.wrapped().fillArray(param0, param1);
                if (this.lastArray != null && this.lastArray.length == param0.length) {
                    System.arraycopy(param0, 0, this.lastArray, 0, param0.length);
                } else {
                    this.lastArray = (double[])param0.clone();
                }

                this.lastArrayCounter = NoiseChunk.this.arrayInterpolationCounter;
            }
        }

        @Override
        public DensityFunction wrapped() {
            return this.function;
        }
    }

    class FlatCache implements NoiseChunk.NoiseChunkDensityFunction {
        private final DensityFunction noiseFiller;
        final double[][] values;

        FlatCache(DensityFunction param0, boolean param1) {
            this.noiseFiller = param0;
            this.values = new double[NoiseChunk.this.noiseSizeXZ + 1][NoiseChunk.this.noiseSizeXZ + 1];
            if (param1) {
                for(int param2 = 0; param2 <= NoiseChunk.this.noiseSizeXZ; ++param2) {
                    int var0 = NoiseChunk.this.firstNoiseX + param2;
                    int var1 = QuartPos.toBlock(var0);

                    for(int var2 = 0; var2 <= NoiseChunk.this.noiseSizeXZ; ++var2) {
                        int var3 = NoiseChunk.this.firstNoiseZ + var2;
                        int var4 = QuartPos.toBlock(var3);
                        this.values[param2][var2] = param0.compute(new DensityFunction.SinglePointContext(var1, 0, var4));
                    }
                }
            }

        }

        @Override
        public double compute(DensityFunction.FunctionContext param0) {
            int var0 = QuartPos.fromBlock(param0.blockX());
            int var1 = QuartPos.fromBlock(param0.blockZ());
            int var2 = var0 - NoiseChunk.this.firstNoiseX;
            int var3 = var1 - NoiseChunk.this.firstNoiseZ;
            int var4 = this.values.length;
            return var2 >= 0 && var3 >= 0 && var2 < var4 && var3 < var4 ? this.values[var2][var3] : this.noiseFiller.compute(param0);
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            param1.fillAllDirectly(param0, this);
        }

        @Override
        public DensityFunction wrapped() {
            return this.noiseFiller;
        }
    }

    interface NoiseChunkDensityFunction extends DensityFunction {
        DensityFunction wrapped();

        @Override
        default DensityFunction mapAll(DensityFunction.Visitor param0) {
            return this.wrapped().mapAll(param0);
        }

        @Override
        default double minValue() {
            return this.wrapped().minValue();
        }

        @Override
        default double maxValue() {
            return this.wrapped().maxValue();
        }
    }

    public class NoiseInterpolator implements NoiseChunk.NoiseChunkDensityFunction {
        double[][] slice0;
        double[][] slice1;
        private final DensityFunction noiseFiller;
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

        NoiseInterpolator(DensityFunction param1) {
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
        public double compute(DensityFunction.FunctionContext param0) {
            if (param0 != NoiseChunk.this) {
                return this.noiseFiller.compute(param0);
            } else if (!NoiseChunk.this.interpolating) {
                throw new IllegalStateException("Trying to sample interpolator outside the interpolation loop");
            } else {
                return NoiseChunk.this.fillingCell
                    ? Mth.lerp3(
                        (double)NoiseChunk.this.inCellX / (double)NoiseChunk.this.cellWidth,
                        (double)NoiseChunk.this.inCellY / (double)NoiseChunk.this.cellHeight,
                        (double)NoiseChunk.this.inCellZ / (double)NoiseChunk.this.cellWidth,
                        this.noise000,
                        this.noise100,
                        this.noise010,
                        this.noise110,
                        this.noise001,
                        this.noise101,
                        this.noise011,
                        this.noise111
                    )
                    : this.value;
            }
        }

        @Override
        public void fillArray(double[] param0, DensityFunction.ContextProvider param1) {
            if (NoiseChunk.this.fillingCell) {
                param1.fillAllDirectly(param0, this);
            } else {
                this.wrapped().fillArray(param0, param1);
            }
        }

        @Override
        public DensityFunction wrapped() {
            return this.noiseFiller;
        }

        private void swapSlices() {
            double[][] var0 = this.slice0;
            this.slice0 = this.slice1;
            this.slice1 = var0;
        }
    }
}
