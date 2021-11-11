package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.Registry;

public abstract class IntProvider {
    private static final Codec<Either<Integer, IntProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(
        Codec.INT, Registry.INT_PROVIDER_TYPES.byNameCodec().dispatch(IntProvider::getType, IntProviderType::codec)
    );
    public static final Codec<IntProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(
        param0 -> param0.map(ConstantInt::of, param0x -> param0x),
        param0 -> param0.getType() == IntProviderType.CONSTANT ? Either.left(((ConstantInt)param0).getValue()) : Either.right(param0)
    );
    public static final Codec<IntProvider> NON_NEGATIVE_CODEC = codec(0, Integer.MAX_VALUE);
    public static final Codec<IntProvider> POSITIVE_CODEC = codec(1, Integer.MAX_VALUE);

    public static Codec<IntProvider> codec(int param0, int param1) {
        Function<IntProvider, DataResult<IntProvider>> var0 = param2 -> {
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

    public abstract int sample(Random var1);

    public abstract int getMinValue();

    public abstract int getMaxValue();

    public abstract IntProviderType<?> getType();
}
