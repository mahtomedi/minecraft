package net.minecraft.commands.execution.tasks;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;

public class ExecuteCommand<T extends ExecutionCommandSource<T>> implements UnboundEntryAction<T> {
    private final String commandInput;
    private final ChainModifiers modifiers;
    private final CommandContext<T> executionContext;

    public ExecuteCommand(String param0, ChainModifiers param1, CommandContext<T> param2) {
        this.commandInput = param0;
        this.modifiers = param1;
        this.executionContext = param2;
    }

    public void execute(T param0, ExecutionContext<T> param1, Frame param2) {
        param1.profiler().push(() -> "execute " + this.commandInput);

        try {
            param1.incrementCost();
            int var0 = ContextChain.runExecutable(this.executionContext, param0, ExecutionCommandSource.resultConsumer(), this.modifiers.isForked());
            TraceCallbacks var1 = param1.tracer();
            if (var1 != null) {
                var1.onReturn(param2.depth(), this.commandInput, var0);
            }
        } catch (CommandSyntaxException var9) {
            param0.handleError(var9, this.modifiers.isForked(), param1.tracer());
        } finally {
            param1.profiler().pop();
        }

    }
}
