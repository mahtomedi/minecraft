package net.minecraft.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Registry;

public abstract class FloatProvider {
    private static final Codec<Either<Float, FloatProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(
        Codec.FLOAT, Registry.FLOAT_PROVIDER_TYPES.dispatch(FloatProvider::getType, FloatProviderType::codec)
    );
    public static final Codec<FloatProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(
        param0 -> param0.map(ConstantFloat::of, param0x -> param0x),
        param0 -> param0.getType() == FloatProviderType.CONSTANT ? Either.left(((ConstantFloat)param0).getValue()) : Either.right(param0)
    );

    public static Codec<FloatProvider> codec(float param0, float param1) {
        Function<FloatProvider, DataResult<FloatProvider>> var0 = param2 -> {
            if (param2.getMinValue() < param0) {
                return DataResult.error("Value provider too low: " + param0 + " [" + param2.getMinValue() + "-" + param2.getMaxValue() + "]");
            } else {
                return param2.getMaxValue() > param1
                    ? DataResult.error("Value provider too high: " + param1 + " [" + param2.getMinValue() + "-" + param2.getMaxValue() + "]")
                    : DataResult.success(param2);
            }
        };
        return CODEC.flatXmap(var0, var0);
    }

    public abstract float sample(Random var1);

    public abstract float getMinValue();

    public abstract float getMaxValue();

    public abstract FloatProviderType<?> getType();
}
