package net.minecraft.data.worldgen;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;

public class BiomeDefaultFeatures {
    public static void addDefaultOverworldLandMesaStructures(Biome param0) {
        param0.addStructureStart(StructureFeatures.MINESHAFT_MESA);
        param0.addStructureStart(StructureFeatures.STRONGHOLD);
    }

    public static void addDefaultOverworldLandStructures(Biome param0) {
        param0.addStructureStart(StructureFeatures.MINESHAFT);
        param0.addStructureStart(StructureFeatures.STRONGHOLD);
    }

    public static void addDefaultOverworldOceanStructures(Biome param0) {
        param0.addStructureStart(StructureFeatures.MINESHAFT);
        param0.addStructureStart(StructureFeatures.SHIPWRECK);
    }

    public static void addDefaultCarvers(Biome param0) {
        param0.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE);
        param0.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
    }

    public static void addOceanCarvers(Biome param0) {
        param0.addCarver(GenerationStep.Carving.AIR, Carvers.OCEAN_CAVE);
        param0.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
        param0.addCarver(GenerationStep.Carving.LIQUID, Carvers.UNDERWATER_CANYON);
        param0.addCarver(GenerationStep.Carving.LIQUID, Carvers.UNDERWATER_CAVE);
    }

    public static void addDefaultLakes(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_WATER);
        param0.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
    }

    public static void addDesertLakes(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
    }

    public static void addDefaultMonsterRoom(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.MONSTER_ROOM);
    }

    public static void addDefaultUndergroundVariety(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIRT);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GRAVEL);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GRANITE);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIORITE);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_ANDESITE);
    }

    public static void addDefaultOres(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_COAL);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_IRON);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_REDSTONE);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIAMOND);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_LAPIS);
    }

    public static void addExtraGold(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD_EXTRA);
    }

    public static void addExtraEmeralds(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_EMERALD);
    }

    public static void addInfestedStone(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_INFESTED);
    }

    public static void addDefaultSoftDisks(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_SAND);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_GRAVEL);
    }

    public static void addSwampClayDisk(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
    }

    public static void addMossyStoneBlock(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.FOREST_ROCK);
    }

    public static void addFerns(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_LARGE_FERN);
    }

    public static void addBerryBushes(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_DECORATED);
    }

    public static void addSparseBerryBushes(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_SPARSE);
    }

    public static void addLightBambooVegetation(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO_LIGHT);
    }

    public static void addBambooVegetation(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO_VEGETATION);
    }

    public static void addTaigaTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TAIGA_VEGETATION);
    }

    public static void addWaterTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_WATER);
    }

    public static void addBirchTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BIRCH_BEES_0002);
    }

    public static void addOtherBirchTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BIRCH_OTHER);
    }

    public static void addTallBirchTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BIRCH_TALL);
    }

    public static void addSavannaTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SAVANNA);
    }

    public static void addShatteredSavannaTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SHATTERED_SAVANNA);
    }

    public static void addMountainTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN);
    }

    public static void addMountainEdgeTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN_EDGE);
    }

    public static void addJungleTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE);
    }

    public static void addJungleEdgeTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE_EDGE);
    }

    public static void addBadlandsTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.OAK_BADLANDS);
    }

    public static void addSnowyTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRUCE_SNOWY);
    }

    public static void addGiantSpruceTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_GIANT_SPRUCE);
    }

    public static void addGiantTrees(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_GIANT);
    }

    public static void addJungleGrass(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_JUNGLE);
    }

    public static void addSavannaGrass(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS);
    }

    public static void addShatteredSavannaGrass(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
    }

    public static void addSavannaExtraGrass(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_SAVANNA);
    }

    public static void addBadlandGrass(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_BADLANDS);
    }

    public static void addForestFlowers(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION);
    }

    public static void addForestGrass(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_FOREST);
    }

    public static void addSwampVegetation(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SWAMP_TREE);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_SWAMP);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_WATERLILLY);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_SWAMP);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_SWAMP);
    }

    public static void addMushroomFieldVegetation(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.MUSHROOM_FIELD_VEGETATION);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
    }

    public static void addPlainVegetation(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PLAIN_VEGETATION);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_PLAIN_DECORATED);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_PLAIN);
    }

    public static void addDesertVegetation(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_2);
    }

    public static void addGiantTaigaVegetation(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_GIANT);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_GIANT);
    }

    public static void addDefaultFlowers(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_DEFAULT);
    }

    public static void addWarmFlowers(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_WARM);
    }

    public static void addDefaultGrass(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
    }

    public static void addTaigaGrass(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA_2);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
    }

    public static void addPlainGrass(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS_2);
    }

    public static void addDefaultMushrooms(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_NORMAL);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_NORMAL);
    }

    public static void addDefaultExtraVegetation(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
    }

    public static void addBadlandExtraVegetation(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_BADLANDS);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DECORATED);
    }

    public static void addJungleExtraVegetation(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_MELON);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.VINES);
    }

    public static void addDesertExtraVegetation(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_DESERT);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DESERT);
    }

    public static void addSwampExtraVegetation(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_SWAMP);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
    }

    public static void addDesertExtraDecoration(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.WELL);
    }

    public static void addFossilDecoration(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.FOSSIL);
    }

    public static void addColdOceanExtraVegetation(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.KELP_COLD);
    }

    public static void addDefaultSeagrass(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SIMPLE);
    }

    public static void addLukeWarmKelp(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.KELP_WARM);
    }

    public static void addDefaultSprings(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_WATER);
        param0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
    }

    public static void addIcebergs(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_PACKED);
        param0.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_BLUE);
    }

    public static void addBlueIce(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.BLUE_ICE);
    }

    public static void addSurfaceFreezing(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Features.FREEZE_TOP_LAYER);
    }

    public static void addNetherDefaultOres(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GRAVEL_NETHER);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_BLACKSTONE);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_NETHER);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_NETHER);
        addAncientDebris(param0);
    }

    public static void addAncientDebris(Biome param0) {
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_LARGE);
        param0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_SMALL);
    }

    public static void farmAnimals(Biome param0) {
        param0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.SHEEP, 12, 4, 4));
        param0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PIG, 10, 4, 4));
        param0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
        param0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.COW, 8, 4, 4));
    }

    private static void ambientSpawns(Biome param0) {
        param0.addSpawn(MobCategory.AMBIENT, new Biome.SpawnerData(EntityType.BAT, 10, 8, 8));
    }

    public static void commonSpawns(Biome param0) {
        ambientSpawns(param0);
        monsters(param0, 95, 5, 100);
    }

    public static void snowySpawns(Biome param0) {
        param0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.RABBIT, 10, 2, 3));
        param0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
        ambientSpawns(param0);
        monsters(param0, 95, 5, 20);
        param0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.STRAY, 80, 4, 4));
    }

    public static void desertSpawns(Biome param0) {
        param0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        ambientSpawns(param0);
        monsters(param0, 19, 1, 100);
        param0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.HUSK, 80, 4, 4));
    }

    public static void giantTaigaSpawns(Biome param0) {
        farmAnimals(param0);
        param0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.WOLF, 8, 4, 4));
        param0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        param0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.FOX, 8, 2, 4));
        ambientSpawns(param0);
        monsters(param0, 100, 25, 100);
    }

    private static void monsters(Biome param0, int param1, int param2, int param3) {
        param0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SPIDER, 100, 4, 4));
        param0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE, param1, 4, 4));
        param0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE_VILLAGER, param2, 1, 1));
        param0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SKELETON, param3, 4, 4));
        param0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.CREEPER, 100, 4, 4));
        param0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 100, 4, 4));
        param0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
        param0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.WITCH, 5, 1, 1));
    }

    public static void mooshroomSpawns(Biome param0) {
        param0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.MOOSHROOM, 8, 4, 8));
        ambientSpawns(param0);
    }

    public static void baseJungleSpawns(Biome param0) {
        farmAnimals(param0);
        param0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
        commonSpawns(param0);
    }

    public static void endSpawns(Biome param0) {
        param0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 10, 4, 4));
    }
}
