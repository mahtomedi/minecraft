package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public class EntityHasScoreCondition implements LootItemCondition {
    private final Map<String, RandomValueBounds> scores;
    private final LootContext.EntityTarget entityTarget;

    private EntityHasScoreCondition(Map<String, RandomValueBounds> param0, LootContext.EntityTarget param1) {
        this.scores = ImmutableMap.copyOf(param0);
        this.entityTarget = param1;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ENTITY_SCORES;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(this.entityTarget.getParam());
    }

    public boolean test(LootContext param0) {
        Entity var0 = param0.getParamOrNull(this.entityTarget.getParam());
        if (var0 == null) {
            return false;
        } else {
            Scoreboard var1 = var0.level.getScoreboard();

            for(Entry<String, RandomValueBounds> var2 : this.scores.entrySet()) {
                if (!this.hasScore(var0, var1, var2.getKey(), var2.getValue())) {
                    return false;
                }
            }

            return true;
        }
    }

    protected boolean hasScore(Entity param0, Scoreboard param1, String param2, RandomValueBounds param3) {
        Objective var0 = param1.getObjective(param2);
        if (var0 == null) {
            return false;
        } else {
            String var1 = param0.getScoreboardName();
            return !param1.hasPlayerScore(var1, var0) ? false : param3.matchesValue(param1.getOrCreatePlayerScore(var1, var0).getScore());
        }
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<EntityHasScoreCondition> {
        public void serialize(JsonObject param0, EntityHasScoreCondition param1, JsonSerializationContext param2) {
            JsonObject var0 = new JsonObject();

            for(Entry<String, RandomValueBounds> var1 : param1.scores.entrySet()) {
                var0.add(var1.getKey(), param2.serialize(var1.getValue()));
            }

            param0.add("scores", var0);
            param0.add("entity", param2.serialize(param1.entityTarget));
        }

        public EntityHasScoreCondition deserialize(JsonObject param0, JsonDeserializationContext param1) {
            Set<Entry<String, JsonElement>> var0 = GsonHelper.getAsJsonObject(param0, "scores").entrySet();
            Map<String, RandomValueBounds> var1 = Maps.newLinkedHashMap();

            for(Entry<String, JsonElement> var2 : var0) {
                var1.put(var2.getKey(), GsonHelper.convertToObject(var2.getValue(), "score", param1, RandomValueBounds.class));
            }

            return new EntityHasScoreCondition(var1, GsonHelper.getAsObject(param0, "entity", param1, LootContext.EntityTarget.class));
        }
    }
}
