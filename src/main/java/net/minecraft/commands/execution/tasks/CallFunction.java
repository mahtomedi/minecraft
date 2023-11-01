package net.minecraft.commands.execution.tasks;

import java.util.List;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.Frame;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.commands.functions.InstantiatedFunction;

public class CallFunction<T extends ExecutionCommandSource<T>> implements UnboundEntryAction<T> {
    private final InstantiatedFunction<T> function;
    private final CommandResultCallback resultCallback;
    private final boolean returnParentFrame;

    public CallFunction(InstantiatedFunction<T> param0, CommandResultCallback param1, boolean param2) {
        this.function = param0;
        this.resultCallback = param1;
        this.returnParentFrame = param2;
    }

    public void execute(T param0, ExecutionContext<T> param1, Frame param2) {
        param1.incrementCost();
        List<UnboundEntryAction<T>> var0 = this.function.entries();
        TraceCallbacks var1 = param1.tracer();
        if (var1 != null) {
            var1.onCall(param2.depth(), this.function.id(), this.function.entries().size());
        }

        int var2 = param2.depth() + 1;
        Frame.FrameControl var3 = this.returnParentFrame ? param2.frameControl() : param1.frameControlForDepth(var2);
        Frame var4 = new Frame(var2, this.resultCallback, var3);
        ContinuationTask.schedule(param1, var4, var0, (param1x, param2x) -> new CommandQueueEntry<>(param1x, param2x.bind(param0)));
    }
}
