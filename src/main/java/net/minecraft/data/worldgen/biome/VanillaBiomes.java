package net.minecraft.data.worldgen.biome;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;

public class VanillaBiomes {
    public static Biome giantTreeTaiga(float param0, float param1, float param2, boolean param3, @Nullable String param4) {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.GIANT_TREE_TAIGA)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.TAIGA)
                .depth(param0)
                .scale(param1)
                .temperature(param2)
                .downfall(0.8F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(param4)
        );
        BiomeDefaultFeatures.addDefaultOverworldLandStructures(var0);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addMossyStoneBlock(var0);
        BiomeDefaultFeatures.addFerns(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, param3 ? Features.TREES_GIANT_SPRUCE : Features.TREES_GIANT);
        BiomeDefaultFeatures.addDefaultFlowers(var0);
        BiomeDefaultFeatures.addGiantTaigaVegetation(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addSparseBerryBushes(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.farmAnimals(var0);
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.WOLF, 8, 4, 4));
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.FOX, 8, 2, 4));
        if (param3) {
            BiomeDefaultFeatures.commonSpawns(var0);
        } else {
            BiomeDefaultFeatures.ambientSpawns(var0);
            BiomeDefaultFeatures.monsters(var0, 100, 25, 100);
        }

        return var0;
    }

    public static Biome birchForestBiome(float param0, float param1, @Nullable String param2, boolean param3) {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.GRASS)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.FOREST)
                .depth(param0)
                .scale(param1)
                .temperature(0.6F)
                .downfall(0.6F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(param2)
        );
        BiomeDefaultFeatures.addDefaultOverworldLandStructures(var0);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addForestFlowers(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        if (param3) {
            BiomeDefaultFeatures.addTallBirchTrees(var0);
        } else {
            BiomeDefaultFeatures.addBirchTrees(var0);
        }

        BiomeDefaultFeatures.addDefaultFlowers(var0);
        BiomeDefaultFeatures.addForestGrass(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.farmAnimals(var0);
        BiomeDefaultFeatures.commonSpawns(var0);
        return var0;
    }

    public static Biome jungleBiome() {
        return jungleBiome(0.1F, 0.2F, 40, 2, 3);
    }

    public static Biome jungleEdgeBiome() {
        return baseJungleBiome(null, 0.1F, 0.2F, 0.8F, false, true, false);
    }

    public static Biome modifiedJungleEdgeBiome() {
        return baseJungleBiome("jungle_edge", 0.2F, 0.4F, 0.8F, false, true, true);
    }

    public static Biome modifiedJungleBiome() {
        Biome var0 = baseJungleBiome("jungle", 0.2F, 0.4F, 0.9F, false, false, true);
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PARROT, 10, 1, 1));
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.OCELOT, 2, 1, 1));
        return var0;
    }

    public static Biome jungleHillsBiome() {
        return jungleBiome(0.45F, 0.3F, 10, 1, 1);
    }

    public static Biome bambooJungleBiome() {
        return bambooJungleBiome(0.1F, 0.2F, 40, 2);
    }

    public static Biome bambooJungleHillsBiome() {
        return bambooJungleBiome(0.45F, 0.3F, 10, 1);
    }

    private static Biome jungleBiome(float param0, float param1, int param2, int param3, int param4) {
        Biome var0 = baseJungleBiome(null, param0, param1, 0.9F, false, false, false);
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PARROT, param2, 1, param3));
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.OCELOT, 2, 1, param4));
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PANDA, 1, 1, 2));
        return var0;
    }

    private static Biome bambooJungleBiome(float param0, float param1, int param2, int param3) {
        Biome var0 = baseJungleBiome(null, param0, param1, 0.9F, true, false, false);
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PARROT, param2, 1, param3));
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PANDA, 80, 1, 2));
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.OCELOT, 2, 1, 1));
        return var0;
    }

    private static Biome baseJungleBiome(@Nullable String param0, float param1, float param2, float param3, boolean param4, boolean param5, boolean param6) {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.GRASS)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.JUNGLE)
                .depth(param1)
                .scale(param2)
                .temperature(0.95F)
                .downfall(param3)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(param0)
        );
        if (!param5 && !param6) {
            var0.addStructureStart(StructureFeatures.JUNGLE_TEMPLE);
        }

        BiomeDefaultFeatures.addDefaultOverworldLandStructures(var0);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_JUNGLE);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        if (param4) {
            BiomeDefaultFeatures.addBambooVegetation(var0);
        } else {
            if (!param5 && !param6) {
                BiomeDefaultFeatures.addLightBambooVegetation(var0);
            }

            if (param5) {
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
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.baseJungleSpawns(var0);
        return var0;
    }

    public static Biome mountainBiome(
        float param0, float param1, ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> param2, boolean param3, @Nullable String param4
    ) {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(param2)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.EXTREME_HILLS)
                .depth(param0)
                .scale(param1)
                .temperature(0.2F)
                .downfall(0.3F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(param4)
        );
        BiomeDefaultFeatures.addDefaultOverworldLandStructures(var0);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_MOUNTAIN);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        if (param3) {
            BiomeDefaultFeatures.addMountainEdgeTrees(var0);
        } else {
            BiomeDefaultFeatures.addMountainTrees(var0);
        }

        BiomeDefaultFeatures.addDefaultFlowers(var0);
        BiomeDefaultFeatures.addDefaultGrass(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addExtraEmeralds(var0);
        BiomeDefaultFeatures.addInfestedStone(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.farmAnimals(var0);
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.LLAMA, 5, 4, 6));
        BiomeDefaultFeatures.commonSpawns(var0);
        return var0;
    }

    public static Biome desertBiome(@Nullable String param0, float param1, float param2, boolean param3, boolean param4, boolean param5) {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.DESERT)
                .precipitation(Biome.Precipitation.NONE)
                .biomeCategory(Biome.BiomeCategory.DESERT)
                .depth(param1)
                .scale(param2)
                .temperature(2.0F)
                .downfall(0.0F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(param0)
        );
        if (param3) {
            var0.addStructureStart(StructureFeatures.VILLAGE_DESERT);
            var0.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
        }

        if (param4) {
            var0.addStructureStart(StructureFeatures.DESERT_PYRAMID);
        }

        if (param5) {
            BiomeDefaultFeatures.addFossilDecoration(var0);
        }

        BiomeDefaultFeatures.addDefaultOverworldLandStructures(var0);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_DESERT);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDesertLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addDefaultFlowers(var0);
        BiomeDefaultFeatures.addDefaultGrass(var0);
        BiomeDefaultFeatures.addDesertVegetation(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDesertExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addDesertExtraDecoration(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.desertSpawns(var0);
        return var0;
    }

    public static Biome plainsBiome(@Nullable String param0, boolean param1) {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.GRASS)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.PLAINS)
                .depth(0.125F)
                .scale(0.05F)
                .temperature(0.8F)
                .downfall(0.4F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(param0)
        );
        if (!param1) {
            var0.addStructureStart(StructureFeatures.VILLAGE_PLAINS);
            var0.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
        }

        BiomeDefaultFeatures.addDefaultOverworldLandStructures(var0);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addPlainGrass(var0);
        if (param1) {
            var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUNFLOWER);
        }

        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addPlainVegetation(var0);
        if (param1) {
            var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
        }

        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        if (param1) {
            var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
        } else {
            BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        }

        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.plainsSpawns(var0);
        return var0;
    }

    public static Biome endBarrensBiome() {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.END)
                .precipitation(Biome.Precipitation.NONE)
                .biomeCategory(Biome.BiomeCategory.THEEND)
                .depth(0.1F)
                .scale(0.2F)
                .temperature(0.5F)
                .downfall(0.5F)
                .skyColor(0)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(10518688)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(null)
        );
        BiomeDefaultFeatures.endSpawns(var0);
        return var0;
    }

    public static Biome theEndBiome() {
        Biome var0 = endBarrensBiome();
        var0.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_SPIKE);
        return var0;
    }

    public static Biome endMidlandsBiome() {
        Biome var0 = endBarrensBiome();
        var0.addStructureStart(StructureFeatures.END_CITY);
        return var0;
    }

    public static Biome endHighlandsBiome() {
        Biome var0 = endMidlandsBiome();
        var0.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_GATEWAY);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CHORUS_PLANT);
        return var0;
    }

    public static Biome smallEndIslandsBiome() {
        Biome var0 = endBarrensBiome();
        var0.addFeature(GenerationStep.Decoration.RAW_GENERATION, Features.END_ISLAND_DECORATED);
        return var0;
    }

    public static Biome mushroomFieldsBiome(float param0, float param1) {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.MYCELIUM)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.MUSHROOM)
                .depth(param0)
                .scale(param1)
                .temperature(0.9F)
                .downfall(1.0F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(null)
        );
        BiomeDefaultFeatures.addDefaultOverworldLandStructures(var0);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addMushroomFieldVegetation(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.mooshroomSpawns(var0);
        return var0;
    }

    public static Biome savannaBiome(@Nullable String param0, float param1, float param2, float param3, boolean param4, boolean param5) {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(param5 ? SurfaceBuilders.SHATTERED_SAVANNA : SurfaceBuilders.GRASS)
                .precipitation(Biome.Precipitation.NONE)
                .biomeCategory(Biome.BiomeCategory.SAVANNA)
                .depth(param1)
                .scale(param2)
                .temperature(param3)
                .downfall(0.0F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(param0)
        );
        if (!param4 && !param5) {
            var0.addStructureStart(StructureFeatures.VILLAGE_SAVANNA);
            var0.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
        }

        BiomeDefaultFeatures.addDefaultOverworldLandStructures(var0);
        var0.addStructureStart(param4 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        if (!param5) {
            BiomeDefaultFeatures.addSavannaGrass(var0);
        }

        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        if (param5) {
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
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.farmAnimals(var0);
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.HORSE, 1, 2, 6));
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.DONKEY, 1, 1, 1));
        BiomeDefaultFeatures.commonSpawns(var0);
        return var0;
    }

    public static Biome savanaPlateauBiome() {
        Biome var0 = savannaBiome(null, 1.5F, 0.025F, 1.0F, true, false);
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.LLAMA, 8, 4, 4));
        return var0;
    }

    private static Biome baseBadlandsBiome(
        @Nullable String param0, ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> param1, float param2, float param3, boolean param4, boolean param5
    ) {
        Biome var0 = new BadlandsBiome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(param1)
                .precipitation(Biome.Precipitation.NONE)
                .biomeCategory(Biome.BiomeCategory.MESA)
                .depth(param2)
                .scale(param3)
                .temperature(2.0F)
                .downfall(0.0F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(param0)
        );
        BiomeDefaultFeatures.addDefaultOverworldLandMesaStructures(var0);
        var0.addStructureStart(param4 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addExtraGold(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        if (param5) {
            BiomeDefaultFeatures.addBadlandsTrees(var0);
        }

        BiomeDefaultFeatures.addBadlandGrass(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addBadlandExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.commonSpawns(var0);
        return var0;
    }

    public static Biome badlandsBiome(@Nullable String param0, float param1, float param2, boolean param3) {
        return baseBadlandsBiome(param0, SurfaceBuilders.BADLANDS, param1, param2, param3, false);
    }

    public static Biome woodedBadlandsPlateauBiome(@Nullable String param0, float param1, float param2) {
        return baseBadlandsBiome(param0, SurfaceBuilders.WOODED_BADLANDS, param1, param2, true, true);
    }

    public static Biome erodedBadlandsBiome() {
        return baseBadlandsBiome("badlands", SurfaceBuilders.ERODED_BADLANDS, 0.1F, 0.2F, true, false);
    }

    private static Biome baseOceanBiome(
        ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> param0, int param1, int param2, boolean param3, boolean param4, boolean param5
    ) {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(param0)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.OCEAN)
                .depth(param3 ? -1.8F : -1.0F)
                .scale(0.1F)
                .temperature(0.5F)
                .downfall(0.5F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(param1)
                        .waterFogColor(param2)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(null)
        );
        ConfiguredStructureFeature<?, ?> var1 = param4 ? StructureFeatures.OCEAN_RUIN_WARM : StructureFeatures.OCEAN_RUIN_COLD;
        if (param5) {
            if (param3) {
                var0.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
            }

            BiomeDefaultFeatures.addDefaultOverworldOceanStructures(var0);
            var0.addStructureStart(var1);
        } else {
            var0.addStructureStart(var1);
            if (param3) {
                var0.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
            }

            BiomeDefaultFeatures.addDefaultOverworldOceanStructures(var0);
        }

        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
        BiomeDefaultFeatures.addOceanCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
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
        Biome var0 = baseOceanBiome(SurfaceBuilders.GRASS, 4020182, 329011, param0, false, !param0);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, param0 ? Features.SEAGRASS_DEEP_COLD : Features.SEAGRASS_COLD);
        BiomeDefaultFeatures.addDefaultSeagrass(var0);
        BiomeDefaultFeatures.addColdOceanExtraVegetation(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.oceanSpawns(var0, 3, 4, 15);
        var0.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.SALMON, 15, 1, 5));
        return var0;
    }

    public static Biome oceanBiome(boolean param0) {
        Biome var0 = baseOceanBiome(SurfaceBuilders.GRASS, 4159204, 329011, param0, false, true);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, param0 ? Features.SEAGRASS_DEEP : Features.SEAGRASS_NORMAL);
        BiomeDefaultFeatures.addDefaultSeagrass(var0);
        BiomeDefaultFeatures.addColdOceanExtraVegetation(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.oceanSpawns(var0, 1, 4, 10);
        var0.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.DOLPHIN, 1, 1, 2));
        return var0;
    }

    public static Biome lukeWarmOceanBiome(boolean param0) {
        Biome var0 = baseOceanBiome(SurfaceBuilders.OCEAN_SAND, 4566514, 267827, param0, true, false);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, param0 ? Features.SEAGRASS_DEEP_WARM : Features.SEAGRASS_WARM);
        if (param0) {
            BiomeDefaultFeatures.addDefaultSeagrass(var0);
        }

        BiomeDefaultFeatures.addLukeWarmKelp(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        if (param0) {
            BiomeDefaultFeatures.oceanSpawns(var0, 8, 4, 8);
        } else {
            BiomeDefaultFeatures.oceanSpawns(var0, 10, 2, 15);
        }

        var0.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.PUFFERFISH, 5, 1, 3));
        var0.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
        var0.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
        return var0;
    }

    public static Biome warmOceanBiome() {
        Biome var0 = baseOceanBiome(SurfaceBuilders.FULL_SAND, 4445678, 270131, false, true, false);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARM_OCEAN_VEGETATION);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_WARM);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEA_PICKLE);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        var0.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.PUFFERFISH, 15, 1, 3));
        BiomeDefaultFeatures.warmOceanSpawns(var0, 10, 4);
        return var0;
    }

    public static Biome deepWarmOceanBiome() {
        Biome var0 = baseOceanBiome(SurfaceBuilders.FULL_SAND, 4445678, 270131, true, true, false);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_DEEP_WARM);
        BiomeDefaultFeatures.addDefaultSeagrass(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.warmOceanSpawns(var0, 5, 1);
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.DROWNED, 5, 1, 1));
        return var0;
    }

    public static Biome frozenOceanBiome(boolean param0) {
        Biome var0 = new FrozenOceanBiome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.FROZEN_OCEAN)
                .precipitation(param0 ? Biome.Precipitation.RAIN : Biome.Precipitation.SNOW)
                .biomeCategory(Biome.BiomeCategory.OCEAN)
                .depth(param0 ? -1.8F : -1.0F)
                .scale(0.1F)
                .temperature(param0 ? 0.5F : 0.0F)
                .downfall(0.5F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(3750089)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(null)
        );
        var0.addStructureStart(StructureFeatures.OCEAN_RUIN_COLD);
        if (param0) {
            var0.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
        }

        BiomeDefaultFeatures.addDefaultOverworldOceanStructures(var0);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
        BiomeDefaultFeatures.addOceanCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addIcebergs(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addBlueIce(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addWaterTrees(var0);
        BiomeDefaultFeatures.addDefaultFlowers(var0);
        BiomeDefaultFeatures.addDefaultGrass(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        var0.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.SQUID, 1, 1, 4));
        var0.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.SALMON, 15, 1, 5));
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
        BiomeDefaultFeatures.commonSpawns(var0);
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.DROWNED, 5, 1, 1));
        return var0;
    }

    private static Biome baseForestBiome(@Nullable String param0, float param1, float param2, boolean param3) {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.GRASS)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.FOREST)
                .depth(param1)
                .scale(param2)
                .temperature(0.7F)
                .downfall(0.8F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(param0)
        );
        BiomeDefaultFeatures.addDefaultOverworldLandStructures(var0);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        if (param3) {
            var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION_COMMON);
        } else {
            BiomeDefaultFeatures.addForestFlowers(var0);
        }

        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        if (param3) {
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
        BiomeDefaultFeatures.farmAnimals(var0);
        BiomeDefaultFeatures.commonSpawns(var0);
        return var0;
    }

    public static Biome forestBiome(float param0, float param1) {
        Biome var0 = baseForestBiome(null, param0, param1, false);
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.WOLF, 5, 4, 4));
        return var0;
    }

    public static Biome flowerForestBiome() {
        Biome var0 = baseForestBiome("forest", 0.1F, 0.4F, true);
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        return var0;
    }

    public static Biome taigaBiome(@Nullable String param0, float param1, float param2, boolean param3, boolean param4, boolean param5, boolean param6) {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.GRASS)
                .precipitation(param3 ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.TAIGA)
                .depth(param1)
                .scale(param2)
                .temperature(param3 ? -0.5F : 0.25F)
                .downfall(param3 ? 0.4F : 0.8F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(param3 ? 4020182 : 4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(param0)
        );
        if (param5) {
            var0.addStructureStart(StructureFeatures.VILLAGE_TAIGA);
            var0.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
        }

        if (param6) {
            var0.addStructureStart(StructureFeatures.IGLOO);
        }

        BiomeDefaultFeatures.addDefaultOverworldLandStructures(var0);
        var0.addStructureStart(param4 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addFerns(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addTaigaTrees(var0);
        BiomeDefaultFeatures.addDefaultFlowers(var0);
        BiomeDefaultFeatures.addTaigaGrass(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        if (param3) {
            BiomeDefaultFeatures.addBerryBushes(var0);
        } else {
            BiomeDefaultFeatures.addSparseBerryBushes(var0);
        }

        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.farmAnimals(var0);
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.WOLF, 8, 4, 4));
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.RABBIT, 4, 2, 3));
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.FOX, 8, 2, 4));
        BiomeDefaultFeatures.commonSpawns(var0);
        return var0;
    }

    public static Biome darkForestBiome(@Nullable String param0, float param1, float param2, boolean param3) {
        Biome var0 = new DarkForestBiome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.GRASS)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.FOREST)
                .depth(param1)
                .scale(param2)
                .temperature(0.7F)
                .downfall(0.8F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(param0)
        );
        var0.addStructureStart(StructureFeatures.WOODLAND_MANSION);
        BiomeDefaultFeatures.addDefaultOverworldLandStructures(var0);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, param3 ? Features.DARK_FOREST_VEGETATION_RED : Features.DARK_FOREST_VEGETATION_BROWN);
        BiomeDefaultFeatures.addForestFlowers(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addDefaultFlowers(var0);
        BiomeDefaultFeatures.addForestGrass(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.farmAnimals(var0);
        BiomeDefaultFeatures.commonSpawns(var0);
        return var0;
    }

    public static Biome swampBiome(@Nullable String param0, float param1, float param2, boolean param3) {
        Biome var0 = new SwampBiome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.SWAMP)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.SWAMP)
                .depth(param1)
                .scale(param2)
                .temperature(0.8F)
                .downfall(0.9F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(6388580)
                        .waterFogColor(2302743)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(param0)
        );
        if (!param3) {
            var0.addStructureStart(StructureFeatures.SWAMP_HUT);
        }

        var0.addStructureStart(StructureFeatures.MINESHAFT);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_SWAMP);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        if (!param3) {
            BiomeDefaultFeatures.addFossilDecoration(var0);
        }

        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addSwampClayDisk(var0);
        BiomeDefaultFeatures.addSwampVegetation(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addSwampExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        if (param3) {
            BiomeDefaultFeatures.addFossilDecoration(var0);
        } else {
            var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SWAMP);
        }

        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.farmAnimals(var0);
        BiomeDefaultFeatures.commonSpawns(var0);
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 1, 1, 1));
        return var0;
    }

    public static Biome tundraBiome(@Nullable String param0, float param1, float param2, boolean param3, boolean param4) {
        Biome var0 = new TundraBiome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(param3 ? SurfaceBuilders.ICE_SPIKES : SurfaceBuilders.GRASS)
                .precipitation(Biome.Precipitation.SNOW)
                .biomeCategory(Biome.BiomeCategory.ICY)
                .depth(param1)
                .scale(param2)
                .temperature(0.0F)
                .downfall(0.5F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(param0)
        );
        if (!param3 && !param4) {
            var0.addStructureStart(StructureFeatures.VILLAGE_SNOWY);
            var0.addStructureStart(StructureFeatures.IGLOO);
        }

        BiomeDefaultFeatures.addDefaultOverworldLandStructures(var0);
        if (!param3 && !param4) {
            var0.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
        }

        var0.addStructureStart(param4 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        if (param3) {
            var0.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.ICE_SPIKE);
            var0.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.ICE_PATCH);
        }

        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addSnowyTrees(var0);
        BiomeDefaultFeatures.addDefaultFlowers(var0);
        BiomeDefaultFeatures.addDefaultGrass(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        BiomeDefaultFeatures.snowySpawns(var0);
        return var0;
    }

    public static Biome riverBiome(float param0, float param1, float param2, int param3, boolean param4) {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.GRASS)
                .precipitation(param4 ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.RIVER)
                .depth(param0)
                .scale(param1)
                .temperature(param2)
                .downfall(0.5F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(param3)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(null)
        );
        var0.addStructureStart(StructureFeatures.MINESHAFT);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
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
        if (!param4) {
            var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_RIVER);
        }

        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        var0.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.SQUID, 2, 1, 4));
        var0.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.SALMON, 5, 1, 5));
        BiomeDefaultFeatures.commonSpawns(var0);
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.DROWNED, param4 ? 1 : 100, 1, 1));
        return var0;
    }

    public static Biome beachBiome(float param0, float param1, float param2, float param3, int param4, boolean param5, boolean param6) {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(param6 ? SurfaceBuilders.STONE : SurfaceBuilders.DESERT)
                .precipitation(param5 ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN)
                .biomeCategory(param6 ? Biome.BiomeCategory.NONE : Biome.BiomeCategory.BEACH)
                .depth(param0)
                .scale(param1)
                .temperature(param2)
                .downfall(param3)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(param4)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(null)
        );
        if (param6) {
            BiomeDefaultFeatures.addDefaultOverworldLandStructures(var0);
        } else {
            var0.addStructureStart(StructureFeatures.MINESHAFT);
            var0.addStructureStart(StructureFeatures.BURIED_TREASURE);
            var0.addStructureStart(StructureFeatures.SHIPWRECH_BEACHED);
        }

        var0.addStructureStart(param6 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
        BiomeDefaultFeatures.addDefaultCarvers(var0);
        BiomeDefaultFeatures.addDefaultLakes(var0);
        BiomeDefaultFeatures.addDefaultMonsterRoom(var0);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(var0);
        BiomeDefaultFeatures.addDefaultOres(var0);
        BiomeDefaultFeatures.addDefaultSoftDisks(var0);
        BiomeDefaultFeatures.addDefaultFlowers(var0);
        BiomeDefaultFeatures.addDefaultGrass(var0);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        BiomeDefaultFeatures.addDefaultExtraVegetation(var0);
        BiomeDefaultFeatures.addDefaultSprings(var0);
        BiomeDefaultFeatures.addSurfaceFreezing(var0);
        if (!param6 && !param5) {
            var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.TURTLE, 5, 2, 5));
        }

        BiomeDefaultFeatures.commonSpawns(var0);
        return var0;
    }

    public static Biome theVoidBiome() {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.NOPE)
                .precipitation(Biome.Precipitation.NONE)
                .biomeCategory(Biome.BiomeCategory.NONE)
                .depth(0.1F)
                .scale(0.2F)
                .temperature(0.5F)
                .downfall(0.5F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build()
                )
                .parent(null)
        );
        var0.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Features.VOID_START_PLATFORM);
        return var0;
    }

    public static Biome netherWastesBiome() {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.NETHER)
                .precipitation(Biome.Precipitation.NONE)
                .biomeCategory(Biome.BiomeCategory.NETHER)
                .depth(0.1F)
                .scale(0.2F)
                .temperature(2.0F)
                .downfall(0.0F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(3344392)
                        .ambientLoopSound(SoundEvents.AMBIENT_NETHER_WASTES_LOOP)
                        .ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_NETHER_WASTES_MOOD, 6000, 8, 2.0))
                        .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS, 0.0111))
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_NETHER_WASTES))
                        .build()
                )
                .parent(null)
        );
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER);
        var0.addStructureStart(StructureFeatures.NETHER_BRIDGE);
        var0.addStructureStart(StructureFeatures.BASTION_REMNANT);
        var0.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
        BiomeDefaultFeatures.addNetherDefaultOres(var0);
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.GHAST, 50, 4, 4));
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 100, 4, 4));
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.MAGMA_CUBE, 2, 4, 4));
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 1, 4, 4));
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.PIGLIN, 15, 4, 4));
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.STRIDER, 60, 1, 2));
        return var0;
    }

    public static Biome soulSandValleyBiome() {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.SOUL_SAND_VALLEY)
                .precipitation(Biome.Precipitation.NONE)
                .biomeCategory(Biome.BiomeCategory.NETHER)
                .depth(0.1F)
                .scale(0.2F)
                .temperature(2.0F)
                .downfall(0.0F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(1787717)
                        .ambientParticle(new AmbientParticleSettings(ParticleTypes.ASH, 0.00625F))
                        .ambientLoopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP)
                        .ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0))
                        .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111))
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY))
                        .build()
                )
                .parent(null)
        );
        var0.addStructureStart(StructureFeatures.NETHER_BRIDGE);
        var0.addStructureStart(StructureFeatures.NETHER_FOSSIL);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER);
        var0.addStructureStart(StructureFeatures.BASTION_REMNANT);
        var0.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
        var0.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.BASALT_PILLAR);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_CRIMSON_ROOTS);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_SOUL_SAND);
        BiomeDefaultFeatures.addNetherDefaultOres(var0);
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SKELETON, 20, 5, 5));
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.GHAST, 50, 4, 4));
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 1, 4, 4));
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.STRIDER, 60, 1, 2));
        double var1 = 0.7;
        double var2 = 0.15;
        var0.addMobCharge(EntityType.SKELETON, 0.7, 0.15);
        var0.addMobCharge(EntityType.GHAST, 0.7, 0.15);
        var0.addMobCharge(EntityType.ENDERMAN, 0.7, 0.15);
        var0.addMobCharge(EntityType.STRIDER, 0.7, 0.15);
        return var0;
    }

    public static Biome basaltDeltasBiome() {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.BASALT_DELTAS)
                .precipitation(Biome.Precipitation.NONE)
                .biomeCategory(Biome.BiomeCategory.NETHER)
                .depth(0.1F)
                .scale(0.2F)
                .temperature(2.0F)
                .downfall(0.0F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(4341314)
                        .fogColor(6840176)
                        .ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.118093334F))
                        .ambientLoopSound(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP)
                        .ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0))
                        .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.0111))
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS))
                        .build()
                )
                .parent(null)
        );
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER);
        var0.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
        var0.addStructureStart(StructureFeatures.NETHER_BRIDGE);
        var0.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.DELTA);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA_DOUBLE);
        var0.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.SMALL_BASALT_COLUMNS);
        var0.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.LARGE_BASALT_COLUMNS);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BASALT_BLOBS);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BLACKSTONE_BLOBS);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_DELTA);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED_DOUBLE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_DELTAS);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_DELTAS);
        BiomeDefaultFeatures.addAncientDebris(var0);
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.GHAST, 40, 1, 1));
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.MAGMA_CUBE, 100, 2, 5));
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.STRIDER, 60, 1, 2));
        return var0;
    }

    public static Biome crimsonForestBiome() {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.CRIMSON_FOREST)
                .precipitation(Biome.Precipitation.NONE)
                .biomeCategory(Biome.BiomeCategory.NETHER)
                .depth(0.1F)
                .scale(0.2F)
                .temperature(2.0F)
                .downfall(0.0F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(3343107)
                        .ambientParticle(new AmbientParticleSettings(ParticleTypes.CRIMSON_SPORE, 0.025F))
                        .ambientLoopSound(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP)
                        .ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD, 6000, 8, 2.0))
                        .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111))
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_CRIMSON_FOREST))
                        .build()
                )
                .parent(null)
        );
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER);
        var0.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
        var0.addStructureStart(StructureFeatures.NETHER_BRIDGE);
        var0.addStructureStart(StructureFeatures.BASTION_REMNANT);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WEEPING_VINES);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FUNGI);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FOREST_VEGETATION);
        BiomeDefaultFeatures.addNetherDefaultOres(var0);
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 1, 2, 4));
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.HOGLIN, 9, 3, 4));
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.PIGLIN, 5, 3, 4));
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.STRIDER, 60, 1, 2));
        return var0;
    }

    public static Biome warpedForestBiome() {
        Biome var0 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.WARPED_FOREST)
                .precipitation(Biome.Precipitation.NONE)
                .biomeCategory(Biome.BiomeCategory.NETHER)
                .depth(0.1F)
                .scale(0.2F)
                .temperature(2.0F)
                .downfall(0.0F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(1705242)
                        .ambientParticle(new AmbientParticleSettings(ParticleTypes.WARPED_SPORE, 0.01428F))
                        .ambientLoopSound(SoundEvents.AMBIENT_WARPED_FOREST_LOOP)
                        .ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_WARPED_FOREST_MOOD, 6000, 8, 2.0))
                        .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS, 0.0111))
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_WARPED_FOREST))
                        .build()
                )
                .parent(null)
        );
        var0.addStructureStart(StructureFeatures.NETHER_BRIDGE);
        var0.addStructureStart(StructureFeatures.BASTION_REMNANT);
        var0.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER);
        var0.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
        BiomeDefaultFeatures.addDefaultMushrooms(var0);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA);
        var0.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FUNGI);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FOREST_VEGETATION);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.NETHER_SPROUTS);
        var0.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TWISTING_VINES);
        BiomeDefaultFeatures.addNetherDefaultOres(var0);
        var0.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 1, 4, 4));
        var0.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.STRIDER, 60, 1, 2));
        var0.addMobCharge(EntityType.ENDERMAN, 1.0, 0.12);
        return var0;
    }
}
