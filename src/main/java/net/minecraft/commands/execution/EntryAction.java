package net.minecraft.commands.execution;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface EntryAction<T> {
    void execute(ExecutionContext<T> var1, int var2) throws CommandSyntaxException;
}
