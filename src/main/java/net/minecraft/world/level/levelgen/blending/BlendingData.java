package net.minecraft.world.level.levelgen.blending;

import com.google.common.primitives.Doubles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public class BlendingData {
    private static final double BLENDING_DENSITY_FACTOR = 0.1;
    protected static final int CELL_WIDTH = 4;
    protected static final int CELL_HEIGHT = 8;
    protected static final int CELL_RATIO = 2;
    private static final double SOLID_DENSITY = 1.0;
    private static final double AIR_DENSITY = -1.0;
    private static final int CELLS_PER_SECTION_Y = 2;
    private static final int QUARTS_PER_SECTION = QuartPos.fromBlock(16);
    private static final int CELL_HORIZONTAL_MAX_INDEX_INSIDE = QUARTS_PER_SECTION - 1;
    private static final int CELL_HORIZONTAL_MAX_INDEX_OUTSIDE = QUARTS_PER_SECTION;
    private static final int CELL_COLUMN_INSIDE_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX_INSIDE + 1;
    private static final int CELL_COLUMN_OUTSIDE_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX_OUTSIDE + 1;
    private static final int CELL_COLUMN_COUNT = CELL_COLUMN_INSIDE_COUNT + CELL_COLUMN_OUTSIDE_COUNT;
    private final LevelHeightAccessor areaWithOldGeneration;
    private static final List<Block> SURFACE_BLOCKS = List.of(
        new Block[]{
            Blocks.PODZOL,
            Blocks.GRAVEL,
            Blocks.GRASS_BLOCK,
            Blocks.STONE,
            Blocks.COARSE_DIRT,
            Blocks.SAND,
            Blocks.RED_SAND,
            Blocks.MYCELIUM,
            Blocks.SNOW_BLOCK,
            Blocks.TERRACOTTA,
            Blocks.DIRT
        }
    );
    protected static final double NO_VALUE = Double.MAX_VALUE;
    private boolean hasCalculatedData;
    private final double[] heights;
    private final List<List<Holder<Biome>>> biomes;
    private final transient double[][] densities;
    private static final Codec<double[]> DOUBLE_ARRAY_CODEC = Codec.DOUBLE.listOf().xmap(Doubles::toArray, Doubles::asList);
    public static final Codec<BlendingData> CODEC = RecordCodecBuilder.<BlendingData>create(
            param0 -> param0.group(
                        Codec.INT.fieldOf("min_section").forGetter(param0x -> param0x.areaWithOldGeneration.getMinSection()),
                        Codec.INT.fieldOf("max_section").forGetter(param0x -> param0x.areaWithOldGeneration.getMaxSection()),
                        DOUBLE_ARRAY_CODEC.optionalFieldOf("heights")
                            .forGetter(
                                param0x -> DoubleStream.of(param0x.heights).anyMatch(param0xx -> param0xx != Double.MAX_VALUE)
                                        ? Optional.of(param0x.heights)
                                        : Optional.empty()
                            )
                    )
                    .apply(param0, BlendingData::new)
        )
        .comapFlatMap(BlendingData::validateArraySize, Function.identity());

    private static DataResult<BlendingData> validateArraySize(BlendingData param0) {
        return param0.heights.length != CELL_COLUMN_COUNT ? DataResult.error("heights has to be of length " + CELL_COLUMN_COUNT) : DataResult.success(param0);
    }

    private BlendingData(int param0, int param1, Optional<double[]> param2) {
        this.heights = param2.orElse(Util.make(new double[CELL_COLUMN_COUNT], param0x -> Arrays.fill(param0x, Double.MAX_VALUE)));
        this.densities = new double[CELL_COLUMN_COUNT][];
        ObjectArrayList<List<Holder<Biome>>> var0 = new ObjectArrayList<>(CELL_COLUMN_COUNT);
        var0.size(CELL_COLUMN_COUNT);
        this.biomes = var0;
        int var1 = SectionPos.sectionToBlockCoord(param0);
        int var2 = SectionPos.sectionToBlockCoord(param1) - var1;
        this.areaWithOldGeneration = LevelHeightAccessor.create(var1, var2);
    }

    @Nullable
    public static BlendingData getOrUpdateBlendingData(WorldGenRegion param0, int param1, int param2) {
        ChunkAccess var0 = param0.getChunk(param1, param2);
        BlendingData var1 = var0.getBlendingData();
        if (var1 == null) {
            return null;
        } else {
            var1.calculateData(var0, sideByGenerationAge(param0, param1, param2, false));
            return var1;
        }
    }

    public static Set<Direction8> sideByGenerationAge(WorldGenLevel param0, int param1, int param2, boolean param3) {
        Set<Direction8> var0 = EnumSet.noneOf(Direction8.class);

        for(Direction8 var1 : Direction8.values()) {
            int var2 = param1 + var1.getStepX();
            int var3 = param2 + var1.getStepZ();
            if (param0.getChunk(var2, var3).isOldNoiseGeneration() == param3) {
                var0.add(var1);
            }
        }

        return var0;
    }

    private void calculateData(ChunkAccess param0, Set<Direction8> param1) {
        if (!this.hasCalculatedData) {
            if (param1.contains(Direction8.NORTH) || param1.contains(Direction8.WEST) || param1.contains(Direction8.NORTH_WEST)) {
                this.addValuesForColumn(getInsideIndex(0, 0), param0, 0, 0);
            }

            if (param1.contains(Direction8.NORTH)) {
                for(int var0 = 1; var0 < QUARTS_PER_SECTION; ++var0) {
                    this.addValuesForColumn(getInsideIndex(var0, 0), param0, 4 * var0, 0);
                }
            }

            if (param1.contains(Direction8.WEST)) {
                for(int var1 = 1; var1 < QUARTS_PER_SECTION; ++var1) {
                    this.addValuesForColumn(getInsideIndex(0, var1), param0, 0, 4 * var1);
                }
            }

            if (param1.contains(Direction8.EAST)) {
                for(int var2 = 1; var2 < QUARTS_PER_SECTION; ++var2) {
                    this.addValuesForColumn(getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, var2), param0, 15, 4 * var2);
                }
            }

            if (param1.contains(Direction8.SOUTH)) {
                for(int var3 = 0; var3 < QUARTS_PER_SECTION; ++var3) {
                    this.addValuesForColumn(getOutsideIndex(var3, CELL_HORIZONTAL_MAX_INDEX_OUTSIDE), param0, 4 * var3, 15);
                }
            }

            if (param1.contains(Direction8.EAST) && param1.contains(Direction8.NORTH_EAST)) {
                this.addValuesForColumn(getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, 0), param0, 15, 0);
            }

            if (param1.contains(Direction8.EAST) && param1.contains(Direction8.SOUTH) && param1.contains(Direction8.SOUTH_EAST)) {
                this.addValuesForColumn(getOutsideIndex(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE, CELL_HORIZONTAL_MAX_INDEX_OUTSIDE), param0, 15, 15);
            }

            this.hasCalculatedData = true;
        }
    }

    private void addValuesForColumn(int param0, ChunkAccess param1, int param2, int param3) {
        if (this.heights[param0] == Double.MAX_VALUE) {
            this.heights[param0] = (double)this.getHeightAtXZ(param1, param2, param3);
        }

        this.densities[param0] = this.getDensityColumn(param1, param2, param3, Mth.floor(this.heights[param0]));
        this.biomes.set(param0, this.getBiomeColumn(param1, param2, param3));
    }

    private int getHeightAtXZ(ChunkAccess param0, int param1, int param2) {
        int var0;
        if (param0.hasPrimedHeightmap(Heightmap.Types.WORLD_SURFACE_WG)) {
            var0 = Math.min(param0.getHeight(Heightmap.Types.WORLD_SURFACE_WG, param1, param2) + 1, this.areaWithOldGeneration.getMaxBuildHeight());
        } else {
            var0 = this.areaWithOldGeneration.getMaxBuildHeight();
        }

        int var2 = this.areaWithOldGeneration.getMinBuildHeight();
        BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos(param1, var0, param2);

        while(var3.getY() > var2) {
            var3.move(Direction.DOWN);
            if (SURFACE_BLOCKS.contains(param0.getBlockState(var3).getBlock())) {
                return var3.getY();
            }
        }

        return var2;
    }

    private static double read1(ChunkAccess param0, BlockPos.MutableBlockPos param1) {
        return isGround(param0, param1.move(Direction.DOWN)) ? 1.0 : -1.0;
    }

    private static double read7(ChunkAccess param0, BlockPos.MutableBlockPos param1) {
        double var0 = 0.0;

        for(int var1 = 0; var1 < 7; ++var1) {
            var0 += read1(param0, param1);
        }

        return var0;
    }

    private double[] getDensityColumn(ChunkAccess param0, int param1, int param2, int param3) {
        double[] var0 = new double[this.cellCountPerColumn()];
        Arrays.fill(var0, -1.0);
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos(param1, this.areaWithOldGeneration.getMaxBuildHeight(), param2);
        double var2 = read7(param0, var1);

        for(int var3 = var0.length - 2; var3 >= 0; --var3) {
            double var4 = read1(param0, var1);
            double var5 = read7(param0, var1);
            var0[var3] = (var2 + var4 + var5) / 15.0;
            var2 = var5;
        }

        int var6 = this.getCellYIndex(Mth.floorDiv(param3, 8));
        if (var6 >= 0 && var6 < var0.length - 1) {
            double var7 = ((double)param3 + 0.5) % 8.0 / 8.0;
            double var8 = (1.0 - var7) / var7;
            double var9 = Math.max(var8, 1.0) * 0.25;
            var0[var6 + 1] = -var8 / var9;
            var0[var6] = 1.0 / var9;
        }

        return var0;
    }

    private List<Holder<Biome>> getBiomeColumn(ChunkAccess param0, int param1, int param2) {
        ObjectArrayList<Holder<Biome>> var0 = new ObjectArrayList<>(this.quartCountPerColumn());
        var0.size(this.quartCountPerColumn());

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            int var2 = var1 + QuartPos.fromBlock(this.areaWithOldGeneration.getMinBuildHeight());
            var0.set(var1, param0.getNoiseBiome(QuartPos.fromBlock(param1), var2, QuartPos.fromBlock(param2)));
        }

        return var0;
    }

    private static boolean isGround(ChunkAccess param0, BlockPos param1) {
        BlockState var0 = param0.getBlockState(param1);
        if (var0.isAir()) {
            return false;
        } else if (var0.is(BlockTags.LEAVES)) {
            return false;
        } else if (var0.is(BlockTags.LOGS)) {
            return false;
        } else if (var0.is(Blocks.BROWN_MUSHROOM_BLOCK) || var0.is(Blocks.RED_MUSHROOM_BLOCK)) {
            return false;
        } else {
            return !var0.getCollisionShape(param0, param1).isEmpty();
        }
    }

    protected double getHeight(int param0, int param1, int param2) {
        if (param0 == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE || param2 == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE) {
            return this.heights[getOutsideIndex(param0, param2)];
        } else {
            return param0 != 0 && param2 != 0 ? Double.MAX_VALUE : this.heights[getInsideIndex(param0, param2)];
        }
    }

    private double getDensity(@Nullable double[] param0, int param1) {
        if (param0 == null) {
            return Double.MAX_VALUE;
        } else {
            int var0 = this.getCellYIndex(param1);
            return var0 >= 0 && var0 < param0.length ? param0[var0] * 0.1 : Double.MAX_VALUE;
        }
    }

    protected double getDensity(int param0, int param1, int param2) {
        if (param1 == this.getMinY()) {
            return 0.1;
        } else if (param0 == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE || param2 == CELL_HORIZONTAL_MAX_INDEX_OUTSIDE) {
            return this.getDensity(this.densities[getOutsideIndex(param0, param2)], param1);
        } else {
            return param0 != 0 && param2 != 0 ? Double.MAX_VALUE : this.getDensity(this.densities[getInsideIndex(param0, param2)], param1);
        }
    }

    protected void iterateBiomes(int param0, int param1, int param2, BlendingData.BiomeConsumer param3) {
        if (param1 >= QuartPos.fromBlock(this.areaWithOldGeneration.getMinBuildHeight())
            && param1 < QuartPos.fromBlock(this.areaWithOldGeneration.getMaxBuildHeight())) {
            int var0 = param1 - QuartPos.fromBlock(this.areaWithOldGeneration.getMinBuildHeight());

            for(int var1 = 0; var1 < this.biomes.size(); ++var1) {
                if (this.biomes.get(var1) != null) {
                    Holder<Biome> var2 = this.biomes.get(var1).get(var0);
                    if (var2 != null) {
                        param3.consume(param0 + getX(var1), param2 + getZ(var1), var2);
                    }
                }
            }

        }
    }

    protected void iterateHeights(int param0, int param1, BlendingData.HeightConsumer param2) {
        for(int var0 = 0; var0 < this.heights.length; ++var0) {
            double var1 = this.heights[var0];
            if (var1 != Double.MAX_VALUE) {
                param2.consume(param0 + getX(var0), param1 + getZ(var0), var1);
            }
        }

    }

    protected void iterateDensities(int param0, int param1, int param2, int param3, BlendingData.DensityConsumer param4) {
        int var0 = this.getColumnMinY();
        int var1 = Math.max(0, param2 - var0);
        int var2 = Math.min(this.cellCountPerColumn(), param3 - var0);

        for(int var3 = 0; var3 < this.densities.length; ++var3) {
            double[] var4 = this.densities[var3];
            if (var4 != null) {
                int var5 = param0 + getX(var3);
                int var6 = param1 + getZ(var3);

                for(int var7 = var1; var7 < var2; ++var7) {
                    param4.consume(var5, var7 + var0, var6, var4[var7] * 0.1);
                }
            }
        }

    }

    private int cellCountPerColumn() {
        return this.areaWithOldGeneration.getSectionsCount() * 2;
    }

    private int quartCountPerColumn() {
        return QuartPos.fromSection(this.areaWithOldGeneration.getSectionsCount());
    }

    private int getColumnMinY() {
        return this.getMinY() + 1;
    }

    private int getMinY() {
        return this.areaWithOldGeneration.getMinSection() * 2;
    }

    private int getCellYIndex(int param0) {
        return param0 - this.getColumnMinY();
    }

    private static int getInsideIndex(int param0, int param1) {
        return CELL_HORIZONTAL_MAX_INDEX_INSIDE - param0 + param1;
    }

    private static int getOutsideIndex(int param0, int param1) {
        return CELL_COLUMN_INSIDE_COUNT + param0 + CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - param1;
    }

    private static int getX(int param0) {
        if (param0 < CELL_COLUMN_INSIDE_COUNT) {
            return zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX_INSIDE - param0);
        } else {
            int var0 = param0 - CELL_COLUMN_INSIDE_COUNT;
            return CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - var0);
        }
    }

    private static int getZ(int param0) {
        if (param0 < CELL_COLUMN_INSIDE_COUNT) {
            return zeroIfNegative(param0 - CELL_HORIZONTAL_MAX_INDEX_INSIDE);
        } else {
            int var0 = param0 - CELL_COLUMN_INSIDE_COUNT;
            return CELL_HORIZONTAL_MAX_INDEX_OUTSIDE - zeroIfNegative(var0 - CELL_HORIZONTAL_MAX_INDEX_OUTSIDE);
        }
    }

    private static int zeroIfNegative(int param0) {
        return param0 & ~(param0 >> 31);
    }

    public LevelHeightAccessor getAreaWithOldGeneration() {
        return this.areaWithOldGeneration;
    }

    protected interface BiomeConsumer {
        void consume(int var1, int var2, Holder<Biome> var3);
    }

    protected interface DensityConsumer {
        void consume(int var1, int var2, int var3, double var4);
    }

    protected interface HeightConsumer {
        void consume(int var1, int var2, double var3);
    }
}
