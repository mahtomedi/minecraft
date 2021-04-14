package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;

public class GeodeConfiguration implements FeatureConfiguration {
    public static final Codec<Double> CHANCE_RANGE = Codec.doubleRange(0.0, 1.0);
    public static final Codec<GeodeConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    GeodeBlockSettings.CODEC.fieldOf("blocks").forGetter(param0x -> param0x.geodeBlockSettings),
                    GeodeLayerSettings.CODEC.fieldOf("layers").forGetter(param0x -> param0x.geodeLayerSettings),
                    GeodeCrackSettings.CODEC.fieldOf("crack").forGetter(param0x -> param0x.geodeCrackSettings),
                    CHANCE_RANGE.fieldOf("use_potential_placements_chance").orElse(0.35).forGetter(param0x -> param0x.usePotentialPlacementsChance),
                    CHANCE_RANGE.fieldOf("use_alternate_layer0_chance").orElse(0.0).forGetter(param0x -> param0x.useAlternateLayer0Chance),
                    Codec.BOOL.fieldOf("placements_require_layer0_alternate").orElse(true).forGetter(param0x -> param0x.placementsRequireLayer0Alternate),
                    IntProvider.codec(1, 20).fieldOf("outer_wall_distance").orElse(UniformInt.of(4, 5)).forGetter(param0x -> param0x.outerWallDistance),
                    IntProvider.codec(1, 20).fieldOf("distribution_points").orElse(UniformInt.of(3, 4)).forGetter(param0x -> param0x.distributionPoints),
                    IntProvider.codec(0, 10).fieldOf("point_offset").orElse(UniformInt.of(1, 2)).forGetter(param0x -> param0x.pointOffset),
                    Codec.INT.fieldOf("min_gen_offset").orElse(-16).forGetter(param0x -> param0x.minGenOffset),
                    Codec.INT.fieldOf("max_gen_offset").orElse(16).forGetter(param0x -> param0x.maxGenOffset),
                    CHANCE_RANGE.fieldOf("noise_multiplier").orElse(0.05).forGetter(param0x -> param0x.noiseMultiplier),
                    Codec.INT.fieldOf("invalid_blocks_threshold").forGetter(param0x -> param0x.invalidBlocksThreshold)
                )
                .apply(param0, GeodeConfiguration::new)
    );
    public final GeodeBlockSettings geodeBlockSettings;
    public final GeodeLayerSettings geodeLayerSettings;
    public final GeodeCrackSettings geodeCrackSettings;
    public final double usePotentialPlacementsChance;
    public final double useAlternateLayer0Chance;
    public final boolean placementsRequireLayer0Alternate;
    public final IntProvider outerWallDistance;
    public final IntProvider distributionPoints;
    public final IntProvider pointOffset;
    public final int minGenOffset;
    public final int maxGenOffset;
    public final double noiseMultiplier;
    public final int invalidBlocksThreshold;

    public GeodeConfiguration(
        GeodeBlockSettings param0,
        GeodeLayerSettings param1,
        GeodeCrackSettings param2,
        double param3,
        double param4,
        boolean param5,
        IntProvider param6,
        IntProvider param7,
        IntProvider param8,
        int param9,
        int param10,
        double param11,
        int param12
    ) {
        this.geodeBlockSettings = param0;
        this.geodeLayerSettings = param1;
        this.geodeCrackSettings = param2;
        this.usePotentialPlacementsChance = param3;
        this.useAlternateLayer0Chance = param4;
        this.placementsRequireLayer0Alternate = param5;
        this.outerWallDistance = param6;
        this.distributionPoints = param7;
        this.pointOffset = param8;
        this.minGenOffset = param9;
        this.maxGenOffset = param10;
        this.noiseMultiplier = param11;
        this.invalidBlocksThreshold = param12;
    }
}
