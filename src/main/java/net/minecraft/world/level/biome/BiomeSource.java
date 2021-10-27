package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
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
import org.apache.commons.lang3.mutable.MutableInt;

public abstract class BiomeSource {
    public static final Codec<BiomeSource> CODEC = Registry.BIOME_SOURCE.dispatchStable(BiomeSource::codec, Function.identity());
    private final List<Biome> possibleBiomes;
    private final ImmutableList<ImmutableList<ConfiguredFeature<?, ?>>> featuresPerStep;

    protected BiomeSource(Stream<Supplier<Biome>> param0) {
        this(param0.map(Supplier::get).distinct().collect(ImmutableList.toImmutableList()));
    }

    protected BiomeSource(List<Biome> param0) {
        this.possibleBiomes = param0;
        Object2IntMap<ConfiguredFeature<?, ?>> var0 = new Object2IntOpenHashMap<>();
        MutableInt var1 = new MutableInt(0);

        record FeatureData(int featureIndex, int step, ConfiguredFeature<?, ?> feature) {
        }

        Comparator<FeatureData> var2 = Comparator.comparingInt(FeatureData::step).thenComparingInt(FeatureData::featureIndex);
        Map<FeatureData, Set<FeatureData>> var3 = new TreeMap<>(var2);
        int var4 = 0;

        for(Biome var5 : param0) {
            List<FeatureData> var6 = Lists.newArrayList();
            List<List<Supplier<ConfiguredFeature<?, ?>>>> var7 = var5.getGenerationSettings().features();
            var4 = Math.max(var4, var7.size());

            for(int var8 = 0; var8 < var7.size(); ++var8) {
                for(Supplier<ConfiguredFeature<?, ?>> var9 : var7.get(var8)) {
                    ConfiguredFeature<?, ?> var10 = var9.get();
                    var6.add(new FeatureData(var0.computeIfAbsent(var10, param1 -> var1.getAndIncrement()), var8, var10));
                }
            }

            for(int var11 = 0; var11 < var6.size(); ++var11) {
                Set<FeatureData> var12 = var3.computeIfAbsent(var6.get(var11), param1 -> new TreeSet<>(var2));
                if (var11 < var6.size() - 1) {
                    var12.add(var6.get(var11 + 1));
                }
            }
        }

        Set<FeatureData> var13 = new TreeSet<>(var2);
        Set<FeatureData> var14 = new TreeSet<>(var2);
        List<FeatureData> var15 = Lists.newArrayList();

        for(FeatureData var16 : var3.keySet()) {
            if (!var14.isEmpty()) {
                throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
            }

            if (!var13.contains(var16) && Graph.depthFirstSearch(var3, var13, var14, var15::add, var16)) {
                Collections.reverse(var15);
                throw new IllegalStateException(
                    "Feature order cycle found: " + (String)var15.stream().filter(var14::contains).map(Object::toString).collect(Collectors.joining(", "))
                );
            }
        }

        Collections.reverse(var15);
        Builder<ImmutableList<ConfiguredFeature<?, ?>>> var17 = ImmutableList.builder();

        for(int var18 = 0; var18 < var4; ++var18) {
            int var19 = var18;
            var17.add(var15.stream().filter(param1 -> param1.step() == var19).map(FeatureData::feature).collect(ImmutableList.toImmutableList()));
        }

        this.featuresPerStep = var17.build();
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
