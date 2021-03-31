package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public interface FloatProviderType<P extends FloatProvider> {
    FloatProviderType<ConstantFloat> CONSTANT = register("constant", ConstantFloat.CODEC);
    FloatProviderType<UniformFloat> UNIFORM = register("uniform", UniformFloat.CODEC);
    FloatProviderType<ClampedNormalFloat> CLAMPED_NORMAL = register("clamped_normal", ClampedNormalFloat.CODEC);
    FloatProviderType<TrapezoidFloat> TRAPEZOID = register("trapezoid", TrapezoidFloat.CODEC);

    Codec<P> codec();

    static <P extends FloatProvider> FloatProviderType<P> register(String param0, Codec<P> param1) {
        return Registry.register(Registry.FLOAT_PROVIDER_TYPES, param0, () -> param1);
    }
}
