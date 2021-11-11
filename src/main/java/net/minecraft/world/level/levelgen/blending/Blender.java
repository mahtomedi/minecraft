package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.TerrainInfo;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

public class Blender {
    private static final Blender EMPTY = new Blender(null, List.of(), List.of()) {
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
    private final WorldGenRegion region;
    private final List<Blender.PositionedBlendingData> heightData;
    private final List<Blender.PositionedBlendingData> densityData;

    public static Blender empty() {
        return EMPTY;
    }

    public static Blender of(@Nullable WorldGenRegion param0) {
        if (param0 == null) {
            return EMPTY;
        } else {
            List<Blender.PositionedBlendingData> var0 = Lists.newArrayList();
            List<Blender.PositionedBlendingData> var1 = Lists.newArrayList();
            ChunkPos var2 = param0.getCenter();

            for(int var3 = -HEIGHT_BLENDING_RANGE_CHUNKS; var3 <= HEIGHT_BLENDING_RANGE_CHUNKS; ++var3) {
                for(int var4 = -HEIGHT_BLENDING_RANGE_CHUNKS; var4 <= HEIGHT_BLENDING_RANGE_CHUNKS; ++var4) {
                    int var5 = var2.x + var3;
                    int var6 = var2.z + var4;
                    BlendingData var7 = BlendingData.getOrUpdateBlendingData(param0, var5, var6);
                    if (var7 != null) {
                        Blender.PositionedBlendingData var8 = new Blender.PositionedBlendingData(var5, var6, var7);
                        var0.add(var8);
                        if (var3 >= -DENSITY_BLENDING_RANGE_CHUNKS
                            && var3 <= DENSITY_BLENDING_RANGE_CHUNKS
                            && var4 >= -DENSITY_BLENDING_RANGE_CHUNKS
                            && var4 <= DENSITY_BLENDING_RANGE_CHUNKS) {
                            var1.add(var8);
                        }
                    }
                }
            }

            return var0.isEmpty() && var1.isEmpty() ? EMPTY : new Blender(param0, var0, var1);
        }
    }

    Blender(WorldGenRegion param0, List<Blender.PositionedBlendingData> param1, List<Blender.PositionedBlendingData> param2) {
        this.region = param0;
        this.heightData = param1;
        this.densityData = param2;
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

            for(Blender.PositionedBlendingData var6 : this.heightData) {
                var6.blendingData.iterateHeights(QuartPos.fromSection(var6.chunkX), QuartPos.fromSection(var6.chunkZ), (param5, param6, param7) -> {
                    double var0x = Mth.length((double)(var0 - param5), (double)(var1 - param6));
                    if (!(var0x > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
                        if (var0x < var5.doubleValue()) {
                            var5.setValue(var0x);
                        }

                        double var1x = 1.0 / (var0x * var0x * var0x * var0x);
                        var4.add(param7 * var1x);
                        var3.add(var1x);
                    }
                });
            }

            if (var5.doubleValue() == Double.POSITIVE_INFINITY) {
                return param2;
            } else {
                double var7 = var4.doubleValue() / var3.doubleValue();
                double var8 = Mth.clamp(var5.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
                var8 = 3.0 * var8 * var8 - 2.0 * var8 * var8 * var8;
                double var9 = Mth.lerp(var8, heightToOffset(var7), param2.offset());
                double var10 = Mth.lerp(var8, 10.0, param2.factor());
                double var11 = Mth.lerp(var8, 0.0, param2.jaggedness());
                return new TerrainInfo(var9, var10, var11);
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

            for(Blender.PositionedBlendingData var7 : this.densityData) {
                var7.blendingData
                    .iterateDensities(
                        QuartPos.fromSection(var7.chunkX), QuartPos.fromSection(var7.chunkZ), var1 - 2, var1 + 2, (param6, param7, param8, param9) -> {
                            double var0x = Mth.length((double)(var0 - param6), (double)(var1 - param7), (double)(var2 - param8));
                            if (!(var0x > 2.0)) {
                                if (var0x < var6.doubleValue()) {
                                    var6.setValue(var0x);
                                }
        
                                double var1x = 1.0 / (var0x * var0x * var0x * var0x);
                                var5.add(param9 * var1x);
                                var4.add(var1x);
                            }
                        }
                    );
            }

            if (var6.doubleValue() == Double.POSITIVE_INFINITY) {
                return param3;
            } else {
                double var8 = var5.doubleValue() / var4.doubleValue();
                double var9 = Mth.clamp(var6.doubleValue() / 3.0, 0.0, 1.0);
                return Mth.lerp(var9, var8, param3);
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
        BlendingData var0 = BlendingData.getOrUpdateBlendingData(this.region, param1, param2);
        return var0 != null ? param0.get(var0, param3 - QuartPos.fromSection(param1), param4, param5 - QuartPos.fromSection(param2)) : Double.MAX_VALUE;
    }

    public BiomeResolver getBiomeResolver(BiomeResolver param0) {
        return (param1, param2, param3, param4) -> {
            Biome var0 = this.blendBiome(param1, param2, param3);
            return var0 == null ? param0.getNoiseBiome(param1, param2, param3, param4) : var0;
        };
    }

    @Nullable
    private Biome blendBiome(int param0, int param1, int param2) {
        double var0 = (double)param0 + SHIFT_NOISE.getValue((double)param0, 0.0, (double)param2) * 12.0;
        double var1 = (double)param2 + SHIFT_NOISE.getValue((double)param2, (double)param0, 0.0) * 12.0;
        MutableDouble var2 = new MutableDouble(Double.POSITIVE_INFINITY);
        BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();
        MutableObject<ChunkPos> var4 = new MutableObject<>();

        for(Blender.PositionedBlendingData var5 : this.heightData) {
            var5.blendingData.iterateHeights(QuartPos.fromSection(var5.chunkX), QuartPos.fromSection(var5.chunkZ), (param6, param7, param8) -> {
                double var0x = Mth.length(var0 - (double)param6, var1 - (double)param7);
                if (!(var0x > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
                    if (var0x < var2.doubleValue()) {
                        var4.setValue(new ChunkPos(var5.chunkX, var5.chunkZ));
                        var3.set(param6, QuartPos.fromBlock(Mth.floor(param8)), param7);
                        var2.setValue(var0x);
                    }

                }
            });
        }

        if (var2.doubleValue() == Double.POSITIVE_INFINITY) {
            return null;
        } else {
            double var6 = Mth.clamp(var2.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
            if (var6 > 0.5) {
                return null;
            } else {
                ChunkAccess var7 = this.region.getChunk(var4.getValue().x, var4.getValue().z);
                return var7.getNoiseBiome(Math.min(var3.getX() & 3, 3), var3.getY(), Math.min(var3.getZ() & 3, 3));
            }
        }
    }

    interface CellValueGetter {
        double get(BlendingData var1, int var2, int var3, int var4);
    }

    static record PositionedBlendingData(int chunkX, int chunkZ, BlendingData blendingData) {
    }
}
