package net.minecraft.commands.execution;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;

public interface CustomCommandExecutor<T> {
    void run(T var1, ContextChain<T> var2, boolean var3, ExecutionControl<T> var4);

    public interface CommandAdapter<T> extends Command<T>, CustomCommandExecutor<T> {
        @Override
        default int run(CommandContext<T> param0) throws CommandSyntaxException {
            throw new UnsupportedOperationException("This function should not run");
        }
    }

    public abstract static class WithErrorHandling<T extends ExecutionCommandSource<T>> implements CustomCommandExecutor<T> {
        public final void run(T param0, ContextChain<T> param1, boolean param2, ExecutionControl<T> param3) {
            try {
                this.runGuarded(param0, param1, param2, param3);
            } catch (CommandSyntaxException var6) {
                this.onError(var6, param0, param2, param3.tracer());
                param0.storeResults(false, 0);
            }

        }

        protected void onError(CommandSyntaxException param0, T param1, boolean param2, @Nullable TraceCallbacks param3) {
            param1.handleError(param0, param2, param3);
        }

        protected abstract void runGuarded(T var1, ContextChain<T> var2, boolean var3, ExecutionControl<T> var4) throws CommandSyntaxException;
    }
}
