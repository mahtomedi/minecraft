package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class LootItemConditions {
    private static final Map<ResourceLocation, LootItemCondition.Serializer<?>> CONDITIONS_BY_NAME = Maps.newHashMap();
    private static final Map<Class<? extends LootItemCondition>, LootItemCondition.Serializer<?>> CONDITIONS_BY_CLASS = Maps.newHashMap();

    public static <T extends LootItemCondition> void register(LootItemCondition.Serializer<? extends T> param0) {
        ResourceLocation var0 = param0.getName();
        Class<T> var1 = param0.getPredicateClass();
        if (CONDITIONS_BY_NAME.containsKey(var0)) {
            throw new IllegalArgumentException("Can't re-register item condition name " + var0);
        } else if (CONDITIONS_BY_CLASS.containsKey(var1)) {
            throw new IllegalArgumentException("Can't re-register item condition class " + var1.getName());
        } else {
            CONDITIONS_BY_NAME.put(var0, param0);
            CONDITIONS_BY_CLASS.put(var1, param0);
        }
    }

    public static LootItemCondition.Serializer<?> getSerializer(ResourceLocation param0) {
        LootItemCondition.Serializer<?> var0 = CONDITIONS_BY_NAME.get(param0);
        if (var0 == null) {
            throw new IllegalArgumentException("Unknown loot item condition '" + param0 + "'");
        } else {
            return var0;
        }
    }

    public static <T extends LootItemCondition> LootItemCondition.Serializer<T> getSerializer(T param0) {
        LootItemCondition.Serializer<T> var0 = (LootItemCondition.Serializer)CONDITIONS_BY_CLASS.get(param0.getClass());
        if (var0 == null) {
            throw new IllegalArgumentException("Unknown loot item condition " + param0);
        } else {
            return var0;
        }
    }

    public static <T> Predicate<T> andConditions(Predicate<T>[] param0) {
        switch(param0.length) {
            case 0:
                return param0x -> true;
            case 1:
                return param0[0];
            case 2:
                return param0[0].and(param0[1]);
            default:
                return param1 -> {
                    for(Predicate<T> var0x : param0) {
                        if (!var0x.test(param1)) {
                            return false;
                        }
                    }

                    return true;
                };
        }
    }

    public static <T> Predicate<T> orConditions(Predicate<T>[] param0) {
        switch(param0.length) {
            case 0:
                return param0x -> false;
            case 1:
                return param0[0];
            case 2:
                return param0[0].or(param0[1]);
            default:
                return param1 -> {
                    for(Predicate<T> var0x : param0) {
                        if (var0x.test(param1)) {
                            return true;
                        }
                    }

                    return false;
                };
        }
    }

    static {
        register(new InvertedLootItemCondition.Serializer());
        register(new AlternativeLootItemCondition.Serializer());
        register(new LootItemRandomChanceCondition.Serializer());
        register(new LootItemRandomChanceWithLootingCondition.Serializer());
        register(new LootItemEntityPropertyCondition.Serializer());
        register(new LootItemKilledByPlayerCondition.Serializer());
        register(new EntityHasScoreCondition.Serializer());
        register(new LootItemBlockStatePropertyCondition.Serializer());
        register(new MatchTool.Serializer());
        register(new BonusLevelTableCondition.Serializer());
        register(new ExplosionCondition.Serializer());
        register(new DamageSourceCondition.Serializer());
        register(new LocationCheck.Serializer());
        register(new WeatherCheck.Serializer());
    }

    public static class Serializer implements JsonDeserializer<LootItemCondition>, JsonSerializer<LootItemCondition> {
        public LootItemCondition deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "condition");
            ResourceLocation var1 = new ResourceLocation(GsonHelper.getAsString(var0, "condition"));

            LootItemCondition.Serializer<?> var2;
            try {
                var2 = LootItemConditions.getSerializer(var1);
            } catch (IllegalArgumentException var8) {
                throw new JsonSyntaxException("Unknown condition '" + var1 + "'");
            }

            return var2.deserialize(var0, param2);
        }

        public JsonElement serialize(LootItemCondition param0, Type param1, JsonSerializationContext param2) {
            LootItemCondition.Serializer<LootItemCondition> var0 = LootItemConditions.getSerializer(param0);
            JsonObject var1 = new JsonObject();
            var1.addProperty("condition", var0.getName().toString());
            var0.serialize(var1, param0, param2);
            return var1;
        }
    }
}
