package net.minecraft.world.level.storage.loot.predicates;

import java.util.function.Function;

public interface ConditionUserBuilder<T extends ConditionUserBuilder<T>> {
    T when(LootItemCondition.Builder var1);

    default <E> T when(Iterable<E> param0, Function<E, LootItemCondition.Builder> param1) {
        T var0 = this.unwrap();

        for(E var1 : param0) {
            var0 = var0.when(param1.apply(var1));
        }

        return var0;
    }

    T unwrap();
}
