package net.minecraft.world.level.storage.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SetNbtFunction extends LootItemConditionalFunction {
    private final CompoundTag tag;

    private SetNbtFunction(LootItemCondition[] param0, CompoundTag param1) {
        super(param0);
        this.tag = param1;
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        param0.getOrCreateTag().merge(this.tag);
        return param0;
    }

    public static LootItemConditionalFunction.Builder<?> setTag(CompoundTag param0) {
        return simpleBuilder(param1 -> new SetNbtFunction(param1, param0));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetNbtFunction> {
        public Serializer() {
            super(new ResourceLocation("set_nbt"), SetNbtFunction.class);
        }

        public void serialize(JsonObject param0, SetNbtFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            param0.addProperty("tag", param1.tag.toString());
        }

        public SetNbtFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            try {
                CompoundTag var0 = TagParser.parseTag(GsonHelper.getAsString(param0, "tag"));
                return new SetNbtFunction(param2, var0);
            } catch (CommandSyntaxException var5) {
                throw new JsonSyntaxException(var5.getMessage());
            }
        }
    }
}
