package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

public class Blender {
    private static final Blender EMPTY = new Blender(new Long2ObjectOpenHashMap(), new Long2ObjectOpenHashMap()) {
        @Override
        public Blender.BlendingOutput blendOffsetAndFactor(int param0, int param1) {
            return new Blender.BlendingOutput(1.0, 0.0);
        }

        @Override
        public double blendDensity(DensityFunction.FunctionContext param0, double param1) {
            return param1;
        }

        @Override
        public BiomeResolver getBiomeResolver(BiomeResolver param0) {
            return param0;
        }
    };
    private static final NormalNoise SHIFT_NOISE = NormalNoise.create(new XoroshiroRandomSource(42L), BuiltinRegistries.NOISE.getOrThrow(Noises.SHIFT));
    private static final int HEIGHT_BLENDING_RANGE_CELLS = QuartPos.fromSection(7) - 1;
    private static final int HEIGHT_BLENDING_RANGE_CHUNKS = QuartPos.toSection(HEIGHT_BLENDING_RANGE_CELLS + 3);
    private static final int DENSITY_BLENDING_RANGE_CELLS = 2;
    private static final int DENSITY_BLENDING_RANGE_CHUNKS = QuartPos.toSection(5);
    private static final double OLD_CHUNK_XZ_RADIUS = 8.0;
    private final Long2ObjectOpenHashMap<BlendingData> heightAndBiomeBlendingData;
    private final Long2ObjectOpenHashMap<BlendingData> densityBlendingData;

    public static Blender empty() {
        return EMPTY;
    }

    public static Blender of(@Nullable WorldGenRegion param0) {
        if (param0 == null) {
            return EMPTY;
        } else {
            ChunkPos var0 = param0.getCenter();
            if (!param0.isOldChunkAround(var0, HEIGHT_BLENDING_RANGE_CHUNKS)) {
                return EMPTY;
            } else {
                Long2ObjectOpenHashMap<BlendingData> var1 = new Long2ObjectOpenHashMap<>();
                Long2ObjectOpenHashMap<BlendingData> var2 = new Long2ObjectOpenHashMap<>();
                int var3 = Mth.square(HEIGHT_BLENDING_RANGE_CHUNKS + 1);

                for(int var4 = -HEIGHT_BLENDING_RANGE_CHUNKS; var4 <= HEIGHT_BLENDING_RANGE_CHUNKS; ++var4) {
                    for(int var5 = -HEIGHT_BLENDING_RANGE_CHUNKS; var5 <= HEIGHT_BLENDING_RANGE_CHUNKS; ++var5) {
                        if (var4 * var4 + var5 * var5 <= var3) {
                            int var6 = var0.x + var4;
                            int var7 = var0.z + var5;
                            BlendingData var8 = BlendingData.getOrUpdateBlendingData(param0, var6, var7);
                            if (var8 != null) {
                                var1.put(ChunkPos.asLong(var6, var7), var8);
                                if (var4 >= -DENSITY_BLENDING_RANGE_CHUNKS
                                    && var4 <= DENSITY_BLENDING_RANGE_CHUNKS
                                    && var5 >= -DENSITY_BLENDING_RANGE_CHUNKS
                                    && var5 <= DENSITY_BLENDING_RANGE_CHUNKS) {
                                    var2.put(ChunkPos.asLong(var6, var7), var8);
                                }
                            }
                        }
                    }
                }

                return var1.isEmpty() && var2.isEmpty() ? EMPTY : new Blender(var1, var2);
            }
        }
    }

    Blender(Long2ObjectOpenHashMap<BlendingData> param0, Long2ObjectOpenHashMap<BlendingData> param1) {
        this.heightAndBiomeBlendingData = param0;
        this.densityBlendingData = param1;
    }

