package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public record DistancePredicate(
    MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z, MinMaxBounds.Doubles horizontal, MinMaxBounds.Doubles absolute
) {
    public static final Codec<DistancePredicate> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "x", MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::x),
                    ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "y", MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::y),
                    ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "z", MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::z),
                    ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "horizontal", MinMaxBounds.Doubles.ANY)
                        .forGetter(DistancePredicate::horizontal),
                    ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "absolute", MinMaxBounds.Doubles.ANY).forGetter(DistancePredicate::absolute)
                )
                .apply(param0, DistancePredicate::new)
    );

    public static DistancePredicate horizontal(MinMaxBounds.Doubles param0) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, param0, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate vertical(MinMaxBounds.Doubles param0) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, param0, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
    }

    public static DistancePredicate absolute(MinMaxBounds.Doubles param0) {
        return new DistancePredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, param0);
    }

    public boolean matches(double param0, double param1, double param2, double param3, double param4, double param5) {
        float var0 = (float)(param0 - param3);
        float var1 = (float)(param1 - param4);
        float var2 = (float)(param2 - param5);
        if (!this.x.matches((double)Mth.abs(var0)) || !this.y.matches((double)Mth.abs(var1)) || !this.z.matches((double)Mth.abs(var2))) {
            return false;
        } else if (!this.horizontal.matchesSqr((double)(var0 * var0 + var2 * var2))) {
            return false;
        } else {
            return this.absolute.matchesSqr((double)(var0 * var0 + var1 * var1 + var2 * var2));
        }
    }
}
