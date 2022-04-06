package net.minecraft.data.worldgen.biome;

import javax.annotation.Nullable;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

public class OverworldBiomes {
    protected static final int NORMAL_WATER_COLOR = 4159204;
    protected static final int NORMAL_WATER_FOG_COLOR = 329011;
    private static final int OVERWORLD_FOG_COLOR = 12638463;
    @Nullable
    private static final Music NORMAL_MUSIC = null;

    protected static int calculateSkyColor(float param0) {
        float var0 = param0 / 3.0F;
        var0 = Mth.clamp(var0, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - var0 * 0.05F, 0.5F + var0 * 0.1F, 1.0F);
    }

    private static Biome biome(
        Biome.Precipitation param0, float param1, float param2, MobSpawnSettings.Builder param3, BiomeGenerationSettings.Builder param4, @Nullable Music param5
    ) {
        return biome(param0, param1, param2, 4159204, 329011, param3, param4, param5);
    }

    private static Biome biome(
        Biome.Precipitation param0,
        float param1,
        float param2,
        int param3,
        int param4,
        MobSpawnSettings.Builder param5,
        BiomeGenerationSettings.Builder param6,
        @Nullable Music param7
    ) {
        return new Biome.BiomeBuilder()
            .precipitation(param0)
            .temperature(param1)
            .downfall(param2)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(param3)
                    .waterFogColor(param4)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(param1))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .backgroundMusic(param7)
                    .build()
            )
            .mobSpawnSettings(param5.build())
            .generationSettings(param6.build())
            .build();
    }

    private static void globalOverworldGeneration(BiomeGenerationSettings.Builder param0) {
        BiomeDefaultFeatures.addDefaultCarversAndLakes(param0);
        BiomeDefaultFeatures.addDefaultCrystalFormations(param0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(param0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(param0);
        BiomeDefaultFeatures.addDefaultSprings(param0);
        BiomeDefaultFeatures.addSurfaceFreezing(param0);
    }

    public static Biome oldGrowthTaiga(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var0);
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4));
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
        if (param0) {
            BiomeDefaultFeatures.commonSpawns(var0);
        } else {
            BiomeDefaultFeatures.caveSpawns(var0);
            BiomeDefaultFeatures.monsters(var0, 100, 25, 100, false);
        }

        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var1);
        BiomeDefaultFeatures.addMossyStoneBlock(var1);
        BiomeDefaultFeatures.addFerns(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        var1.addFeature(
            GenerationStep.Decoration.VEGETAL_DECORATION,
            param0 ? VegetationPlacements.TREES_OLD_GROWTH_SPRUCE_TAIGA : VegetationPlacements.TREES_OLD_GROWTH_PINE_TAIGA
        );
        BiomeDefaultFeatures.addDefaultFlowers(var1);
        BiomeDefaultFeatures.addGiantTaigaVegetation(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        BiomeDefaultFeatures.addCommonBerryBushes(var1);
        return biome(Biome.Precipitation.RAIN, param0 ? 0.25F : 0.3F, 0.8F, var0, var1, NORMAL_MUSIC);
    }

    public static Biome sparseJungle() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(var0);
        return baseJungle(0.8F, false, true, false, var0);
    }

    public static Biome jungle() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(var0);
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 40, 1, 2))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 3))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 1, 1, 2));
        return baseJungle(0.9F, false, false, true, var0);
    }

    public static Biome bambooJungle() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(var0);
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 40, 1, 2))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 80, 1, 2))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 1));
        return baseJungle(0.9F, true, false, true, var0);
    }

    private static Biome baseJungle(float param0, boolean param1, boolean param2, boolean param3, MobSpawnSettings.Builder param4) {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        if (param1) {
            BiomeDefaultFeatures.addBambooVegetation(var0);
        } else {
            if (param3) {
                BiomeDefaultFeatures.addLightBambooVegetation(var0);
            }

            if (param2) {
                BiomeDefaultFeatures.addSparseJungleTrees(var0);
            } else {
                BiomeDefaultFeatures.addJungleTrees(var0);
            }
        }

        BiomeDefaultFeatures.addWarmFlowers(var0);
        BiomeDefaultFeatures.addJungleGrass(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addJungleVines(var0);
        if (param2) {
            BiomeDefaultFeatures.addSparseJungleMelons(var0);
        } else {
            BiomeDefaultFeatures.addJungleMelons(var0);
        }

        return biome(Biome.Precipitation.RAIN, 0.95F, param0, param4, var0, NORMAL_MUSIC);
    }

    public static Biome windsweptHills(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var0);
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 5, 4, 6));
        BiomeDefaultFeatures.commonSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        if (param0) {
            BiomeDefaultFeatures.addMountainForestTrees(var1);
        } else {
            BiomeDefaultFeatures.addMountainTrees(var1);
        }

        BiomeDefaultFeatures.addDefaultFlowers(var1);
        BiomeDefaultFeatures.addDefaultGrass(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        BiomeDefaultFeatures.addExtraEmeralds(var1);
        BiomeDefaultFeatures.addInfestedStone(var1);
        return biome(Biome.Precipitation.RAIN, 0.2F, 0.3F, var0, var1, NORMAL_MUSIC);
    }

    public static Biome desert() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.desertSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder();
        BiomeDefaultFeatures.addFossilDecoration(var1);
        globalOverworldGeneration(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addDefaultFlowers(var1);
        BiomeDefaultFeatures.addDefaultGrass(var1);
        BiomeDefaultFeatures.addDesertVegetation(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDesertExtraVegetation(var1);
        BiomeDefaultFeatures.addDesertExtraDecoration(var1);
        return biome(Biome.Precipitation.NONE, 2.0F, 0.0F, var0, var1, NORMAL_MUSIC);
    }

    public static Biome plains(boolean param0, boolean param1, boolean param2) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var1);
        if (param1) {
            var0.creatureGenerationProbability(0.07F);
            BiomeDefaultFeatures.snowySpawns(var0);
            if (param2) {
                var1.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_SPIKE);
                var1.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_PATCH);
            }
        } else {
            BiomeDefaultFeatures.plainsSpawns(var0);
            BiomeDefaultFeatures.addPlainGrass(var1);
            if (param0) {
                var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUNFLOWER);
            }
        }

        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        if (param1) {
            BiomeDefaultFeatures.addSnowyTrees(var1);
            BiomeDefaultFeatures.addDefaultFlowers(var1);
            BiomeDefaultFeatures.addDefaultGrass(var1);
        } else {
            BiomeDefaultFeatures.addPlainVegetation(var1);
        }

        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        if (param0) {
            var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUGAR_CANE);
            var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_PUMPKIN);
        } else {
            BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        }

        float var2 = param1 ? 0.0F : 0.8F;
        return biome(param1 ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN, var2, param1 ? 0.5F : 0.4F, var0, var1, NORMAL_MUSIC);
    }

    public static Biome mushroomFields() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.mooshroomSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addMushroomFieldVegetation(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        return biome(Biome.Precipitation.RAIN, 0.9F, 1.0F, var0, var1, NORMAL_MUSIC);
    }

    public static Biome savanna(boolean param0, boolean param1) {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var0);
        if (!param0) {
            BiomeDefaultFeatures.addSavannaGrass(var0);
        }

        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        if (param0) {
            BiomeDefaultFeatures.addShatteredSavannaTrees(var0);
            BiomeDefaultFeatures.addDefaultFlowers(var0);
            BiomeDefaultFeatures.addShatteredSavannaGrass(var0);
        } else {
            BiomeDefaultFeatures.addSavannaTrees(var0);
            BiomeDefaultFeatures.addWarmFlowers(var0);
            BiomeDefaultFeatures.addSavannaExtraGrass(var0);
        }

        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var1);
        var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 1, 2, 6))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 1));
        BiomeDefaultFeatures.commonSpawns(var1);
        if (param1) {
            var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 8, 4, 4));
        }

        return biome(Biome.Precipitation.NONE, 2.0F, 0.0F, var1, var0, NORMAL_MUSIC);
    }

    public static Biome badlands(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addExtraGold(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        if (param0) {
            BiomeDefaultFeatures.addBadlandsTrees(var1);
        }

        BiomeDefaultFeatures.addBadlandGrass(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addBadlandExtraVegetation(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.NONE)
            .temperature(2.0F)
            .downfall(0.0F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(2.0F))
                    .foliageColorOverride(10387789)
                    .grassColorOverride(9470285)
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    private static Biome baseOcean(MobSpawnSettings.Builder param0, int param1, int param2, BiomeGenerationSettings.Builder param3) {
        return biome(Biome.Precipitation.RAIN, 0.5F, 0.5F, param1, param2, param0, param3, NORMAL_MUSIC);
    }

    private static BiomeGenerationSettings.Builder baseOceanGeneration() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addWaterTrees(var0);
        BiomeDefaultFeatures.addDefaultFlowers(var0);
        BiomeDefaultFeatures.addDefaultGrass(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        return var0;
    }

    public static Biome coldOcean(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.oceanSpawns(var0, 3, 4, 15);
        var0.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5));
        BiomeGenerationSettings.Builder var1 = baseOceanGeneration();
        var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, param0 ? AquaticPlacements.SEAGRASS_DEEP_COLD : AquaticPlacements.SEAGRASS_COLD);
        BiomeDefaultFeatures.addDefaultSeagrass(var1);
        BiomeDefaultFeatures.addColdOceanExtraVegetation(var1);
        return baseOcean(var0, 4020182, 329011, var1);
    }

    public static Biome ocean(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.oceanSpawns(var0, 1, 4, 10);
        var0.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 1, 1, 2));
        BiomeGenerationSettings.Builder var1 = baseOceanGeneration();
        var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, param0 ? AquaticPlacements.SEAGRASS_DEEP : AquaticPlacements.SEAGRASS_NORMAL);
        BiomeDefaultFeatures.addDefaultSeagrass(var1);
        BiomeDefaultFeatures.addColdOceanExtraVegetation(var1);
        return baseOcean(var0, 4159204, 329011, var1);
    }

    public static Biome lukeWarmOcean(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        if (param0) {
            BiomeDefaultFeatures.oceanSpawns(var0, 8, 4, 8);
        } else {
            BiomeDefaultFeatures.oceanSpawns(var0, 10, 2, 15);
        }

        var0.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 5, 1, 3))
            .addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8))
            .addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
        BiomeGenerationSettings.Builder var1 = baseOceanGeneration();
        var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, param0 ? AquaticPlacements.SEAGRASS_DEEP_WARM : AquaticPlacements.SEAGRASS_WARM);
        if (param0) {
            BiomeDefaultFeatures.addDefaultSeagrass(var1);
        }

        BiomeDefaultFeatures.addLukeWarmKelp(var1);
        return baseOcean(var0, 4566514, 267827, var1);
    }

    public static Biome warmOcean() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder()
            .addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 15, 1, 3));
        BiomeDefaultFeatures.warmOceanSpawns(var0, 10, 4);
        BiomeGenerationSettings.Builder var1 = baseOceanGeneration()
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.WARM_OCEAN_VEGETATION)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_WARM)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEA_PICKLE);
        return baseOcean(var0, 4445678, 270131, var1);
    }

    public static Biome frozenOcean(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder()
            .addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 1, 1, 4))
            .addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
        BiomeDefaultFeatures.commonSpawns(var0);
        var0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
        float var1 = param0 ? 0.5F : 0.0F;
        BiomeGenerationSettings.Builder var2 = new BiomeGenerationSettings.Builder();
        BiomeDefaultFeatures.addIcebergs(var2);
        globalOverworldGeneration(var2);
        BiomeDefaultFeatures.addBlueIce(var2);
        BiomeDefaultFeatures.addDefaultOres(var2);
        BiomeDefaultFeatures.addDefaultSoftDisks(var2);
        BiomeDefaultFeatures.addWaterTrees(var2);
        BiomeDefaultFeatures.addDefaultFlowers(var2);
        BiomeDefaultFeatures.addDefaultGrass(var2);
        BiomeDefaultFeatures.addDefaultMushrooms(var2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var2);
        return new Biome.BiomeBuilder()
            .precipitation(param0 ? Biome.Precipitation.RAIN : Biome.Precipitation.SNOW)
            .temperature(var1)
            .temperatureAdjustment(Biome.TemperatureModifier.FROZEN)
            .downfall(0.5F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(3750089)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(var1))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var2.build())
            .build();
    }

    public static Biome forest(boolean param0, boolean param1, boolean param2) {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var0);
        if (param2) {
            var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_FOREST_FLOWERS);
        } else {
            BiomeDefaultFeatures.addForestFlowers(var0);
        }

        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        if (param2) {
            var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_FLOWER_FOREST);
            var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_FLOWER_FOREST);
            BiomeDefaultFeatures.addDefaultGrass(var0);
        } else {
            if (param0) {
                if (param1) {
                    BiomeDefaultFeatures.addTallBirchTrees(var0);
                } else {
                    BiomeDefaultFeatures.addBirchTrees(var0);
                }
            } else {
                BiomeDefaultFeatures.addOtherBirchTrees(var0);
            }

            BiomeDefaultFeatures.addDefaultFlowers(var0);
            BiomeDefaultFeatures.addForestGrass(var0);
        }

        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var1);
        BiomeDefaultFeatures.commonSpawns(var1);
        if (param2) {
            var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        } else if (!param0) {
            var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 5, 4, 4));
        }

        float var2 = param0 ? 0.6F : 0.7F;
        return biome(Biome.Precipitation.RAIN, var2, param0 ? 0.6F : 0.8F, var1, var0, NORMAL_MUSIC);
    }

    public static Biome taiga(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var0);
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
        BiomeDefaultFeatures.commonSpawns(var0);
        float var1 = param0 ? -0.5F : 0.25F;
        BiomeGenerationSettings.Builder var2 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var2);
        BiomeDefaultFeatures.addFerns(var2);
        BiomeDefaultFeatures.addDefaultOres(var2);
        BiomeDefaultFeatures.addDefaultSoftDisks(var2);
        BiomeDefaultFeatures.addTaigaTrees(var2);
        BiomeDefaultFeatures.addDefaultFlowers(var2);
        BiomeDefaultFeatures.addTaigaGrass(var2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var2);
        if (param0) {
            BiomeDefaultFeatures.addRareBerryBushes(var2);
        } else {
            BiomeDefaultFeatures.addCommonBerryBushes(var2);
        }

        return biome(
            param0 ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN,
            var1,
            param0 ? 0.4F : 0.8F,
            param0 ? 4020182 : 4159204,
            329011,
            var0,
            var2,
            NORMAL_MUSIC
        );
    }

    public static Biome darkForest() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var0);
        BiomeDefaultFeatures.commonSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var1);
        var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.DARK_FOREST_VEGETATION);
        BiomeDefaultFeatures.addForestFlowers(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addDefaultFlowers(var1);
        BiomeDefaultFeatures.addForestGrass(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .temperature(0.7F)
            .downfall(0.8F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.7F))
                    .grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST)
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    public static Biome swamp() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var0);
        BiomeDefaultFeatures.commonSpawns(var0);
        var0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1, 1));
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FROG, 10, 2, 5));
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder();
        BiomeDefaultFeatures.addFossilDecoration(var1);
        globalOverworldGeneration(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addSwampClayDisk(var1);
        BiomeDefaultFeatures.addSwampVegetation(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addSwampExtraVegetation(var1);
        var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SWAMP);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .temperature(0.8F)
            .downfall(0.9F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(6388580)
                    .waterFogColor(2302743)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.8F))
                    .foliageColorOverride(6975545)
                    .grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP)
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    public static Biome mangroveSwamp() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(var0);
        var0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1, 1));
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FROG, 10, 2, 5));
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder();
        BiomeDefaultFeatures.addFossilDecoration(var1);
        globalOverworldGeneration(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addMangroveSwampDisks(var1);
        BiomeDefaultFeatures.addMangroveSwampVegetation(var1);
        var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SWAMP);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .temperature(0.8F)
            .downfall(0.9F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(3832426)
                    .waterFogColor(2302743)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.8F))
                    .foliageColorOverride(9285927)
                    .grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP)
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    public static Biome river(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder()
            .addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 2, 1, 4))
            .addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 5, 1, 5));
        BiomeDefaultFeatures.commonSpawns(var0);
        var0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, param0 ? 1 : 100, 1, 1));
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addWaterTrees(var1);
        BiomeDefaultFeatures.addDefaultFlowers(var1);
        BiomeDefaultFeatures.addDefaultGrass(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        if (!param0) {
            var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_RIVER);
        }

        float var2 = param0 ? 0.0F : 0.5F;
        return biome(param0 ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN, var2, 0.5F, param0 ? 3750089 : 4159204, 329011, var0, var1, NORMAL_MUSIC);
    }

    public static Biome beach(boolean param0, boolean param1) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        boolean var1 = !param1 && !param0;
        if (var1) {
            var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.TURTLE, 5, 2, 5));
        }

        BiomeDefaultFeatures.commonSpawns(var0);
        BiomeGenerationSettings.Builder var2 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var2);
        BiomeDefaultFeatures.addDefaultOres(var2);
        BiomeDefaultFeatures.addDefaultSoftDisks(var2);
        BiomeDefaultFeatures.addDefaultFlowers(var2);
        BiomeDefaultFeatures.addDefaultGrass(var2);
        BiomeDefaultFeatures.addDefaultMushrooms(var2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var2);
        float var3;
        if (param0) {
            var3 = 0.05F;
        } else if (param1) {
            var3 = 0.2F;
        } else {
            var3 = 0.8F;
        }

        return biome(
            param0 ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN,
            var3,
            var1 ? 0.4F : 0.3F,
            param0 ? 4020182 : 4159204,
            329011,
            var0,
            var2,
            NORMAL_MUSIC
        );
    }

    public static Biome theVoid() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder();
        var0.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, MiscOverworldPlacements.VOID_START_PLATFORM);
        return biome(Biome.Precipitation.NONE, 0.5F, 0.5F, new MobSpawnSettings.Builder(), var0, NORMAL_MUSIC);
    }

    public static Biome meadow() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder();
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 2))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 2, 2, 6))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 2, 2, 4));
        BiomeDefaultFeatures.commonSpawns(var1);
        globalOverworldGeneration(var0);
        BiomeDefaultFeatures.addPlainGrass(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addMeadowVegetation(var0);
        BiomeDefaultFeatures.addExtraEmeralds(var0);
        BiomeDefaultFeatures.addInfestedStone(var0);
        Music var2 = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_MEADOW);
        return biome(Biome.Precipitation.RAIN, 0.5F, 0.8F, 937679, 329011, var1, var0, var2);
    }

    public static Biome frozenPeaks() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder();
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 1, 3));
        BiomeDefaultFeatures.commonSpawns(var1);
        globalOverworldGeneration(var0);
        BiomeDefaultFeatures.addFrozenSprings(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addExtraEmeralds(var0);
        BiomeDefaultFeatures.addInfestedStone(var0);
        Music var2 = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FROZEN_PEAKS);
        return biome(Biome.Precipitation.SNOW, -0.7F, 0.9F, var1, var0, var2);
    }

    public static Biome jaggedPeaks() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder();
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 1, 3));
        BiomeDefaultFeatures.commonSpawns(var1);
        globalOverworldGeneration(var0);
        BiomeDefaultFeatures.addFrozenSprings(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addExtraEmeralds(var0);
        BiomeDefaultFeatures.addInfestedStone(var0);
        Music var2 = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_JAGGED_PEAKS);
        return biome(Biome.Precipitation.SNOW, -0.7F, 0.9F, var1, var0, var2);
    }

    public static Biome stonyPeaks() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder();
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(var1);
        globalOverworldGeneration(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addExtraEmeralds(var0);
        BiomeDefaultFeatures.addInfestedStone(var0);
        Music var2 = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_STONY_PEAKS);
        return biome(Biome.Precipitation.RAIN, 1.0F, 0.3F, var1, var0, var2);
    }

    public static Biome snowySlopes() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder();
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 1, 3));
        BiomeDefaultFeatures.commonSpawns(var1);
        globalOverworldGeneration(var0);
        BiomeDefaultFeatures.addFrozenSprings(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addExtraEmeralds(var0);
        BiomeDefaultFeatures.addInfestedStone(var0);
        Music var2 = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SNOWY_SLOPES);
        return biome(Biome.Precipitation.SNOW, -0.3F, 0.9F, var1, var0, var2);
    }

    public static Biome grove() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder();
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var1);
        var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
        BiomeDefaultFeatures.commonSpawns(var1);
        globalOverworldGeneration(var0);
        BiomeDefaultFeatures.addFrozenSprings(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addGroveTrees(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addExtraEmeralds(var0);
        BiomeDefaultFeatures.addInfestedStone(var0);
        Music var2 = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_GROVE);
        return biome(Biome.Precipitation.SNOW, -0.2F, 0.8F, var1, var0, var2);
    }

    public static Biome lushCaves() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        var0.addSpawn(MobCategory.AXOLOTLS, new MobSpawnSettings.SpawnerData(EntityType.AXOLOTL, 10, 4, 6));
        var0.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
        BiomeDefaultFeatures.commonSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var1);
        BiomeDefaultFeatures.addPlainGrass(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addLushCavesSpecialOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addLushCavesVegetationFeatures(var1);
        Music var2 = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES);
        return biome(Biome.Precipitation.RAIN, 0.5F, 0.5F, var0, var1, var2);
    }

    public static Biome dripstoneCaves() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.dripstoneCavesSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder();
        globalOverworldGeneration(var1);
        BiomeDefaultFeatures.addPlainGrass(var1);
        BiomeDefaultFeatures.addDefaultOres(var1, true);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addPlainVegetation(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        BiomeDefaultFeatures.addDripstone(var1);
        Music var2 = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_DRIPSTONE_CAVES);
        return biome(Biome.Precipitation.RAIN, 0.8F, 0.4F, var0, var1, var2);
    }

    public static Biome deepDark() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder();
        var1.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE);
        var1.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE_EXTRA_UNDERGROUND);
        var1.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        BiomeDefaultFeatures.addPlainGrass(var1);
        BiomeDefaultFeatures.addDefaultOres(var1, true);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addPlainVegetation(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        BiomeDefaultFeatures.addSculk(var1);
        Music var2 = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_DEEP_DARK);
        return biome(Biome.Precipitation.RAIN, 0.8F, 0.4F, var0, var1, var2);
    }
}
