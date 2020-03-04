package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public final class JungleBiome extends Biome {
    public JungleBiome() {
        super(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.CONFIG_GRASS)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.JUNGLE)
                .depth(0.1F)
                .scale(0.2F)
                .temperature(0.95F)
                .downfall(0.9F)
                .specialEffects(
                    new BiomeSpecialEffects.Builder()
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .ambientMoodSound(SoundEvents.AMBIENT_CAVE)
                        .build()
                )
                .parent(null)
                .optimalParameters(ImmutableList.of(new Biome.ClimateParameters(0.5F, 0.5F, 0.0F, 0.0F, 1.0F)))
        );
        this.addStructureStart(Feature.JUNGLE_TEMPLE.configured(FeatureConfiguration.NONE));
        this.addStructureStart(Feature.MINESHAFT.configured(new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL)));
        this.addStructureStart(Feature.STRONGHOLD.configured(FeatureConfiguration.NONE));
        BiomeDefaultFeatures.addDefaultCarvers(this);
        BiomeDefaultFeatures.addStructureFeaturePlacement(this);
        BiomeDefaultFeatures.addDefaultLakes(this);
        BiomeDefaultFeatures.addDefaultMonsterRoom(this);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(this);
        BiomeDefaultFeatures.addDefaultOres(this);
        BiomeDefaultFeatures.addDefaultSoftDisks(this);
        BiomeDefaultFeatures.addLightBambooVegetation(this);
        BiomeDefaultFeatures.addJungleTrees(this);
        BiomeDefaultFeatures.addWarmFlowers(this);
        BiomeDefaultFeatures.addJungleGrass(this);
        BiomeDefaultFeatures.addDefaultMushrooms(this);
        BiomeDefaultFeatures.addDefaultExtraVegetation(this);
        BiomeDefaultFeatures.addDefaultSprings(this);
        BiomeDefaultFeatures.addJungleExtraVegetation(this);
        BiomeDefaultFeatures.addSurfaceFreezing(this);
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.SHEEP, 12, 4, 4));
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PIG, 10, 4, 4));
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.COW, 8, 4, 4));
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PARROT, 40, 1, 2));
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PANDA, 1, 1, 2));
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
        this.addSpawn(MobCategory.AMBIENT, new Biome.SpawnerData(EntityType.BAT, 10, 8, 8));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SPIDER, 100, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE, 95, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SKELETON, 100, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.CREEPER, 100, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 100, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.WITCH, 5, 1, 1));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.OCELOT, 2, 1, 3));
    }
}
