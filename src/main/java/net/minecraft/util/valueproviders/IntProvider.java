package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public abstract class IntProvider {
    private static final Codec<Either<Integer, IntProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either(
        Codec.INT, BuiltInRegistries.INT_PROVIDER_TYPE.byNameCodec().dispatch(IntProvider::getType, IntProviderType::codec)
    );
    public static final Codec<IntProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(
        param0 -> param0.map(ConstantInt::of, param0x -> param0x),
        param0 -> param0.getType() == IntProviderType.CONSTANT ? Either.left(((ConstantInt)param0).getValue()) : Either.right(param0)
    );
    public static final Codec<IntProvider> NON_NEGATIVE_CODEC = codec(0, Integer.MAX_VALUE);
    public static final Codec<IntProvider> POSITIVE_CODEC = codec(1, Integer.MAX_VALUE);

    public static Codec<IntProvider> codec(int param0, int param1) {
        return codec(param0, param1, CODEC);
    }

    public static <T extends IntProvider> Codec<T> codec(int param0, int param1, Codec<T> param2) {
        return ExtraCodecs.validate(
            param2,
            param2x -> {
                if (param2x.getMinValue() < param0) {
                    return DataResult.error("Value provider too low: " + param0 + " [" + param2x.getMinValue() + "-" + param2x.getMaxValue() + "]");
                } else {
                    return param2x.getMaxValue() > param1
                        ? DataResult.error("Value provider too high: " + param1 + " [" + param2x.getMinValue() + "-" + param2x.getMaxValue() + "]")
                        : DataResult.success(param2x);
                }
            }
        );
    }

    public abstract int sample(RandomSource var1);

    public abstract int getMinValue();

    public abstract int getMaxValue();

    public abstract IntProviderType<?> getType();
}
