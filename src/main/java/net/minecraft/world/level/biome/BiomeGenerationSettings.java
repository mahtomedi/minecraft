package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.slf4j.Logger;

public class BiomeGenerationSettings {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings(ImmutableMap.of(), ImmutableList.of());
    public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    Codec.simpleMap(
                            GenerationStep.Carving.CODEC,
                            ConfiguredWorldCarver.LIST_CODEC.promotePartial(Util.prefix("Carver: ", LOGGER::error)),
                            StringRepresentable.keys(GenerationStep.Carving.values())
                        )
                        .fieldOf("carvers")
                        .forGetter(param0x -> param0x.carvers),
                    PlacedFeature.LIST_OF_LISTS_CODEC
                        .promotePartial(Util.prefix("Features: ", LOGGER::error))
                        .fieldOf("features")
                        .forGetter(param0x -> param0x.features)
                )
                .apply(param0, BiomeGenerationSettings::new)
    );
    private final Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>> carvers;
    private final List<HolderSet<PlacedFeature>> features;
    private final Supplier<List<ConfiguredFeature<?, ?>>> flowerFeatures;
    private final Supplier<Set<PlacedFeature>> featureSet;

    BiomeGenerationSettings(Map<GenerationStep.Carving, HolderSet<ConfiguredWorldCarver<?>>> param0, List<HolderSet<PlacedFeature>> param1) {
        this.carvers = param0;
        this.features = param1;
        this.flowerFeatures = Suppliers.memoize(
            () -> param1.stream()
                    .flatMap(HolderSet::stream)
                    .map(Holder::value)
                    .flatMap(PlacedFeature::getFeatures)
                    .filter(param0x -> param0x.feature() == Feature.FLOWER)
                    .collect(ImmutableList.toImmutableList())
        );
        this.featureSet = Suppliers.memoize(() -> param1.stream().flatMap(HolderSet::stream).map(Holder::value).collect(Collectors.toSet()));
    }

    public Iterable<Holder<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving param0) {
        return Objects.requireNonNullElseGet(this.carvers.get(param0), List::of);
    }

    public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
        return this.flowerFeatures.get();
    }

    public List<HolderSet<PlacedFeature>> features() {
        return this.features;
    }

    public boolean hasFeature(PlacedFeature param0) {
        return this.featureSet.get().contains(param0);
    }

    public static class Builder {
        private final Map<GenerationStep.Carving, List<Holder<ConfiguredWorldCarver<?>>>> carvers = Maps.newLinkedHashMap();
        private final List<List<Holder<PlacedFeature>>> features = Lists.newArrayList();

        public BiomeGenerationSettings.Builder addFeature(GenerationStep.Decoration param0, Holder<PlacedFeature> param1) {
            return this.addFeature(param0.ordinal(), param1);
        }

        public BiomeGenerationSettings.Builder addFeature(int param0, Holder<PlacedFeature> param1) {
            this.addFeatureStepsUpTo(param0);
            this.features.get(param0).add(param1);
            return this;
        }

        public BiomeGenerationSettings.Builder addCarver(GenerationStep.Carving param0, Holder<? extends ConfiguredWorldCarver<?>> param1) {
            this.carvers.computeIfAbsent(param0, param0x -> Lists.newArrayList()).add(Holder.hackyErase(param1));
            return this;
        }

        private void addFeatureStepsUpTo(int param0) {
            while(this.features.size() <= param0) {
                this.features.add(Lists.newArrayList());
            }

        }

        public BiomeGenerationSettings build() {
            return new BiomeGenerationSettings(
                this.carvers.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, param0 -> HolderSet.direct(param0.getValue()))),
                this.features.stream().map(HolderSet::direct).collect(ImmutableList.toImmutableList())
            );
        }
    }
}
