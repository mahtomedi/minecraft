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
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.ChainModifiers;
import net.minecraft.commands.execution.CommandQueueEntry;
import net.minecraft.commands.execution.CustomCommandExecutor;
import net.minecraft.commands.execution.CustomModifierExecutor;
import net.minecraft.commands.execution.EntryAction;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.execution.ExecutionControl;
import net.minecraft.commands.execution.Frame;
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

    protected void execute(T param0, List<T> param1, ExecutionContext<T> param2, Frame param3, ChainModifiers param4) {
        ContextChain<T> var0 = this.command;
        ChainModifiers var1 = param4;
        List<T> var2 = param1;
        if (var0.getStage() != Stage.EXECUTE) {
            param2.profiler().push(() -> "prepare " + this.commandInput);

            try {
                for(int var3 = param2.forkLimit(); var0.getStage() != Stage.EXECUTE; var0 = var0.nextStage()) {
                    CommandContext<T> var4 = var0.getTopContext();
                    if (var4.isForked()) {
                        var1 = var1.setForked();
                    }

                    RedirectModifier<T> var5 = var4.getRedirectModifier();
                    if (var5 instanceof CustomModifierExecutor var6) {
                        var6.apply(param0, var2, var0, var1, ExecutionControl.create(param2, param3));
                        return;
                    }

                    if (var5 != null) {
                        param2.incrementCost();
                        var6 = var1.isForked();
                        List<T> var8 = new ArrayList<>();

                        for(T var9 : var2) {
                            try {
                                Collection<T> var10 = ContextChain.runModifier(var4, var9, (param0x, param1x, param2x) -> {
                                }, (boolean)var6);
                                if (var8.size() + var10.size() >= var3) {
                                    param0.handleError(ERROR_FORK_LIMIT_REACHED.create(var3), (boolean)var6, param2.tracer());
                                    return;
                                }

                                var8.addAll(var10);
                            } catch (CommandSyntaxException var20) {
                                var9.handleError(var20, (boolean)var6, param2.tracer());
                                if (var6 == false) {
                                    return;
                                }
                            }
                        }

                        var2 = var8;
                    }
                }
            } finally {
                param2.profiler().pop();
            }
        }

        if (var2.isEmpty()) {
            if (var1.isReturn()) {
                param2.queueNext(new CommandQueueEntry<>(param3, FallthroughTask.instance()));
            }

        } else {
            CommandContext<T> var12 = var0.getTopContext();
            Command<T> var13 = var12.getCommand();
            if (var13 instanceof CustomCommandExecutor var14) {
                ExecutionControl<T> var15 = ExecutionControl.create(param2, param3);

                for(T var16 : var2) {
                    var14.run(var16, var0, var1, var15);
                }
            } else {
                if (var1.isReturn()) {
                    T var17 = var2.get(0);
                    var17 = var17.withCallback(CommandResultCallback.chain(var17.callback(), param3.returnValueConsumer()));
                    var2 = List.of(var17);
                }

                ExecuteCommand<T> var18 = new ExecuteCommand<>(this.commandInput, var1, var12);
                ContinuationTask.schedule(param2, param3, var2, (param1x, param2x) -> new CommandQueueEntry<>(param1x, var18.bind(param2x)));
            }

        }
    }

    protected void traceCommandStart(ExecutionContext<T> param0, Frame param1) {
        TraceCallbacks var0 = param0.tracer();
        if (var0 != null) {
            var0.onCommand(param1.depth(), this.commandInput);
        }

    }

    @Override
    public String toString() {
        return this.commandInput;
    }

    public static class Continuation<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {
        private final ChainModifiers modifiers;
        private final T originalSource;
        private final List<T> sources;

        public Continuation(String param0, ContextChain<T> param1, ChainModifiers param2, T param3, List<T> param4) {
            super(param0, param1);
            this.originalSource = param3;
            this.sources = param4;
            this.modifiers = param2;
        }

        @Override
        public void execute(ExecutionContext<T> param0, Frame param1) {
            this.execute(this.originalSource, this.sources, param0, param1, this.modifiers);
        }
    }

    public static class TopLevel<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements EntryAction<T> {
        private final T source;

        public TopLevel(String param0, ContextChain<T> param1, T param2) {
            super(param0, param1);
            this.source = param2;
        }

        @Override
        public void execute(ExecutionContext<T> param0, Frame param1) {
            this.traceCommandStart(param0, param1);
            this.execute(this.source, List.of(this.source), param0, param1, ChainModifiers.DEFAULT);
        }
    }

    public static class Unbound<T extends ExecutionCommandSource<T>> extends BuildContexts<T> implements UnboundEntryAction<T> {
        public Unbound(String param0, ContextChain<T> param1) {
            super(param0, param1);
        }

        public void execute(T param0, ExecutionContext<T> param1, Frame param2) {
            this.traceCommandStart(param1, param2);
            this.execute(param0, List.of(param0), param1, param2, ChainModifiers.DEFAULT);
        }
    }
}
