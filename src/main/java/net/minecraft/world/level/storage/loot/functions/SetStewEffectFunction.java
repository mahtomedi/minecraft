package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public class SetStewEffectFunction extends LootItemConditionalFunction {
    final Map<MobEffect, NumberProvider> effectDurationMap;

    SetStewEffectFunction(LootItemCondition[] param0, Map<MobEffect, NumberProvider> param1) {
        super(param0);
        this.effectDurationMap = ImmutableMap.copyOf(param1);
    }

    @Override
    public LootItemFunctionType getType() {
        return LootItemFunctions.SET_STEW_EFFECT;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return this.effectDurationMap.values().stream().flatMap(param0 -> param0.getReferencedContextParams().stream()).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public ItemStack run(ItemStack param0, LootContext param1) {
        if (param0.is(Items.SUSPICIOUS_STEW) && !this.effectDurationMap.isEmpty()) {
            RandomSource var0 = param1.getRandom();
            int var1 = var0.nextInt(this.effectDurationMap.size());
            Entry<MobEffect, NumberProvider> var2 = Iterables.get(this.effectDurationMap.entrySet(), var1);
            MobEffect var3 = var2.getKey();
            int var4 = var2.getValue().getInt(param1);
            if (!var3.isInstantenous()) {
                var4 *= 20;
            }

            SuspiciousStewItem.saveMobEffect(param0, var3, var4);
            return param0;
        } else {
            return param0;
        }
    }

    public static SetStewEffectFunction.Builder stewEffect() {
        return new SetStewEffectFunction.Builder();
    }

    public static class Builder extends LootItemConditionalFunction.Builder<SetStewEffectFunction.Builder> {
        private final Map<MobEffect, NumberProvider> effectDurationMap = Maps.newLinkedHashMap();

        protected SetStewEffectFunction.Builder getThis() {
            return this;
        }

        public SetStewEffectFunction.Builder withEffect(MobEffect param0, NumberProvider param1) {
            this.effectDurationMap.put(param0, param1);
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new SetStewEffectFunction(this.getConditions(), this.effectDurationMap);
        }
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetStewEffectFunction> {
        public void serialize(JsonObject param0, SetStewEffectFunction param1, JsonSerializationContext param2) {
            super.serialize(param0, param1, param2);
            if (!param1.effectDurationMap.isEmpty()) {
                JsonArray var0 = new JsonArray();

                for(MobEffect var1 : param1.effectDurationMap.keySet()) {
                    JsonObject var2 = new JsonObject();
                    ResourceLocation var3 = Registry.MOB_EFFECT.getKey(var1);
                    if (var3 == null) {
                        throw new IllegalArgumentException("Don't know how to serialize mob effect " + var1);
                    }

                    var2.add("type", new JsonPrimitive(var3.toString()));
                    var2.add("duration", param2.serialize(param1.effectDurationMap.get(var1)));
                    var0.add(var2);
                }

                param0.add("effects", var0);
            }

        }

        public SetStewEffectFunction deserialize(JsonObject param0, JsonDeserializationContext param1, LootItemCondition[] param2) {
            Map<MobEffect, NumberProvider> var0 = Maps.newLinkedHashMap();
            if (param0.has("effects")) {
                for(JsonElement var2 : GsonHelper.getAsJsonArray(param0, "effects")) {
                    String var3 = GsonHelper.getAsString(var2.getAsJsonObject(), "type");
                    MobEffect var4 = Registry.MOB_EFFECT
                        .getOptional(new ResourceLocation(var3))
                        .orElseThrow(() -> new JsonSyntaxException("Unknown mob effect '" + var3 + "'"));
                    NumberProvider var5 = GsonHelper.getAsObject(var2.getAsJsonObject(), "duration", param1, NumberProvider.class);
                    var0.put(var4, var5);
                }
            }

            return new SetStewEffectFunction(param2, var0);
        }
    }
}
