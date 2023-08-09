package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public record DamageSourceCondition(Optional<DamageSourcePredicate> predicate) implements LootItemCondition {
    public static final Codec<DamageSourceCondition> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(ExtraCodecs.strictOptionalField(DamageSourcePredicate.CODEC, "predicate").forGetter(DamageSourceCondition::predicate))
                .apply(param0, DamageSourceCondition::new)
    );

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
        if (var1 != null && var0 != null) {
            return this.predicate.isEmpty() || this.predicate.get().matches(param0.getLevel(), var1, var0);
        } else {
            return false;
        }
    }

    public static LootItemCondition.Builder hasDamageSource(DamageSourcePredicate.Builder param0) {
        return () -> new DamageSourceCondition(param0.build());
    }
}
