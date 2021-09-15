package net.minecraft.data.worldgen.biome;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;

public class VanillaBiomes {
    private static int calculateSkyColor(float param0) {
        float var0 = param0 / 3.0F;
        var0 = Mth.clamp(var0, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - var0 * 0.05F, 0.5F + var0 * 0.1F, 1.0F);
    }

    public static Biome giantTreeTaiga(float param0, boolean param1) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var0);
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4));
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
        if (param1) {
            BiomeDefaultFeatures.commonSpawns(var0);
        } else {
            BiomeDefaultFeatures.caveSpawns(var0);
            BiomeDefaultFeatures.monsters(var0, 100, 25, 100);
        }

        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GIANT_TREE_TAIGA);
        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addDefaultLakes(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        BiomeDefaultFeatures.addMossyStoneBlock(var1);
        BiomeDefaultFeatures.addFerns(var1);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, param1 ? Features.TREES_GIANT_SPRUCE : Features.TREES_GIANT);
        BiomeDefaultFeatures.addDefaultFlowers(var1);
        BiomeDefaultFeatures.addGiantTaigaVegetation(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        BiomeDefaultFeatures.addDefaultSprings(var1);
        BiomeDefaultFeatures.addSparseBerryBushes(var1);
        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.TAIGA)
            .temperature(param0)
            .downfall(0.8F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(param0))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    public static Biome birchForestBiome(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var0);
        BiomeDefaultFeatures.commonSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addDefaultLakes(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        BiomeDefaultFeatures.addForestFlowers(var1);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        if (param0) {
            BiomeDefaultFeatures.addTallBirchTrees(var1);
        } else {
            BiomeDefaultFeatures.addBirchTrees(var1);
        }

        BiomeDefaultFeatures.addDefaultFlowers(var1);
        BiomeDefaultFeatures.addForestGrass(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        BiomeDefaultFeatures.addDefaultSprings(var1);
        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.FOREST)
            .temperature(0.6F)
            .downfall(0.6F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.6F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    public static Biome jungleBiome() {
        return jungleBiome(40, 2, 3);
    }

    public static Biome jungleEdgeBiome() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(var0);
        return baseJungleBiome(0.8F, false, true, false, var0);
    }

    public static Biome modifiedJungleEdgeBiome() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(var0);
        return baseJungleBiome(0.8F, false, true, false, var0);
    }

    public static Biome modifiedJungleBiome() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(var0);
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 10, 1, 1))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 1));
        return baseJungleBiome(0.9F, false, false, false, var0);
    }

    public static Biome jungleHillsBiome() {
        return jungleBiome(10, 1, 1);
    }

    public static Biome bambooJungleBiome() {
        return bambooJungleBiome(40, 2);
    }

    public static Biome bambooJungleHillsBiome() {
        return bambooJungleBiome(10, 1);
    }

    private static Biome jungleBiome(int param0, int param1, int param2) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(var0);
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, param0, 1, param1))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, param2))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 1, 1, 2));
        var0.setPlayerCanSpawn();
        return baseJungleBiome(0.9F, false, false, true, var0);
    }

    private static Biome bambooJungleBiome(int param0, int param1) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(var0);
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, param0, 1, param1))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 80, 1, 2))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 1));
        return baseJungleBiome(0.9F, true, false, true, var0);
    }

    private static Biome baseJungleBiome(float param0, boolean param1, boolean param2, boolean param3, MobSpawnSettings.Builder param4) {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        if (param1) {
            BiomeDefaultFeatures.addBambooVegetation(var0);
        } else {
            if (param3) {
                BiomeDefaultFeatures.addLightBambooVegetation(var0);
            }

            if (param2) {
                BiomeDefaultFeatures.addJungleEdgeTrees(var0);
            } else {
                BiomeDefaultFeatures.addJungleTrees(var0);
            }
        }

        BiomeDefaultFeatures.addWarmFlowers(var0);
        BiomeDefaultFeatures.addJungleGrass(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addJungleExtraVegetation(var0);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.JUNGLE)
            .temperature(0.95F)
            .downfall(param0)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.95F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(param4.build())
            .generationSettings(var0.build())
            .build();
    }

    public static Biome mountainBiome(ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> param0, boolean param1) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var0);
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 5, 4, 6));
        BiomeDefaultFeatures.commonSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder().surfaceBuilder(param0);
        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addDefaultLakes(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        if (param1) {
            BiomeDefaultFeatures.addMountainEdgeTrees(var1);
        } else {
            BiomeDefaultFeatures.addMountainTrees(var1);
        }

        BiomeDefaultFeatures.addDefaultFlowers(var1);
        BiomeDefaultFeatures.addDefaultGrass(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        BiomeDefaultFeatures.addDefaultSprings(var1);
        BiomeDefaultFeatures.addExtraEmeralds(var1);
        BiomeDefaultFeatures.addInfestedStone(var1);
        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.EXTREME_HILLS)
            .temperature(0.2F)
            .downfall(0.3F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.2F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    public static Biome desertBiome(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.desertSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.DESERT);
        if (param0) {
            BiomeDefaultFeatures.addFossilDecoration(var1);
        }

        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addLavaLakes(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addDefaultFlowers(var1);
        BiomeDefaultFeatures.addDefaultGrass(var1);
        BiomeDefaultFeatures.addDesertVegetation(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDesertExtraVegetation(var1);
        BiomeDefaultFeatures.addDefaultSprings(var1);
        BiomeDefaultFeatures.addDesertExtraDecoration(var1);
        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.NONE)
            .biomeCategory(Biome.BiomeCategory.DESERT)
            .temperature(2.0F)
            .downfall(0.0F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(2.0F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    public static Biome plainsBiome(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.plainsSpawns(var0);
        if (!param0) {
            var0.setPlayerCanSpawn();
        }

        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addDefaultLakes(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        BiomeDefaultFeatures.addPlainGrass(var1);
        if (param0) {
            var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUNFLOWER);
        }

        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addPlainVegetation(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        if (param0) {
            var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
            var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
        } else {
            BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        }

        BiomeDefaultFeatures.addDefaultSprings(var1);
        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.PLAINS)
            .temperature(0.8F)
            .downfall(0.4F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.8F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    private static Biome baseEndBiome(BiomeGenerationSettings.Builder param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.endSpawns(var0);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.NONE)
            .biomeCategory(Biome.BiomeCategory.THEEND)
            .temperature(0.5F)
            .downfall(0.5F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(10518688)
                    .skyColor(0)
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(param0.build())
            .build();
    }

    public static Biome endBarrensBiome() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.END);
        return baseEndBiome(var0);
    }

    public static Biome theEndBiome() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder()
            .surfaceBuilder(SurfaceBuilders.END)
            .addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_SPIKE);
        return baseEndBiome(var0);
    }

    public static Biome endMidlandsBiome() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.END);
        return baseEndBiome(var0);
    }

    public static Biome endHighlandsBiome() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder()
            .surfaceBuilder(SurfaceBuilders.END)
            .addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_GATEWAY)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CHORUS_PLANT);
        return baseEndBiome(var0);
    }

    public static Biome smallEndIslandsBiome() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder()
            .surfaceBuilder(SurfaceBuilders.END)
            .addFeature(GenerationStep.Decoration.RAW_GENERATION, Features.END_ISLAND_DECORATED);
        return baseEndBiome(var0);
    }

    public static Biome mushroomFieldsBiome() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.mooshroomSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.MYCELIUM);
        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addDefaultLakes(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addMushroomFieldVegetation(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        BiomeDefaultFeatures.addDefaultSprings(var1);
        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.MUSHROOM)
            .temperature(0.9F)
            .downfall(1.0F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.9F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    private static Biome baseSavannaBiome(float param0, boolean param1, MobSpawnSettings.Builder param2) {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder()
            .surfaceBuilder(param1 ? SurfaceBuilders.SHATTERED_SAVANNA : SurfaceBuilders.GRASS);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addLavaLakes(var0);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        if (!param1) {
            BiomeDefaultFeatures.addSavannaGrass(var0);
        }

        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        if (param1) {
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
        BiomeDefaultFeatures.addDefaultSprings(var0);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.NONE)
            .biomeCategory(Biome.BiomeCategory.SAVANNA)
            .temperature(param0)
            .downfall(0.0F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(param0))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(param2.build())
            .generationSettings(var0.build())
            .build();
    }

    public static Biome savannaBiome(float param0, boolean param1) {
        MobSpawnSettings.Builder var0 = savannaMobs();
        return baseSavannaBiome(param0, param1, var0);
    }

    private static MobSpawnSettings.Builder savannaMobs() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var0);
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 1, 2, 6))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 1));
        BiomeDefaultFeatures.commonSpawns(var0);
        return var0;
    }

    public static Biome savanaPlateauBiome() {
        MobSpawnSettings.Builder var0 = savannaMobs();
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 8, 4, 4));
        return baseSavannaBiome(1.0F, false, var0);
    }

    private static Biome baseBadlandsBiome(ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> param0, boolean param1) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder().surfaceBuilder(param0);
        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addLavaLakes(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addExtraGold(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        if (param1) {
            BiomeDefaultFeatures.addBadlandsTrees(var1);
        }

        BiomeDefaultFeatures.addBadlandGrass(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addBadlandExtraVegetation(var1);
        BiomeDefaultFeatures.addDefaultSprings(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.NONE)
            .biomeCategory(Biome.BiomeCategory.MESA)
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

    public static Biome badlandsBiome() {
        return baseBadlandsBiome(SurfaceBuilders.BADLANDS, false);
    }

    public static Biome woodedBadlandsPlateauBiome() {
        return baseBadlandsBiome(SurfaceBuilders.WOODED_BADLANDS, true);
    }

    public static Biome erodedBadlandsBiome() {
        return baseBadlandsBiome(SurfaceBuilders.ERODED_BADLANDS, false);
    }

    private static Biome baseOceanBiome(MobSpawnSettings.Builder param0, int param1, int param2, BiomeGenerationSettings.Builder param3) {
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.OCEAN)
            .temperature(0.5F)
            .downfall(0.5F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(param1)
                    .waterFogColor(param2)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.5F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(param0.build())
            .generationSettings(param3.build())
            .build();
    }

    private static BiomeGenerationSettings.Builder baseOceanGeneration(ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> param0) {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder().surfaceBuilder(param0);
        BiomeDefaultFeatures.addOceanCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addWaterTrees(var0);
        BiomeDefaultFeatures.addDefaultFlowers(var0);
        BiomeDefaultFeatures.addDefaultGrass(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        return var0;
    }

    public static Biome coldOceanBiome(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.oceanSpawns(var0, 3, 4, 15);
        var0.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5));
        boolean var1 = !param0;
        BiomeGenerationSettings.Builder var2 = baseOceanGeneration(SurfaceBuilders.GRASS);
        var2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, param0 ? Features.SEAGRASS_DEEP_COLD : Features.SEAGRASS_COLD);
        BiomeDefaultFeatures.addDefaultSeagrass(var2);
        BiomeDefaultFeatures.addColdOceanExtraVegetation(var2);
        BiomeDefaultFeatures.addSurfaceFreezing(var2);
        return baseOceanBiome(var0, 4020182, 329011, var2);
    }

    public static Biome oceanBiome(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.oceanSpawns(var0, 1, 4, 10);
        var0.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 1, 1, 2));
        BiomeGenerationSettings.Builder var1 = baseOceanGeneration(SurfaceBuilders.GRASS);
        var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, param0 ? Features.SEAGRASS_DEEP : Features.SEAGRASS_NORMAL);
        BiomeDefaultFeatures.addDefaultSeagrass(var1);
        BiomeDefaultFeatures.addColdOceanExtraVegetation(var1);
        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        return baseOceanBiome(var0, 4159204, 329011, var1);
    }

    public static Biome lukeWarmOceanBiome(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        if (param0) {
            BiomeDefaultFeatures.oceanSpawns(var0, 8, 4, 8);
        } else {
            BiomeDefaultFeatures.oceanSpawns(var0, 10, 2, 15);
        }

        var0.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 5, 1, 3))
            .addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8))
            .addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
        BiomeGenerationSettings.Builder var1 = baseOceanGeneration(SurfaceBuilders.OCEAN_SAND);
        var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, param0 ? Features.SEAGRASS_DEEP_WARM : Features.SEAGRASS_WARM);
        if (param0) {
            BiomeDefaultFeatures.addDefaultSeagrass(var1);
        }

        BiomeDefaultFeatures.addLukeWarmKelp(var1);
        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        return baseOceanBiome(var0, 4566514, 267827, var1);
    }

    public static Biome warmOceanBiome() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder()
            .addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 15, 1, 3));
        BiomeDefaultFeatures.warmOceanSpawns(var0, 10, 4);
        BiomeGenerationSettings.Builder var1 = baseOceanGeneration(SurfaceBuilders.FULL_SAND)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARM_OCEAN_VEGETATION)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_WARM)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEA_PICKLE);
        return baseOceanBiome(var0, 4445678, 270131, var1);
    }

    public static Biome deepWarmOceanBiome() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.warmOceanSpawns(var0, 5, 1);
        BiomeGenerationSettings.Builder var1 = baseOceanGeneration(SurfaceBuilders.FULL_SAND)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_DEEP_WARM);
        BiomeDefaultFeatures.addDefaultSeagrass(var1);
        return baseOceanBiome(var0, 4445678, 270131, var1);
    }

    public static Biome frozenOceanBiome(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder()
            .addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 1, 1, 4))
            .addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
        BiomeDefaultFeatures.commonSpawns(var0);
        var0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
        float var1 = param0 ? 0.5F : 0.0F;
        BiomeGenerationSettings.Builder var2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.FROZEN_OCEAN);
        BiomeDefaultFeatures.addOceanCarvers(var2);
        BiomeDefaultFeatures.addDefaultLakes(var2);
        BiomeDefaultFeatures.addIcebergs(var2);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var2);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var2);
        BiomeDefaultFeatures.addBlueIce(var2);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var2);
        BiomeDefaultFeatures.addDefaultOres(var2);
        BiomeDefaultFeatures.addDefaultSoftDisks(var2);
        BiomeDefaultFeatures.addWaterTrees(var2);
        BiomeDefaultFeatures.addDefaultFlowers(var2);
        BiomeDefaultFeatures.addDefaultGrass(var2);
        BiomeDefaultFeatures.addDefaultMushrooms(var2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var2);
        BiomeDefaultFeatures.addDefaultSprings(var2);
        BiomeDefaultFeatures.addSurfaceFreezing(var2);
        return new Biome.BiomeBuilder()
            .precipitation(param0 ? Biome.Precipitation.RAIN : Biome.Precipitation.SNOW)
            .biomeCategory(Biome.BiomeCategory.OCEAN)
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

    private static Biome baseForestBiome(boolean param0, MobSpawnSettings.Builder param1) {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        if (param0) {
            var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION_COMMON);
        } else {
            BiomeDefaultFeatures.addForestFlowers(var0);
        }

        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        if (param0) {
            var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_TREES);
            var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_FOREST);
            BiomeDefaultFeatures.addDefaultGrass(var0);
        } else {
            BiomeDefaultFeatures.addOtherBirchTrees(var0);
            BiomeDefaultFeatures.addDefaultFlowers(var0);
            BiomeDefaultFeatures.addForestGrass(var0);
        }

        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.FOREST)
            .temperature(0.7F)
            .downfall(0.8F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.7F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(param1.build())
            .generationSettings(var0.build())
            .build();
    }

    private static MobSpawnSettings.Builder defaultSpawns() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var0);
        BiomeDefaultFeatures.commonSpawns(var0);
        return var0;
    }

    public static Biome forestBiome() {
        MobSpawnSettings.Builder var0 = defaultSpawns()
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 5, 4, 4))
            .setPlayerCanSpawn();
        return baseForestBiome(false, var0);
    }

    public static Biome flowerForestBiome() {
        MobSpawnSettings.Builder var0 = defaultSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        return baseForestBiome(true, var0);
    }

    public static Biome taigaBiome(boolean param0, boolean param1) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var0);
        var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
        if (!param0 && !param1) {
            var0.setPlayerCanSpawn();
        }

        BiomeDefaultFeatures.commonSpawns(var0);
        float var1 = param0 ? -0.5F : 0.25F;
        BiomeGenerationSettings.Builder var2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
        BiomeDefaultFeatures.addDefaultCarvers(var2);
        BiomeDefaultFeatures.addDefaultLakes(var2);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var2);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var2);
        BiomeDefaultFeatures.addFerns(var2);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var2);
        BiomeDefaultFeatures.addDefaultOres(var2);
        BiomeDefaultFeatures.addDefaultSoftDisks(var2);
        BiomeDefaultFeatures.addTaigaTrees(var2);
        BiomeDefaultFeatures.addDefaultFlowers(var2);
        BiomeDefaultFeatures.addTaigaGrass(var2);
        BiomeDefaultFeatures.addDefaultMushrooms(var2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var2);
        BiomeDefaultFeatures.addDefaultSprings(var2);
        if (param0) {
            BiomeDefaultFeatures.addBerryBushes(var2);
        } else {
            BiomeDefaultFeatures.addSparseBerryBushes(var2);
        }

        BiomeDefaultFeatures.addSurfaceFreezing(var2);
        return new Biome.BiomeBuilder()
            .precipitation(param0 ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.TAIGA)
            .temperature(var1)
            .downfall(param0 ? 0.4F : 0.8F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(param0 ? 4020182 : 4159204)
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

    public static Biome darkForestBiome(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var0);
        BiomeDefaultFeatures.commonSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addDefaultLakes(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, param0 ? Features.DARK_FOREST_VEGETATION_RED : Features.DARK_FOREST_VEGETATION_BROWN);
        BiomeDefaultFeatures.addForestFlowers(var1);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addDefaultFlowers(var1);
        BiomeDefaultFeatures.addForestGrass(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        BiomeDefaultFeatures.addDefaultSprings(var1);
        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.FOREST)
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

    public static Biome swampBiome(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var0);
        BiomeDefaultFeatures.commonSpawns(var0);
        var0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1, 1));
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.SWAMP);
        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addFossilDecoration(var1);
        BiomeDefaultFeatures.addDefaultLakes(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addSwampClayDisk(var1);
        BiomeDefaultFeatures.addSwampVegetation(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addSwampExtraVegetation(var1);
        BiomeDefaultFeatures.addDefaultSprings(var1);
        if (!param0) {
            var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SWAMP);
        }

        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.SWAMP)
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

    public static Biome tundraBiome(boolean param0) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder().creatureGenerationProbability(0.07F);
        BiomeDefaultFeatures.snowySpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder()
            .surfaceBuilder(param0 ? SurfaceBuilders.ICE_SPIKES : SurfaceBuilders.GRASS);
        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addDefaultLakes(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        if (param0) {
            var1.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.ICE_SPIKE);
            var1.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.ICE_PATCH);
        }

        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addSnowyTrees(var1);
        BiomeDefaultFeatures.addDefaultFlowers(var1);
        BiomeDefaultFeatures.addDefaultGrass(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        BiomeDefaultFeatures.addDefaultSprings(var1);
        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.SNOW)
            .biomeCategory(Biome.BiomeCategory.ICY)
            .temperature(0.0F)
            .downfall(0.5F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.0F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    public static Biome riverBiome(float param0, int param1, boolean param2) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder()
            .addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 2, 1, 4))
            .addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 5, 1, 5));
        BiomeDefaultFeatures.commonSpawns(var0);
        var0.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, param2 ? 1 : 100, 1, 1));
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addWaterTrees(var1);
        BiomeDefaultFeatures.addDefaultFlowers(var1);
        BiomeDefaultFeatures.addDefaultGrass(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        BiomeDefaultFeatures.addDefaultSprings(var1);
        if (!param2) {
            var1.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_RIVER);
        }

        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        return new Biome.BiomeBuilder()
            .precipitation(param2 ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.RIVER)
            .temperature(param0)
            .downfall(0.5F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(param1)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(param0))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    public static Biome beachBiome(float param0, float param1, int param2, boolean param3, boolean param4) {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        if (!param4 && !param3) {
            var0.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.TURTLE, 5, 2, 5));
        }

        BiomeDefaultFeatures.commonSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder()
            .surfaceBuilder(param4 ? SurfaceBuilders.STONE_SHORE : SurfaceBuilders.DESERT);
        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addDefaultFlowers(var1);
        BiomeDefaultFeatures.addDefaultGrass(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        BiomeDefaultFeatures.addDefaultSprings(var1);
        BiomeDefaultFeatures.addSurfaceFreezing(var1);
        Biome.BiomeBuilder var2 = new Biome.BiomeBuilder()
            .precipitation(param3 ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN)
            .biomeCategory(param4 ? Biome.BiomeCategory.NONE : Biome.BiomeCategory.BEACH);
        return var2.temperature(param0)
            .downfall(param1)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(param2)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(param0))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    public static Biome theVoidBiome() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.NOPE);
        var0.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Features.VOID_START_PLATFORM);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.NONE)
            .biomeCategory(Biome.BiomeCategory.NONE)
            .temperature(0.5F)
            .downfall(0.5F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.5F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(MobSpawnSettings.EMPTY)
            .generationSettings(var0.build())
            .build();
    }

    public static Biome netherWastesBiome() {
        MobSpawnSettings var0 = new MobSpawnSettings.Builder()
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 100, 4, 4))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 2, 4, 4))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 15, 4, 4))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
            .build();
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder()
            .surfaceBuilder(SurfaceBuilders.NETHER)
            .addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        var1.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
        BiomeDefaultFeatures.addNetherDefaultOres(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.NONE)
            .biomeCategory(Biome.BiomeCategory.NETHER)
            .temperature(2.0F)
            .downfall(0.0F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(3344392)
                    .skyColor(calculateSkyColor(2.0F))
                    .ambientLoopSound(SoundEvents.AMBIENT_NETHER_WASTES_LOOP)
                    .ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_NETHER_WASTES_MOOD, 6000, 8, 2.0))
                    .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS, 0.0111))
                    .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_NETHER_WASTES))
                    .build()
            )
            .mobSpawnSettings(var0)
            .generationSettings(var1.build())
            .build();
    }

    public static Biome soulSandValleyBiome() {
        double var0 = 0.7;
        double var1 = 0.15;
        MobSpawnSettings var2 = new MobSpawnSettings.Builder()
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 20, 5, 5))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
            .addMobCharge(EntityType.SKELETON, 0.7, 0.15)
            .addMobCharge(EntityType.GHAST, 0.7, 0.15)
            .addMobCharge(EntityType.ENDERMAN, 0.7, 0.15)
            .addMobCharge(EntityType.STRIDER, 0.7, 0.15)
            .build();
        BiomeGenerationSettings.Builder var3 = new BiomeGenerationSettings.Builder()
            .surfaceBuilder(SurfaceBuilders.SOUL_SAND_VALLEY)
            .addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA)
            .addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.BASALT_PILLAR)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_CRIMSON_ROOTS)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_SOUL_SAND);
        BiomeDefaultFeatures.addNetherDefaultOres(var3);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.NONE)
            .biomeCategory(Biome.BiomeCategory.NETHER)
            .temperature(2.0F)
            .downfall(0.0F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(1787717)
                    .skyColor(calculateSkyColor(2.0F))
                    .ambientParticle(new AmbientParticleSettings(ParticleTypes.ASH, 0.00625F))
                    .ambientLoopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP)
                    .ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0))
                    .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111))
                    .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY))
                    .build()
            )
            .mobSpawnSettings(var2)
            .generationSettings(var3.build())
            .build();
    }

    public static Biome basaltDeltasBiome() {
        MobSpawnSettings var0 = new MobSpawnSettings.Builder()
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 40, 1, 1))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 100, 2, 5))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
            .build();
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder()
            .surfaceBuilder(SurfaceBuilders.BASALT_DELTAS)
            .addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
            .addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.DELTA)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA_DOUBLE)
            .addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.SMALL_BASALT_COLUMNS)
            .addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.LARGE_BASALT_COLUMNS)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BASALT_BLOBS)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BLACKSTONE_BLOBS)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_DELTA)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED_DOUBLE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_DELTAS)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_DELTAS);
        BiomeDefaultFeatures.addAncientDebris(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.NONE)
            .biomeCategory(Biome.BiomeCategory.NETHER)
            .temperature(2.0F)
            .downfall(0.0F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(4341314)
                    .fogColor(6840176)
                    .skyColor(calculateSkyColor(2.0F))
                    .ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.118093334F))
                    .ambientLoopSound(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP)
                    .ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0))
                    .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.0111))
                    .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS))
                    .build()
            )
            .mobSpawnSettings(var0)
            .generationSettings(var1.build())
            .build();
    }

    public static Biome crimsonForestBiome() {
        MobSpawnSettings var0 = new MobSpawnSettings.Builder()
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 1, 2, 4))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HOGLIN, 9, 3, 4))
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 5, 3, 4))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
            .build();
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder()
            .surfaceBuilder(SurfaceBuilders.CRIMSON_FOREST)
            .addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        var1.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WEEPING_VINES)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FUNGI)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FOREST_VEGETATION);
        BiomeDefaultFeatures.addNetherDefaultOres(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.NONE)
            .biomeCategory(Biome.BiomeCategory.NETHER)
            .temperature(2.0F)
            .downfall(0.0F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(3343107)
                    .skyColor(calculateSkyColor(2.0F))
                    .ambientParticle(new AmbientParticleSettings(ParticleTypes.CRIMSON_SPORE, 0.025F))
                    .ambientLoopSound(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP)
                    .ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD, 6000, 8, 2.0))
                    .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111))
                    .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_CRIMSON_FOREST))
                    .build()
            )
            .mobSpawnSettings(var0)
            .generationSettings(var1.build())
            .build();
    }

    public static Biome warpedForestBiome() {
        MobSpawnSettings var0 = new MobSpawnSettings.Builder()
            .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
            .addMobCharge(EntityType.ENDERMAN, 1.0, 0.12)
            .build();
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder()
            .surfaceBuilder(SurfaceBuilders.WARPED_FOREST)
            .addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        var1.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA)
            .addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FUNGI)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FOREST_VEGETATION)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.NETHER_SPROUTS)
            .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TWISTING_VINES);
        BiomeDefaultFeatures.addNetherDefaultOres(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.NONE)
            .biomeCategory(Biome.BiomeCategory.NETHER)
            .temperature(2.0F)
            .downfall(0.0F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(1705242)
                    .skyColor(calculateSkyColor(2.0F))
                    .ambientParticle(new AmbientParticleSettings(ParticleTypes.WARPED_SPORE, 0.01428F))
                    .ambientLoopSound(SoundEvents.AMBIENT_WARPED_FOREST_LOOP)
                    .ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_WARPED_FOREST_MOOD, 6000, 8, 2.0))
                    .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS, 0.0111))
                    .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_WARPED_FOREST))
                    .build()
            )
            .mobSpawnSettings(var0)
            .generationSettings(var1.build())
            .build();
    }

    public static Biome meadowBiome() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 2))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 2, 2, 6))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 2, 2, 4));
        BiomeDefaultFeatures.commonSpawns(var1);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addPlainGrass(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addMeadowVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addExtraEmeralds(var0);
        BiomeDefaultFeatures.addInfestedStone(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.MOUNTAIN)
            .temperature(0.5F)
            .downfall(0.8F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(937679)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.7F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var1.build())
            .generationSettings(var0.build())
            .build();
    }

    public static Biome snowcappedPeaksBiome() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.SNOWCAPPED_PEAKS);
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 1, 3));
        BiomeDefaultFeatures.commonSpawns(var1);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addExtraEmeralds(var0);
        BiomeDefaultFeatures.addInfestedStone(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.SNOW)
            .biomeCategory(Biome.BiomeCategory.MOUNTAIN)
            .temperature(-0.7F)
            .downfall(0.9F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.7F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var1.build())
            .generationSettings(var0.build())
            .build();
    }

    public static Biome loftyPeaksBiome() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.LOFTY_PEAKS);
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 1, 3));
        BiomeDefaultFeatures.commonSpawns(var1);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addExtraEmeralds(var0);
        BiomeDefaultFeatures.addInfestedStone(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.SNOW)
            .biomeCategory(Biome.BiomeCategory.MOUNTAIN)
            .temperature(-0.7F)
            .downfall(0.9F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.7F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var1.build())
            .generationSettings(var0.build())
            .build();
    }

    public static Biome stonyPeaksBiome() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.STONY_PEAKS);
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 1, 3));
        BiomeDefaultFeatures.commonSpawns(var1);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addExtraEmeralds(var0);
        BiomeDefaultFeatures.addInfestedStone(var0);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.MOUNTAIN)
            .temperature(1.0F)
            .downfall(0.3F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.2F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var1.build())
            .generationSettings(var0.build())
            .build();
    }

    public static Biome snowySlopesBiome() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.SNOWY_SLOPES);
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 5, 1, 3));
        BiomeDefaultFeatures.commonSpawns(var1);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addExtraEmeralds(var0);
        BiomeDefaultFeatures.addInfestedStone(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.SNOW)
            .biomeCategory(Biome.BiomeCategory.MOUNTAIN)
            .temperature(-0.3F)
            .downfall(0.9F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.7F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var1.build())
            .generationSettings(var0.build())
            .build();
    }

    public static Biome groveBiome() {
        BiomeGenerationSettings.Builder var0 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GROVE);
        MobSpawnSettings.Builder var1 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(var1);
        var1.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3))
            .addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
        BiomeDefaultFeatures.commonSpawns(var1);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addTaigaTrees(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addExtraEmeralds(var0);
        BiomeDefaultFeatures.addInfestedStone(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.SNOW)
            .biomeCategory(Biome.BiomeCategory.FOREST)
            .temperature(-0.2F)
            .downfall(0.8F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.7F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var1.build())
            .generationSettings(var0.build())
            .build();
    }

    public static Biome lushCaves() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        var0.addSpawn(MobCategory.AXOLOTLS, new MobSpawnSettings.SpawnerData(EntityType.AXOLOTL, 10, 4, 6));
        BiomeDefaultFeatures.commonSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addDefaultLakes(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        BiomeDefaultFeatures.addPlainGrass(var1);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1);
        BiomeDefaultFeatures.addLushCavesSpecialOres(var1);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addDefaultSprings(var1);
        BiomeDefaultFeatures.addLushCavesVegetationFeatures(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.UNDERGROUND)
            .temperature(0.5F)
            .downfall(0.5F)
            .specialEffects(
                new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.5F)).build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }

    public static Biome dripstoneCaves() {
        MobSpawnSettings.Builder var0 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(var0);
        BiomeGenerationSettings.Builder var1 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.STONE);
        BiomeDefaultFeatures.addDefaultCarvers(var1);
        BiomeDefaultFeatures.addDefaultLakes(var1);
        BiomeDefaultFeatures.addDefaultCrystalFormations(var1);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var1);
        BiomeDefaultFeatures.addPlainGrass(var1);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var1);
        BiomeDefaultFeatures.addDefaultOres(var1, true);
        BiomeDefaultFeatures.addDefaultSoftDisks(var1);
        BiomeDefaultFeatures.addPlainVegetation(var1);
        BiomeDefaultFeatures.addDefaultMushrooms(var1);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var1);
        BiomeDefaultFeatures.addDefaultSprings(var1);
        BiomeDefaultFeatures.addDripstone(var1);
        return new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.RAIN)
            .biomeCategory(Biome.BiomeCategory.UNDERGROUND)
            .temperature(0.8F)
            .downfall(0.4F)
            .specialEffects(
                new BiomeSpecialEffects.Builder()
                    .waterColor(4159204)
                    .waterFogColor(329011)
                    .fogColor(12638463)
                    .skyColor(calculateSkyColor(0.8F))
                    .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                    .build()
            )
            .mobSpawnSettings(var0.build())
            .generationSettings(var1.build())
            .build();
    }
}
