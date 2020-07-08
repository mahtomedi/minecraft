package net.minecraft.data.worldgen.biome;

import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;

public final class MountainEdgeBiome extends Biome {
    public MountainEdgeBiome() {
        super(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.GRASS)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.EXTREME_HILLS)
                .depth(0.8F)
                .scale(0.3F)
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
                .parent(null)
        );
        BiomeDefaultFeatures.addDefaultOverworldLandStructures(this);
        this.addStructureStart(StructureFeatures.RUINED_PORTAL_MOUNTAIN);
        BiomeDefaultFeatures.addDefaultCarvers(this);
        BiomeDefaultFeatures.addDefaultLakes(this);
        BiomeDefaultFeatures.addDefaultMonsterRoom(this);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(this);
        BiomeDefaultFeatures.addDefaultOres(this);
        BiomeDefaultFeatures.addDefaultSoftDisks(this);
        BiomeDefaultFeatures.addMountainEdgeTrees(this);
        BiomeDefaultFeatures.addDefaultFlowers(this);
        BiomeDefaultFeatures.addDefaultGrass(this);
        BiomeDefaultFeatures.addDefaultMushrooms(this);
        BiomeDefaultFeatures.addDefaultExtraVegetation(this);
        BiomeDefaultFeatures.addDefaultSprings(this);
        BiomeDefaultFeatures.addExtraEmeralds(this);
        BiomeDefaultFeatures.addInfestedStone(this);
        BiomeDefaultFeatures.addSurfaceFreezing(this);
        BiomeDefaultFeatures.farmAnimals(this);
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.LLAMA, 5, 4, 6));
        BiomeDefaultFeatures.commonSpawns(this);
    }
}