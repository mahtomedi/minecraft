package net.minecraft.world.level.storage.loot.providers.score;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;

public class ScoreboardNameProviders {
    public static final LootScoreProviderType FIXED = register("fixed", new FixedScoreboardNameProvider.Serializer());
    public static final LootScoreProviderType CONTEXT = register("context", new ContextScoreboardNameProvider.Serializer());

    private static LootScoreProviderType register(String param0, Serializer<? extends ScoreboardNameProvider> param1) {
        return Registry.register(BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE, new ResourceLocation(param0), new LootScoreProviderType(param1));
    }

    public static Object createGsonAdapter() {
        return GsonAdapterFactory.builder(BuiltInRegistries.LOOT_SCORE_PROVIDER_TYPE, "provider", "type", ScoreboardNameProvider::getType)
            .withInlineSerializer(CONTEXT, new ContextScoreboardNameProvider.InlineSerializer())
            .build();
    }
}
