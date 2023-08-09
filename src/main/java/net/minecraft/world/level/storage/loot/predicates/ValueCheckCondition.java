package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public record ValueCheckCondition(NumberProvider provider, IntRange range) implements LootItemCondition {
    public static final Codec<ValueCheckCondition> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    NumberProviders.CODEC.fieldOf("value").forGetter(ValueCheckCondition::provider),
                    IntRange.CODEC.fieldOf("range").forGetter(ValueCheckCondition::range)
                )
                .apply(param0, ValueCheckCondition::new)
    );

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.VALUE_CHECK;
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(this.provider.getReferencedContextParams(), this.range.getReferencedContextParams());
    }

    public boolean test(LootContext param0) {
        return this.range.test(param0, this.provider.getInt(param0));
    }

    public static LootItemCondition.Builder hasValue(NumberProvider param0, IntRange param1) {
        return () -> new ValueCheckCondition(param0, param1);
    }
}
