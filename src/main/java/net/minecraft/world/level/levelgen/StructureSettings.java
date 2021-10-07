package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public class StructureSettings {
    public static final Codec<StructureSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    StrongholdConfiguration.CODEC.optionalFieldOf("stronghold").forGetter(param0x -> Optional.ofNullable(param0x.stronghold)),
                    Codec.simpleMap(Registry.STRUCTURE_FEATURE, StructureFeatureConfiguration.CODEC, Registry.STRUCTURE_FEATURE)
                        .fieldOf("structures")
                        .forGetter(param0x -> param0x.structureConfig)
                )
                .apply(param0, StructureSettings::new)
    );
    public static final ImmutableMap<StructureFeature<?>, StructureFeatureConfiguration> DEFAULTS = ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder(
            
        )
        .put(StructureFeature.VILLAGE, new StructureFeatureConfiguration(34, 8, 10387312))
        .put(StructureFeature.DESERT_PYRAMID, new StructureFeatureConfiguration(32, 8, 14357617))
        .put(StructureFeature.IGLOO, new StructureFeatureConfiguration(32, 8, 14357618))
        .put(StructureFeature.JUNGLE_TEMPLE, new StructureFeatureConfiguration(32, 8, 14357619))
        .put(StructureFeature.SWAMP_HUT, new StructureFeatureConfiguration(32, 8, 14357620))
        .put(StructureFeature.PILLAGER_OUTPOST, new StructureFeatureConfiguration(32, 8, 165745296))
        .put(StructureFeature.STRONGHOLD, new StructureFeatureConfiguration(1, 0, 0))
        .put(StructureFeature.OCEAN_MONUMENT, new StructureFeatureConfiguration(32, 5, 10387313))
        .put(StructureFeature.END_CITY, new StructureFeatureConfiguration(20, 11, 10387313))
        .put(StructureFeature.WOODLAND_MANSION, new StructureFeatureConfiguration(80, 20, 10387319))
        .put(StructureFeature.BURIED_TREASURE, new StructureFeatureConfiguration(1, 0, 0))
        .put(StructureFeature.MINESHAFT, new StructureFeatureConfiguration(1, 0, 0))
        .put(StructureFeature.RUINED_PORTAL, new StructureFeatureConfiguration(40, 15, 34222645))
        .put(StructureFeature.SHIPWRECK, new StructureFeatureConfiguration(24, 4, 165745295))
        .put(StructureFeature.OCEAN_RUIN, new StructureFeatureConfiguration(20, 8, 14357621))
        .put(StructureFeature.BASTION_REMNANT, new StructureFeatureConfiguration(27, 4, 30084232))
        .put(StructureFeature.NETHER_BRIDGE, new StructureFeatureConfiguration(27, 4, 30084232))
        .put(StructureFeature.NETHER_FOSSIL, new StructureFeatureConfiguration(2, 1, 14357921))
        .build();
    public static final StrongholdConfiguration DEFAULT_STRONGHOLD;
    private final Map<StructureFeature<?>, StructureFeatureConfiguration> structureConfig;
    private final ImmutableMap<StructureFeature<?>, ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> configuredStructures;
    @Nullable
    private final StrongholdConfiguration stronghold;

    private StructureSettings(Map<StructureFeature<?>, StructureFeatureConfiguration> param0, @Nullable StrongholdConfiguration param1) {
        this.stronghold = param1;
        this.structureConfig = param0;
        HashMap<StructureFeature<?>, Builder<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> var0 = new HashMap<>();
        StructureFeatures.registerStructures(
            (param1x, param2) -> var0.computeIfAbsent(param1x.feature, param0x -> ImmutableMultimap.builder()).put(param1x, param2)
        );
        this.configuredStructures = var0.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, param0x -> param0x.getValue().build()));
    }

    public StructureSettings(Optional<StrongholdConfiguration> param0, Map<StructureFeature<?>, StructureFeatureConfiguration> param1) {
        this(param1, param0.orElse(null));
    }

    public StructureSettings(boolean param0) {
        this(Maps.newHashMap(DEFAULTS), param0 ? DEFAULT_STRONGHOLD : null);
    }

    @VisibleForTesting
    public Map<StructureFeature<?>, StructureFeatureConfiguration> structureConfig() {
        return this.structureConfig;
    }

    @Nullable
    public StructureFeatureConfiguration getConfig(StructureFeature<?> param0) {
        return this.structureConfig.get(param0);
    }

    @Nullable
    public StrongholdConfiguration stronghold() {
        return this.stronghold;
    }

    public ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> structures(StructureFeature<?> param0) {
        return this.configuredStructures.getOrDefault(param0, ImmutableMultimap.of());
    }

    static {
        for(StructureFeature<?> var0 : Registry.STRUCTURE_FEATURE) {
            if (!DEFAULTS.containsKey(var0)) {
                throw new IllegalStateException("Structure feature without default settings: " + Registry.STRUCTURE_FEATURE.getKey(var0));
            }
        }

        DEFAULT_STRONGHOLD = new StrongholdConfiguration(32, 3, 128);
    }
}