    public Blender.BlendingOutput blendOffsetAndFactor(int param0, int param1) {
        int var0 = QuartPos.fromBlock(param0);
        int var1 = QuartPos.fromBlock(param1);
        double var2 = this.getBlendingDataValue(var0, 0, var1, BlendingData::getHeight);
        if (var2 != Double.MAX_VALUE) {
            return new Blender.BlendingOutput(0.0, heightToOffset(var2));
        } else {
            MutableDouble var3 = new MutableDouble(0.0);
            MutableDouble var4 = new MutableDouble(0.0);
            MutableDouble var5 = new MutableDouble(Double.POSITIVE_INFINITY);
            this.heightAndBiomeBlendingData
                .forEach(
                    (param5, param6) -> param6.iterateHeights(
                            QuartPos.fromSection(ChunkPos.getX(param5)), QuartPos.fromSection(ChunkPos.getZ(param5)), (param5x, param6x, param7) -> {
                                double var0x = Mth.length((double)(var0 - param5x), (double)(var1 - param6x));
                                if (!(var0x > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
                                    if (var0x < var5.doubleValue()) {
                                        var5.setValue(var0x);
                                    }
            
                                    double var1x = 1.0 / (var0x * var0x * var0x * var0x);
                                    var4.add(param7 * var1x);
                                    var3.add(var1x);
                                }
                            }
                        )
                );
            if (var5.doubleValue() == Double.POSITIVE_INFINITY) {
                return new Blender.BlendingOutput(1.0, 0.0);
            } else {
                double var6 = var4.doubleValue() / var3.doubleValue();
                double var7 = Mth.clamp(var5.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
                var7 = 3.0 * var7 * var7 - 2.0 * var7 * var7 * var7;
                return new Blender.BlendingOutput(var7, heightToOffset(var6));
            }
        }
    }

    private static double heightToOffset(double param0) {
        double var0 = 1.0;
        double var1 = param0 + 0.5;
        double var2 = Mth.positiveModulo(var1, 8.0);
        return 1.0 * (32.0 * (var1 - 128.0) - 3.0 * (var1 - 120.0) * var2 + 3.0 * var2 * var2) / (128.0 * (32.0 - 3.0 * var2));
    }

    public double blendDensity(DensityFunction.FunctionContext param0, double param1) {
        int var0 = QuartPos.fromBlock(param0.blockX());
        int var1 = param0.blockY() / 8;
        int var2 = QuartPos.fromBlock(param0.blockZ());
        double var3 = this.getBlendingDataValue(var0, var1, var2, BlendingData::getDensity);
        if (var3 != Double.MAX_VALUE) {
            return var3;
        } else {
            MutableDouble var4 = new MutableDouble(0.0);
            MutableDouble var5 = new MutableDouble(0.0);
            MutableDouble var6 = new MutableDouble(Double.POSITIVE_INFINITY);
            this.densityBlendingData
                .forEach(
                    (param6, param7) -> param7.iterateDensities(
                            QuartPos.fromSection(ChunkPos.getX(param6)),
                            QuartPos.fromSection(ChunkPos.getZ(param6)),
                            var1 - 1,
                            var1 + 1,
                            (param6x, param7x, param8, param9) -> {
                                double var0x = Mth.length((double)(var0 - param6x), (double)((var1 - param7x) * 2), (double)(var2 - param8));
                                if (!(var0x > 2.0)) {
                                    if (var0x < var6.doubleValue()) {
                                        var6.setValue(var0x);
                                    }
            
                                    double var1x = 1.0 / (var0x * var0x * var0x * var0x);
                                    var5.add(param9 * var1x);
                                    var4.add(var1x);
                                }
                            }
                        )
                );
            if (var6.doubleValue() == Double.POSITIVE_INFINITY) {
                return param1;
            } else {
                double var7 = var5.doubleValue() / var4.doubleValue();
                double var8 = Mth.clamp(var6.doubleValue() / 3.0, 0.0, 1.0);
                return Mth.lerp(var8, var7, param1);
            }
        }
    }

    private double getBlendingDataValue(int param0, int param1, int param2, Blender.CellValueGetter param3) {
        int var0 = QuartPos.toSection(param0);
        int var1 = QuartPos.toSection(param2);
        boolean var2 = (param0 & 3) == 0;
        boolean var3 = (param2 & 3) == 0;
        double var4 = this.getBlendingDataValue(param3, var0, var1, param0, param1, param2);
        if (var4 == Double.MAX_VALUE) {
            if (var2 && var3) {
                var4 = this.getBlendingDataValue(param3, var0 - 1, var1 - 1, param0, param1, param2);
            }

            if (var4 == Double.MAX_VALUE) {
                if (var2) {
                    var4 = this.getBlendingDataValue(param3, var0 - 1, var1, param0, param1, param2);
                }

                if (var4 == Double.MAX_VALUE && var3) {
                    var4 = this.getBlendingDataValue(param3, var0, var1 - 1, param0, param1, param2);
                }
            }
        }

        return var4;
    }

    private double getBlendingDataValue(Blender.CellValueGetter param0, int param1, int param2, int param3, int param4, int param5) {
        BlendingData var0 = this.heightAndBiomeBlendingData.get(ChunkPos.asLong(param1, param2));
        return var0 != null ? param0.get(var0, param3 - QuartPos.fromSection(param1), param4, param5 - QuartPos.fromSection(param2)) : Double.MAX_VALUE;
    }

    public BiomeResolver getBiomeResolver(BiomeResolver param0) {
        return (param1, param2, param3, param4) -> {
            Holder<Biome> var0 = this.blendBiome(param1, param2, param3);
            return var0 == null ? param0.getNoiseBiome(param1, param2, param3, param4) : var0;
        };
    }

    @Nullable
    private Holder<Biome> blendBiome(int param0, int param1, int param2) {
        MutableDouble var0 = new MutableDouble(Double.POSITIVE_INFINITY);
        MutableObject<Holder<Biome>> var1 = new MutableObject<>();
        this.heightAndBiomeBlendingData
            .forEach(
                (param5, param6) -> param6.iterateBiomes(
                        QuartPos.fromSection(ChunkPos.getX(param5)), param1, QuartPos.fromSection(ChunkPos.getZ(param5)), (param4x, param5x, param6x) -> {
                            double var0x = Mth.length((double)(param0 - param4x), (double)(param2 - param5x));
                            if (!(var0x > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
                                if (var0x < var0.doubleValue()) {
                                    var1.setValue(param6x);
                                    var0.setValue(var0x);
                                }
            
                            }
                        }
                    )
            );
        if (var0.doubleValue() == Double.POSITIVE_INFINITY) {
            return null;
        } else {
            double var2 = SHIFT_NOISE.getValue((double)param0, 0.0, (double)param2) * 12.0;
            double var3 = Mth.clamp((var0.doubleValue() + var2) / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
            return var3 > 0.5 ? null : var1.getValue();
        }
    }

    public static void generateBorderTicks(WorldGenRegion param0, ChunkAccess param1) {
        ChunkPos var0 = param1.getPos();
        boolean var1 = param1.isOldNoiseGeneration();
        BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();
        BlockPos var3 = new BlockPos(var0.getMinBlockX(), 0, var0.getMinBlockZ());
        BlendingData var4 = param1.getBlendingData();
        if (var4 != null) {
            int var5 = var4.getAreaWithOldGeneration().getMinBuildHeight();
            int var6 = var4.getAreaWithOldGeneration().getMaxBuildHeight() - 1;
            if (var1) {
                for(int var7 = 0; var7 < 16; ++var7) {
                    for(int var8 = 0; var8 < 16; ++var8) {
                        generateBorderTick(param1, var2.setWithOffset(var3, var7, var5 - 1, var8));
                        generateBorderTick(param1, var2.setWithOffset(var3, var7, var5, var8));
                        generateBorderTick(param1, var2.setWithOffset(var3, var7, var6, var8));
                        generateBorderTick(param1, var2.setWithOffset(var3, var7, var6 + 1, var8));
                    }
                }
            }

            for(Direction var9 : Direction.Plane.HORIZONTAL) {
                if (param0.getChunk(var0.x + var9.getStepX(), var0.z + var9.getStepZ()).isOldNoiseGeneration() != var1) {
                    int var10 = var9 == Direction.EAST ? 15 : 0;
                    int var11 = var9 == Direction.WEST ? 0 : 15;
                    int var12 = var9 == Direction.SOUTH ? 15 : 0;
                    int var13 = var9 == Direction.NORTH ? 0 : 15;

                    for(int var14 = var10; var14 <= var11; ++var14) {
                        for(int var15 = var12; var15 <= var13; ++var15) {
                            int var16 = Math.min(var6, param1.getHeight(Heightmap.Types.MOTION_BLOCKING, var14, var15)) + 1;

                            for(int var17 = var5; var17 < var16; ++var17) {
                                generateBorderTick(param1, var2.setWithOffset(var3, var14, var17, var15));
                            }
                        }
                    }
                }
            }

        }
    }

    private static void generateBorderTick(ChunkAccess param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        if (var0.is(BlockTags.LEAVES)) {
            param0.markPosForPostprocessing(param1);
        }

        FluidState var1 = param0.getFluidState(param1);
        if (!var1.isEmpty()) {
            param0.markPosForPostprocessing(param1);
        }

    }

    public static void addAroundOldChunksCarvingMaskFilter(WorldGenLevel param0, ProtoChunk param1) {
        ChunkPos var0 = param1.getPos();
        Builder<Direction8, BlendingData> var1 = ImmutableMap.builder();

        for(Direction8 var2 : Direction8.values()) {
            int var3 = var0.x + var2.getStepX();
            int var4 = var0.z + var2.getStepZ();
            BlendingData var5 = param0.getChunk(var3, var4).getBlendingData();
            if (var5 != null) {
                var1.put(var2, var5);
            }
        }

        ImmutableMap<Direction8, BlendingData> var6 = var1.build();
        if (param1.isOldNoiseGeneration() || !var6.isEmpty()) {
            Blender.DistanceGetter var7 = makeOldChunkDistanceGetter(param1.getBlendingData(), var6);
            CarvingMask.Mask var8 = (param1x, param2, param3) -> {
                double var0x = (double)param1x + 0.5 + SHIFT_NOISE.getValue((double)param1x, (double)param2, (double)param3) * 4.0;
                double var1x = (double)param2 + 0.5 + SHIFT_NOISE.getValue((double)param2, (double)param3, (double)param1x) * 4.0;
                double var2x = (double)param3 + 0.5 + SHIFT_NOISE.getValue((double)param3, (double)param1x, (double)param2) * 4.0;
                return var7.getDistance(var0x, var1x, var2x) < 4.0;
            };
            Stream.of(GenerationStep.Carving.values()).map(param1::getOrCreateCarvingMask).forEach(param1x -> param1x.setAdditionalMask(var8));
        }
    }

    public static Blender.DistanceGetter makeOldChunkDistanceGetter(@Nullable BlendingData param0, Map<Direction8, BlendingData> param1) {
        List<Blender.DistanceGetter> var0 = Lists.newArrayList();
        if (param0 != null) {
            var0.add(makeOffsetOldChunkDistanceGetter(null, param0));
        }

        param1.forEach((param1x, param2) -> var0.add(makeOffsetOldChunkDistanceGetter(param1x, param2)));
        return (param1x, param2, param3) -> {
            double var0x = Double.POSITIVE_INFINITY;

            for(Blender.DistanceGetter var1x : var0) {
                double var2x = var1x.getDistance(param1x, param2, param3);
                if (var2x < var0x) {
                    var0x = var2x;
                }
            }

            return var0x;
        };
    }

    private static Blender.DistanceGetter makeOffsetOldChunkDistanceGetter(@Nullable Direction8 param0, BlendingData param1) {
        double var0 = 0.0;
        double var1 = 0.0;
        if (param0 != null) {
            for(Direction var2 : param0.getDirections()) {
                var0 += (double)(var2.getStepX() * 16);
                var1 += (double)(var2.getStepZ() * 16);
            }
        }

        double var3 = var0;
        double var4 = var1;
        double var5 = (double)param1.getAreaWithOldGeneration().getHeight() / 2.0;
        double var6 = (double)param1.getAreaWithOldGeneration().getMinBuildHeight() + var5;
        return (param4, param5, param6) -> distanceToCube(param4 - 8.0 - var3, param5 - var6, param6 - 8.0 - var4, 8.0, var5, 8.0);
    }

    private static double distanceToCube(double param0, double param1, double param2, double param3, double param4, double param5) {
        double var0 = Math.abs(param0) - param3;
        double var1 = Math.abs(param1) - param4;
        double var2 = Math.abs(param2) - param5;
        return Mth.length(Math.max(0.0, var0), Math.max(0.0, var1), Math.max(0.0, var2));
    }

    public static record BlendingOutput(double alpha, double blendingOffset) {
    }

    interface CellValueGetter {
        double get(BlendingData var1, int var2, int var3, int var4);
    }

    public interface DistanceGetter {
        double getDistance(double var1, double var3, double var5);
    }
}
