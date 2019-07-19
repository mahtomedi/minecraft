package net.minecraft.world.level.storage.loot.entries;

import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SequentialEntry extends CompositeEntryBase {
    SequentialEntry(LootPoolEntryContainer[] param0, LootItemCondition[] param1) {
        super(param0, param1);
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
}
