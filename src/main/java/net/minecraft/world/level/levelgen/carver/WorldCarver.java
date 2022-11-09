package net.minecraft.world.level.levelgen.carver;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.commons.lang3.mutable.MutableBoolean;

public abstract class WorldCarver<C extends CarverConfiguration> {
    public static final WorldCarver<CaveCarverConfiguration> CAVE = register("cave", new CaveWorldCarver(CaveCarverConfiguration.CODEC));
    public static final WorldCarver<CaveCarverConfiguration> NETHER_CAVE = register("nether_cave", new NetherWorldCarver(CaveCarverConfiguration.CODEC));
    public static final WorldCarver<CanyonCarverConfiguration> CANYON = register("canyon", new CanyonWorldCarver(CanyonCarverConfiguration.CODEC));
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    protected static final FluidState WATER = Fluids.WATER.defaultFluidState();
    protected static final FluidState LAVA = Fluids.LAVA.defaultFluidState();
    protected Set<Fluid> liquids = ImmutableSet.of(Fluids.WATER);
    private final Codec<ConfiguredWorldCarver<C>> configuredCodec;

    private static <C extends CarverConfiguration, F extends WorldCarver<C>> F register(String param0, F param1) {
        return Registry.register(BuiltInRegistries.CARVER, param0, param1);
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
        Function<BlockPos, Holder<Biome>> param3,
        Aquifer param4,
        double param5,
        double param6,
        double param7,
        double param8,
        double param9,
        CarvingMask param10,
        WorldCarver.CarveSkipChecker param11
    ) {
        ChunkPos var0 = param2.getPos();
        double var1 = (double)var0.getMiddleBlockX();
        double var2 = (double)var0.getMiddleBlockZ();
        double var3 = 16.0 + param8 * 2.0;
        if (!(Math.abs(param5 - var1) > var3) && !(Math.abs(param7 - var2) > var3)) {
            int var4 = var0.getMinBlockX();
            int var5 = var0.getMinBlockZ();
            int var6 = Math.max(Mth.floor(param5 - param8) - var4 - 1, 0);
            int var7 = Math.min(Mth.floor(param5 + param8) - var4, 15);
            int var8 = Math.max(Mth.floor(param6 - param9) - 1, param0.getMinGenY() + 1);
            int var9 = param2.isUpgrading() ? 0 : 7;
            int var10 = Math.min(Mth.floor(param6 + param9) + 1, param0.getMinGenY() + param0.getGenDepth() - 1 - var9);
            int var11 = Math.max(Mth.floor(param7 - param8) - var5 - 1, 0);
            int var12 = Math.min(Mth.floor(param7 + param8) - var5, 15);
            boolean var13 = false;
            BlockPos.MutableBlockPos var14 = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos var15 = new BlockPos.MutableBlockPos();

            for(int var16 = var6; var16 <= var7; ++var16) {
                int var17 = var0.getBlockX(var16);
                double var18 = ((double)var17 + 0.5 - param5) / param8;

                for(int var19 = var11; var19 <= var12; ++var19) {
                    int var20 = var0.getBlockZ(var19);
                    double var21 = ((double)var20 + 0.5 - param7) / param8;
                    if (!(var18 * var18 + var21 * var21 >= 1.0)) {
                        MutableBoolean var22 = new MutableBoolean(false);

                        for(int var23 = var10; var23 > var8; --var23) {
                            double var24 = ((double)var23 - 0.5 - param6) / param9;
                            if (!param11.shouldSkip(param0, var18, var24, var21, var23) && (!param10.get(var16, var23, var19) || isDebugEnabled(param1))) {
                                param10.set(var16, var23, var19);
                                var14.set(var17, var23, var20);
                                var13 |= this.carveBlock(param0, param1, param2, param3, param10, var14, var15, param4, var22);
                            }
                        }
                    }
                }
            }

            return var13;
        } else {
            return false;
        }
    }

    protected boolean carveBlock(
        CarvingContext param0,
        C param1,
        ChunkAccess param2,
        Function<BlockPos, Holder<Biome>> param3,
        CarvingMask param4,
        BlockPos.MutableBlockPos param5,
        BlockPos.MutableBlockPos param6,
        Aquifer param7,
        MutableBoolean param8
    ) {
        BlockState var0 = param2.getBlockState(param5);
        if (var0.is(Blocks.GRASS_BLOCK) || var0.is(Blocks.MYCELIUM)) {
            param8.setTrue();
        }

        if (!this.canReplaceBlock(param1, var0) && !isDebugEnabled(param1)) {
            return false;
        } else {
            BlockState var1 = this.getCarveState(param0, param1, param5, param7);
            if (var1 == null) {
                return false;
            } else {
                param2.setBlockState(param5, var1, false);
                if (param7.shouldScheduleFluidUpdate() && !var1.getFluidState().isEmpty()) {
                    param2.markPosForPostprocessing(param5);
                }

                if (param8.isTrue()) {
                    param6.setWithOffset(param5, Direction.DOWN);
                    if (param2.getBlockState(param6).is(Blocks.DIRT)) {
                        param0.topMaterial(param3, param2, param6, !var1.getFluidState().isEmpty()).ifPresent(param2x -> {
                            param2.setBlockState(param6, param2x, false);
                            if (!param2x.getFluidState().isEmpty()) {
                                param2.markPosForPostprocessing(param6);
                            }

                        });
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
            BlockState var0 = param3.computeSubstance(new DensityFunction.SinglePointContext(param2.getX(), param2.getY(), param2.getZ()), 0.0);
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
        CarvingContext var1, C var2, ChunkAccess var3, Function<BlockPos, Holder<Biome>> var4, RandomSource var5, Aquifer var6, ChunkPos var7, CarvingMask var8
    );

    public abstract boolean isStartChunk(C var1, RandomSource var2);

    protected boolean canReplaceBlock(C param0, BlockState param1) {
        return param1.is(param0.replaceable);
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
