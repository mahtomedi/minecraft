package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EmptyLootItem extends LootPoolSingletonContainer {
    EmptyLootItem(int param0, int param1, LootItemCondition[] param2, LootItemFunction[] param3) {
        super(param0, param1, param2, param3);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.EMPTY;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> param0, LootContext param1) {
    }

    public static LootPoolSingletonContainer.Builder<?> emptyItem() {
        return simpleBuilder(EmptyLootItem::new);
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<EmptyLootItem> {
        public EmptyLootItem deserialize(
            JsonObject param0, JsonDeserializationContext param1, int param2, int param3, LootItemCondition[] param4, LootItemFunction[] param5
        ) {
            return new EmptyLootItem(param2, param3, param4, param5);
        }
    }
}
