package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class ExplosionCondition implements LootItemCondition {
    static final ExplosionCondition INSTANCE = new ExplosionCondition();

    private ExplosionCondition() {
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.SURVIVES_EXPLOSION;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.EXPLOSION_RADIUS);
    }

    public boolean test(LootContext param0) {
        Float var0 = param0.getParamOrNull(LootContextParams.EXPLOSION_RADIUS);
        if (var0 != null) {
            Random var1 = param0.getRandom();
            float var2 = 1.0F / var0;
            return var1.nextFloat() <= var2;
        } else {
            return true;
        }
    }

    public static LootItemCondition.Builder survivesExplosion() {
        return () -> INSTANCE;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ExplosionCondition> {
        public void serialize(JsonObject param0, ExplosionCondition param1, JsonSerializationContext param2) {
        }

        public ExplosionCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            return ExplosionCondition.INSTANCE;
        }
    }
}
