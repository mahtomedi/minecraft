package net.minecraft.commands.execution.tasks;

import java.util.function.Consumer;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;

public class IsolatedCall<T extends ExecutionCommandSource<T>> implements EntryAction<T> {
    private final Consumer<ExecutionControl<T>> taskProducer;
    private final CommandResultCallback output;

    public IsolatedCall(Consumer<ExecutionControl<T>> param0, CommandResultCallback param1) {
        this.taskProducer = param0;
        this.output = param1;
    }

    @Override
    public void execute(ExecutionContext<T> param0, Frame param1) {
        int var0 = param1.depth() + 1;
        Frame var1 = new Frame(var0, this.output, param0.frameControlForDepth(var0));
        this.taskProducer.accept(ExecutionControl.create(param0, var1));
    }
}
