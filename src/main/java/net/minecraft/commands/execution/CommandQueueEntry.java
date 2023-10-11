package net.minecraft.commands.execution;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

public record CommandQueueEntry<T>(int depth, EntryAction<T> action) {
    public void execute(ExecutionContext<T> param0) {
        TraceCallbacks var0 = param0.tracer();

        try {
            this.action.execute(param0, this.depth);
        } catch (CommandSyntaxException var4) {
            if (var0 != null) {
                var0.onError(this.depth, var4.getRawMessage().getString());
            }
        } catch (Exception var5) {
            if (var0 != null) {
                var0.onError(this.depth, var5.getMessage());
            }
        }

    }
}
