package net.minecraft.data.worldgen;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

public class BiomeDefaultFeatures {
    public static void addDefaultOverworldLandMesaStructures(BiomeGenerationSettings.Builder param0) {
        param0.addStructureStart(StructureFeatures.MINESHAFT_MESA);
        param0.addStructureStart(StructureFeatures.STRONGHOLD);
    }

    public static void addDefaultOverworldLandStructures(BiomeGenerationSettings.Builder param0) {
        param0.addStructureStart(StructureFeatures.MINESHAFT);
        param0.addStructureStart(StructureFeatures.STRONGHOLD);
    }

    public static void addDefaultOverworldOceanStructures(BiomeGenerationSettings.Builder param0) {
        param0.addStructureStart(StructureFeatures.MINESHAFT);
        param0.addStructureStart(StructureFeatures.SHIPWRECK);
    }

    public static void addDefaultCarvers(BiomeGenerationSettings.Builder param0) {
        param0.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE);
        param0.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
    }

    public static void addOceanCarvers(BiomeGenerationSettings.Builder param0) {
        param0.addCarver(GenerationStep.Carving.AIR, Carvers.OCEAN_CAVE);
        param0.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
        param0.addCarver(GenerationStep.Carving.LIQUID, Carvers.UNDERWATER_CANYON);
        param0.addCarver(GenerationStep.Carving.LIQUID, Carvers.UNDERWATER_CAVE);
    }

    public static void addDefaultLakes(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_WATER);
        param0.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
    }

    public static void addDesertLakes(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
    }

    public static void addDefaultMonsterRoom(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.MONSTER_ROOM);
    }

    public static void addDefaultUndergroundVariety(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIRT);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GRAVEL);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GRANITE);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIORITE);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_ANDESITE);
    }

    public static void addDefaultOres(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_COAL);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_IRON);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_REDSTONE);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIAMOND);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_LAPIS);
    }

    public static void addExtraGold(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD_EXTRA);
    }

    public static void addExtraEmeralds(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_EMERALD);
    }

    public static void addInfestedStone(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_INFESTED);
    }

    public static void addDefaultSoftDisks(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_SAND);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_GRAVEL);
    }

    public static void addSwampClayDisk(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
    }

    public static void addMossyStoneBlock(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.FOREST_ROCK);
    }

    public static void addFerns(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_LARGE_FERN);
    }

    public static void addBerryBushes(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_DECORATED);
    }

    public static void addSparseBerryBushes(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_SPARSE);
    }

    public static void addLightBambooVegetation(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO_LIGHT);
    }

    public static void addBambooVegetation(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO_VEGETATION);
    }

    public static void addTaigaTrees(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TAIGA_VEGETATION);
    }

    public static void addWaterTrees(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_WATER);
    }

    public static void addBirchTrees(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_BIRCH);
    }

    public static void addOtherBirchTrees(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BIRCH_OTHER);
    }

    public static void addTallBirchTrees(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BIRCH_TALL);
    }

    public static void addSavannaTrees(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SAVANNA);
    }

    public static void addShatteredSavannaTrees(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SHATTERED_SAVANNA);
    }

    public static void addMountainTrees(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN);
    }

    public static void addMountainEdgeTrees(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN_EDGE);
    }

    public static void addJungleTrees(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE);
    }

    public static void addJungleEdgeTrees(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE_EDGE);
    }

    public static void addBadlandsTrees(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.OAK_BADLANDS);
    }

    public static void addSnowyTrees(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRUCE_SNOWY);
    }

    public static void addJungleGrass(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_JUNGLE);
    }

    public static void addSavannaGrass(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS);
    }

    public static void addShatteredSavannaGrass(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
    }

    public static void addSavannaExtraGrass(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_SAVANNA);
    }

    public static void addBadlandGrass(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_BADLANDS);
    }

    public static void addForestFlowers(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION);
    }

    public static void addForestGrass(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_FOREST);
    }

    public static void addSwampVegetation(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SWAMP_TREE);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_SWAMP);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_WATERLILLY);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_SWAMP);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_SWAMP);
    }

    public static void addMushroomFieldVegetation(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.MUSHROOM_FIELD_VEGETATION);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
    }

    public static void addPlainVegetation(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PLAIN_VEGETATION);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_PLAIN_DECORATED);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_PLAIN);
    }

    public static void addDesertVegetation(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_2);
    }

    public static void addGiantTaigaVegetation(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_GIANT);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_GIANT);
    }

    public static void addDefaultFlowers(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_DEFAULT);
    }

    public static void addWarmFlowers(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_WARM);
    }

    public static void addDefaultGrass(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
    }

    public static void addTaigaGrass(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA_2);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
    }

    public static void addPlainGrass(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS_2);
    }

    public static void addDefaultMushrooms(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_NORMAL);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_NORMAL);
    }

    public static void addDefaultExtraVegetation(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
    }

    public static void addBadlandExtraVegetation(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_BADLANDS);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DECORATED);
    }

    public static void addJungleExtraVegetation(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_MELON);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.VINES);
    }

    public static void addDesertExtraVegetation(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_DESERT);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DESERT);
    }

    public static void addSwampExtraVegetation(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_SWAMP);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
    }

    public static void addDesertExtraDecoration(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.WELL);
    }

    public static void addFossilDecoration(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.FOSSIL);
    }

    public static void addColdOceanExtraVegetation(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.KELP_COLD);
    }

    public static void addDefaultSeagrass(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SIMPLE);
    }

    public static void addLukeWarmKelp(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.KELP_WARM);
    }

    public static void addDefaultSprings(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_WATER);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
    }

    public static void addIcebergs(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_PACKED);
        param0.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_BLUE);
    }

    public static void addBlueIce(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.BLUE_ICE);
    }

    public static void addSurfaceFreezing(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Features.FREEZE_TOP_LAYER);
    }

    public static void addNetherDefaultOres(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GRAVEL_NETHER);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_BLACKSTONE);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_NETHER);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_NETHER);
        addAncientDebris(param0);
    }

    public static void addAncientDebris(BiomeGenerationSettings.Builder param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_LARGE);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_SMALL);
    }

    public static void farmAnimals(MobSpawnSettings.Builder param0) {
        param0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 12, 4, 4));
        param0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PIG, 10, 4, 4));
        param0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
        param0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 8, 4, 4));
    }

    public static void ambientSpawns(MobSpawnSettings.Builder param0) {
        param0.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.BAT, 10, 8, 8));
    }

    public static void commonSpawns(MobSpawnSettings.Builder param0) {
        ambientSpawns(param0);
        monsters(param0, 95, 5, 100);
    }

    public static void oceanSpawns(MobSpawnSettings.Builder param0, int param1, int param2, int param3) {
        param0.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, param1, 1, param2));
        param0.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.COD, param3, 3, 6));
        commonSpawns(param0);
        param0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
    }

    public static void warmOceanSpawns(MobSpawnSettings.Builder param0, int param1, int param2) {
        param0.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, param1, param2, 4));
        param0.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
        param0.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
        commonSpawns(param0);
    }

    public static void plainsSpawns(MobSpawnSettings.Builder param0) {
        farmAnimals(param0);
        param0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 5, 2, 6));
        param0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 3));
        commonSpawns(param0);
    }

    public static void snowySpawns(MobSpawnSettings.Builder param0) {
        param0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 10, 2, 3));
        param0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
        ambientSpawns(param0);
        monsters(param0, 95, 5, 20);
        param0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.STRAY, 80, 4, 4));
    }

    public static void desertSpawns(MobSpawnSettings.Builder param0) {
        param0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        ambientSpawns(param0);
        monsters(param0, 19, 1, 100);
        param0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HUSK, 80, 4, 4));
    }

    public static void monsters(MobSpawnSettings.Builder param0, int param1, int param2, int param3) {
        param0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 100, 4, 4));
        param0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, param1, 4, 4));
        param0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE_VILLAGER, param2, 1, 1));
        param0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, param3, 4, 4));
        param0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 100, 4, 4));
        param0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 100, 4, 4));
        param0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
        param0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 5, 1, 1));
    }

    public static void mooshroomSpawns(MobSpawnSettings.Builder param0) {
        param0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.MOOSHROOM, 8, 4, 8));
        ambientSpawns(param0);
    }

    public static void baseJungleSpawns(MobSpawnSettings.Builder param0) {
        farmAnimals(param0);
        param0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
        commonSpawns(param0);
    }

    public static void endSpawns(MobSpawnSettings.Builder param0) {
        param0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 4, 4));
    }
}
