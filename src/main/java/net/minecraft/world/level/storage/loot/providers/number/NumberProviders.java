package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class NumberProviders {
    private static final Codec<NumberProvider> TYPED_CODEC = BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE
        .byNameCodec()
        .dispatch(NumberProvider::getType, LootNumberProviderType::codec);
    public static final Codec<NumberProvider> CODEC = ExtraCodecs.lazyInitializedCodec(
        () -> {
            Codec<NumberProvider> var0 = ExtraCodecs.withAlternative(TYPED_CODEC, UniformGenerator.CODEC);
            return Codec.either(ConstantValue.INLINE_CODEC, var0)
                .xmap(
                    param0 -> param0.map(Function.identity(), Function.identity()),
                    param0 -> param0 instanceof ConstantValue var0x ? Either.left(var0x) : Either.right(param0)
                );
        }
    );
    public static final LootNumberProviderType CONSTANT = register("constant", ConstantValue.CODEC);
    public static final LootNumberProviderType UNIFORM = register("uniform", UniformGenerator.CODEC);
    public static final LootNumberProviderType BINOMIAL = register("binomial", BinomialDistributionGenerator.CODEC);
    public static final LootNumberProviderType SCORE = register("score", ScoreboardValue.CODEC);

    private static LootNumberProviderType register(String param0, Codec<? extends NumberProvider> param1) {
        return Registry.register(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, new ResourceLocation(param0), new LootNumberProviderType(param1));
    }
}
