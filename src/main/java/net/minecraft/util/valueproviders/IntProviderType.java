package net.minecraft.util.valueproviders;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;

public interface IntProviderType<P extends IntProvider> {
    IntProviderType<ConstantInt> CONSTANT = register("constant", ConstantInt.CODEC);
    IntProviderType<UniformInt> UNIFORM = register("uniform", UniformInt.CODEC);

    Codec<P> codec();

    static <P extends IntProvider> IntProviderType<P> register(String param0, Codec<P> param1) {
        return Registry.register(Registry.INT_PROVIDER_TYPES, param0, () -> param1);
    }
}
