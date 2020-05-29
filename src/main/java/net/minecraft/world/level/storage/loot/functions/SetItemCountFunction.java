package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomIntGenerator;
import net.minecraft.world.level.storage.loot.RandomIntGenerators;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetItemCountFunction extends LootItemConditionalFunction {
    private final RandomIntGenerator value;

    private SetItemCountFunction(LootItemCondition[] param0, RandomIntGenerator param1) {
        super(param0);
        this.value = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_COUNT;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        param0.setCount(this.value.getInt(param1.getRandom()));
        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> setCount(RandomIntGenerator param0) {
        return simpleBuilder(param1 -> new SetItemCountFunction(param1, param0));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemCountFunction> {
        public void serialize(JsonObject param0, SetItemCountFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.add("count", RandomIntGenerators.serialize(param1.value, param2));
        }

        public SetItemCountFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            RandomIntGenerator var0 = RandomIntGenerators.deserialize(param0.get("count"), param1);
            return new SetItemCountFunction(param2, var0);
        }
    }
}
