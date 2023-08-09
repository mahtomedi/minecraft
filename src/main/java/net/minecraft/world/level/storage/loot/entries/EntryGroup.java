package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntryGroup extends CompositeEntryBase {
    public static final Codec<EntryGroup> CODEC = createCodec(EntryGroup::new);

    EntryGroup(List<LootPoolEntryContainer> param0, List<LootItemCondition> param1) {
        super(param0, param1);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.GROUP;
    }

    @Override
    protected ComposableEntryContainer compose(List<? extends ComposableEntryContainer> param0) {
        return switch(param0.size()) {
            case 0 -> ALWAYS_TRUE;
            case 1 -> (ComposableEntryContainer)param0.get(0);
            case 2 -> {
                ComposableEntryContainer var0 = param0.get(0);
                ComposableEntryContainer var1 = param0.get(1);
                yield (param2, param3) -> {
                    var0.expand(param2, param3);
                    var1.expand(param2, param3);
                    return true;
                };
            }
            default -> (param1, param2) -> {
            for(ComposableEntryContainer var0x : param0) {
                var0x.expand(param1, param2);
            }

            return true;
        };
        };
    }

    public static EntryGroup.Builder list(LootPoolEntryContainer.Builder<?>... param0) {
        return new EntryGroup.Builder(param0);
    }

    public static class Builder extends LootPoolEntryContainer.Builder<EntryGroup.Builder> {
        private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();

        public Builder(LootPoolEntryContainer.Builder<?>... param0) {
            for(LootPoolEntryContainer.Builder<?> var0 : param0) {
                this.entries.add(var0.build());
            }

        }

        protected EntryGroup.Builder getThis() {
            return this;
        }

        @Override
        public EntryGroup.Builder append(LootPoolEntryContainer.Builder<?> param0) {
            this.entries.add(param0.build());
            return this;
        }

        @Override
        public LootPoolEntryContainer build() {
            return new EntryGroup(this.entries.build(), this.getConditions());
        }
    }
}
