package net.minecraft.world.level.storage.loot.entries;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LootItem extends LootPoolSingletonContainer {
    final Item item;

    LootItem(Item param0, int param1, int param2, LootItemCondition[] param3, LootItemFunction[] param4) {
        super(param1, param2, param3, param4);
        this.item = param0;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.ITEM;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> param0, LootContext param1) {
        param0.accept(new ItemStack(this.item));
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableItem(ItemLike param0) {
        return simpleBuilder((param1, param2, param3, param4) -> new LootItem(param0.asItem(), param1, param2, param3, param4));
    }

    public static class Serializer extends LootPoolSingletonContainer.Serializer<LootItem> {
        public void serializeCustom(JsonObject param0, LootItem param1, JsonSerializationContext param2) {
            super.serializeCustom(param0, param1, param2);
            ResourceLocation var0 = BuiltInRegistries.ITEM.getKey(param1.item);
            if (var0 == null) {
                throw new IllegalArgumentException("Can't serialize unknown item " + param1.item);
            } else {
                param0.addProperty("name", var0.toString());
            }
        }

        protected LootItem deserialize(
            JsonObject param0, JsonDeserializationContext param1, int param2, int param3, LootItemCondition[] param4, LootItemFunction[] param5
        ) {
            Item var0 = GsonHelper.getAsItem(param0, "name");
            return new LootItem(var0, param2, param3, param4, param5);
        }
    }
}
