package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetPotionFunction extends LootItemConditionalFunction {
    final Potion potion;

    SetPotionFunction(LootItemCondition[] param0, Potion param1) {
        super(param0);
        this.potion = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_POTION;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        PotionUtils.setPotion(param0, this.potion);
        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> setPotion(Potion param0) {
        return simpleBuilder(param1 -> new SetPotionFunction(param1, param0));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetPotionFunction> {
        public void serialize(JsonObject param0, SetPotionFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.addProperty("id", BuiltInRegistries.POTION.getKey(param1.potion).toString());
        }

        public SetPotionFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            String var0 = GsonHelper.getAsString(param0, "id");
            Potion var1 = BuiltInRegistries.POTION
                .getOptional(ResourceLocation.tryParse(var0))
                .orElseThrow(() -> new JsonSyntaxException("Unknown potion '" + var0 + "'"));
            return new SetPotionFunction(param2, var1);
        }
    }
}
