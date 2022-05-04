package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetInstrumentFunction extends LootItemConditionalFunction {
    final TagKey<Instrument> options;

    SetInstrumentFunction(LootItemCondition[] param0, TagKey<Instrument> param1) {
        super(param0);
        this.options = param1;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_INSTRUMENT;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        InstrumentItem.setRandom(param0, this.options, param1.getRandom());
        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> setInstrumentOptions(TagKey<Instrument> param0) {
        return simpleBuilder(param1 -> new SetInstrumentFunction(param1, param0));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetInstrumentFunction> {
        public void serialize(JsonObject param0, SetInstrumentFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.addProperty("options", "#" + param1.options.location());
        }

        public SetInstrumentFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            String var0 = GsonHelper.getAsString(param0, "options");
            if (!var0.startsWith("#")) {
                throw new JsonSyntaxException("Inline tag value not supported: " + var0);
            } else {
                return new SetInstrumentFunction(param2, TagKey.create(Registry.INSTRUMENT_REGISTRY, new ResourceLocation(var0.substring(1))));
            }
        }
    }
}
