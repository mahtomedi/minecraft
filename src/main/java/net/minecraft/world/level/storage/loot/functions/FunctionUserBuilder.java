package net.minecraft.world.level.storage.loot.functions;

import java.util.Arrays;
import java.util.function.Function;

public interface FunctionUserBuilder<T extends FunctionUserBuilder<T>> {
    T apply(LootItemFunction.Builder var1);

    default <E> T apply(Iterable<E> param0, Function<E, LootItemFunction.Builder> param1) {
        T var0 = this.unwrap();

        for(E var1 : param0) {
            var0 = var0.apply(param1.apply(var1));
        }

        return var0;
    }

    default <E> T apply(E[] param0, Function<E, LootItemFunction.Builder> param1) {
        return this.apply(Arrays.asList(param0), param1);
    }

    T unwrap();
}
