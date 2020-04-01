package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.Dynamic;
import java.util.BitSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public abstract class WorldCarver<C extends CarverConfiguration> {
    public static final WorldCarver<ProbabilityFeatureConfiguration> CAVE = register(
        "cave", new CaveWorldCarver(ProbabilityFeatureConfiguration::deserialize, 256)
    );
    public static final WorldCarver<ProbabilityFeatureConfiguration> NETHER_CAVE = register(
        "nether_cave", new NetherWorldCarver(ProbabilityFeatureConfiguration::deserialize)
    );
    public static final WorldCarver<ProbabilityFeatureConfiguration> CANYON = register(
        "canyon", new CanyonWorldCarver(ProbabilityFeatureConfiguration::deserialize)
    );
    public static final WorldCarver<ProbabilityFeatureConfiguration> UNDERWATER_CANYON = register(
        "underwater_canyon", new UnderwaterCanyonWorldCarver(ProbabilityFeatureConfiguration::deserialize)
    );
    public static final WorldCarver<ProbabilityFeatureConfiguration> UNDERWATER_CAVE = register(
        "underwater_cave", new UnderwaterCaveWorldCarver(ProbabilityFeatureConfiguration::deserialize)
    );
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
    protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
    protected Set<Block> replaceableBlocks = ImmutableSet.of(
        Blocks.STONE,
        Blocks.GRANITE,
        Blocks.DIORITE,
        Blocks.ANDESITE,
        Blocks.DIRT,
        Blocks.COARSE_DIRT,
        Blocks.PODZOL,
        Blocks.GRASS_BLOCK,
        Blocks.TERRACOTTA,
        Blocks.WHITE_TERRACOTTA,
        Blocks.ORANGE_TERRACOTTA,
        Blocks.MAGENTA_TERRACOTTA,
        Blocks.LIGHT_BLUE_TERRACOTTA,
        Blocks.YELLOW_TERRACOTTA,
        Blocks.LIME_TERRACOTTA,
        Blocks.PINK_TERRACOTTA,
        Blocks.GRAY_TERRACOTTA,
        Blocks.LIGHT_GRAY_TERRACOTTA,
        Blocks.CYAN_TERRACOTTA,
        Blocks.PURPLE_TERRACOTTA,
        Blocks.BLUE_TERRACOTTA,
        Blocks.BROWN_TERRACOTTA,
        Blocks.GREEN_TERRACOTTA,
        Blocks.RED_TERRACOTTA,
        Blocks.BLACK_TERRACOTTA,
        Blocks.SANDSTONE,
        Blocks.RED_SANDSTONE,
        Blocks.MYCELIUM,
        Blocks.SNOW,
        Blocks.PACKED_ICE
    );
    protected Set<Fluid> liquids = ImmutableSet.of(Fluids.WATER);
    private final Function<Dynamic<?>, ? extends C> configurationFactory;
    protected final int genHeight;

    private static <C extends CarverConfiguration, F extends WorldCarver<C>> F register(String param0, F param1) {
        return Registry.register(Registry.CARVER, param0, param1);
    }

    public WorldCarver(Function<Dynamic<?>, ? extends C> param0, int param1) {
        this.configurationFactory = param0;
        this.genHeight = param1;
    }

    public int getRange() {
        return 4;
    }

    protected boolean carveSphere(
        ChunkAccess param0,
        Function<BlockPos, Biome> param1,
        long param2,
        int param3,
        int param4,
        int param5,
        double param6,
        double param7,
        double param8,
        double param9,
        double param10,
        BitSet param11
    ) {
        Random var0 = new Random(param2 + (long)param4 + (long)param5);
        double var1 = (double)(param4 * 16 + 8);
        double var2 = (double)(param5 * 16 + 8);
        if (!(param6 < var1 - 16.0 - param9 * 2.0)
            && !(param8 < var2 - 16.0 - param9 * 2.0)
            && !(param6 > var1 + 16.0 + param9 * 2.0)
            && !(param8 > var2 + 16.0 + param9 * 2.0)) {
            int var3 = Math.max(Mth.floor(param6 - param9) - param4 * 16 - 1, 0);
            int var4 = Math.min(Mth.floor(param6 + param9) - param4 * 16 + 1, 16);
            int var5 = Math.max(Mth.floor(param7 - param10) - 1, 1);
            int var6 = Math.min(Mth.floor(param7 + param10) + 1, this.genHeight - 8);
            int var7 = Math.max(Mth.floor(param8 - param9) - param5 * 16 - 1, 0);
            int var8 = Math.min(Mth.floor(param8 + param9) - param5 * 16 + 1, 16);
            if (this.hasWater(param0, param4, param5, var3, var4, var5, var6, var7, var8)) {
                return false;
            } else {
                boolean var9 = false;
                BlockPos.MutableBlockPos var10 = new BlockPos.MutableBlockPos();
                BlockPos.MutableBlockPos var11 = new BlockPos.MutableBlockPos();
                BlockPos.MutableBlockPos var12 = new BlockPos.MutableBlockPos();

                for(int var13 = var3; var13 < var4; ++var13) {
                    int var14 = var13 + param4 * 16;
                    double var15 = ((double)var14 + 0.5 - param6) / param9;

                    for(int var16 = var7; var16 < var8; ++var16) {
                        int var17 = var16 + param5 * 16;
                        double var18 = ((double)var17 + 0.5 - param8) / param9;
                        if (!(var15 * var15 + var18 * var18 >= 1.0)) {
                            AtomicBoolean var19 = new AtomicBoolean(false);

                            for(int var20 = var6; var20 > var5; --var20) {
                                double var21 = ((double)var20 - 0.5 - param7) / param10;
                                if (!this.skip(var15, var21, var18, var20)) {
                                    var9 |= this.carveBlock(
                                        param0, param1, param11, var0, var10, var11, var12, param3, param4, param5, var14, var17, var13, var20, var16, var19
                                    );
                                }
                            }
                        }
                    }
                }

                return var9;
            }
        } else {
            return false;
        }
    }

    protected boolean carveBlock(
        ChunkAccess param0,
        Function<BlockPos, Biome> param1,
        BitSet param2,
        Random param3,
        BlockPos.MutableBlockPos param4,
        BlockPos.MutableBlockPos param5,
        BlockPos.MutableBlockPos param6,
        int param7,
        int param8,
        int param9,
        int param10,
        int param11,
        int param12,
        int param13,
        int param14,
        AtomicBoolean param15
    ) {
        int var0 = param12 | param14 << 4 | param13 << 8;
        if (param2.get(var0)) {
            return false;
        } else {
            param2.set(var0);
            param4.set(param10, param13, param11);
            BlockState var1 = param0.getBlockState(param4);
            BlockState var2 = param0.getBlockState(param5.setWithOffset(param4, Direction.UP));
            if (var1.getBlock() == Blocks.GRASS_BLOCK || var1.getBlock() == Blocks.MYCELIUM) {
                param15.set(true);
            }

            if (!this.canReplaceBlock(var1, var2)) {
                return false;
            } else {
                if (param13 < 11) {
                    param0.setBlockState(param4, LAVA.createLegacyBlock(), false);
                } else {
                    param0.setBlockState(param4, CAVE_AIR, false);
                    if (param15.get()) {
                        param6.setWithOffset(param4, Direction.DOWN);
                        if (param0.getBlockState(param6).getBlock() == Blocks.DIRT) {
                            param0.setBlockState(param6, param1.apply(param4).getSurfaceBuilderConfig().getTopMaterial(), false);
                        }
                    }
                }

                return true;
            }
        }
    }

    public abstract boolean carve(
        ChunkAccess var1, Function<BlockPos, Biome> var2, Random var3, int var4, int var5, int var6, int var7, int var8, BitSet var9, C var10
    );

    public abstract boolean isStartChunk(Random var1, int var2, int var3, C var4);

    protected boolean canReplaceBlock(BlockState param0) {
        return this.replaceableBlocks.contains(param0.getBlock());
    }

    protected boolean canReplaceBlock(BlockState param0, BlockState param1) {
        Block var0 = param0.getBlock();
        return this.canReplaceBlock(param0) || (var0 == Blocks.SAND || var0 == Blocks.GRAVEL) && !param1.getFluidState().is(FluidTags.WATER);
    }

    protected boolean hasWater(ChunkAccess param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, int param8) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = param3; var1 < param4; ++var1) {
            for(int var2 = param7; var2 < param8; ++var2) {
                for(int var3 = param5 - 1; var3 <= param6 + 1; ++var3) {
                    if (this.liquids.contains(param0.getFluidState(var0.set(var1 + param1 * 16, var3, var2 + param2 * 16)).getType())) {
                        return true;
                    }

                    if (var3 != param6 + 1 && !this.isEdge(param3, param4, param7, param8, var1, var2)) {
                        var3 = param6;
                    }
                }
            }
        }

        return false;
    }

    private boolean isEdge(int param0, int param1, int param2, int param3, int param4, int param5) {
        return param4 == param0 || param4 == param1 - 1 || param5 == param2 || param5 == param3 - 1;
    }

    protected boolean canReach(int param0, int param1, double param2, double param3, int param4, int param5, float param6) {
        double var0 = (double)(param0 * 16 + 8);
        double var1 = (double)(param1 * 16 + 8);
        double var2 = param2 - var0;
        double var3 = param3 - var1;
        double var4 = (double)(param5 - param4);
        double var5 = (double)(param6 + 2.0F + 16.0F);
        return var2 * var2 + var3 * var3 - var4 * var4 <= var5 * var5;
    }

    protected abstract boolean skip(double var1, double var3, double var5, int var7);

    public abstract C randomConfig(Random var1);
}
