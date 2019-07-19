package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.commons.lang3.ArrayUtils;

public class LootPoolEntries {
    private static final Map<ResourceLocation, LootPoolEntryContainer.Serializer<?>> ID_TO_SERIALIZER = Maps.newHashMap();
    private static final Map<Class<?>, LootPoolEntryContainer.Serializer<?>> CLASS_TO_SERIALIZER = Maps.newHashMap();

    private static void register(LootPoolEntryContainer.Serializer<?> param0) {
        ID_TO_SERIALIZER.put(param0.getName(), param0);
        CLASS_TO_SERIALIZER.put(param0.getContainerClass(), param0);
    }

    static {
        register(CompositeEntryBase.createSerializer(new ResourceLocation("alternatives"), AlternativesEntry.class, AlternativesEntry::new));
        register(CompositeEntryBase.createSerializer(new ResourceLocation("sequence"), SequentialEntry.class, SequentialEntry::new));
        register(CompositeEntryBase.createSerializer(new ResourceLocation("group"), EntryGroup.class, EntryGroup::new));
        register(new EmptyLootItem.Serializer());
        register(new LootItem.Serializer());
        register(new LootTableReference.Serializer());
        register(new DynamicLoot.Serializer());
        register(new TagEntry.Serializer());
    }

    public static class Serializer implements JsonDeserializer<LootPoolEntryContainer>, JsonSerializer<LootPoolEntryContainer> {
        public LootPoolEntryContainer deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "entry");
            ResourceLocation var1 = new ResourceLocation(GsonHelper.getAsString(var0, "type"));
            LootPoolEntryContainer.Serializer<?> var2 = LootPoolEntries.ID_TO_SERIALIZER.get(var1);
            if (var2 == null) {
                throw new JsonParseException("Unknown item type: " + var1);
            } else {
                LootItemCondition[] var3 = GsonHelper.getAsObject(var0, "conditions", new LootItemCondition[0], param2, LootItemCondition[].class);
                return var2.deserialize(var0, param2, var3);
            }
        }

        public JsonElement serialize(LootPoolEntryContainer param0, Type param1, JsonSerializationContext param2) {
            JsonObject var0 = new JsonObject();
            LootPoolEntryContainer.Serializer<LootPoolEntryContainer> var1 = getSerializer(param0.getClass());
            var0.addProperty("type", var1.getName().toString());
            if (!ArrayUtils.isEmpty((Object[])param0.conditions)) {
                var0.add("conditions", param2.serialize(param0.conditions));
            }

            var1.serialize(var0, param0, param2);
            return var0;
        }

        private static LootPoolEntryContainer.Serializer<LootPoolEntryContainer> getSerializer(Class<?> param0) {
            LootPoolEntryContainer.Serializer<?> var0 = LootPoolEntries.CLASS_TO_SERIALIZER.get(param0);
            if (var0 == null) {
                throw new JsonParseException("Unknown item type: " + param0);
            } else {
                return var0;
            }
        }
    }
}
