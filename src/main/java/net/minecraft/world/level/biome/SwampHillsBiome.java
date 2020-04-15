package net.minecraft.world.level.biome;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public final class SwampHillsBiome extends Biome {
    protected SwampHillsBiome() {
        super(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilder.SWAMP, SurfaceBuilder.CONFIG_GRASS)
                .precipitation(Biome.Precipitation.RAIN)
                .biomeCategory(Biome.BiomeCategory.SWAMP)
                .depth(-0.1F)
                .scale(0.3F)
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
                .parent("swamp")
        );
        this.addStructureStart(Feature.MINESHAFT.configured(new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL)));
        this.addStructureStart(Feature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.SWAMP)));
        BiomeDefaultFeatures.addDefaultCarvers(this);
        BiomeDefaultFeatures.addStructureFeaturePlacement(this);
        BiomeDefaultFeatures.addDefaultLakes(this);
        BiomeDefaultFeatures.addDefaultMonsterRoom(this);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(this);
        BiomeDefaultFeatures.addDefaultOres(this);
        BiomeDefaultFeatures.addSwampClayDisk(this);
        BiomeDefaultFeatures.addSwampVegetation(this);
        BiomeDefaultFeatures.addDefaultMushrooms(this);
        BiomeDefaultFeatures.addSwampExtraVegetation(this);
        BiomeDefaultFeatures.addDefaultSprings(this);
        BiomeDefaultFeatures.addSwampExtraDecoration(this);
        BiomeDefaultFeatures.addSurfaceFreezing(this);
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.SHEEP, 12, 4, 4));
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PIG, 10, 4, 4));
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.COW, 8, 4, 4));
        this.addSpawn(MobCategory.AMBIENT, new Biome.SpawnerData(EntityType.BAT, 10, 8, 8));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SPIDER, 100, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE, 95, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE_VILLAGER, 5, 1, 1));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SKELETON, 100, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.CREEPER, 100, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 100, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.WITCH, 5, 1, 1));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 1, 1, 1));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getGrassColor(double param0, double param1) {
        double var0 = BIOME_INFO_NOISE.getValue(param0 * 0.0225, param1 * 0.0225, false);
        return var0 < -0.1 ? 5011004 : 6975545;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getFoliageColor() {
        return 6975545;
    }
}
