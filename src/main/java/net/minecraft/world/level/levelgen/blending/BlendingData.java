package net.minecraft.world.level.levelgen.blending;

import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public class BlendingData {
    private static final double BLENDING_DENSITY_FACTOR = 1.0;
    private static final LevelHeightAccessor AREA_WITH_OLD_GENERATION = new LevelHeightAccessor() {
        @Override
        public int getHeight() {
            return 256;
        }

        @Override
        public int getMinBuildHeight() {
            return 0;
        }
    };
    public static final int CELL_HEIGHT = 8;
    private static final int CELLS_PER_SECTION_Y = 2;
    private static final int CELL_HORIZONTAL_MAX_INDEX = QuartPos.fromBlock(16) - 1;
    private static final int CELL_COLUMN_COUNT = 2 * CELL_HORIZONTAL_MAX_INDEX + 1;
    private static final List<Block> SURFACE_BLOCKS = List.of(
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
    );
    public static final BlendingData EMPTY = new BlendingData(null);
    public static final double NO_VALUE = Double.POSITIVE_INFINITY;
    private final ChunkPos pos;
    private final double[] heightDataArray;
    private final double[][] densityDataArray;

    public static BlendingData getOrCreateAndStoreToChunk(WorldGenRegion param0, int param1, int param2) {
        ChunkAccess var0 = param0.getChunk(param1, param2);
        BlendingData var1 = var0.getBlendingData();
        if (var1 != null) {
            return var1;
        } else {
            ChunkAccess var2 = param0.getChunk(param1 - 1, param2);
            ChunkAccess var3 = param0.getChunk(param1, param2 - 1);
            ChunkAccess var4 = param0.getChunk(param1 - 1, param2 - 1);
            boolean var5 = var0.isOldNoiseGeneration();
            BlendingData var6;
            if (var5 == var2.isOldNoiseGeneration() && var5 == var3.isOldNoiseGeneration() && var5 == var4.isOldNoiseGeneration()) {
                var6 = EMPTY;
            } else {
                var6 = new BlendingData(var0, var2, var3, var4);
            }

            var0.setBlendingData(var6);
            return var6;
        }
    }

    private BlendingData(ChunkPos param0) {
        this.pos = param0;
        this.heightDataArray = new double[CELL_COLUMN_COUNT];
        Arrays.fill(this.heightDataArray, Double.POSITIVE_INFINITY);
        this.densityDataArray = new double[CELL_COLUMN_COUNT][];
    }

    private BlendingData(ChunkAccess param0, ChunkAccess param1, ChunkAccess param2, ChunkAccess param3) {
        this(param0.getPos());
        if (param0.isOldNoiseGeneration()) {
            this.addValuesForColumn(getIndex(0, 0), param0, 0, 0);
            if (!param1.isOldNoiseGeneration()) {
                this.addValuesForColumn(getIndex(0, 1), param0, 0, 4);
                this.addValuesForColumn(getIndex(0, 2), param0, 0, 8);
                this.addValuesForColumn(getIndex(0, 3), param0, 0, 12);
            }

            if (!param2.isOldNoiseGeneration()) {
                this.addValuesForColumn(getIndex(1, 0), param0, 4, 0);
                this.addValuesForColumn(getIndex(2, 0), param0, 8, 0);
                this.addValuesForColumn(getIndex(3, 0), param0, 12, 0);
            }
        } else {
            if (param1.isOldNoiseGeneration()) {
                this.addValuesForColumn(getIndex(0, 0), param1, 15, 0);
                this.addValuesForColumn(getIndex(0, 1), param1, 15, 4);
                this.addValuesForColumn(getIndex(0, 2), param1, 15, 8);
                this.addValuesForColumn(getIndex(0, 3), param1, 15, 12);
            }

            if (param2.isOldNoiseGeneration()) {
                if (!param1.isOldNoiseGeneration()) {
                    this.addValuesForColumn(getIndex(0, 0), param2, 0, 15);
                }

                this.addValuesForColumn(getIndex(1, 0), param2, 4, 15);
                this.addValuesForColumn(getIndex(2, 0), param2, 8, 15);
                this.addValuesForColumn(getIndex(3, 0), param2, 12, 15);
            }

            if (param3.isOldNoiseGeneration() && !param1.isOldNoiseGeneration() && !param2.isOldNoiseGeneration()) {
                this.addValuesForColumn(getIndex(0, 0), param3, 15, 15);
            }
        }

    }

    private void addValuesForColumn(int param0, ChunkAccess param1, int param2, int param3) {
        this.heightDataArray[param0] = (double)getHeightAtXZ(param1, param2, param3);
        this.densityDataArray[param0] = getDensityColumn(param1, param2, param3);
    }

    private static int getHeightAtXZ(ChunkAccess param0, int param1, int param2) {
        int var0;
        if (param0.hasPrimedHeightmap(Heightmap.Types.WORLD_SURFACE_WG)) {
            var0 = Math.min(param0.getHeight(Heightmap.Types.WORLD_SURFACE_WG, param1, param2), AREA_WITH_OLD_GENERATION.getMaxBuildHeight());
        } else {
            var0 = AREA_WITH_OLD_GENERATION.getMaxBuildHeight();
        }

        int var2 = AREA_WITH_OLD_GENERATION.getMinBuildHeight();
        BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos(param1, var0, param2);

        while(var3.getY() > var2) {
            var3.move(Direction.DOWN);
            if (SURFACE_BLOCKS.contains(param0.getBlockState(var3).getBlock())) {
                return var3.getY();
            }
        }

        return var2;
    }

    private static double[] getDensityColumn(ChunkAccess param0, int param1, int param2) {
        int var0 = AREA_WITH_OLD_GENERATION.getSectionsCount() * 2 + 1;
        int var1 = AREA_WITH_OLD_GENERATION.getMinSection() * 2;
        double[] var2 = new double[var0];
        double var3 = 3.0;
        double var4 = 0.0;
        double var5 = 0.0;
        BlockPos.MutableBlockPos var6 = new BlockPos.MutableBlockPos();
        double var7 = 15.0;

        for(int var8 = AREA_WITH_OLD_GENERATION.getMaxBuildHeight() - 1; var8 >= AREA_WITH_OLD_GENERATION.getMinBuildHeight(); --var8) {
            double var9 = isGround(param0, var6.set(param1, var8, param2)) ? 1.0 : -1.0;
            int var10 = var8 % 8;
            if (var10 == 0) {
                double var11 = var4 / 15.0;
                int var12 = var8 / 8 + 1;
                var2[var12 - var1] = var11 * var3;
                var4 = var5;
                var5 = 0.0;
                if (var11 > 0.0) {
                    var3 = 0.1;
                }
            } else {
                var5 += var9;
            }

            var4 += var9;
        }

        var2[0] = var4 / 8.0 * var3;
        return var2;
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

    protected double getHeight(int param0, int param1) {
        int var0 = param0 & 3;
        int var1 = param1 & 3;
        return var0 != 0 && var1 != 0 ? Double.POSITIVE_INFINITY : this.heightDataArray[getIndex(var0, var1)];
    }

    protected double getDensity(int param0, int param1, int param2) {
        int var0 = param0 & 3;
        int var1 = param2 & 3;
        if (var0 != 0 && var1 != 0) {
            return Double.POSITIVE_INFINITY;
        } else {
            double[] var2 = this.densityDataArray[getIndex(var0, var1)];
            if (var2 == null) {
                return Double.POSITIVE_INFINITY;
            } else {
                int var3 = param1 - AREA_WITH_OLD_GENERATION.getMinSection() * 2;
                return var3 >= 0 && var3 < var2.length ? var2[var3] * 1.0 : Double.POSITIVE_INFINITY;
            }
        }
    }

    protected void iterateHeights(BlendingData.HeightConsumer param0) {
        for(int var0 = 0; var0 < this.heightDataArray.length; ++var0) {
            double var1 = this.heightDataArray[var0];
            if (var1 != Double.POSITIVE_INFINITY) {
                param0.consume(getX(var0) + QuartPos.fromSection(this.pos.x), getZ(var0) + QuartPos.fromSection(this.pos.z), var1);
            }
        }

    }

    protected void iterateDensities(int param0, int param1, BlendingData.DensityConsumer param2) {
        int var0 = Math.max(0, param0 - AREA_WITH_OLD_GENERATION.getMinSection() * 2);
        int var1 = Math.min(AREA_WITH_OLD_GENERATION.getSectionsCount() * 2 + 1, param1 - AREA_WITH_OLD_GENERATION.getMinSection() * 2);

        for(int var2 = 0; var2 < this.densityDataArray.length; ++var2) {
            double[] var3 = this.densityDataArray[var2];
            if (var3 != null) {
                for(int var4 = var0; var4 < var1; ++var4) {
                    param2.consume(
                        getX(var2) + QuartPos.fromSection(this.pos.x),
                        var4 + AREA_WITH_OLD_GENERATION.getMinSection() * 2,
                        getZ(var2) + QuartPos.fromSection(this.pos.z),
                        var3[var4] * 1.0
                    );
                }
            }
        }

    }

    private static int getIndex(int param0, int param1) {
        return CELL_HORIZONTAL_MAX_INDEX - param0 + param1;
    }

    private static int getX(int param0) {
        return zeroIfNegative(CELL_HORIZONTAL_MAX_INDEX - param0);
    }

    private static int getZ(int param0) {
        return zeroIfNegative(param0 - CELL_HORIZONTAL_MAX_INDEX);
    }

    private static int zeroIfNegative(int param0) {
        return param0 & ~(param0 >> 31);
    }

    protected interface DensityConsumer {
        void consume(int var1, int var2, int var3, double var4);
    }

    protected interface HeightConsumer {
        void consume(int var1, int var2, double var3);
    }
}
