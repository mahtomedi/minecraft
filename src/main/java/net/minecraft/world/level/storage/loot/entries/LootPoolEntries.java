package net.minecraft.world.level.storage.loot.entries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.Serializer;

public class LootPoolEntries {
    public static final LootPoolEntryType EMPTY = register("empty", new EmptyLootItem.Serializer());
    public static final LootPoolEntryType ITEM = register("item", new LootItem.Serializer());
    public static final LootPoolEntryType REFERENCE = register("loot_table", new LootTableReference.Serializer());
    public static final LootPoolEntryType DYNAMIC = register("dynamic", new DynamicLoot.Serializer());
    public static final LootPoolEntryType TAG = register("tag", new TagEntry.Serializer());
    public static final LootPoolEntryType ALTERNATIVES = register("alternatives", CompositeEntryBase.createSerializer(AlternativesEntry::new));
    public static final LootPoolEntryType SEQUENCE = register("sequence", CompositeEntryBase.createSerializer(SequentialEntry::new));
    public static final LootPoolEntryType GROUP = register("group", CompositeEntryBase.createSerializer(EntryGroup::new));

    private static LootPoolEntryType register(String param0, Serializer<? extends LootPoolEntryContainer> param1) {
        return Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, new ResourceLocation(param0), new LootPoolEntryType(param1));
    }

    public static Object createGsonAdapter() {
        return GsonAdapterFactory.builder(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, "entry", "type", LootPoolEntryContainer::getType).build();
    }
}
