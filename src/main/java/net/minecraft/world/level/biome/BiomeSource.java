package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Graph;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public abstract class BiomeSource {
    public static final Codec<BiomeSource> CODEC = Registry.BIOME_SOURCE.dispatchStable(BiomeSource::codec, Function.identity());
    private final List<Biome> possibleBiomes;
    private final ImmutableList<ImmutableList<ConfiguredFeature<?, ?>>> featuresPerStep;

    protected BiomeSource(Stream<Supplier<Biome>> param0) {
        this(param0.map(Supplier::get).collect(ImmutableList.toImmutableList()));
    }

    protected BiomeSource(List<Biome> param0) {
        this.possibleBiomes = param0;

        record FeatureData(int step, ConfiguredFeature<?, ?> feature) {
        }

        Map<FeatureData, Set<FeatureData>> var0 = Maps.newHashMap();
        int var1 = 0;

        for(Biome var2 : param0) {
            List<FeatureData> var3 = Lists.newArrayList();
            List<List<Supplier<ConfiguredFeature<?, ?>>>> var4 = var2.getGenerationSettings().features();
            var1 = Math.max(var1, var4.size());

            for(int var5 = 0; var5 < var4.size(); ++var5) {
                for(Supplier<ConfiguredFeature<?, ?>> var6 : var4.get(var5)) {
                    var3.add(new FeatureData(var5, var6.get()));
                }
            }

            for(int var7 = 0; var7 < var3.size(); ++var7) {
                Set<FeatureData> var8 = var0.computeIfAbsent(var3.get(var7), param0x -> Sets.newHashSet());
                if (var7 < var3.size() - 1) {
                    var8.add(var3.get(var7 + 1));
                }
            }
        }

        Set<FeatureData> var9 = Sets.newHashSet();
        Set<FeatureData> var10 = Sets.newHashSet();
        List<FeatureData> var11 = Lists.newArrayList();

        for(FeatureData var12 : var0.keySet()) {
            if (!var10.isEmpty()) {
                throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
            }

            if (!var9.contains(var12) && Graph.depthFirstSearch(var0, var9, var10, var11::add, var12)) {
                Collections.reverse(var11);
                throw new IllegalStateException(
                    "Feature order cycle found: " + (String)var11.stream().filter(var10::contains).map(Object::toString).collect(Collectors.joining(", "))
                );
            }
        }

        Collections.reverse(var11);
        Builder<ImmutableList<ConfiguredFeature<?, ?>>> var13 = ImmutableList.builder();

        for(int var14 = 0; var14 < var1; ++var14) {
            int var15 = var14;
            var13.add(var11.stream().filter(param1 -> param1.step() == var15).map(FeatureData::feature).collect(ImmutableList.toImmutableList()));
        }

        this.featuresPerStep = var13.build();
    }

    protected abstract Codec<? extends BiomeSource> codec();

    public abstract BiomeSource withSeed(long var1);

    public List<Biome> possibleBiomes() {
        return this.possibleBiomes;
    }

    public Set<Biome> getBiomesWithin(int param0, int param1, int param2, int param3, Climate.Sampler param4) {
        int var0 = QuartPos.fromBlock(param0 - param3);
        int var1 = QuartPos.fromBlock(param1 - param3);
        int var2 = QuartPos.fromBlock(param2 - param3);
        int var3 = QuartPos.fromBlock(param0 + param3);
        int var4 = QuartPos.fromBlock(param1 + param3);
        int var5 = QuartPos.fromBlock(param2 + param3);
        int var6 = var3 - var0 + 1;
        int var7 = var4 - var1 + 1;
        int var8 = var5 - var2 + 1;
        Set<Biome> var9 = Sets.newHashSet();

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
    public BlockPos findBiomeHorizontal(int param0, int param1, int param2, int param3, Predicate<Biome> param4, Random param5, Climate.Sampler param6) {
        return this.findBiomeHorizontal(param0, param1, param2, param3, 1, param4, param5, false, param6);
    }

    @Nullable
    public BlockPos findBiomeHorizontal(
        int param0, int param1, int param2, int param3, int param4, Predicate<Biome> param5, Random param6, boolean param7, Climate.Sampler param8
    ) {
        int var0 = QuartPos.fromBlock(param0);
        int var1 = QuartPos.fromBlock(param2);
        int var2 = QuartPos.fromBlock(param3);
        int var3 = QuartPos.fromBlock(param1);
        BlockPos var4 = null;
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
                    if (param5.test(this.getNoiseBiome(var12, var3, var13, param8))) {
                        if (var4 == null || param6.nextInt(var5 + 1) == 0) {
                            var4 = new BlockPos(QuartPos.toBlock(var12), param1, QuartPos.toBlock(var13));
                            if (param7) {
                                return var4;
                            }
                        }

                        ++var5;
                    }
                }
            }
        }

        return var4;
    }

    public abstract Biome getNoiseBiome(int var1, int var2, int var3, Climate.Sampler var4);

    public void addMultinoiseDebugInfo(List<String> param0, BlockPos param1, Climate.Sampler param2) {
    }

    public ImmutableList<ImmutableList<ConfiguredFeature<?, ?>>> featuresPerStep() {
        return this.featuresPerStep;
    }

    static {
        Registry.register(Registry.BIOME_SOURCE, "fixed", FixedBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "multi_noise", MultiNoiseBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "the_end", TheEndBiomeSource.CODEC);
    }
}
