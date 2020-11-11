package net.minecraft.world.level.storage.loot.providers.number;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;

public class NumberProviders {
    public static final LootNumberProviderType CONSTANT = register("constant", new ConstantValue.Serializer());
    public static final LootNumberProviderType UNIFORM = register("uniform", new UniformGenerator.Serializer());
    public static final LootNumberProviderType BINOMIAL = register("binomial", new BinomialDistributionGenerator.Serializer());
    public static final LootNumberProviderType SCORE = register("score", new ScoreboardValue.Serializer());

    private static LootNumberProviderType register(String param0, Serializer<? extends NumberProvider> param1) {
        return Registry.register(Registry.LOOT_NUMBER_PROVIDER_TYPE, new ResourceLocation(param0), new LootNumberProviderType(param1));
    }

    public static Object createGsonAdapter() {
        return GsonAdapterFactory.builder(Registry.LOOT_NUMBER_PROVIDER_TYPE, "provider", "type", NumberProvider::getType)
            .withDefaultSerializer(CONSTANT, new ConstantValue.DefaultSerializer())
            .build();
    }
}