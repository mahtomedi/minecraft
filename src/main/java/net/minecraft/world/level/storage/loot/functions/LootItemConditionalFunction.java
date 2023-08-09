package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products.P1;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public abstract class LootItemConditionalFunction implements LootItemFunction {
    protected final List<LootItemCondition> predicates;
    private final Predicate<LootContext> compositePredicates;

    protected LootItemConditionalFunction(List<LootItemCondition> param0) {
        this.predicates = param0;
        this.compositePredicates = LootItemConditions.andConditions(param0);
    }

    protected static <T extends LootItemConditionalFunction> P1<Mu<T>, List<LootItemCondition>> commonFields(Instance<T> param0) {
        return param0.group(
            ExtraCodecs.strictOptionalField(LootItemConditions.CODEC.listOf(), "conditions", List.of()).forGetter(param0x -> param0x.predicates)
        );
    }

    public final ItemStack apply(ItemStack param0, LootContext param1) {
        return this.compositePredicates.test(param1) ? this.run(param0, param1) : param0;
    }

    protected abstract ItemStack run(ItemStack var1, LootContext var2);

    @Override
    public void validate(ValidationContext param0) {
        LootItemFunction.super.validate(param0);

        for(int var0 = 0; var0 < this.predicates.size(); ++var0) {
            this.predicates.get(var0).validate(param0.forChild(".conditions[" + var0 + "]"));
        }

    }

    protected static LootItemConditionalFunction.Builder<?> simpleBuilder(Function<List<LootItemCondition>, LootItemFunction> param0) {
        return new LootItemConditionalFunction.DummyBuilder(param0);
    }

    public abstract static class Builder<T extends LootItemConditionalFunction.Builder<T>> implements LootItemFunction.Builder, ConditionUserBuilder<T> {
        private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();

        public T when(LootItemCondition.Builder param0) {
            this.conditions.add(param0.build());
            return this.getThis();
        }

        public final T unwrap() {
            return this.getThis();
        }

        protected abstract T getThis();

        protected List<LootItemCondition> getConditions() {
            return this.conditions.build();
        }
    }

    static final class DummyBuilder extends LootItemConditionalFunction.Builder<LootItemConditionalFunction.DummyBuilder> {
        private final Function<List<LootItemCondition>, LootItemFunction> constructor;

        public DummyBuilder(Function<List<LootItemCondition>, LootItemFunction> param0) {
            this.constructor = param0;
        }

        protected LootItemConditionalFunction.DummyBuilder getThis() {
            return this;
        }

        @Override
        public LootItemFunction build() {
            return this.constructor.apply(this.getConditions());
        }
    }
}
