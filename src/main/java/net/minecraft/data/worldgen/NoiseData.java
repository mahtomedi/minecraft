package net.minecraft.data.worldgen;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseData {
    public static NormalNoise.NoiseParameters bootstrap() {
        registerBiomeNoises(0, Noises.TEMPERATURE, Noises.VEGETATION, Noises.CONTINENTALNESS, Noises.EROSION, Noises.RIDGE, Noises.SHIFT);
        registerBiomeNoises(
            -2, Noises.TEMPERATURE_LARGE, Noises.VEGETATION_LARGE, Noises.CONTINENTALNESS_LARGE, Noises.EROSION_LARGE, Noises.RIDGE_LARGE, Noises.SHIFT_LARGE
        );
        register(Noises.AQUIFER_BARRIER, -3, 1.0);
        register(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS, -7, 1.0);
        register(Noises.AQUIFER_LAVA, -1, 1.0);
        register(Noises.AQUIFER_FLUID_LEVEL_SPREAD, -5, 1.0);
        register(Noises.PILLAR, -7, 1.0, 1.0);
        register(Noises.PILLAR_RARENESS, -8, 1.0);
        register(Noises.PILLAR_THICKNESS, -8, 1.0);
        register(Noises.SPAGHETTI_2D, -7, 1.0);
        register(Noises.SPAGHETTI_2D_ELEVATION, -8, 1.0);
        register(Noises.SPAGHETTI_2D_MODULATOR, -11, 1.0);
        register(Noises.SPAGHETTI_2D_THICKNESS, -11, 1.0);
        register(Noises.SPAGHETTI_3D_1, -7, 1.0);
        register(Noises.SPAGHETTI_3D_2, -7, 1.0);
        register(Noises.SPAGHETTI_3D_RARITY, -11, 1.0);
        register(Noises.SPAGHETTI_3D_THICKNESS, -8, 1.0);
        register(Noises.SPAGHETTI_ROUGHNESS, -5, 1.0);
        register(Noises.SPAGHETTI_ROUGHNESS_MODULATOR, -8, 1.0);
        register(Noises.CAVE_ENTRANCE, -7, 0.4, 0.5, 1.0);
        register(Noises.CAVE_LAYER, -8, 1.0);
        register(Noises.CAVE_CHEESE, -8, 0.5, 1.0, 2.0, 1.0, 2.0, 1.0, 0.0, 2.0, 0.0);
        register(Noises.ORE_VEININESS, -8, 1.0);
        register(Noises.ORE_VEIN_A, -7, 1.0);
        register(Noises.ORE_VEIN_B, -7, 1.0);
        register(Noises.ORE_GAP, -5, 1.0);
        register(Noises.NOODLE, -8, 1.0);
        register(Noises.NOODLE_THICKNESS, -8, 1.0);
        register(Noises.NOODLE_RIDGE_A, -7, 1.0);
        register(Noises.NOODLE_RIDGE_B, -7, 1.0);
        register(Noises.JAGGED, -16, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
        register(Noises.SURFACE, -6, 1.0, 1.0, 1.0);
        register(Noises.CLAY_BANDS_OFFSET, -8, 1.0);
        register(Noises.BADLANDS_PILLAR, -2, 1.0, 1.0, 1.0, 1.0);
        register(Noises.BADLANDS_PILLAR_ROOF, -8, 1.0);
        register(Noises.BADLANDS_SURFACE, -6, 1.0, 1.0, 1.0);
        register(Noises.ICEBERG_PILLAR, -6, 1.0, 1.0, 1.0, 1.0);
        register(Noises.ICEBERG_PILLAR_ROOF, -3, 1.0);
        register(Noises.ICEBERG_SURFACE, -6, 1.0, 1.0, 1.0);
        register(Noises.SWAMP, -2, 1.0);
        register(Noises.CALCITE, -9, 1.0, 1.0, 1.0, 1.0);
        register(Noises.GRAVEL, -8, 1.0, 1.0, 1.0, 1.0);
        register(Noises.POWDER_SNOW, -6, 1.0, 1.0, 1.0, 1.0);
        register(Noises.PACKED_ICE, -7, 1.0, 1.0, 1.0, 1.0);
        register(Noises.ICE, -4, 1.0, 1.0, 1.0, 1.0);
        register(Noises.SOUL_SAND_LAYER, -8, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334);
        register(Noises.GRAVEL_LAYER, -8, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334);
        register(Noises.PATCH, -5, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334);
        register(Noises.NETHERRACK, -3, 1.0, 0.0, 0.0, 0.35);
        register(Noises.NETHER_WART, -3, 1.0, 0.0, 0.0, 0.9);
        register(Noises.NETHER_STATE_SELECTOR, -4, 1.0);
        return BuiltinRegistries.NOISE.iterator().next();
    }

    private static void registerBiomeNoises(
        int param0,
        ResourceKey<NormalNoise.NoiseParameters> param1,
        ResourceKey<NormalNoise.NoiseParameters> param2,
        ResourceKey<NormalNoise.NoiseParameters> param3,
        ResourceKey<NormalNoise.NoiseParameters> param4,
        ResourceKey<NormalNoise.NoiseParameters> param5,
        ResourceKey<NormalNoise.NoiseParameters> param6
    ) {
        register(param1, -10 + param0, 1.5, 0.0, 1.0, 0.0, 0.0, 0.0);
        register(param2, -8 + param0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0);
        register(param3, -9 + param0, 1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0);
        register(param4, -9 + param0, 1.0, 1.0, 0.0, 1.0, 1.0);
        register(param5, -7 + param0, 1.0, 2.0, 1.0, 0.0, 0.0, 0.0);
        register(param6, -3 + param0, 1.0, 1.0, 1.0, 0.0);
    }

    private static void register(ResourceKey<NormalNoise.NoiseParameters> param0, int param1, double param2, double... param3) {
        BuiltinRegistries.register(BuiltinRegistries.NOISE, param0, new NormalNoise.NoiseParameters(param1, param2, param3));
    }
}
