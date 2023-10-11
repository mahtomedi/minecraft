package net.minecraft.commands.execution;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface UnboundEntryAction<T> {
    void execute(T var1, ExecutionContext<T> var2, int var3) throws CommandSyntaxException;

    default EntryAction<T> bind(T param0) {
        return (param1, param2) -> this.execute(param0, param1, param2);
    }
}
