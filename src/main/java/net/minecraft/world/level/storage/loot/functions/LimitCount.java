package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.IntLimiter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LimitCount extends LootItemConditionalFunction {
    private final IntLimiter limiter;

    private LimitCount(LootItemCondition[] param0, IntLimiter param1) {
        super(param0);
        this.limiter = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.LIMIT_COUNT;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        int var0 = this.limiter.applyAsInt(param0.getCount());
        param0.setCount(var0);
        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> limitCount(IntLimiter param0) {
        return simpleBuilder(param1 -> new LimitCount(param1, param0));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<LimitCount> {
        public void serialize(JsonObject param0, LimitCount param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.add("limit", param2.serialize(param1.limiter));
        }

        public LimitCount deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            IntLimiter var0 = GsonHelper.getAsObject(param0, "limit", param1, IntLimiter.class);
            return new LimitCount(param2, var0);
        }
    }
}
