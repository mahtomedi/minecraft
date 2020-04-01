package net.minecraft.world.level.biome;

import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class BiomeGenerator {
    public static Biome magicize(int param0) {
        Random var0 = new Random((long)param0);
        return new BiomeGenerator.GeneratedBiome(var0);
    }

    static class GeneratedBiome extends Biome {
        public GeneratedBiome(Random param0) {
            super(Biome.random(param0));
            Util.randomObjectStream(param0, 4, Registry.CARVER)
                .forEach(param1 -> this.addCarver(Util.randomEnum(GenerationStep.Carving.class, param0), Biome.makeRandomCarver(param1, param0)));
            Util.randomObjectStream(param0, 32, Registry.ENTITY_TYPE).forEach(param1 -> {
                int var0x = param0.nextInt(4);
                int var1x = var0x + param0.nextInt(4);
                this.addSpawn(param1.getCategory(), new Biome.SpawnerData(param1, param0.nextInt(20) + 1, var0x, var1x));
            });
            Util.randomObjectStream(param0, 5, Registry.STRUCTURE_FEATURE)
                .forEach(param1 -> this.addFeatureStart(Util.randomEnum(GenerationStep.Decoration.class, param0), param1.random2(param0)));

            for(int var0 = 0; var0 < 32; ++var0) {
                this.addFeature(
                    Util.randomEnum(GenerationStep.Decoration.class, param0),
                    Registry.FEATURE.getRandom(param0).random(param0).decorated(Registry.DECORATOR.getRandom(param0).random(param0))
                );
            }

        }

        private <C extends FeatureConfiguration> void addFeatureStart(
            GenerationStep.Decoration param0, ConfiguredFeature<C, ? extends StructureFeature<C>> param1
        ) {
            this.addStructureStart(param1);
            this.addFeature(param0, param1);
        }
    }
}
