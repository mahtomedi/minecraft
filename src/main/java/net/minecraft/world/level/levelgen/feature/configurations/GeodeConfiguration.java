package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
                    Codec.intRange(1, 10).fieldOf("min_outer_wall_distance").orElse(4).forGetter(param0x -> param0x.minOuterWallDistance),
                    Codec.intRange(1, 20).fieldOf("max_outer_wall_distance").orElse(6).forGetter(param0x -> param0x.maxOuterWallDistance),
                    Codec.intRange(1, 10).fieldOf("min_distribution_points").orElse(3).forGetter(param0x -> param0x.minDistributionPoints),
                    Codec.intRange(1, 20).fieldOf("max_distribution_points").orElse(5).forGetter(param0x -> param0x.maxDistributionPoints),
                    Codec.intRange(0, 10).fieldOf("min_point_offset").orElse(1).forGetter(param0x -> param0x.minPointOffset),
                    Codec.intRange(0, 10).fieldOf("max_point_offset").orElse(3).forGetter(param0x -> param0x.maxPointOffset),
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
    public final int minOuterWallDistance;
    public final int maxOuterWallDistance;
    public final int minDistributionPoints;
    public final int maxDistributionPoints;
    public final int minPointOffset;
    public final int maxPointOffset;
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
        int param6,
        int param7,
        int param8,
        int param9,
        int param10,
        int param11,
        int param12,
        int param13,
        double param14,
        int param15
    ) {
        this.geodeBlockSettings = param0;
        this.geodeLayerSettings = param1;
        this.geodeCrackSettings = param2;
        this.usePotentialPlacementsChance = param3;
        this.useAlternateLayer0Chance = param4;
        this.placementsRequireLayer0Alternate = param5;
        this.minOuterWallDistance = param6;
        this.maxOuterWallDistance = param7;
        this.minDistributionPoints = param8;
        this.maxDistributionPoints = param9;
        this.minPointOffset = param10;
        this.maxPointOffset = param11;
        this.minGenOffset = param12;
        this.maxGenOffset = param13;
        this.noiseMultiplier = param14;
        this.invalidBlocksThreshold = param15;
    }
}
