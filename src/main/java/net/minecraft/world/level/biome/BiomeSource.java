package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Graph;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;

public abstract class BiomeSource implements BiomeResolver {
    public static final Codec<BiomeSource> CODEC = Registry.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
    private final Set<Holder<Biome>> possibleBiomes;
    private final Supplier<List<BiomeSource.StepFeatureData>> featuresPerStep;

    protected BiomeSource(Stream<Holder<Biome>> param0) {
        this(param0.distinct().toList());
    }

    protected BiomeSource(List<Holder<Biome>> param0) {
        this.possibleBiomes = new ObjectLinkedOpenHashSet<>(param0);
        this.featuresPerStep = Suppliers.memoize(() -> this.buildFeaturesPerStep(param0.stream().map(Holder::value).toList(), true));
    }

    private List<BiomeSource.StepFeatureData> buildFeaturesPerStep(List<Biome> param0, boolean param1) {
        Object2IntMap<PlacedFeature> var0 = new Object2IntOpenHashMap<>();
        MutableInt var1 = new MutableInt(0);

        record FeatureData(int featureIndex, int step, PlacedFeature feature) {
        }

        Comparator<FeatureData> var2 = Comparator.comparingInt(FeatureData::step).thenComparingInt(FeatureData::featureIndex);
        Map<FeatureData, Set<FeatureData>> var3 = new TreeMap<>(var2);
        int var4 = 0;

        for(Biome var5 : param0) {
            List<FeatureData> var6 = Lists.newArrayList();
            List<HolderSet<PlacedFeature>> var7 = var5.getGenerationSettings().features();
            var4 = Math.max(var4, var7.size());

            for(int var8 = 0; var8 < var7.size(); ++var8) {
                for(Holder<PlacedFeature> var9 : var7.get(var8)) {
                    PlacedFeature var10 = var9.value();
                    var6.add(new FeatureData(var0.computeIfAbsent(var10, param1x -> var1.getAndIncrement()), var8, var10));
                }
            }

            for(int var11 = 0; var11 < var6.size(); ++var11) {
                Set<FeatureData> var12 = var3.computeIfAbsent(var6.get(var11), param1x -> new TreeSet<>(var2));
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
                if (!param1) {
                    throw new IllegalStateException("Feature order cycle found");
                }

                List<Biome> var17 = new ArrayList<>(param0);

                int var18;
                do {
                    var18 = var17.size();
                    ListIterator<Biome> var19 = var17.listIterator();

                    while(var19.hasNext()) {
                        Biome var20 = var19.next();
                        var19.remove();

                        try {
                            this.buildFeaturesPerStep(var17, false);
                        } catch (IllegalStateException var18) {
                            continue;
                        }

                        var19.add(var20);
                    }
                } while(var18 != var17.size());

                throw new IllegalStateException("Feature order cycle found, involved biomes: " + var17);
            }
        }

        Collections.reverse(var15);
        Builder<BiomeSource.StepFeatureData> var22 = ImmutableList.builder();

        for(int var23 = 0; var23 < var4; ++var23) {
            int var24 = var23;
            List<PlacedFeature> var25 = var15.stream().filter(param1x -> param1x.step() == var24).map(FeatureData::feature).collect(Collectors.toList());
            int var26 = var25.size();
            Object2IntMap<PlacedFeature> var27 = new Object2IntOpenCustomHashMap<>(var26, Util.identityStrategy());

            for(int var28 = 0; var28 < var26; ++var28) {
                var27.put(var25.get(var28), var28);
            }

            var22.add(new BiomeSource.StepFeatureData(var25, var27));
        }

        return var22.build();
    }

    protected abstract Codec<? extends BiomeSource> codec();

    public abstract BiomeSource withSeed(long var1);

    public Set<Holder<Biome>> possibleBiomes() {
        return this.possibleBiomes;
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
        int param0, int param1, int param2, int param3, Predicate<Holder<Biome>> param4, Random param5, Climate.Sampler param6
    ) {
        return this.findBiomeHorizontal(param0, param1, param2, param3, 1, param4, param5, false, param6);
    }

    @Nullable
    public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(
        int param0, int param1, int param2, int param3, int param4, Predicate<Holder<Biome>> param5, Random param6, boolean param7, Climate.Sampler param8
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

    public List<BiomeSource.StepFeatureData> featuresPerStep() {
        return this.featuresPerStep.get();
    }

    static {
        Registry.register(Registry.BIOME_SOURCE, "fixed", FixedBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "multi_noise", MultiNoiseBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
        Registry.register(Registry.BIOME_SOURCE, "the_end", TheEndBiomeSource.CODEC);
    }

    public static record StepFeatureData(List<PlacedFeature> features, ToIntFunction<PlacedFeature> indexMapping) {
    }
}
