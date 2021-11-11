package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeGenerationSettings {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings(ImmutableMap.of(), ImmutableList.of());
    public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    Codec.simpleMap(
                            GenerationStep.Carving.CODEC,
                            ConfiguredWorldCarver.LIST_CODEC
                                .promotePartial(Util.prefix("Carver: ", LOGGER::error))
                                .flatXmap(ExtraCodecs.nonNullSupplierListCheck(), ExtraCodecs.nonNullSupplierListCheck()),
                            StringRepresentable.keys(GenerationStep.Carving.values())
                        )
                        .fieldOf("carvers")
                        .forGetter(param0x -> param0x.carvers),
                    PlacedFeature.LIST_CODEC
                        .promotePartial(Util.prefix("Feature: ", LOGGER::error))
                        .flatXmap(ExtraCodecs.nonNullSupplierListCheck(), ExtraCodecs.nonNullSupplierListCheck())
                        .listOf()
                        .fieldOf("features")
                        .forGetter(param0x -> param0x.features)
                )
                .apply(param0, BiomeGenerationSettings::new)
    );
    private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers;
    private final List<List<Supplier<PlacedFeature>>> features;
    private final List<ConfiguredFeature<?, ?>> flowerFeatures;
    private final Set<PlacedFeature> featureSet;

    BiomeGenerationSettings(Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> param0, List<List<Supplier<PlacedFeature>>> param1) {
        this.carvers = param0;
        this.features = param1;
        this.flowerFeatures = param1.stream()
            .flatMap(Collection::stream)
            .map(Supplier::get)
            .flatMap(PlacedFeature::getFeatures)
            .filter(param0x -> param0x.feature == Feature.FLOWER)
            .collect(ImmutableList.toImmutableList());
        this.featureSet = param1.stream().flatMap(Collection::stream).map(Supplier::get).collect(Collectors.toSet());
    }

    public List<Supplier<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving param0) {
        return this.carvers.getOrDefault(param0, ImmutableList.of());
    }

    public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
        return this.flowerFeatures;
    }

    public List<List<Supplier<PlacedFeature>>> features() {
        return this.features;
    }

    public boolean hasFeature(PlacedFeature param0) {
        return this.featureSet.contains(param0);
    }

    public static class Builder {
        private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers = Maps.newLinkedHashMap();
        private final List<List<Supplier<PlacedFeature>>> features = Lists.newArrayList();

        public BiomeGenerationSettings.Builder addFeature(GenerationStep.Decoration param0, PlacedFeature param1) {
            return this.addFeature(param0.ordinal(), () -> param1);
        }

        public BiomeGenerationSettings.Builder addFeature(int param0, Supplier<PlacedFeature> param1) {
            this.addFeatureStepsUpTo(param0);
            this.features.get(param0).add(param1);
            return this;
        }

        public <C extends CarverConfiguration> BiomeGenerationSettings.Builder addCarver(GenerationStep.Carving param0, ConfiguredWorldCarver<C> param1) {
            this.carvers.computeIfAbsent(param0, param0x -> Lists.newArrayList()).add(() -> param1);
            return this;
        }

        private void addFeatureStepsUpTo(int param0) {
            while(this.features.size() <= param0) {
                this.features.add(Lists.newArrayList());
            }

        }

        public BiomeGenerationSettings build() {
            return new BiomeGenerationSettings(
                this.carvers.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, param0 -> ImmutableList.copyOf(param0.getValue()))),
                this.features.stream().map(ImmutableList::copyOf).collect(ImmutableList.toImmutableList())
            );
        }
    }
}
