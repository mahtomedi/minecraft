package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;

public record NoiseRouterWithOnlyNoises(
    DensityFunction barrierNoise,
    DensityFunction fluidLevelFloodednessNoise,
    DensityFunction fluidLevelSpreadNoise,
    DensityFunction lavaNoise,
    DensityFunction temperature,
    DensityFunction vegetation,
    DensityFunction continents,
    DensityFunction erosion,
    DensityFunction depth,
    DensityFunction ridges,
    DensityFunction initialDensityWithoutJaggedness,
    DensityFunction finalDensity,
    DensityFunction veinToggle,
    DensityFunction veinRidged,
    DensityFunction veinGap
) {
    public static final Codec<NoiseRouterWithOnlyNoises> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    field("barrier", NoiseRouterWithOnlyNoises::barrierNoise),
                    field("fluid_level_floodedness", NoiseRouterWithOnlyNoises::fluidLevelFloodednessNoise),
                    field("fluid_level_spread", NoiseRouterWithOnlyNoises::fluidLevelSpreadNoise),
                    field("lava", NoiseRouterWithOnlyNoises::lavaNoise),
                    field("temperature", NoiseRouterWithOnlyNoises::temperature),
                    field("vegetation", NoiseRouterWithOnlyNoises::vegetation),
                    field("continents", NoiseRouterWithOnlyNoises::continents),
                    field("erosion", NoiseRouterWithOnlyNoises::erosion),
                    field("depth", NoiseRouterWithOnlyNoises::depth),
                    field("ridges", NoiseRouterWithOnlyNoises::ridges),
                    field("initial_density_without_jaggedness", NoiseRouterWithOnlyNoises::initialDensityWithoutJaggedness),
                    field("final_density", NoiseRouterWithOnlyNoises::finalDensity),
                    field("vein_toggle", NoiseRouterWithOnlyNoises::veinToggle),
                    field("vein_ridged", NoiseRouterWithOnlyNoises::veinRidged),
                    field("vein_gap", NoiseRouterWithOnlyNoises::veinGap)
                )
                .apply(param0, NoiseRouterWithOnlyNoises::new)
    );

    private static RecordCodecBuilder<NoiseRouterWithOnlyNoises, DensityFunction> field(
        String param0, Function<NoiseRouterWithOnlyNoises, DensityFunction> param1
    ) {
        return DensityFunction.HOLDER_HELPER_CODEC.fieldOf(param0).forGetter(param1);
    }

    public NoiseRouterWithOnlyNoises mapAll(DensityFunction.Visitor param0) {
        return new NoiseRouterWithOnlyNoises(
            this.barrierNoise.mapAll(param0),
            this.fluidLevelFloodednessNoise.mapAll(param0),
            this.fluidLevelSpreadNoise.mapAll(param0),
            this.lavaNoise.mapAll(param0),
            this.temperature.mapAll(param0),
            this.vegetation.mapAll(param0),
            this.continents.mapAll(param0),
            this.erosion.mapAll(param0),
            this.depth.mapAll(param0),
            this.ridges.mapAll(param0),
            this.initialDensityWithoutJaggedness.mapAll(param0),
            this.finalDensity.mapAll(param0),
            this.veinToggle.mapAll(param0),
            this.veinRidged.mapAll(param0),
            this.veinGap.mapAll(param0)
        );
    }
}
