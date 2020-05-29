package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SetItemDamageFunction extends LootItemConditionalFunction {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RandomValueBounds damage;

    private SetItemDamageFunction(LootItemCondition[] param0, RandomValueBounds param1) {
        super(param0);
        this.damage = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_DAMAGE;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        if (param0.isDamageableItem()) {
            float var0 = 1.0F - this.damage.getFloat(param1.getRandom());
            param0.setDamageValue(Mth.floor(var0 * (float)param0.getMaxDamage()));
        } else {
            LOGGER.warn("Couldn't set damage of loot item {}", param0);
        }

        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> setDamage(RandomValueBounds param0) {
        return simpleBuilder(param1 -> new SetItemDamageFunction(param1, param0));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetItemDamageFunction> {
        public void serialize(JsonObject param0, SetItemDamageFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.add("damage", param2.serialize(param1.damage));
        }

        public SetItemDamageFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            return new SetItemDamageFunction(param2, GsonHelper.getAsObject(param0, "damage", param1, RandomValueBounds.class));
        }
    }
}
