package net.minecraft.world.level.storage.loot.entries;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.world.level.storage.loot.LootContext;

@FunctionalInterface
interface ComposableEntryContainer {
    ComposableEntryContainer ALWAYS_FALSE = (param0, param1) -> false;
    ComposableEntryContainer ALWAYS_TRUE = (param0, param1) -> true;

    boolean expand(LootContext var1, Consumer<LootPoolEntry> var2);

    default ComposableEntryContainer and(ComposableEntryContainer param0) {
        Objects.requireNonNull(param0);
        return (param1, param2) -> this.expand(param1, param2) && param0.expand(param1, param2);
    }

    default ComposableEntryContainer or(ComposableEntryContainer param0) {
        Objects.requireNonNull(param0);
        return (param1, param2) -> this.expand(param1, param2) || param0.expand(param1, param2);
    }
}
