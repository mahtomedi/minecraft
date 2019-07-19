package net.minecraft.world.level.biome;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public final class FrozenOceanBiome extends Biome {
    protected static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise(new Random(3456L), 3);

    public FrozenOceanBiome() {
        super(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilder.FROZEN_OCEAN, SurfaceBuilder.CONFIG_GRASS)
                .precipitation(Biome.Precipitation.SNOW)
                .biomeCategory(Biome.BiomeCategory.OCEAN)
                .depth(-1.0F)
                .scale(0.1F)
                .temperature(0.0F)
                .downfall(0.5F)
                .waterColor(3750089)
                .waterFogColor(329011)
                .parent(null)
        );
        this.addStructureStart(Feature.OCEAN_RUIN, new OceanRuinConfiguration(OceanRuinFeature.Type.COLD, 0.3F, 0.9F));
        this.addStructureStart(Feature.MINESHAFT, new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL));
        this.addStructureStart(Feature.SHIPWRECK, new ShipwreckConfiguration(false));
        BiomeDefaultFeatures.addOceanCarvers(this);
        BiomeDefaultFeatures.addStructureFeaturePlacement(this);
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
        this.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.SALMON, 15, 1, 5));
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
        this.addSpawn(MobCategory.AMBIENT, new Biome.SpawnerData(EntityType.BAT, 10, 8, 8));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SPIDER, 100, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE, 95, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.DROWNED, 5, 1, 1));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SKELETON, 100, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.CREEPER, 100, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 100, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.WITCH, 5, 1, 1));
    }

    @Override
    protected float getTemperatureNoCache(BlockPos param0) {
        float var0 = this.getTemperature();
        double var1 = FROZEN_TEMPERATURE_NOISE.getValue((double)param0.getX() * 0.05, (double)param0.getZ() * 0.05);
        double var2 = BIOME_INFO_NOISE.getValue((double)param0.getX() * 0.2, (double)param0.getZ() * 0.2);
        double var3 = var1 + var2;
        if (var3 < 0.3) {
            double var4 = BIOME_INFO_NOISE.getValue((double)param0.getX() * 0.09, (double)param0.getZ() * 0.09);
            if (var4 < 0.8) {
                var0 = 0.2F;
            }
        }

        if (param0.getY() > 64) {
            float var5 = (float)(TEMPERATURE_NOISE.getValue((double)((float)param0.getX() / 8.0F), (double)((float)param0.getZ() / 8.0F)) * 4.0);
            return var0 - (var5 + (float)param0.getY() - 64.0F) * 0.05F / 30.0F;
        } else {
            return var0;
        }
    }
}
