package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BiomeGenerationSettings {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final BiomeGenerationSettings EMPTY = new BiomeGenerationSettings(
        () -> SurfaceBuilders.NOPE, ImmutableMap.of(), ImmutableList.of(), ImmutableList.of()
    );
    public static final MapCodec<BiomeGenerationSettings> CODEC = RecordCodecBuilder.mapCodec(
        param0 -> param0.group(
                    ConfiguredSurfaceBuilder.CODEC.fieldOf("surface_builder").forGetter(param0x -> param0x.surfaceBuilder),
                    Codec.simpleMap(
                            GenerationStep.Carving.CODEC,
                            ConfiguredWorldCarver.CODEC.listOf().promotePartial(Util.prefix("Carver: ", LOGGER::error)),
                            StringRepresentable.keys(GenerationStep.Carving.values())
                        )
                        .fieldOf("carvers")
                        .forGetter(param0x -> param0x.carvers),
                    ConfiguredFeature.CODEC
                        .listOf()
                        .promotePartial(Util.prefix("Feature: ", LOGGER::error))
                        .listOf()
                        .fieldOf("features")
                        .forGetter(param0x -> param0x.features),
                    ConfiguredStructureFeature.CODEC
                        .listOf()
                        .promotePartial(Util.prefix("Structure start: ", LOGGER::error))
                        .fieldOf("starts")
                        .forGetter(param0x -> param0x.structureStarts)
                )
                .apply(param0, BiomeGenerationSettings::new)
    );
    private final Supplier<ConfiguredSurfaceBuilder<?>> surfaceBuilder;
    private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers;
    private final List<List<Supplier<ConfiguredFeature<?, ?>>>> features;
    private final List<Supplier<ConfiguredStructureFeature<?, ?>>> structureStarts;
    private final List<ConfiguredFeature<?, ?>> flowerFeatures;

    private BiomeGenerationSettings(
        Supplier<ConfiguredSurfaceBuilder<?>> param0,
        Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> param1,
        List<List<Supplier<ConfiguredFeature<?, ?>>>> param2,
        List<Supplier<ConfiguredStructureFeature<?, ?>>> param3
    ) {
        this.surfaceBuilder = param0;
        this.carvers = param1;
        this.features = param2;
        this.structureStarts = param3;
        this.flowerFeatures = param2.stream()
            .flatMap(Collection::stream)
            .map(Supplier::get)
            .flatMap(ConfiguredFeature::getFeatures)
            .filter(param0x -> param0x.feature == Feature.FLOWER)
            .collect(ImmutableList.toImmutableList());
    }

    public List<Supplier<ConfiguredWorldCarver<?>>> getCarvers(GenerationStep.Carving param0) {
        return this.carvers.getOrDefault(param0, ImmutableList.of());
    }

    public boolean isValidStart(StructureFeature<?> param0) {
        return this.structureStarts.stream().anyMatch(param1 -> param1.get().feature == param0);
    }

    public Collection<Supplier<ConfiguredStructureFeature<?, ?>>> structures() {
        return this.structureStarts;
    }

    public ConfiguredStructureFeature<?, ?> withBiomeConfig(ConfiguredStructureFeature<?, ?> param0) {
        return DataFixUtils.orElse(this.structureStarts.stream().map(Supplier::get).filter(param1 -> param1.feature == param0.feature).findAny(), param0);
    }

    public List<ConfiguredFeature<?, ?>> getFlowerFeatures() {
        return this.flowerFeatures;
    }

    public List<List<Supplier<ConfiguredFeature<?, ?>>>> features() {
        return this.features;
    }

    public Supplier<ConfiguredSurfaceBuilder<?>> getSurfaceBuilder() {
        return this.surfaceBuilder;
    }

    public SurfaceBuilderConfiguration getSurfaceBuilderConfig() {
        return this.surfaceBuilder.get().config();
    }

    public static class Builder {
        private Optional<Supplier<ConfiguredSurfaceBuilder<?>>> surfaceBuilder = Optional.empty();
        private final Map<GenerationStep.Carving, List<Supplier<ConfiguredWorldCarver<?>>>> carvers = Maps.newLinkedHashMap();
        private final List<List<Supplier<ConfiguredFeature<?, ?>>>> features = Lists.newArrayList();
        private final List<Supplier<ConfiguredStructureFeature<?, ?>>> structureStarts = Lists.newArrayList();

        public BiomeGenerationSettings.Builder surfaceBuilder(ConfiguredSurfaceBuilder<?> param0) {
            return this.surfaceBuilder(() -> param0);
        }

        public BiomeGenerationSettings.Builder surfaceBuilder(Supplier<ConfiguredSurfaceBuilder<?>> param0) {
            this.surfaceBuilder = Optional.of(param0);
            return this;
        }

        public BiomeGenerationSettings.Builder addFeature(GenerationStep.Decoration param0, ConfiguredFeature<?, ?> param1) {
            return this.addFeature(param0.ordinal(), () -> param1);
        }

        public BiomeGenerationSettings.Builder addFeature(int param0, Supplier<ConfiguredFeature<?, ?>> param1) {
            this.addFeatureStepsUpTo(param0);
            this.features.get(param0).add(param1);
            return this;
        }

        public <C extends CarverConfiguration> BiomeGenerationSettings.Builder addCarver(GenerationStep.Carving param0, ConfiguredWorldCarver<C> param1) {
            this.carvers.computeIfAbsent(param0, param0x -> Lists.newArrayList()).add(() -> param1);
            return this;
        }

        public BiomeGenerationSettings.Builder addStructureStart(ConfiguredStructureFeature<?, ?> param0) {
            this.structureStarts.add(() -> param0);
            return this;
        }

        private void addFeatureStepsUpTo(int param0) {
            while(this.features.size() <= param0) {
                this.features.add(Lists.newArrayList());
            }

        }

        public BiomeGenerationSettings build() {
            return new BiomeGenerationSettings(
                this.surfaceBuilder.orElseThrow(() -> new IllegalStateException("Missing surface builder")),
                this.carvers
                    .entrySet()
                    .stream()
                    .collect(ImmutableMap.toImmutableMap(Entry::getKey, param0 -> ImmutableList.copyOf((Collection)param0.getValue()))),
                this.features.stream().map(ImmutableList::copyOf).collect(ImmutableList.toImmutableList()),
                ImmutableList.copyOf(this.structureStarts)
            );
        }
    }
}
