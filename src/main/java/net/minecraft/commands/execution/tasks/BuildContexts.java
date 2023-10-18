package net.minecraft.commands.execution.tasks;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.context.ContextChain.Stage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.TraceCallbacks;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.network.chat.Component;

public class BuildContexts<T extends ExecutionCommandSource<T>> {
    @VisibleForTesting
    public static final DynamicCommandExceptionType ERROR_FORK_LIMIT_REACHED = new DynamicCommandExceptionType(
        param0 -> Component.translatableEscape("command.forkLimit", param0)
    );
    private final String commandInput;
    private final ContextChain<T> command;

    public BuildContexts(String param0, ContextChain<T> param1) {
        this.commandInput = param0;
        this.command = param1;
    }

    protected void execute(List<T> param0, ExecutionContext<T> param1, int param2, boolean param3) {
        ContextChain<T> var0 = this.command;
        boolean var1 = param3;
        List<T> var2 = param0;
        if (var0.getStage() != Stage.EXECUTE) {
            param1.profiler().push(() -> "prepare " + this.commandInput);

            try {
                for(int var3 = param1.forkLimit(); var0.getStage() != Stage.EXECUTE; var0 = var0.nextStage()) {
                    CommandContext<T> var4 = var0.getTopContext();
                    var1 |= var4.isForked();
                    RedirectModifier<T> var5 = var4.getRedirectModifier();
                    if (var5 instanceof CustomModifierExecutor var6) {
                        var6.apply(var2, var0, var1, createExecutionControl(param1, param2));
                        return;
                    }

                    if (var5 != null) {
                        param1.incrementCost();
                        var6 = new ArrayList<T>();

                        for(T var8 : var2) {
                            try {
                                for(T var10 : ContextChain.runModifier(var4, var8, ExecutionCommandSource.resultConsumer(), var1)) {
                                    var6.add(var10);
                                    if (var6.size() >= var3) {
                                        var10.handleError(ERROR_FORK_LIMIT_REACHED.create(var3), var1, param1.tracer());
                                        return;
                                    }
                                }
                            } catch (CommandSyntaxException var20) {
                                var8.handleError(var20, var1, param1.tracer());
                                if (!var1) {
                                    return;
                                }
                            }
                        }

                        var2 = var6;
                    }
                }
            } finally {
                param1.profiler().pop();
            }
        }

        CommandContext<T> var12 = var0.getTopContext();
        Command<T> var13 = var12.getCommand();
        if (var13 instanceof CustomCommandExecutor var14) {
            ExecutionControl<T> var15 = createExecutionControl(param1, param2);

            for(T var16 : var2) {
                var14.run(var16, var0, var1, var15);
            }
        } else {
            ExecuteCommand<T> var17 = new ExecuteCommand<>(this.commandInput, var1, var12);
            ContinuationTask.schedule(param1, param2, var2, (param1x, param2x) -> new CommandQueueEntry<>(param1x, var17.bind(param2x)));
        }

    }

    private static <T extends ExecutionCommandSource<T>> ExecutionControl<T> createExecutionControl(final ExecutionContext<T> param0, final int param1) {
        return new ExecutionControl<T>() {
            @Override
            public void queueNext(EntryAction<T> param0x) {
                param0.queueNext(new CommandQueueEntry<>(param1, param0));
            }

            @Override
            public void discardCurrentDepth() {
                param0.discardAtDepthOrHigher(param1);
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
        };
    }

    protected void traceCommandStart(ExecutionContext<T> param0, int param1) {
        TraceCallbacks var0 = param0.tracer();
        if (var0 != null) {
            var0.onCommand(param1, this.commandInput);
        }

    }

    @Override
    public String toString() {
        return this.commandInput;
    }

    public static class Continuation<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {
        private final boolean startForked;
        private final List<T> sources;

        public Continuation(String param0, ContextChain<T> param1, boolean param2, List<T> param3) {
            super(param0, param1);
            this.startForked = param2;
            this.sources = param3;
        }

        @Override
        public void execute(ExecutionContext<T> param0, int param1) {
            this.execute(this.sources, param0, param1, this.startForked);
        }
    }

    public static class TopLevel<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {
        private final T source;

        public TopLevel(String param0, ContextChain<T> param1, T param2) {
            super(param0, param1);
            this.source = param2;
        }

        @Override
        public void execute(ExecutionContext<T> param0, int param1) {
            this.traceCommandStart(param0, param1);
            this.execute(List.of(this.source), param0, param1, false);
        }
    }

    public static class Unbound<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements UnboundEntryAction<T> {
        public Unbound(String param0, ContextChain<T> param1) {
            super(param0, param1);
        }

        public void execute(T param0, ExecutionContext<T> param1, int param2) {
            this.traceCommandStart(param1, param2);
            this.execute(List.of(param0), param1, param2, false);
        }
    }
}
