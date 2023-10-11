package net.minecraft.commands.execution.tasks;

import java.util.List;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.InstantiatedFunction;

public class CallFunction<T> implements UnboundEntryAction<T> {
    private final InstantiatedFunction<T> function;

    public CallFunction(InstantiatedFunction<T> param0) {
        this.function = param0;
    }

    @Override
    public void execute(T param0, ExecutionContext<T> param1, int param2) {
        param1.incrementCost();
        List<UnboundEntryAction<T>> var0 = this.function.entries();
        TraceCallbacks var1 = param1.tracer();
        if (var1 != null) {
            var1.onCall(param2, this.function.id(), this.function.entries().size());
        }

        ContinuationTask.schedule(param1, param2 + 1, var0, (param1x, param2x) -> new CommandQueueEntry<>(param1x, param2x.bind(param0)));
    }
}
