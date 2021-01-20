package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class GeodeLayerSettings {
    private static final Codec<Double> LAYER_RANGE = Codec.doubleRange(0.01, 50.0);
    public static final Codec<GeodeLayerSettings> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    LAYER_RANGE.fieldOf("filling").orElse(1.7).forGetter(param0x -> param0x.filling),
                    LAYER_RANGE.fieldOf("inner_layer").orElse(2.2).forGetter(param0x -> param0x.innerLayer),
                    LAYER_RANGE.fieldOf("middle_layer").orElse(3.2).forGetter(param0x -> param0x.middleLayer),
                    LAYER_RANGE.fieldOf("outer_layer").orElse(4.2).forGetter(param0x -> param0x.outerLayer)
                )
                .apply(param0, GeodeLayerSettings::new)
    );
    public final double filling;
    public final double innerLayer;
    public final double middleLayer;
    public final double outerLayer;

    public GeodeLayerSettings(double param0, double param1, double param2, double param3) {
        this.filling = param0;
        this.innerLayer = param1;
        this.middleLayer = param2;
        this.outerLayer = param3;
    }
}
