package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.BastionPieces;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;
import net.minecraft.world.level.levelgen.feature.configurations.ChanceRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CountRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MultiJigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public class SoulSandValleyBiome extends Biome {
    protected SoulSandValleyBiome() {
        super(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilder.SOUL_SAND_VALLEY, SurfaceBuilder.CONFIG_SOUL_SAND_VALLEY)
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
                        .ambientParticle(new AmbientParticleSettings(ParticleTypes.ASH, 0.00625F, param0 -> 0.0, param0 -> 0.0, param0 -> 0.0))
                        .ambientLoopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP)
                        .ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0))
                        .ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111))
                        .backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY))
                        .build()
                )
                .parent(null)
                .optimalParameters(ImmutableList.of(new Biome.ClimateParameters(0.0F, -0.7F, 0.0F, 0.0F, 0.4F)))
        );
        this.addStructureStart(Feature.NETHER_BRIDGE.configured(FeatureConfiguration.NONE));
        this.addStructureStart(Feature.NETHER_FOSSIL.configured(FeatureConfiguration.NONE));
        this.addStructureStart(Feature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.NETHER)));
        this.addStructureStart(Feature.BASTION_REMNANT.configured(new MultiJigsawConfiguration(BastionPieces.POOLS)));
        BiomeDefaultFeatures.addStructureFeaturePlacement(this);
        this.addCarver(GenerationStep.Carving.AIR, makeCarver(WorldCarver.NETHER_CAVE, new ProbabilityFeatureConfiguration(0.2F)));
        this.addFeature(
            GenerationStep.Decoration.VEGETAL_DECORATION,
            Feature.SPRING
                .configured(BiomeDefaultFeatures.LAVA_SPRING_CONFIG)
                .decorated(FeatureDecorator.COUNT_VERY_BIASED_RANGE.configured(new CountRangeDecoratorConfiguration(20, 8, 16, 256)))
        );
        this.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Feature.NETHER_BRIDGE.configured(FeatureConfiguration.NONE));
        this.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Feature.NETHER_FOSSIL.configured(FeatureConfiguration.NONE));
        this.addFeature(
            GenerationStep.Decoration.UNDERGROUND_DECORATION, Feature.RUINED_PORTAL.configured(new RuinedPortalConfiguration(RuinedPortalFeature.Type.NETHER))
        );
        this.addFeature(
            GenerationStep.Decoration.UNDERGROUND_DECORATION,
            Feature.BASALT_PILLAR
                .configured(FeatureConfiguration.NONE)
                .decorated(FeatureDecorator.COUNT_RANGE.configured(new CountRangeDecoratorConfiguration(10, 0, 0, 128)))
        );
        this.addFeature(
            GenerationStep.Decoration.UNDERGROUND_DECORATION,
            Feature.SPRING
                .configured(BiomeDefaultFeatures.OPEN_NETHER_SPRING_CONFIG)
                .decorated(FeatureDecorator.COUNT_RANGE.configured(new CountRangeDecoratorConfiguration(8, 4, 8, 128)))
        );
        this.addFeature(
            GenerationStep.Decoration.UNDERGROUND_DECORATION,
            Feature.GLOWSTONE_BLOB
                .configured(FeatureConfiguration.NONE)
                .decorated(FeatureDecorator.LIGHT_GEM_CHANCE.configured(new FrequencyDecoratorConfiguration(10)))
        );
        this.addFeature(
            GenerationStep.Decoration.UNDERGROUND_DECORATION,
            Feature.GLOWSTONE_BLOB
                .configured(FeatureConfiguration.NONE)
                .decorated(FeatureDecorator.COUNT_RANGE.configured(new CountRangeDecoratorConfiguration(10, 0, 0, 128)))
        );
        this.addFeature(
            GenerationStep.Decoration.UNDERGROUND_DECORATION,
            Feature.RANDOM_PATCH
                .configured(BiomeDefaultFeatures.CRIMSON_ROOTS_CONFIG)
                .decorated(FeatureDecorator.CHANCE_RANGE.configured(new ChanceRangeDecoratorConfiguration(1.0F, 0, 0, 128)))
        );
        this.addFeature(
            GenerationStep.Decoration.UNDERGROUND_DECORATION,
            Feature.RANDOM_PATCH
                .configured(BiomeDefaultFeatures.FIRE_CONFIG)
                .decorated(FeatureDecorator.FIRE.configured(new FrequencyDecoratorConfiguration(10)))
        );
        this.addFeature(
            GenerationStep.Decoration.UNDERGROUND_DECORATION,
            Feature.RANDOM_PATCH
                .configured(BiomeDefaultFeatures.SOUL_FIRE_CONFIG)
                .decorated(FeatureDecorator.FIRE.configured(new FrequencyDecoratorConfiguration(10)))
        );
        this.addFeature(
            GenerationStep.Decoration.UNDERGROUND_DECORATION,
            Feature.ORE
                .configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.MAGMA_BLOCK.defaultBlockState(), 33))
                .decorated(FeatureDecorator.MAGMA.configured(new FrequencyDecoratorConfiguration(4)))
        );
        this.addFeature(
            GenerationStep.Decoration.UNDERGROUND_DECORATION,
            Feature.SPRING
                .configured(BiomeDefaultFeatures.CLOSED_NETHER_SPRING_CONFIG)
                .decorated(FeatureDecorator.COUNT_RANGE.configured(new CountRangeDecoratorConfiguration(16, 10, 20, 128)))
        );
        BiomeDefaultFeatures.addNetherDefaultOres(this);
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SKELETON, 2, 5, 5));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.GHAST, 50, 4, 4));
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 1, 4, 4));
        this.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.STRIDER, 60, 2, 4));
        double var0 = 1.0;
        double var1 = 0.08;
        this.addMobCharge(EntityType.SKELETON, 1.0, 0.08);
        this.addMobCharge(EntityType.GHAST, 1.0, 0.08);
        this.addMobCharge(EntityType.ENDERMAN, 1.0, 0.08);
        this.addMobCharge(EntityType.STRIDER, 1.0, 0.08);
    }
}
