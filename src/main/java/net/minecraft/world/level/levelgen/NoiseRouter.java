package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;

public record NoiseRouter(
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
    public static final Codec<NoiseRouter> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    field("barrier", NoiseRouter::barrierNoise),
                    field("fluid_level_floodedness", NoiseRouter::fluidLevelFloodednessNoise),
                    field("fluid_level_spread", NoiseRouter::fluidLevelSpreadNoise),
                    field("lava", NoiseRouter::lavaNoise),
                    field("temperature", NoiseRouter::temperature),
                    field("vegetation", NoiseRouter::vegetation),
                    field("continents", NoiseRouter::continents),
                    field("erosion", NoiseRouter::erosion),
                    field("depth", NoiseRouter::depth),
                    field("ridges", NoiseRouter::ridges),
                    field("initial_density_without_jaggedness", NoiseRouter::initialDensityWithoutJaggedness),
                    field("final_density", NoiseRouter::finalDensity),
                    field("vein_toggle", NoiseRouter::veinToggle),
                    field("vein_ridged", NoiseRouter::veinRidged),
                    field("vein_gap", NoiseRouter::veinGap)
                )
                .apply(param0, NoiseRouter::new)
    );

    private static RecordCodecBuilder<NoiseRouter, DensityFunction> field(String param0, Function<NoiseRouter, DensityFunction> param1) {
        return DensityFunction.HOLDER_HELPER_CODEC.fieldOf(param0).forGetter(param1);
    }

    public NoiseRouter mapAll(DensityFunction.Visitor param0) {
        return new NoiseRouter(
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
