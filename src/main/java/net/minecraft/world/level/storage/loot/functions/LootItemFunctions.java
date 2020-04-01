package net.minecraft.world.level.storage.loot.functions;

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
import java.util.function.BiFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class LootItemFunctions {
    private static final Map<ResourceLocation, LootItemFunction.Serializer<?>> FUNCTIONS_BY_NAME = Maps.newHashMap();
    private static final Map<Class<? extends LootItemFunction>, LootItemFunction.Serializer<?>> FUNCTIONS_BY_CLASS = Maps.newHashMap();
    public static final BiFunction<ItemStack, LootContext, ItemStack> IDENTITY = (param0, param1) -> param0;

    public static <T extends LootItemFunction> void register(LootItemFunction.Serializer<? extends T> param0) {
        ResourceLocation var0 = param0.getName();
        Class<T> var1 = param0.getFunctionClass();
        if (FUNCTIONS_BY_NAME.containsKey(var0)) {
            throw new IllegalArgumentException("Can't re-register item function name " + var0);
        } else if (FUNCTIONS_BY_CLASS.containsKey(var1)) {
            throw new IllegalArgumentException("Can't re-register item function class " + var1.getName());
        } else {
            FUNCTIONS_BY_NAME.put(var0, param0);
            FUNCTIONS_BY_CLASS.put(var1, param0);
        }
    }

    public static LootItemFunction.Serializer<?> getSerializer(ResourceLocation param0) {
        LootItemFunction.Serializer<?> var0 = FUNCTIONS_BY_NAME.get(param0);
        if (var0 == null) {
            throw new IllegalArgumentException("Unknown loot item function '" + param0 + "'");
        } else {
            return var0;
        }
    }

    public static <T extends LootItemFunction> LootItemFunction.Serializer<T> getSerializer(T param0) {
        LootItemFunction.Serializer<T> var0 = (LootItemFunction.Serializer)FUNCTIONS_BY_CLASS.get(param0.getClass());
        if (var0 == null) {
            throw new IllegalArgumentException("Unknown loot item function " + param0);
        } else {
            return var0;
        }
    }

    public static BiFunction<ItemStack, LootContext, ItemStack> compose(BiFunction<ItemStack, LootContext, ItemStack>[] param0) {
        switch(param0.length) {
            case 0:
                return IDENTITY;
            case 1:
                return param0[0];
            case 2:
                BiFunction<ItemStack, LootContext, ItemStack> var0 = param0[0];
                BiFunction<ItemStack, LootContext, ItemStack> var1 = param0[1];
                return (param2, param3) -> var1.apply(var0.apply(param2, param3), param3);
            default:
                return (param1, param2) -> {
                    for(BiFunction<ItemStack, LootContext, ItemStack> var0x : param0) {
                        param1 = var0x.apply(param1, param2);
                    }

                    return param1;
                };
        }
    }

    static {
        register(new SetItemCountFunction.Serializer());
        register(new EnchantWithLevelsFunction.Serializer());
        register(new EnchantRandomlyFunction.Serializer());
        register(new SetNbtFunction.Serializer());
        register(new SmeltItemFunction.Serializer());
        register(new LootingEnchantFunction.Serializer());
        register(new SetItemDamageFunction.Serializer());
        register(new SetAttributesFunction.Serializer());
        register(new SetNameFunction.Serializer());
        register(new ExplorationMapFunction.Serializer());
        register(new SetStewEffectFunction.Serializer());
        register(new CopyNameFunction.Serializer());
        register(new SetContainerContents.Serializer());
        register(new LimitCount.Serializer());
        register(new ApplyBonusCount.Serializer());
        register(new SetContainerLootTable.Serializer());
        register(new ApplyExplosionDecay.Serializer());
        register(new SetLoreFunction.Serializer());
        register(new FillPlayerHead.Serializer());
        register(new CopyNbtFunction.Serializer());
        register(new CopyBlockState.Serializer());
        register(new AddBookContents.Serializer());
    }

    public static class Serializer implements JsonDeserializer<LootItemFunction>, JsonSerializer<LootItemFunction> {
        public LootItemFunction deserialize(JsonElement param0, Type param1, JsonDeserializationContext param2) throws JsonParseException {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "function");
            ResourceLocation var1 = new ResourceLocation(GsonHelper.getAsString(var0, "function"));

            LootItemFunction.Serializer<?> var2;
            try {
                var2 = LootItemFunctions.getSerializer(var1);
            } catch (IllegalArgumentException var8) {
                throw new JsonSyntaxException("Unknown function '" + var1 + "'");
            }

            return var2.deserialize(var0, param2);
        }

        public JsonElement serialize(LootItemFunction param0, Type param1, JsonSerializationContext param2) {
            LootItemFunction.Serializer<LootItemFunction> var0 = LootItemFunctions.getSerializer(param0);
            JsonObject var1 = new JsonObject();
            var1.addProperty("function", var0.getName().toString());
            var0.serialize(var1, param0, param2);
            return var1;
        }
    }
}
