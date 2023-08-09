package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public record LootItemEntityPropertyCondition(Optional<EntityPredicate> predicate, LootContext.EntityTarget entityTarget) implements LootItemCondition {
    public static final Codec<LootItemEntityPropertyCondition> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.strictOptionalField(EntityPredicate.CODEC, "predicate").forGetter(LootItemEntityPropertyCondition::predicate),
                    LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(LootItemEntityPropertyCondition::entityTarget)
                )
                .apply(param0, LootItemEntityPropertyCondition::new)
    );

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ENTITY_PROPERTIES;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.ORIGIN, this.entityTarget.getParam());
    }

    public boolean test(LootContext param0) {
        Entity var0 = param0.getParamOrNull(this.entityTarget.getParam());
        Vec3 var1 = param0.getParamOrNull(LootContextParams.ORIGIN);
        return this.predicate.isEmpty() || this.predicate.get().matches(param0.getLevel(), var1, var0);
    }

    public static LootItemCondition.Builder entityPresent(LootContext.EntityTarget param0) {
        return hasProperties(param0, EntityPredicate.Builder.entity());
    }

    public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget param0, EntityPredicate.Builder param1) {
        return () -> new LootItemEntityPropertyCondition(param1.build(), param0);
    }

    public static LootItemCondition.Builder hasProperties(LootContext.EntityTarget param0, EntityPredicate param1) {
        return () -> new LootItemEntityPropertyCondition(Optional.of(param1), param0);
    }
}
