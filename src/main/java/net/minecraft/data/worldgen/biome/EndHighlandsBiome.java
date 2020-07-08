package net.minecraft.data.worldgen.biome;

import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EndHighlandsBiome extends Biome {
    public EndHighlandsBiome() {
        super(
            new Biome.BiomeBuilder()
                .surfaceBuilder(SurfaceBuilders.END)
                .precipitation(Biome.Precipitation.NONE)
                .biomeCategory(Biome.BiomeCategory.THEEND)
                .depth(0.1F)
                .scale(0.2F)
                .temperature(0.5F)
                .downfall(0.5F)
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
        this.addStructureStart(StructureFeatures.END_CITY);
        this.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_GATEWAY);
        this.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CHORUS_PLANT);
        BiomeDefaultFeatures.endSpawns(this);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSkyColor() {
        return 0;
    }
}