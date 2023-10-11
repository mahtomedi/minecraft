package net.minecraft.commands.execution.tasks;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;

public class ContinuationTask<T, P> implements EntryAction<T> {
    private final ContinuationTask.TaskProvider<T, P> taskFactory;
    private final List<P> arguments;
    private final CommandQueueEntry<T> selfEntry;
    private int index;

    private ContinuationTask(ContinuationTask.TaskProvider<T, P> param0, List<P> param1, int param2) {
        this.taskFactory = param0;
        this.arguments = param1;
        this.selfEntry = new CommandQueueEntry<>(param2, this);
    }

    @Override
    public void execute(ExecutionContext<T> param0, int param1) throws CommandSyntaxException {
        P var0 = this.arguments.get(this.index);
        param0.queueNext(this.taskFactory.create(param1, var0));
        if (++this.index < this.arguments.size()) {
            param0.queueNext(this.selfEntry);
        }

    }

    public static <T, P> void schedule(ExecutionContext<T> param0, int param1, List<P> param2, ContinuationTask.TaskProvider<T, P> param3) {
        int var0 = param2.size();
        if (var0 != 0) {
            if (var0 == 1) {
                param0.queueNext(param3.create(param1, param2.get(0)));
            } else if (var0 == 2) {
                param0.queueNext(param3.create(param1, param2.get(0)));
                param0.queueNext(param3.create(param1, param2.get(1)));
            } else {
                param0.queueNext((new ContinuationTask<>(param3, param2, param1)).selfEntry);
            }

        }
    }

    @FunctionalInterface
    public interface TaskProvider<T, P> {
        CommandQueueEntry<T> create(int var1, P var2);
    }
}
