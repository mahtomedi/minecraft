package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;

public abstract class FloatProvider implements SampledFloat {
    private static final Codec<Either<Float, FloatProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(
        Codec.FLOAT, BuiltInRegistries.FLOAT_PROVIDER_TYPE.byNameCodec().dispatch(FloatProvider::getType, FloatProviderType::codec)
    );
    public static final Codec<FloatProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(
        param0 -> param0.map(ConstantFloat::of, param0x -> param0x),
        param0 -> param0.getType() == FloatProviderType.CONSTANT ? Either.left(((ConstantFloat)param0).getValue()) : Either.right(param0)
    );

    public static Codec<FloatProvider> codec(float param0, float param1) {
        return ExtraCodecs.validate(
            CODEC,
            param2 -> {
                if (param2.getMinValue() < param0) {
                    return DataResult.error(() -> "Value provider too low: " + param0 + " [" + param2.getMinValue() + "-" + param2.getMaxValue() + "]");
                } else {
                    return param2.getMaxValue() > param1
                        ? DataResult.error(() -> "Value provider too high: " + param1 + " [" + param2.getMinValue() + "-" + param2.getMaxValue() + "]")
                        : DataResult.success(param2);
                }
            }
        );
    }

    public abstract float getMinValue();

    public abstract float getMaxValue();

    public abstract FloatProviderType<?> getType();
}
