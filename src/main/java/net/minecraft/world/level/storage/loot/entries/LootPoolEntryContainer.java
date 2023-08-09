package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public abstract class LootPoolEntryContainer implements ComposableEntryContainer {
    protected final List<LootItemCondition> conditions;
    private final Predicate<LootContext> compositeCondition;

    protected LootPoolEntryContainer(List<LootItemCondition> param0) {
        this.conditions = param0;
        this.compositeCondition = LootItemConditions.andConditions(param0);
    }

    protected static <T extends LootPoolEntryContainer> P1<Mu<T>, List<LootItemCondition>> commonFields(Instance<T> param0) {
        return param0.group(
            ExtraCodecs.strictOptionalField(LootItemConditions.CODEC.listOf(), "conditions", List.of()).forGetter(param0x -> param0x.conditions)
        );
    }

    public void validate(ValidationContext param0) {
        for(int var0 = 0; var0 < this.conditions.size(); ++var0) {
            this.conditions.get(var0).validate(param0.forChild(".condition[" + var0 + "]"));
        }

    }

    protected final boolean canRun(LootContext param0) {
        return this.compositeCondition.test(param0);
    }

    public abstract LootPoolEntryType getType();

    public abstract static class Builder<T extends LootPoolEntryContainer.Builder<T>> implements ConditionUserBuilder<T> {
        private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();

        protected abstract T getThis();

        public T when(LootItemCondition.Builder param0) {
            this.conditions.add(param0.build());
            return this.getThis();
        }

        public final T unwrap() {
            return this.getThis();
        }

        protected List<LootItemCondition> getConditions() {
            return this.conditions.build();
        }

        public AlternativesEntry.Builder otherwise(LootPoolEntryContainer.Builder<?> param0) {
            return new AlternativesEntry.Builder(this, param0);
        }

        public EntryGroup.Builder append(LootPoolEntryContainer.Builder<?> param0) {
            return new EntryGroup.Builder(this, param0);
        }

        public SequentialEntry.Builder then(LootPoolEntryContainer.Builder<?> param0) {
            return new SequentialEntry.Builder(this, param0);
        }

        public abstract LootPoolEntryContainer build();
    }
}
