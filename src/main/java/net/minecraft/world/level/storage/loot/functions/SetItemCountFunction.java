package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetItemCountFunction extends LootItemConditionalFunction {
    private final NumberProvider value;
    private final boolean add;

    private SetItemCountFunction(LootItemCondition[] param0, NumberProvider param1, boolean param2) {
        super(param0);
        this.value = param1;
        this.add = param2;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_COUNT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.value.getReferencedContextParams();
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        int var0 = this.add ? param0.getCount() : 0;
        param0.setCount(Mth.clamp(var0 + this.value.getInt(param1), 0, param0.getMaxStackSize()));
        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider param0) {
        return simpleBuilder(param1 -> new SetItemCountFunction(param1, param0, false));
    }

    public static LootItemConditionalFunction.Builder<?> setCount(NumberProvider param0, boolean param1) {
        return simpleBuilder(param2 -> new SetItemCountFunction(param2, param0, param1));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemCountFunction> {
        public void serialize(JsonObject param0, SetItemCountFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.add("count", param2.serialize(param1.value));
            param0.addProperty("add", param1.add);
        }

        public SetItemCountFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            NumberProvider var0 = GsonHelper.getAsObject(param0, "count", param1, NumberProvider.class);
            boolean var1 = GsonHelper.getAsBoolean(param0, "add", false);
            return new SetItemCountFunction(param2, var0, var1);
        }
    }
}
