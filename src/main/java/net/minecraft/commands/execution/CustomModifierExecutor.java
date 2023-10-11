package net.minecraft.commands.execution;

import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;

public interface CustomModifierExecutor<T> {
    void apply(List<T> var1, ContextChain<T> var2, boolean var3, ExecutionControl<T> var4) throws CommandSyntaxException;

    public interface ModifierAdapter<T> extends RedirectModifier<T>, CustomModifierExecutor<T> {
        @Override
        default Collection<T> apply(CommandContext<T> param0) throws CommandSyntaxException {
            throw new UnsupportedOperationException("This function should not run");
        }
    }
}
