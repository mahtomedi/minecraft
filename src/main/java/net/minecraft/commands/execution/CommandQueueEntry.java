package net.minecraft.commands.execution;

public record CommandQueueEntry<T>(Frame frame, EntryAction<T> action) {
    public void execute(ExecutionContext<T> param0) {
        TraceCallbacks var0 = param0.tracer();

        try {
            this.action.execute(param0, this.frame);
        } catch (Exception var4) {
            if (var0 != null) {
                var0.onError(var4.getMessage());
            }
        }

    }
}
