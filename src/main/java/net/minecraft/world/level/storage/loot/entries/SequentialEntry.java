package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SequentialEntry extends CompositeEntryBase {
    public static final Codec<SequentialEntry> CODEC = createCodec(SequentialEntry::new);

    SequentialEntry(List<LootPoolEntryContainer> param0, List<LootItemCondition> param1) {
        super(param0, param1);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.SEQUENCE;
    }

    @Override
    protected ComposableEntryContainer compose(List<? extends ComposableEntryContainer> param0) {
        return switch(param0.size()) {
            case 0 -> ALWAYS_TRUE;
            case 1 -> (ComposableEntryContainer)param0.get(0);
            case 2 -> param0.get(0).and(param0.get(1));
            default -> (param1, param2) -> {
            for(ComposableEntryContainer var0 : param0) {
                if (!var0.expand(param1, param2)) {
                    return false;
                }
            }

            return true;
        };
        };
    }

    public static SequentialEntry.Builder sequential(LootPoolEntryContainer.Builder<?>... param0) {
        return new SequentialEntry.Builder(param0);
    }

    public static class Builder extends LootPoolEntryContainer.Builder<SequentialEntry.Builder> {
        private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();

        public Builder(LootPoolEntryContainer.Builder<?>... param0) {
            for(LootPoolEntryContainer.Builder<?> var0 : param0) {
                this.entries.add(var0.build());
            }

        }

        protected SequentialEntry.Builder getThis() {
            return this;
        }

        @Override
        public SequentialEntry.Builder then(LootPoolEntryContainer.Builder<?> param0) {
            this.entries.add(param0.build());
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return new SequentialEntry(this.entries.build(), this.getConditions());
        }
    }
}
