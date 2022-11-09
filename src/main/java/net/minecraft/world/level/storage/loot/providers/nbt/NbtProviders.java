package net.minecraft.world.level.storage.loot.providers.nbt;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;

public class NbtProviders {
    public static final LootNbtProviderType STORAGE = register("storage", new StorageNbtProvider.Serializer());
    public static final LootNbtProviderType CONTEXT = register("context", new ContextNbtProvider.Serializer());

    private static LootNbtProviderType register(String param0, Serializer<? extends NbtProvider> param1) {
        return Registry.register(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, new ResourceLocation(param0), new LootNbtProviderType(param1));
    }

    public static Object createGsonAdapter() {
        return GsonAdapterFactory.builder(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, "provider", "type", NbtProvider::getType)
            .withInlineSerializer(CONTEXT, new ContextNbtProvider.InlineSerializer())
            .build();
    }
}
