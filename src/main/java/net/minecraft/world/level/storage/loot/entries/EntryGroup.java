package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class EntryGroup extends CompositeEntryBase {
    EntryGroup(LootPoolEntryContainer[] param0, LootItemCondition[] param1) {
        super(param0, param1);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.GROUP;
    }

    @Override
    protected ComposableEntryContainer compose(ComposableEntryContainer[] param0) {
        switch(param0.length) {
            case 0:
                return ALWAYS_TRUE;
            case 1:
                return param0[0];
            case 2:
                ComposableEntryContainer var0 = param0[0];
                ComposableEntryContainer var1 = param0[1];
                return (param2, param3) -> {
                    var0.expand(param2, param3);
                    var1.expand(param2, param3);
                    return true;
                };
            default:
                return (param1, param2) -> {
                    for(ComposableEntryContainer var0x : param0) {
                        var0x.expand(param1, param2);
                    }

                    return true;
                };
        }
    }

    public static EntryGroup.Builder list(LootPoolEntryContainer.Builder<?>... param0) {
        return new EntryGroup.Builder(param0);
    }

    public static class Builder extends LootPoolEntryContainer.Builder<EntryGroup.Builder> {
        private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

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
            return new EntryGroup(this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
        }
    }
}
