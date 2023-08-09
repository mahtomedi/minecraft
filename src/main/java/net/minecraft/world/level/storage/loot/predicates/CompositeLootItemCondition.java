package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public abstract class CompositeLootItemCondition implements LootItemCondition {
    protected final List<LootItemCondition> terms;
    private final Predicate<LootContext> composedPredicate;

    protected CompositeLootItemCondition(List<LootItemCondition> param0, Predicate<LootContext> param1) {
        this.terms = param0;
        this.composedPredicate = param1;
    }

    protected static <T extends CompositeLootItemCondition> Codec<T> createCodec(Function<List<LootItemCondition>, T> param0) {
        return RecordCodecBuilder.create(
            param1 -> param1.group(LootItemConditions.CODEC.listOf().fieldOf("terms").forGetter(param0x -> param0x.terms)).apply(param1, param0)
        );
    }

    protected static <T extends CompositeLootItemCondition> Codec<T> createInlineCodec(Function<List<LootItemCondition>, T> param0) {
        return LootItemConditions.CODEC.listOf().xmap(param0, param0x -> param0x.terms);
    }

    public final boolean test(LootContext param0) {
        return this.composedPredicate.test(param0);
    }

    @Override
    public void validate(ValidationContext param0) {
        LootItemCondition.super.validate(param0);

        for(int var0 = 0; var0 < this.terms.size(); ++var0) {
            this.terms.get(var0).validate(param0.forChild(".term[" + var0 + "]"));
        }

    }

    public abstract static class Builder implements LootItemCondition.Builder {
        private final ImmutableList.Builder<LootItemCondition> terms = ImmutableList.builder();

        protected Builder(LootItemCondition.Builder... param0) {
            for(LootItemCondition.Builder var0 : param0) {
                this.terms.add(var0.build());
            }

        }

        public void addTerm(LootItemCondition.Builder param0) {
            this.terms.add(param0.build());
        }

        @Override
        public LootItemCondition build() {
            return this.create(this.terms.build());
        }

        protected abstract LootItemCondition create(List<LootItemCondition> var1);
    }
}
