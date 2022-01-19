package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
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
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.TerrainInfo;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.FluidState;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

public class Blender {
    private static final Blender EMPTY = new Blender(new Long2ObjectOpenHashMap(), new Long2ObjectOpenHashMap()) {
        @Override
        public TerrainInfo blendOffsetAndFactor(int param0, int param1, TerrainInfo param2) {
            return param2;
        }

        @Override
        public double blendDensity(int param0, int param1, int param2, double param3) {
            return param3;
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
    private static final double BLENDING_FACTOR = 10.0;
    private static final double BLENDING_JAGGEDNESS = 0.0;
    private static final double OLD_CHUNK_Y_RADIUS = (double)BlendingData.AREA_WITH_OLD_GENERATION.getHeight() / 2.0;
    private static final double OLD_CHUNK_CENTER_Y = (double)BlendingData.AREA_WITH_OLD_GENERATION.getMinBuildHeight() + OLD_CHUNK_Y_RADIUS;
    private static final double OLD_CHUNK_XZ_RADIUS = 8.0;
    private final Long2ObjectOpenHashMap<BlendingData> blendingData;
    private final Long2ObjectOpenHashMap<BlendingData> blendingDataForDensityBlending;

    public static Blender empty() {
        return EMPTY;
    }

    public static Blender of(@Nullable WorldGenRegion param0) {
        if (param0 == null) {
            return EMPTY;
        } else {
            Long2ObjectOpenHashMap<BlendingData> var0 = new Long2ObjectOpenHashMap<>();
            Long2ObjectOpenHashMap<BlendingData> var1 = new Long2ObjectOpenHashMap<>();
            ChunkPos var2 = param0.getCenter();

            for(int var3 = -HEIGHT_BLENDING_RANGE_CHUNKS; var3 <= HEIGHT_BLENDING_RANGE_CHUNKS; ++var3) {
                for(int var4 = -HEIGHT_BLENDING_RANGE_CHUNKS; var4 <= HEIGHT_BLENDING_RANGE_CHUNKS; ++var4) {
                    int var5 = var2.x + var3;
                    int var6 = var2.z + var4;
                    BlendingData var7 = BlendingData.getOrUpdateBlendingData(param0, var5, var6);
                    if (var7 != null) {
                        var0.put(ChunkPos.asLong(var5, var6), var7);
                        if (var3 >= -DENSITY_BLENDING_RANGE_CHUNKS
                            && var3 <= DENSITY_BLENDING_RANGE_CHUNKS
                            && var4 >= -DENSITY_BLENDING_RANGE_CHUNKS
                            && var4 <= DENSITY_BLENDING_RANGE_CHUNKS) {
                            var1.put(ChunkPos.asLong(var5, var6), var7);
                        }
                    }
                }
            }

            return var0.isEmpty() && var1.isEmpty() ? EMPTY : new Blender(var0, var1);
        }
    }

    Blender(Long2ObjectOpenHashMap<BlendingData> param0, Long2ObjectOpenHashMap<BlendingData> param1) {
        this.blendingData = param0;
        this.blendingDataForDensityBlending = param1;
    }

    public TerrainInfo blendOffsetAndFactor(int param0, int param1, TerrainInfo param2) {
        int var0 = QuartPos.fromBlock(param0);
        int var1 = QuartPos.fromBlock(param1);
        double var2 = this.getBlendingDataValue(var0, 0, var1, BlendingData::getHeight);
        if (var2 != Double.MAX_VALUE) {
            return new TerrainInfo(heightToOffset(var2), 10.0, 0.0);
        } else {
            MutableDouble var3 = new MutableDouble(0.0);
            MutableDouble var4 = new MutableDouble(0.0);
            MutableDouble var5 = new MutableDouble(Double.POSITIVE_INFINITY);
            this.blendingData
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
                return param2;
            } else {
                double var6 = var4.doubleValue() / var3.doubleValue();
                double var7 = Mth.clamp(var5.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
                var7 = 3.0 * var7 * var7 - 2.0 * var7 * var7 * var7;
                double var8 = Mth.lerp(var7, heightToOffset(var6), param2.offset());
                double var9 = Mth.lerp(var7, 10.0, param2.factor());
                double var10 = Mth.lerp(var7, 0.0, param2.jaggedness());
                return new TerrainInfo(var8, var9, var10);
            }
        }
    }

    private static double heightToOffset(double param0) {
        double var0 = 1.0;
        double var1 = param0 + 0.5;
        double var2 = Mth.positiveModulo(var1, 8.0);
        return 1.0 * (32.0 * (var1 - 128.0) - 3.0 * (var1 - 120.0) * var2 + 3.0 * var2 * var2) / (128.0 * (32.0 - 3.0 * var2));
    }

    public double blendDensity(int param0, int param1, int param2, double param3) {
        int var0 = QuartPos.fromBlock(param0);
        int var1 = param1 / 8;
        int var2 = QuartPos.fromBlock(param2);
        double var3 = this.getBlendingDataValue(var0, var1, var2, BlendingData::getDensity);
        if (var3 != Double.MAX_VALUE) {
            return var3;
        } else {
            MutableDouble var4 = new MutableDouble(0.0);
            MutableDouble var5 = new MutableDouble(0.0);
            MutableDouble var6 = new MutableDouble(Double.POSITIVE_INFINITY);
            this.blendingDataForDensityBlending
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
                return param3;
            } else {
                double var7 = var5.doubleValue() / var4.doubleValue();
                double var8 = Mth.clamp(var6.doubleValue() / 3.0, 0.0, 1.0);
                return Mth.lerp(var8, var7, param3);
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
        BlendingData var0 = this.blendingData.get(ChunkPos.asLong(param1, param2));
        return var0 != null ? param0.get(var0, param3 - QuartPos.fromSection(param1), param4, param5 - QuartPos.fromSection(param2)) : Double.MAX_VALUE;
    }

    public BiomeResolver getBiomeResolver(BiomeResolver param0) {
        return (param1, param2, param3, param4) -> {
            Biome var0 = this.blendBiome(param1, param3);
            return var0 == null ? param0.getNoiseBiome(param1, param2, param3, param4) : var0;
        };
    }

    @Nullable
    private Biome blendBiome(int param0, int param1) {
        double var0 = (double)param0 + SHIFT_NOISE.getValue((double)param0, 0.0, (double)param1) * 12.0;
        double var1 = (double)param1 + SHIFT_NOISE.getValue((double)param1, (double)param0, 0.0) * 12.0;
        MutableDouble var2 = new MutableDouble(Double.POSITIVE_INFINITY);
        MutableObject<Biome> var3 = new MutableObject<>();
        this.blendingData
            .forEach(
                (param4, param5) -> param5.iterateBiomes(
                        QuartPos.fromSection(ChunkPos.getX(param4)), QuartPos.fromSection(ChunkPos.getZ(param4)), (param4x, param5x, param6) -> {
                            double var0x = Mth.length(var0 - (double)param4x, var1 - (double)param5x);
                            if (!(var0x > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
                                if (var0x < var2.doubleValue()) {
                                    var3.setValue(param6);
                                    var2.setValue(var0x);
                                }
            
                            }
                        }
                    )
            );
        if (var2.doubleValue() == Double.POSITIVE_INFINITY) {
            return null;
        } else {
            double var4 = Mth.clamp(var2.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
            return var4 > 0.5 ? null : var3.getValue();
        }
    }

    public static void generateBorderTicks(WorldGenRegion param0, ChunkAccess param1) {
        ChunkPos var0 = param1.getPos();
        boolean var1 = param1.isOldNoiseGeneration();
        BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();
        BlockPos var3 = new BlockPos(var0.getMinBlockX(), 0, var0.getMinBlockZ());
        int var4 = BlendingData.AREA_WITH_OLD_GENERATION.getMinBuildHeight();
        int var5 = BlendingData.AREA_WITH_OLD_GENERATION.getMaxBuildHeight() - 1;
        if (var1) {
            for(int var6 = 0; var6 < 16; ++var6) {
                for(int var7 = 0; var7 < 16; ++var7) {
                    generateBorderTick(param1, var2.setWithOffset(var3, var6, var4 - 1, var7));
                    generateBorderTick(param1, var2.setWithOffset(var3, var6, var4, var7));
                    generateBorderTick(param1, var2.setWithOffset(var3, var6, var5, var7));
                    generateBorderTick(param1, var2.setWithOffset(var3, var6, var5 + 1, var7));
                }
            }
        }

        for(Direction var8 : Direction.Plane.HORIZONTAL) {
            if (param0.getChunk(var0.x + var8.getStepX(), var0.z + var8.getStepZ()).isOldNoiseGeneration() != var1) {
                int var9 = var8 == Direction.EAST ? 15 : 0;
                int var10 = var8 == Direction.WEST ? 0 : 15;
                int var11 = var8 == Direction.SOUTH ? 15 : 0;
                int var12 = var8 == Direction.NORTH ? 0 : 15;

                for(int var13 = var9; var13 <= var10; ++var13) {
                    for(int var14 = var11; var14 <= var12; ++var14) {
                        int var15 = Math.min(var5, param1.getHeight(Heightmap.Types.MOTION_BLOCKING, var13, var14)) + 1;

                        for(int var16 = var4; var16 < var15; ++var16) {
                            generateBorderTick(param1, var2.setWithOffset(var3, var13, var16, var14));
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
        Blender.DistanceGetter var1 = makeOldChunkDistanceGetter(param1.isOldNoiseGeneration(), BlendingData.sideByGenerationAge(param0, var0.x, var0.z, true));
        if (var1 != null) {
            CarvingMask.Mask var2 = (param1x, param2, param3) -> {
                double var0x = (double)param1x + 0.5 + SHIFT_NOISE.getValue((double)param1x, (double)param2, (double)param3) * 4.0;
                double var1x = (double)param2 + 0.5 + SHIFT_NOISE.getValue((double)param2, (double)param3, (double)param1x) * 4.0;
                double var2x = (double)param3 + 0.5 + SHIFT_NOISE.getValue((double)param3, (double)param1x, (double)param2) * 4.0;
                return var1.getDistance(var0x, var1x, var2x) < 4.0;
            };
            Stream.of(GenerationStep.Carving.values()).map(param1::getOrCreateCarvingMask).forEach(param1x -> param1x.setAdditionalMask(var2));
        }
    }

    @Nullable
    public static Blender.DistanceGetter makeOldChunkDistanceGetter(boolean param0, Set<Direction8> param1) {
        if (!param0 && param1.isEmpty()) {
            return null;
        } else {
            List<Blender.DistanceGetter> var0 = Lists.newArrayList();
            if (param0) {
                var0.add(makeOffsetOldChunkDistanceGetter(null));
            }

            param1.forEach(param1x -> var0.add(makeOffsetOldChunkDistanceGetter(param1x)));
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
    }

    private static Blender.DistanceGetter makeOffsetOldChunkDistanceGetter(@Nullable Direction8 param0) {
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
        return (param2, param3, param4) -> distanceToCube(param2 - 8.0 - var3, param3 - OLD_CHUNK_CENTER_Y, param4 - 8.0 - var4, 8.0, OLD_CHUNK_Y_RADIUS, 8.0);
    }

    private static double distanceToCube(double param0, double param1, double param2, double param3, double param4, double param5) {
        double var0 = Math.abs(param0) - param3;
        double var1 = Math.abs(param1) - param4;
        double var2 = Math.abs(param2) - param5;
        return Mth.length(Math.max(0.0, var0), Math.max(0.0, var1), Math.max(0.0, var2));
    }

    interface CellValueGetter {
        double get(BlendingData var1, int var2, int var3, int var4);
    }

    public interface DistanceGetter {
        double getDistance(double var1, double var3, double var5);
    }
}
