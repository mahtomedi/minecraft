package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SequentialEntry extends CompositeEntryBase {
    SequentialEntry(LootPoolEntryContainer[] param0, LootItemCondition[] param1) {
        super(param0, param1);
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.SEQUENCE;
    }

    @Override
    protected ComposableEntryContainer compose(ComposableEntryContainer[] param0) {
        switch(param0.length) {
            case 0:
                return ALWAYS_TRUE;
            case 1:
                return param0[0];
            case 2:
                return param0[0].and(param0[1]);
            default:
                return (param1, param2) -> {
                    for(ComposableEntryContainer var0 : param0) {
                        if (!var0.expand(param1, param2)) {
                            return false;
                        }
                    }

                    return true;
                };
        }
    }

    public static SequentialEntry.Builder sequential(LootPoolEntryContainer.Builder<?>... param0) {
        return new SequentialEntry.Builder(param0);
    }

    public static class Builder extends LootPoolEntryContainer.Builder<SequentialEntry.Builder> {
        private final List<LootPoolEntryContainer> entries = Lists.newArrayList();

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
            return new SequentialEntry(this.entries.toArray(new LootPoolEntryContainer[0]), this.getConditions());
        }
    }
}
