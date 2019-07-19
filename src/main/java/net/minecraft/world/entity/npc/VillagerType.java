package net.minecraft.world.entity.npc;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public interface VillagerType {
    VillagerType DESERT = register("desert");
    VillagerType JUNGLE = register("jungle");
    VillagerType PLAINS = register("plains");
    VillagerType SAVANNA = register("savanna");
    VillagerType SNOW = register("snow");
    VillagerType SWAMP = register("swamp");
    VillagerType TAIGA = register("taiga");
    Map<Biome, VillagerType> BY_BIOME = Util.make(Maps.newHashMap(), param0 -> {
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

    static VillagerType register(final String param0) {
        return Registry.register(Registry.VILLAGER_TYPE, new ResourceLocation(param0), new VillagerType() {
            @Override
            public String toString() {
                return param0;
            }
        });
    }

    static VillagerType byBiome(Biome param0) {
        return BY_BIOME.getOrDefault(param0, PLAINS);
    }
}
