package net.minecraft.world.entity.npc;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.Holder;
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
        param0.put(Biomes.DESERT, DESERT);
        param0.put(Biomes.ERODED_BADLANDS, DESERT);
        param0.put(Biomes.WOODED_BADLANDS, DESERT);
        param0.put(Biomes.BAMBOO_JUNGLE, JUNGLE);
        param0.put(Biomes.JUNGLE, JUNGLE);
        param0.put(Biomes.SPARSE_JUNGLE, JUNGLE);
        param0.put(Biomes.SAVANNA_PLATEAU, SAVANNA);
        param0.put(Biomes.SAVANNA, SAVANNA);
        param0.put(Biomes.WINDSWEPT_SAVANNA, SAVANNA);
        param0.put(Biomes.DEEP_FROZEN_OCEAN, SNOW);
        param0.put(Biomes.FROZEN_OCEAN, SNOW);
        param0.put(Biomes.FROZEN_RIVER, SNOW);
        param0.put(Biomes.ICE_SPIKES, SNOW);
        param0.put(Biomes.SNOWY_BEACH, SNOW);
        param0.put(Biomes.SNOWY_TAIGA, SNOW);
        param0.put(Biomes.SNOWY_PLAINS, SNOW);
        param0.put(Biomes.GROVE, SNOW);
        param0.put(Biomes.SNOWY_SLOPES, SNOW);
        param0.put(Biomes.FROZEN_PEAKS, SNOW);
        param0.put(Biomes.JAGGED_PEAKS, SNOW);
        param0.put(Biomes.SWAMP, SWAMP);
        param0.put(Biomes.MANGROVE_SWAMP, SWAMP);
        param0.put(Biomes.OLD_GROWTH_SPRUCE_TAIGA, TAIGA);
        param0.put(Biomes.OLD_GROWTH_PINE_TAIGA, TAIGA);
        param0.put(Biomes.WINDSWEPT_GRAVELLY_HILLS, TAIGA);
        param0.put(Biomes.WINDSWEPT_HILLS, TAIGA);
        param0.put(Biomes.TAIGA, TAIGA);
        param0.put(Biomes.WINDSWEPT_FOREST, TAIGA);
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

    public static VillagerType byBiome(Holder<Biome> param0) {
        return param0.unwrapKey().map(BY_BIOME::get).orElse(PLAINS);
    }
}
