package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.Graph;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;

public class FeatureSorter {
    public static <T> List<FeatureSorter.StepFeatureData> buildFeaturesPerStep(
        List<T> param0, Function<T, List<HolderSet<PlacedFeature>>> param1, boolean param2
    ) {
        Object2IntMap<PlacedFeature> var0 = new Object2IntOpenHashMap<>();
        MutableInt var1 = new MutableInt(0);

        record FeatureData(int featureIndex, int step, PlacedFeature feature) {
        }

        Comparator<FeatureData> var2 = Comparator.comparingInt(FeatureData::step).thenComparingInt(FeatureData::featureIndex);
        Map<FeatureData, Set<FeatureData>> var3 = new TreeMap<>(var2);
        int var4 = 0;

        for(T var5 : param0) {
            List<FeatureData> var6 = Lists.newArrayList();
            List<HolderSet<PlacedFeature>> var7 = param1.apply(var5);
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
                if (!param2) {
                    throw new IllegalStateException("Feature order cycle found");
                }

                List<T> var17 = new ArrayList<>(param0);

                int var18;
                do {
                    var18 = var17.size();
                    ListIterator<T> var19 = var17.listIterator();

                    while(var19.hasNext()) {
                        T var20 = var19.next();
                        var19.remove();

                        try {
                            buildFeaturesPerStep(var17, param1, false);
                        } catch (IllegalStateException var18) {
                            continue;
                        }

                        var19.add(var20);
                    }
                } while(var18 != var17.size());

                throw new IllegalStateException("Feature order cycle found, involved sources: " + var17);
            }
        }

        Collections.reverse(var15);
        Builder<FeatureSorter.StepFeatureData> var22 = ImmutableList.builder();

        for(int var23 = 0; var23 < var4; ++var23) {
            int var24 = var23;
            List<PlacedFeature> var25 = var15.stream().filter(param1x -> param1x.step() == var24).map(FeatureData::feature).collect(Collectors.toList());
            var22.add(new FeatureSorter.StepFeatureData(var25));
        }

        return var22.build();
    }

    public static record StepFeatureData(List<PlacedFeature> features, ToIntFunction<PlacedFeature> indexMapping) {
        StepFeatureData(List<PlacedFeature> param0) {
            this(param0, Util.createIndexIdentityLookup(param0));
        }
    }
}
