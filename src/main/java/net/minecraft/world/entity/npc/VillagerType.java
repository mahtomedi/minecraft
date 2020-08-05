package net.minecraft.world.entity.npc;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class VillagerType {
    public static final VillagerType DESERT = register("desert");
    public static final VillagerType JUNGLE = register("jungle");
    public static final VillagerType PLAINS = register("plains");
    public static final VillagerType SAVANNA = register("savanna");
    public static final VillagerType SNOW = register("snow");
    public static final VillagerType SWAMP = register("swamp");
    public static final VillagerType TAIGA = register("taiga");
    private final String name;
    private static final Map<ResourceKey<Biome>, VillagerType> BY_BIOME = Util.make(Maps.newHashMap(), param0 -> {
        param0.put(Biomes.BADLANDS, DESERT);
        param0.put(Biomes.BADLANDS_PLATEAU, DESERT);
        param0.put(Biomes.DESERT, DESERT);
        param0.put(Biomes.DESERT_HILLS, DESERT);
        param0.put(Biomes.DESERT_LAKES, DESERT);
        param0.put(Biomes.ERODED_BADLANDS, DESERT);
        param0.put(Biomes.MODIFIED_BADLANDS_PLATEAU, DESERT);
        param0.put(Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU, DESERT);
        param0.put(Biomes.WOODED_BADLANDS_PLATEAU, DESERT);
        param0.put(Biomes.BAMBOO_JUNGLE, JUNGLE);
        param0.put(Biomes.BAMBOO_JUNGLE_HILLS, JUNGLE);
        param0.put(Biomes.JUNGLE, JUNGLE);
        param0.put(Biomes.JUNGLE_EDGE, JUNGLE);
        param0.put(Biomes.JUNGLE_HILLS, JUNGLE);
        param0.put(Biomes.MODIFIED_JUNGLE, JUNGLE);
        param0.put(Biomes.MODIFIED_JUNGLE_EDGE, JUNGLE);
        param0.put(Biomes.SAVANNA_PLATEAU, SAVANNA);
        param0.put(Biomes.SAVANNA, SAVANNA);
        param0.put(Biomes.SHATTERED_SAVANNA, SAVANNA);
        param0.put(Biomes.SHATTERED_SAVANNA_PLATEAU, SAVANNA);
        param0.put(Biomes.DEEP_FROZEN_OCEAN, SNOW);
        param0.put(Biomes.FROZEN_OCEAN, SNOW);
        param0.put(Biomes.FROZEN_RIVER, SNOW);
        param0.put(Biomes.ICE_SPIKES, SNOW);
        param0.put(Biomes.SNOWY_BEACH, SNOW);
        param0.put(Biomes.SNOWY_MOUNTAINS, SNOW);
        param0.put(Biomes.SNOWY_TAIGA, SNOW);
        param0.put(Biomes.SNOWY_TAIGA_HILLS, SNOW);
        param0.put(Biomes.SNOWY_TAIGA_MOUNTAINS, SNOW);
        param0.put(Biomes.SNOWY_TUNDRA, SNOW);
        param0.put(Biomes.SWAMP, SWAMP);
        param0.put(Biomes.SWAMP_HILLS, SWAMP);
        param0.put(Biomes.GIANT_SPRUCE_TAIGA, TAIGA);
        param0.put(Biomes.GIANT_SPRUCE_TAIGA_HILLS, TAIGA);
        param0.put(Biomes.GIANT_TREE_TAIGA, TAIGA);
        param0.put(Biomes.GIANT_TREE_TAIGA_HILLS, TAIGA);
        param0.put(Biomes.GRAVELLY_MOUNTAINS, TAIGA);
        param0.put(Biomes.MODIFIED_GRAVELLY_MOUNTAINS, TAIGA);
        param0.put(Biomes.MOUNTAIN_EDGE, TAIGA);
        param0.put(Biomes.MOUNTAINS, TAIGA);
        param0.put(Biomes.TAIGA, TAIGA);
        param0.put(Biomes.TAIGA_HILLS, TAIGA);
        param0.put(Biomes.TAIGA_MOUNTAINS, TAIGA);
        param0.put(Biomes.WOODED_MOUNTAINS, TAIGA);
    });

    private VillagerType(String param0) {
        this.name = param0;
    }

    @Override
    public String toString() {
        return this.name;
    }

    private static VillagerType register(String param0) {
        return Registry.register(Registry.VILLAGER_TYPE, new ResourceLocation(param0), new VillagerType(param0));
    }

    public static VillagerType byBiome(Optional<ResourceKey<Biome>> param0) {
        return param0.<VillagerType>flatMap(param0x -> Optional.ofNullable(BY_BIOME.get(param0x))).orElse(PLAINS);
    }
}
