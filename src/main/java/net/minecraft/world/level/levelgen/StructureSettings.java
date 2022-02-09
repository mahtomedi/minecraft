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
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class StructureSettings {
    public static final Codec<StructureSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.simpleMap(Registry.STRUCTURE_FEATURE.byNameCodec(), StructurePlacement.CODEC, Registry.STRUCTURE_FEATURE)
                        .forGetter(param0x -> param0x.structureConfig)
                )
                .apply(param0, StructureSettings::new)
    );
    public static final ImmutableMap<StructureFeature<?>, StructurePlacement> DEFAULTS = ImmutableMap.<StructureFeature<?>, StructurePlacement>builder()
        .put(StructureFeature.VILLAGE, new RandomSpreadStructurePlacement(34, 8, RandomSpreadType.LINEAR, 10387312))
        .put(StructureFeature.DESERT_PYRAMID, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357617))
        .put(StructureFeature.IGLOO, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357618))
        .put(StructureFeature.JUNGLE_TEMPLE, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357619))
        .put(StructureFeature.SWAMP_HUT, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 14357620))
        .put(StructureFeature.PILLAGER_OUTPOST, new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 165745296))
        .put(StructureFeature.OCEAN_MONUMENT, new RandomSpreadStructurePlacement(32, 5, RandomSpreadType.TRIANGULAR, 10387313))
        .put(StructureFeature.END_CITY, new RandomSpreadStructurePlacement(20, 11, RandomSpreadType.TRIANGULAR, 10387313))
        .put(StructureFeature.WOODLAND_MANSION, new RandomSpreadStructurePlacement(80, 20, RandomSpreadType.TRIANGULAR, 10387319))
        .put(StructureFeature.BURIED_TREASURE, new RandomSpreadStructurePlacement(1, 0, RandomSpreadType.LINEAR, 0, new Vec3i(9, 0, 9)))
        .put(StructureFeature.MINESHAFT, new RandomSpreadStructurePlacement(1, 0, RandomSpreadType.LINEAR, 0))
        .put(StructureFeature.RUINED_PORTAL, new RandomSpreadStructurePlacement(40, 15, RandomSpreadType.LINEAR, 34222645))
        .put(StructureFeature.SHIPWRECK, new RandomSpreadStructurePlacement(24, 4, RandomSpreadType.LINEAR, 165745295))
        .put(StructureFeature.OCEAN_RUIN, new RandomSpreadStructurePlacement(20, 8, RandomSpreadType.LINEAR, 14357621))
        .put(StructureFeature.BASTION_REMNANT, new RandomSpreadStructurePlacement(27, 4, RandomSpreadType.LINEAR, 30084232))
        .put(StructureFeature.NETHER_BRIDGE, new RandomSpreadStructurePlacement(27, 4, RandomSpreadType.LINEAR, 30084232))
        .put(StructureFeature.NETHER_FOSSIL, new RandomSpreadStructurePlacement(2, 1, RandomSpreadType.LINEAR, 14357921))
        .build();
    public static final ConcentricRingsStructurePlacement DEFAULT_STRONGHOLD = new ConcentricRingsStructurePlacement(32, 3, 128);
    public static final ImmutableMap<StructureFeature<?>, StructurePlacement> DEFAULTS_AND_STRONGHOLDS = ImmutableMap.<StructureFeature<?>, StructurePlacement>builder(
            
        )
        .putAll(DEFAULTS)
        .put(StructureFeature.STRONGHOLD, DEFAULT_STRONGHOLD)
        .build();
    private final Map<StructureFeature<?>, StructurePlacement> structureConfig;
    private final ImmutableMap<StructureFeature<?>, ImmutableMultimap<ResourceKey<ConfiguredStructureFeature<?, ?>>, ResourceKey<Biome>>> configuredStructures;

    public StructureSettings(Map<StructureFeature<?>, StructurePlacement> param0) {
        this.structureConfig = param0;
        HashMap<StructureFeature<?>, Builder<ResourceKey<ConfiguredStructureFeature<?, ?>>, ResourceKey<Biome>>> var0 = new HashMap<>();
        StructureFeatures.registerStructures(
            (param1, param2, param3) -> var0.computeIfAbsent(param1, param0x -> ImmutableMultimap.builder()).put(param2, param3)
        );
        this.configuredStructures = var0.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, param0x -> param0x.getValue().build()));
    }

    public StructureSettings(boolean param0) {
        this(Maps.newHashMap(param0 ? DEFAULTS_AND_STRONGHOLDS : DEFAULTS));
    }

    @VisibleForTesting
    public Map<StructureFeature<?>, StructurePlacement> structureConfig() {
        return this.structureConfig;
    }

    @Nullable
    public StructurePlacement getConfig(StructureFeature<?> param0) {
        return this.structureConfig.get(param0);
    }

    public ImmutableMultimap<ResourceKey<ConfiguredStructureFeature<?, ?>>, ResourceKey<Biome>> structures(StructureFeature<?> param0) {
        return this.configuredStructures.getOrDefault(param0, ImmutableMultimap.of());
    }
}
