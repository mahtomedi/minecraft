package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.item.GoatHornItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetGoatHornSoundFunction extends LootItemConditionalFunction {
    SetGoatHornSoundFunction(LootItemCondition[] param0) {
        super(param0);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_GOAT_HORN_SOUND;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        GoatHornItem.setRandomNonScreamingSound(param0, param1.getRandom());
        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> setGoatHornSounds() {
        return simpleBuilder(SetGoatHornSoundFunction::new);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetGoatHornSoundFunction> {
        public void serialize(JsonObject param0, SetGoatHornSoundFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
        }

        public SetGoatHornSoundFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            return new SetGoatHornSoundFunction(param2);
        }
    }
}
