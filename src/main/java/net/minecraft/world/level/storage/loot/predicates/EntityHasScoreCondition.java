package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

public record EntityHasScoreCondition(Map<String, IntRange> scores, LootContext.EntityTarget entityTarget) implements LootItemCondition {
    public static final Codec<EntityHasScoreCondition> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.unboundedMap(Codec.STRING, IntRange.CODEC).fieldOf("scores").forGetter(EntityHasScoreCondition::scores),
                    LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(EntityHasScoreCondition::entityTarget)
                )
                .apply(param0, EntityHasScoreCondition::new)
    );

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ENTITY_SCORES;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Stream.concat(
                Stream.of(this.entityTarget.getParam()), this.scores.values().stream().flatMap(param0 -> param0.getReferencedContextParams().stream())
            )
            .collect(ImmutableSet.toImmutableSet());
    }

    public boolean test(LootContext param0) {
        Entity var0 = param0.getParamOrNull(this.entityTarget.getParam());
        if (var0 == null) {
            return false;
        } else {
            Scoreboard var1 = var0.level().getScoreboard();

            for(Entry<String, IntRange> var2 : this.scores.entrySet()) {
                if (!this.hasScore(param0, var0, var1, var2.getKey(), var2.getValue())) {
                    return false;
                }
            }

            return true;
        }
    }

    protected boolean hasScore(LootContext param0, Entity param1, Scoreboard param2, String param3, IntRange param4) {
        Objective var0 = param2.getObjective(param3);
        if (var0 == null) {
            return false;
        } else {
            String var1 = param1.getScoreboardName();
            return !param2.hasPlayerScore(var1, var0) ? false : param4.test(param0, param2.getOrCreatePlayerScore(var1, var0).getScore());
        }
    }

    public static EntityHasScoreCondition.Builder hasScores(LootContext.EntityTarget param0) {
        return new EntityHasScoreCondition.Builder(param0);
    }

    public static class Builder implements LootItemCondition.Builder {
        private final ImmutableMap.Builder<String, IntRange> scores = ImmutableMap.builder();
        private final LootContext.EntityTarget entityTarget;

        public Builder(LootContext.EntityTarget param0) {
            this.entityTarget = param0;
        }

        public EntityHasScoreCondition.Builder withScore(String param0, IntRange param1) {
            this.scores.put(param0, param1);
            return this;
        }

        @Override
        public LootItemCondition build() {
            return new EntityHasScoreCondition(this.scores.build(), this.entityTarget);
        }
    }
}
