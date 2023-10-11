package net.minecraft.commands.execution;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.logging.LogUtils;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.tasks.BuildContexts;
import net.minecraft.commands.execution.tasks.CallFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class ExecutionContext<T> implements AutoCloseable {
    private static final int MAX_QUEUE_DEPTH = 10000000;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final int commandLimit;
    private final int forkLimit;
    private final ProfilerFiller profiler;
    @Nullable
    private TraceCallbacks tracer;
    private int commandQuota;
    private boolean queueOverflow;
    private final Deque<CommandQueueEntry<T>> commandQueue = Queues.newArrayDeque();
    private final List<CommandQueueEntry<T>> newTopCommands = Lists.newArrayList();

    public ExecutionContext(int param0, int param1, ProfilerFiller param2) {
        this.commandLimit = param0;
        this.forkLimit = param1;
        this.profiler = param2;
        this.commandQuota = param0;
    }

    public void queueInitialFunctionCall(InstantiatedFunction<T> param0, T param1) {
        this.queueNext(new CommandQueueEntry<>(0, new CallFunction<>(param0).bind(param1)));
    }

    public static <T extends ExecutionCommandSource<T>> void queueInitialCommandExecution(
        ExecutionContext<T> param0, String param1, ContextChain<T> param2, T param3
    ) {
        param0.queueNext(new CommandQueueEntry<>(0, new BuildContexts.TopLevel<>(param1, param2, param3)));
    }

    private void handleQueueOverflow() {
        this.queueOverflow = true;
        this.newTopCommands.clear();
        this.commandQueue.clear();
    }

    public void queueNext(CommandQueueEntry<T> param0) {
        if (this.newTopCommands.size() + this.commandQueue.size() > 10000000) {
            this.handleQueueOverflow();
        }

        if (!this.queueOverflow) {
            this.newTopCommands.add(param0);
        }

    }

    public void discardAtDepthOrHigher(int param0) {
        while(!this.commandQueue.isEmpty() && this.commandQueue.peek().depth() >= param0) {
            this.commandQueue.removeFirst();
        }

    }

    public void runCommandQueue() {
        Lists.reverse(this.newTopCommands).forEach(this.commandQueue::addFirst);
        this.newTopCommands.clear();

        while(!this.commandQueue.isEmpty()) {
            if (this.commandQuota == 0) {
                LOGGER.info("Command execution stopped due to limit (executed {} commands)", this.commandLimit);
                break;
            }

            CommandQueueEntry<T> var0 = this.commandQueue.removeFirst();
            var0.execute(this);
            if (this.queueOverflow) {
                LOGGER.error("Command execution stopped due to command queue overflow (max {})", 10000000);
                break;
            }

            Lists.reverse(this.newTopCommands).forEach(this.commandQueue::addFirst);
            this.newTopCommands.clear();
        }

    }

    public void tracer(@Nullable TraceCallbacks param0) {
        this.tracer = param0;
    }

    @Nullable
    public TraceCallbacks tracer() {
        return this.tracer;
    }

    public ProfilerFiller profiler() {
        return this.profiler;
    }

    public int forkLimit() {
        return this.forkLimit;
    }

    public void incrementCost() {
        --this.commandQuota;
    }

    @Override
    public void close() {
        if (this.tracer != null) {
            this.tracer.close();
        }

    }
}
