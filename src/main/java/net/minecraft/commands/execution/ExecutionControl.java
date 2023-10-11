package net.minecraft.commands.execution;

import javax.annotation.Nullable;

public interface ExecutionControl<T> {
    void queueNext(EntryAction<T> var1);

    void discardCurrentDepth();

    void tracer(@Nullable TraceCallbacks var1);

    @Nullable
    TraceCallbacks tracer();
}
