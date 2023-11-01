package net.minecraft.commands.execution;

import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;

public interface ExecutionControl<T> {
    void queueNext(EntryAction<T> var1);

    void tracer(@Nullable TraceCallbacks var1);

    @Nullable
    TraceCallbacks tracer();

    Frame currentFrame();

    static <T extends ExecutionCommandSource<T>> ExecutionControl<T> create(final ExecutionContext<T> param0, final Frame param1) {
        return new ExecutionControl<T>() {
            @Override
            public void queueNext(EntryAction<T> param0x) {
                param0.queueNext(new CommandQueueEntry<>(param1, param0));
            }

            @Override
            public void tracer(@Nullable TraceCallbacks param0x) {
                param0.tracer(param0);
            }

            @Nullable
            @Override
            public TraceCallbacks tracer() {
                return param0.tracer();
            }

            @Override
            public Frame currentFrame() {
                return param1;
            }
        };
    }
}
