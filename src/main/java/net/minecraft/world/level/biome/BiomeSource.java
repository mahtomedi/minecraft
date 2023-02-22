package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;

public abstract class BiomeSource implements BiomeResolver {
    public static final Codec<BiomeSource> CODEC = BuiltInRegistries.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
    private final Supplier<Set<Holder<Biome>>> possibleBiomes = Suppliers.memoize(
        () -> this.collectPossibleBiomes().distinct().collect(ImmutableSet.toImmutableSet())
    );

    protected BiomeSource() {
    }

    protected abstract Codec<? extends BiomeSource> codec();

    protected abstract Stream<Holder<Biome>> collectPossibleBiomes();

    public Set<Holder<Biome>> possibleBiomes() {
        return this.possibleBiomes.get();
    }

    public Set<Holder<Biome>> getBiomesWithin(int param0, int param1, int param2, int param3, Climate.Sampler param4) {
        int var0 = QuartPos.fromBlock(param0 - param3);
        int var1 = QuartPos.fromBlock(param1 - param3);
        int var2 = QuartPos.fromBlock(param2 - param3);
        int var3 = QuartPos.fromBlock(param0 + param3);
        int var4 = QuartPos.fromBlock(param1 + param3);
        int var5 = QuartPos.fromBlock(param2 + param3);
        int var6 = var3 - var0 + 1;
        int var7 = var4 - var1 + 1;
        int var8 = var5 - var2 + 1;
        Set<Holder<Biome>> var9 = Sets.newHashSet();

        for(int var10 = 0; var10 < var8; ++var10) {
            for(int var11 = 0; var11 < var6; ++var11) {
                for(int var12 = 0; var12 < var7; ++var12) {
                    int var13 = var0 + var11;
                    int var14 = var1 + var12;
                    int var15 = var2 + var10;
                    var9.add(this.getNoiseBiome(var13, var14, var15, param4));
                }
            }
        }

        return var9;
    }

    @Nullable
    public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(
        int param0, int param1, int param2, int param3, Predicate<Holder<Biome>> param4, RandomSource param5, Climate.Sampler param6
    ) {
        return this.findBiomeHorizontal(param0, param1, param2, param3, 1, param4, param5, false, param6);
    }

    @Nullable
    public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(
        BlockPos param0, int param1, int param2, int param3, Predicate<Holder<Biome>> param4, Climate.Sampler param5, LevelReader param6
    ) {
        Set<Holder<Biome>> var0 = this.possibleBiomes().stream().filter(param4).collect(Collectors.toUnmodifiableSet());
        if (var0.isEmpty()) {
            return null;
        } else {
            int var1 = Math.floorDiv(param1, param2);
            int[] var2 = Mth.outFromOrigin(param0.getY(), param6.getMinBuildHeight() + 1, param6.getMaxBuildHeight(), param3).toArray();

            for(BlockPos.MutableBlockPos var3 : BlockPos.spiralAround(BlockPos.ZERO, var1, Direction.EAST, Direction.SOUTH)) {
                int var4 = param0.getX() + var3.getX() * param2;
                int var5 = param0.getZ() + var3.getZ() * param2;
                int var6 = QuartPos.fromBlock(var4);
                int var7 = QuartPos.fromBlock(var5);

                for(int var8 : var2) {
                    int var9 = QuartPos.fromBlock(var8);
                    Holder<Biome> var10 = this.getNoiseBiome(var6, var9, var7, param5);
                    if (var0.contains(var10)) {
                        return Pair.of(new BlockPos(var4, var8, var5), var10);
                    }
                }
            }

            return null;
        }
    }

    @Nullable
    public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(
        int param0,
        int param1,
        int param2,
        int param3,
        int param4,
        Predicate<Holder<Biome>> param5,
        RandomSource param6,
        boolean param7,
        Climate.Sampler param8
    ) {
        int var0 = QuartPos.fromBlock(param0);
        int var1 = QuartPos.fromBlock(param2);
        int var2 = QuartPos.fromBlock(param3);
        int var3 = QuartPos.fromBlock(param1);
        Pair<BlockPos, Holder<Biome>> var4 = null;
        int var5 = 0;
        int var6 = param7 ? 0 : var2;

        for(int var7 = var6; var7 <= var2; var7 += param4) {
            for(int var8 = SharedConstants.debugGenerateSquareTerrainWithoutNoise ? 0 : -var7; var8 <= var7; var8 += param4) {
                boolean var9 = Math.abs(var8) == var7;

                for(int var10 = -var7; var10 <= var7; var10 += param4) {
                    if (param7) {
                        boolean var11 = Math.abs(var10) == var7;
                        if (!var11 && !var9) {
                            continue;
                        }
                    }

                    int var12 = var0 + var10;
                    int var13 = var1 + var8;
                    Holder<Biome> var14 = this.getNoiseBiome(var12, var3, var13, param8);
                    if (param5.test(var14)) {
                        if (var4 == null || param6.nextInt(var5 + 1) == 0) {
                            BlockPos var15 = new BlockPos(QuartPos.toBlock(var12), param1, QuartPos.toBlock(var13));
                            if (param7) {
                                return Pair.of(var15, var14);
                            }

                            var4 = Pair.of(var15, var14);
                        }

                        ++var5;
                    }
                }
            }
        }

        return var4;
    }

    @Override
    public abstract Holder<Biome> getNoiseBiome(int var1, int var2, int var3, Climate.Sampler var4);

    public void addDebugInfo(List<String> param0, BlockPos param1, Climate.Sampler param2) {
    }
}
