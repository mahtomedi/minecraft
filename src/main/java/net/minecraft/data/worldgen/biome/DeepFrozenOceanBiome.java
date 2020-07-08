package net.minecraft.data.worldgen.biome;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public class DeepFrozenOceanBiome extends Biome {
    protected static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(3456L), ImmutableList.of(-2, -1, 0));

    public DeepFrozenOceanBiome() {
        super(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.FROZEN_OCEAN)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.OCEAN)
                .depth(-1.8F)
                .scale(0.1F)
                .temperature(0.5F)
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
        this.addStructureStart(StructureFeatures.OCEAN_RUIN_COLD);
        this.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
        BiomeDefaultFeatures.addDefaultOverworldOceanStructures(this);
        this.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
        BiomeDefaultFeatures.addOceanCarvers(this);
        BiomeDefaultFeatures.addDefaultLakes(this);
        BiomeDefaultFeatures.addIcebergs(this);
        BiomeDefaultFeatures.addDefaultMonsterRoom(this);
        BiomeDefaultFeatures.addBlueIce(this);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(this);
        BiomeDefaultFeatures.addDefaultOres(this);
        BiomeDefaultFeatures.addDefaultSoftDisks(this);
        BiomeDefaultFeatures.addWaterTrees(this);
        BiomeDefaultFeatures.addDefaultFlowers(this);
        BiomeDefaultFeatures.addDefaultGrass(this);
        BiomeDefaultFeatures.addDefaultMushrooms(this);
        BiomeDefaultFeatures.addDefaultExtraVegetation(this);
        BiomeDefaultFeatures.addDefaultSprings(this);
        BiomeDefaultFeatures.addSurfaceFreezing(this);
        this.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.SQUID, 1, 1, 4));
        this.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.SALMON, 15, 1, 5));
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
        BiomeDefaultFeatures.commonSpawns(this);
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.DROWNED, 5, 1, 1));
    }

    @Override
    protected float getTemperatureNoCache(BlockPos param0) {
        float var0 = this.getTemperature();
        double var1 = FROZEN_TEMPERATURE_NOISE.getValue((double)param0.getX() * 0.05, (double)param0.getZ() * 0.05, false) * 7.0;
        double var2 = BIOME_INFO_NOISE.getValue((double)param0.getX() * 0.2, (double)param0.getZ() * 0.2, false);
        double var3 = var1 + var2;
        if (var3 < 0.3) {
            double var4 = BIOME_INFO_NOISE.getValue((double)param0.getX() * 0.09, (double)param0.getZ() * 0.09, false);
            if (var4 < 0.8) {
                var0 = 0.2F;
            }
        }

        if (param0.getY() > 64) {
            float var5 = (float)(TEMPERATURE_NOISE.getValue((double)((float)param0.getX() / 8.0F), (double)((float)param0.getZ() / 8.0F), false) * 4.0);
            return var0 - (var5 + (float)param0.getY() - 64.0F) * 0.05F / 30.0F;
        } else {
            return var0;
        }
    }
}