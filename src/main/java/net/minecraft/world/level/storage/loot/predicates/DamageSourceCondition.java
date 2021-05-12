package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public class DamageSourceCondition implements LootItemCondition {
    final DamageSourcePredicate predicate;

    DamageSourceCondition(DamageSourcePredicate param0) {
        this.predicate = param0;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.DAMAGE_SOURCE_PROPERTIES;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ORIGIN, LootContextParams.DAMAGE_SOURCE);
    }

    public boolean test(LootContext param0) {
        DamageSource var0 = param0.getParamOrNull(LootContextParams.DAMAGE_SOURCE);
        Vec3 var1 = param0.getParamOrNull(LootContextParams.ORIGIN);
        return var1 != null && var0 != null && this.predicate.matches(param0.getLevel(), var1, var0);
    }

    public static LootItemCondition.Builder hasDamageSource(DamageSourcePredicate.Builder param0) {
        return () -> new DamageSourceCondition(param0.build());
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<DamageSourceCondition> {
        public void serialize(JsonObject param0, DamageSourceCondition param1, JsonSerializationContext param2) {
            param0.add("predicate", param1.predicate.serializeToJson());
        }

        public DamageSourceCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            DamageSourcePredicate var0 = DamageSourcePredicate.fromJson(param0.get("predicate"));
            return new DamageSourceCondition(var0);
        }
    }
}
