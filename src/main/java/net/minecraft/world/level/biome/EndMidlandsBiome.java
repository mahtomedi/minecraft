package net.minecraft.world.level.biome;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EndMidlandsBiome extends Biome {
    public EndMidlandsBiome() {
        super(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilder.DEFAULT, SurfaceBuilder.CONFIG_THEEND)
                .precipitation(Biome.Precipitation.NONE)
                .biomeCategory(Biome.BiomeCategory.THEEND)
                .depth(0.1F)
                .scale(0.2F)
                .temperature(0.5F)
                .downfall(0.5F)
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(10518688).build())
                .parent(null)
        );
        this.addStructureStart(Feature.END_CITY.configured(FeatureConfiguration.NONE));
        BiomeDefaultFeatures.addEndCity(this);
        this.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 10, 4, 4));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSkyColor() {
        return 0;
    }
}
