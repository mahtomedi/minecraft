package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class MultiNoiseBiomeSourceParameterList {
    public static final Codec<MultiNoiseBiomeSourceParameterList> DIRECT_CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    MultiNoiseBiomeSourceParameterList.Preset.CODEC.fieldOf("preset").forGetter(param0x -> param0x.preset),
                    RegistryOps.retrieveGetter(Registries.BIOME)
                )
                .apply(param0, MultiNoiseBiomeSourceParameterList::new)
    );
    public static final Codec<Holder<MultiNoiseBiomeSourceParameterList>> CODEC = RegistryFileCodec.create(
        Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, DIRECT_CODEC
    );
    private final MultiNoiseBiomeSourceParameterList.Preset preset;
    private final Climate.ParameterList<Holder<Biome>> parameters;

    public MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset param0, HolderGetter<Biome> param1) {
        this.preset = param0;
        this.parameters = param0.provider.apply(param1::getOrThrow);
    }

    public Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters;
    }

    public static Map<MultiNoiseBiomeSourceParameterList.Preset, Climate.ParameterList<ResourceKey<Biome>>> knownPresets() {
        return MultiNoiseBiomeSourceParameterList.Preset.BY_NAME
            .values()
            .stream()
            .collect(
                Collectors.toMap(
                    (Function<? super MultiNoiseBiomeSourceParameterList.Preset, ? extends MultiNoiseBiomeSourceParameterList.Preset>)(param0 -> param0),
                    param0 -> param0.provider().apply(param0x -> param0x)
                )
            );
    }

    public static record Preset<T>(ResourceLocation id, MultiNoiseBiomeSourceParameterList.Preset.SourceProvider provider) {
        public static final MultiNoiseBiomeSourceParameterList.Preset NETHER = new MultiNoiseBiomeSourceParameterList.Preset(
            new ResourceLocation("nether"),
            new MultiNoiseBiomeSourceParameterList.Preset.SourceProvider() {
                @Override
                public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> param0) {
                    return new Climate.ParameterList<>(
                        List.of(
                            Pair.of(Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), param0.apply(Biomes.NETHER_WASTES)),
                            Pair.of(Climate.parameters(0.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), param0.apply(Biomes.SOUL_SAND_VALLEY)),
                            Pair.of(Climate.parameters(0.4F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), param0.apply(Biomes.CRIMSON_FOREST)),
                            Pair.of(Climate.parameters(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.375F), param0.apply(Biomes.WARPED_FOREST)),
                            Pair.of(Climate.parameters(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.175F), param0.apply(Biomes.BASALT_DELTAS))
                        )
                    );
                }
            }
        );
        public static final MultiNoiseBiomeSourceParameterList.Preset OVERWORLD = new MultiNoiseBiomeSourceParameterList.Preset(
            new ResourceLocation("overworld"), new MultiNoiseBiomeSourceParameterList.Preset.SourceProvider() {
                @Override
                public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> param0) {
                    return MultiNoiseBiomeSourceParameterList.Preset.generateOverworldBiomes(param0);
                }
            }
        );
        static final Map<ResourceLocation, MultiNoiseBiomeSourceParameterList.Preset> BY_NAME = Stream.of(NETHER, OVERWORLD)
            .collect(
                Collectors.toMap(
                    MultiNoiseBiomeSourceParameterList.Preset::id,
                    (Function<? super MultiNoiseBiomeSourceParameterList.Preset, ? extends MultiNoiseBiomeSourceParameterList.Preset>)(param0 -> param0)
                )
            );
        public static final Codec<MultiNoiseBiomeSourceParameterList.Preset> CODEC = ResourceLocation.CODEC
            .flatXmap(
                param0 -> Optional.ofNullable(BY_NAME.get(param0))
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Unknown preset: " + param0)),
                param0 -> DataResult.success(param0.id)
            );

        static <T> Climate.ParameterList<T> generateOverworldBiomes(Function<ResourceKey<Biome>, T> param0) {
            Builder<Pair<Climate.ParameterPoint, T>> var0 = ImmutableList.builder();
            new OverworldBiomeBuilder().addBiomes(param2 -> var0.add(param2.mapSecond(param0)));
            return new Climate.ParameterList<>(var0.build());
        }

        public Stream<ResourceKey<Biome>> usedBiomes() {
            return this.provider.apply(param0 -> param0).values().stream().map(Pair::getSecond).distinct();
        }

        @FunctionalInterface
        interface SourceProvider {
            <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> var1);
        }
    }
}
