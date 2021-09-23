package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.BitSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class WorldCarver<C extends CarverConfiguration> {
    public static final WorldCarver<CaveCarverConfiguration> CAVE = register("cave", new CaveWorldCarver(CaveCarverConfiguration.CODEC));
    public static final WorldCarver<CaveCarverConfiguration> NETHER_CAVE = register("nether_cave", new NetherWorldCarver(CaveCarverConfiguration.CODEC));
    public static final WorldCarver<CanyonCarverConfiguration> CANYON = register("canyon", new CanyonWorldCarver(CanyonCarverConfiguration.CODEC));
    public static final WorldCarver<CanyonCarverConfiguration> UNDERWATER_CANYON = register(
        "underwater_canyon", new UnderwaterCanyonWorldCarver(CanyonCarverConfiguration.CODEC)
    );
    public static final WorldCarver<CaveCarverConfiguration> UNDERWATER_CAVE = register(
        "underwater_cave", new UnderwaterCaveWorldCarver(CaveCarverConfiguration.CODEC)
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
        Blocks.PACKED_ICE,
        Blocks.DEEPSLATE,
        Blocks.CALCITE,
        Blocks.SAND,
        Blocks.RED_SAND,
        Blocks.GRAVEL,
        Blocks.TUFF,
        Blocks.GRANITE,
        Blocks.IRON_ORE,
        Blocks.DEEPSLATE_IRON_ORE,
        Blocks.RAW_IRON_BLOCK,
        Blocks.COPPER_ORE,
        Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.RAW_COPPER_BLOCK
    );
    protected Set<Fluid> liquids = ImmutableSet.of(Fluids.WATER);
    private final Codec<ConfiguredWorldCarver<C>> configuredCodec;

    private static <C extends CarverConfiguration, F extends WorldCarver<C>> F register(String param0, F param1) {
        return Registry.register(Registry.CARVER, param0, param1);
    }

    public WorldCarver(Codec<C> param0) {
        this.configuredCodec = param0.fieldOf("config").xmap(this::configured, ConfiguredWorldCarver::config).codec();
    }

    public ConfiguredWorldCarver<C> configured(C param0x) {
        return new ConfiguredWorldCarver<>(this, param0x);
    }

    public Codec<ConfiguredWorldCarver<C>> configuredCodec() {
        return this.configuredCodec;
    }

    public int getRange() {
        return 4;
    }

    protected boolean carveEllipsoid(
        CarvingContext param0,
        C param1,
        ChunkAccess param2,
        Function<BlockPos, Biome> param3,
        long param4,
        Aquifer param5,
        double param6,
        double param7,
        double param8,
        double param9,
        double param10,
        BitSet param11,
        WorldCarver.CarveSkipChecker param12
    ) {
        ChunkPos var0 = param2.getPos();
        int var1 = var0.x;
        int var2 = var0.z;
        Random var3 = new Random(param4 + (long)var1 + (long)var2);
        double var4 = (double)var0.getMiddleBlockX();
        double var5 = (double)var0.getMiddleBlockZ();
        double var6 = 16.0 + param9 * 2.0;
        if (!(Math.abs(param6 - var4) > var6) && !(Math.abs(param8 - var5) > var6)) {
            int var7 = var0.getMinBlockX();
            int var8 = var0.getMinBlockZ();
            int var9 = Math.max(Mth.floor(param6 - param9) - var7 - 1, 0);
            int var10 = Math.min(Mth.floor(param6 + param9) - var7, 15);
            int var11 = Math.max(Mth.floor(param7 - param10) - 1, param0.getMinGenY() + 1);
            int var12 = Math.min(Mth.floor(param7 + param10) + 1, param0.getMinGenY() + param0.getGenDepth() - 8);
            int var13 = Math.max(Mth.floor(param8 - param9) - var8 - 1, 0);
            int var14 = Math.min(Mth.floor(param8 + param9) - var8, 15);
            boolean var15 = false;
            BlockPos.MutableBlockPos var16 = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos var17 = new BlockPos.MutableBlockPos();

            for(int var18 = var9; var18 <= var10; ++var18) {
                int var19 = var0.getBlockX(var18);
                double var20 = ((double)var19 + 0.5 - param6) / param9;

                for(int var21 = var13; var21 <= var14; ++var21) {
                    int var22 = var0.getBlockZ(var21);
                    double var23 = ((double)var22 + 0.5 - param8) / param9;
                    if (!(var20 * var20 + var23 * var23 >= 1.0)) {
                        MutableBoolean var24 = new MutableBoolean(false);

                        for(int var25 = var12; var25 > var11; --var25) {
                            double var26 = ((double)var25 - 0.5 - param7) / param10;
                            if (!param12.shouldSkip(param0, var20, var26, var23, var25)) {
                                int var27 = var25 - param0.getMinGenY();
                                int var28 = var18 | var21 << 4 | var27 << 8;
                                if (!param11.get(var28) || isDebugEnabled(param1)) {
                                    param11.set(var28);
                                    var16.set(var19, var25, var22);
                                    var15 |= this.carveBlock(param0, param1, param2, param3, param11, var3, var16, var17, param5, var24);
                                }
                            }
                        }
                    }
                }
            }

            return var15;
        } else {
            return false;
        }
    }

    protected boolean carveBlock(
        CarvingContext param0,
        C param1,
        ChunkAccess param2,
        Function<BlockPos, Biome> param3,
        BitSet param4,
        Random param5,
        BlockPos.MutableBlockPos param6,
        BlockPos.MutableBlockPos param7,
        Aquifer param8,
        MutableBoolean param9
    ) {
        BlockState var0 = param2.getBlockState(param6);
        if (var0.is(Blocks.GRASS_BLOCK) || var0.is(Blocks.MYCELIUM)) {
            param9.setTrue();
        }

        if (!this.canReplaceBlock(var0) && !isDebugEnabled(param1)) {
            return false;
        } else {
            BlockState var1 = this.getCarveState(param0, param1, param6, param8);
            if (var1 == null) {
                return false;
            } else {
                param2.setBlockState(param6, var1, false);
                if (param8.shouldScheduleFluidUpdate() && !var1.getFluidState().isEmpty()) {
                    param2.getLiquidTicks().scheduleTick(param6, var1.getFluidState().getType(), 0);
                }

                if (param9.isTrue()) {
                    param7.setWithOffset(param6, Direction.DOWN);
                    if (param2.getBlockState(param7).is(Blocks.DIRT)) {
                        param2.setBlockState(param7, param3.apply(param6).getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial(), false);
                    }
                }

                return true;
            }
        }
    }

    @Nullable
    private BlockState getCarveState(CarvingContext param0, C param1, BlockPos param2, Aquifer param3) {
        if (param2.getY() <= param1.lavaLevel.resolveY(param0)) {
            return LAVA.createLegacyBlock();
        } else {
            BlockState var0 = param3.computeSubstance(param2.getX(), param2.getY(), param2.getZ(), 0.0, 0.0);
            if (var0 == null) {
                return isDebugEnabled(param1) ? param1.debugSettings.getBarrierState() : null;
            } else {
                return isDebugEnabled(param1) ? getDebugState(param1, var0) : var0;
            }
        }
    }

    private static BlockState getDebugState(CarverConfiguration param0, BlockState param1) {
        if (param1.is(Blocks.AIR)) {
            return param0.debugSettings.getAirState();
        } else if (param1.is(Blocks.WATER)) {
            BlockState var0 = param0.debugSettings.getWaterState();
            return var0.hasProperty(BlockStateProperties.WATERLOGGED) ? var0.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)) : var0;
        } else {
            return param1.is(Blocks.LAVA) ? param0.debugSettings.getLavaState() : param1;
        }
    }

    public abstract boolean carve(
        CarvingContext var1, C var2, ChunkAccess var3, Function<BlockPos, Biome> var4, Random var5, Aquifer var6, ChunkPos var7, BitSet var8
    );

    public abstract boolean isStartChunk(C var1, Random var2);

    protected boolean canReplaceBlock(BlockState param0) {
        return this.replaceableBlocks.contains(param0.getBlock());
    }

    protected boolean hasDisallowedLiquid(ChunkAccess param0, int param1, int param2, int param3, int param4, int param5, int param6) {
        ChunkPos var0 = param0.getPos();
        int var1 = var0.getMinBlockX();
        int var2 = var0.getMinBlockZ();
        BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

        for(int var4 = param1; var4 <= param2; ++var4) {
            for(int var5 = param5; var5 <= param6; ++var5) {
                for(int var6 = param3 - 1; var6 <= param4 + 1; ++var6) {
                    var3.set(var1 + var4, var6, var2 + var5);
                    if (this.liquids.contains(param0.getFluidState(var3).getType())) {
                        return true;
                    }

                    if (var6 != param4 + 1 && !isEdge(var4, var5, param1, param2, param5, param6)) {
                        var6 = param4;
                    }
                }
            }
        }

        return false;
    }

    private static boolean isEdge(int param0, int param1, int param2, int param3, int param4, int param5) {
        return param0 == param2 || param0 == param3 || param1 == param4 || param1 == param5;
    }

    protected static boolean canReach(ChunkPos param0, double param1, double param2, int param3, int param4, float param5) {
        double var0 = (double)param0.getMiddleBlockX();
        double var1 = (double)param0.getMiddleBlockZ();
        double var2 = param1 - var0;
        double var3 = param2 - var1;
        double var4 = (double)(param4 - param3);
        double var5 = (double)(param5 + 2.0F + 16.0F);
        return var2 * var2 + var3 * var3 - var4 * var4 <= var5 * var5;
    }

    private static boolean isDebugEnabled(CarverConfiguration param0) {
        return param0.debugSettings.isDebugMode();
    }

    public interface CarveSkipChecker {
        boolean shouldSkip(CarvingContext var1, double var2, double var4, double var6, int var8);
    }
}
