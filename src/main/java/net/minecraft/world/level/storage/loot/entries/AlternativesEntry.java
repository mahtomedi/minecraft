package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AlternativesEntry extends CompositeEntryBase {
    public static final Codec<AlternativesEntry> CODEC = createCodec(AlternativesEntry::new);

    AlternativesEntry(List<LootPoolEntryContainer> param0, List<LootItemCondition> param1) {
        super(param0, param1);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.ALTERNATIVES;
    }

    @Override
    protected ComposableEntryContainer compose(List<? extends ComposableEntryContainer> param0) {
        return switch(param0.size()) {
            case 0 -> ALWAYS_FALSE;
            case 1 -> (ComposableEntryContainer)param0.get(0);
            case 2 -> param0.get(0).or(param0.get(1));
            default -> (param1, param2) -> {
            for(ComposableEntryContainer var0 : param0) {
                if (var0.expand(param1, param2)) {
                    return true;
                }
            }

            return false;
        };
        };
    }

    @Override
    public void validate(ValidationContext param0) {
        super.validate(param0);

        for(int var0 = 0; var0 < this.children.size() - 1; ++var0) {
            if (this.children.get(var0).conditions.isEmpty()) {
                param0.reportProblem("Unreachable entry!");
            }
        }

    }

    public static AlternativesEntry.Builder alternatives(LootPoolEntryContainer.Builder<?>... param0) {
        return new AlternativesEntry.Builder(param0);
    }

    public static <E> AlternativesEntry.Builder alternatives(Collection<E> param0, Function<E, LootPoolEntryContainer.Builder<?>> param1) {
        return new AlternativesEntry.Builder(param0.stream().map(param1::apply).toArray(param0x -> new LootPoolEntryContainer.Builder[param0x]));
    }

    public static class Builder extends LootPoolEntryContainer.Builder<AlternativesEntry.Builder> {
        private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();

        public Builder(LootPoolEntryContainer.Builder<?>... param0) {
            for(LootPoolEntryContainer.Builder<?> var0 : param0) {
                this.entries.add(var0.build());
            }

        }

        protected AlternativesEntry.Builder getThis() {
            return this;
        }

        @Override
        public AlternativesEntry.Builder otherwise(LootPoolEntryContainer.Builder<?> param0) {
            this.entries.add(param0.build());
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return new AlternativesEntry(this.entries.build(), this.getConditions());
        }
    }
}
