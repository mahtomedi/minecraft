package net.minecraft.world.level.levelgen.blending;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.TerrainInfo;
import org.apache.commons.lang3.mutable.MutableDouble;

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
    };
    private static final int HEIGHT_BLENDING_RANGE_CELLS = QuartPos.fromSection(7) - 1;
    private static final int HEIGHT_BLENDING_RANGE_CHUNKS = QuartPos.toSection(HEIGHT_BLENDING_RANGE_CELLS + 3);
    private static final int DENSITY_BLENDING_RANGE_CELLS = 2;
    private static final int DENSITY_BLENDING_RANGE_CHUNKS = QuartPos.toSection(5);
    private static final double BLENDING_FACTOR = 10.0;
    private static final double BLENDING_JAGGEDNESS = 0.0;
    private final WorldGenRegion region;
    private final List<BlendingData> heightData;
    private final List<BlendingData> densityData;

    public static Blender empty() {
        return EMPTY;
    }

    public static Blender of(@Nullable WorldGenRegion param0) {
        if (param0 == null) {
            return EMPTY;
        } else {
            List<BlendingData> var0 = Lists.newArrayList();
            List<BlendingData> var1 = Lists.newArrayList();
            ChunkPos var2 = param0.getCenter();

            for(int var3 = -HEIGHT_BLENDING_RANGE_CHUNKS; var3 <= HEIGHT_BLENDING_RANGE_CHUNKS; ++var3) {
                for(int var4 = -HEIGHT_BLENDING_RANGE_CHUNKS; var4 <= HEIGHT_BLENDING_RANGE_CHUNKS; ++var4) {
                    BlendingData var5 = BlendingData.getOrCreateAndStoreToChunk(param0, var2.x + var3, var2.z + var4);
                    if (var5 != BlendingData.EMPTY) {
                        var0.add(var5);
                        if (var3 >= -DENSITY_BLENDING_RANGE_CHUNKS
                            && var3 <= DENSITY_BLENDING_RANGE_CHUNKS
                            && var4 >= -DENSITY_BLENDING_RANGE_CHUNKS
                            && var4 <= DENSITY_BLENDING_RANGE_CHUNKS) {
                            var1.add(var5);
                        }
                    }
                }
            }

            return var0.isEmpty() && var1.isEmpty() ? EMPTY : new Blender(param0, var0, var1);
        }
    }

    Blender(WorldGenRegion param0, List<BlendingData> param1, List<BlendingData> param2) {
        this.region = param0;
        this.heightData = param1;
        this.densityData = param2;
    }

    public TerrainInfo blendOffsetAndFactor(int param0, int param1, TerrainInfo param2) {
        int var0 = SectionPos.blockToSectionCoord(param0);
        int var1 = SectionPos.blockToSectionCoord(param1);
        int var2 = QuartPos.fromBlock(param0);
        int var3 = QuartPos.fromBlock(param1);
        BlendingData var4 = BlendingData.getOrCreateAndStoreToChunk(this.region, var0, var1);
        if (var4 != BlendingData.EMPTY) {
            double var5 = var4.getHeight(var2, var3);
            if (var5 != Double.POSITIVE_INFINITY) {
                return new TerrainInfo(heightToOffset(var5), 10.0, 0.0);
            }
        }

        MutableDouble var6 = new MutableDouble(0.0);
        MutableDouble var7 = new MutableDouble(0.0);
        MutableDouble var8 = new MutableDouble(Double.POSITIVE_INFINITY);

        for(BlendingData var9 : this.heightData) {
            var9.iterateHeights((param5, param6, param7) -> {
                double var0x = Mth.length(var2 - param5, (double)(var3 - param6));
                if (!(var0x > (double)HEIGHT_BLENDING_RANGE_CELLS)) {
                    if (var0x < var8.doubleValue()) {
                        var8.setValue(var0x);
                    }

                    double var1x = 1.0 / (var0x * var0x * var0x * var0x);
                    var7.add(param7 * var1x);
                    var6.add(var1x);
                }
            });
        }

        if (var8.doubleValue() == Double.POSITIVE_INFINITY) {
            return param2;
        } else {
            double var10 = var7.doubleValue() / var6.doubleValue();
            double var11 = Mth.clamp(var8.doubleValue() / (double)(HEIGHT_BLENDING_RANGE_CELLS + 1), 0.0, 1.0);
            var11 = 3.0 * var11 * var11 - 2.0 * var11 * var11 * var11;
            double var12 = Mth.lerp(var11, heightToOffset(var10), param2.offset());
            double var13 = Mth.lerp(var11, 10.0, param2.factor());
            double var14 = Mth.lerp(var11, 0.0, param2.jaggedness());
            return new TerrainInfo(var12, var13, var14);
        }
    }

    private static double heightToOffset(double param0) {
        double var0 = 1.0;
        double var1 = param0 + 0.5;
        double var2 = Mth.positiveModulo(var1, 8.0);
        return 1.0 * (32.0 * (var1 - 128.0) - 3.0 * (var1 - 120.0) * var2 + 3.0 * var2 * var2) / (128.0 * (32.0 - 3.0 * var2));
    }

    public double blendDensity(int param0, int param1, int param2, double param3) {
        int var0 = SectionPos.blockToSectionCoord(param0);
        int var1 = SectionPos.blockToSectionCoord(param2);
        int var2 = QuartPos.fromBlock(param0);
        int var3 = param1 / 8;
        int var4 = QuartPos.fromBlock(param2);
        BlendingData var5 = BlendingData.getOrCreateAndStoreToChunk(this.region, var0, var1);
        if (var5 != BlendingData.EMPTY) {
            double var6 = var5.getDensity(var2, var3, var4);
            if (var6 != Double.POSITIVE_INFINITY) {
                return var6;
            }
        }

        MutableDouble var7 = new MutableDouble(0.0);
        MutableDouble var8 = new MutableDouble(0.0);
        MutableDouble var9 = new MutableDouble(Double.POSITIVE_INFINITY);

        for(BlendingData var10 : this.densityData) {
            var10.iterateDensities(var3 - 2, var3 + 2, (param6, param7, param8, param9) -> {
                double var0x = Mth.length(var2 - param6, (double)(var3 - param7), var4 - param8);
                if (!(var0x > 2.0)) {
                    if (var0x < var9.doubleValue()) {
                        var9.setValue(var0x);
                    }

                    double var1x = 1.0 / (var0x * var0x * var0x * var0x);
                    var8.add(param9 * var1x);
                    var7.add(var1x);
                }
            });
        }

        if (var9.doubleValue() == Double.POSITIVE_INFINITY) {
            return param3;
        } else {
            double var11 = var8.doubleValue() / var7.doubleValue();
            double var12 = Mth.clamp(var9.doubleValue() / 3.0, 0.0, 1.0);
            return Mth.lerp(var12, var11, param3);
        }
    }
}
