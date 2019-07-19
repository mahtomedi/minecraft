package net.minecraft.server.level;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.util.thread.StrictQueue;
import net.minecraft.world.level.ChunkPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkTaskPriorityQueueSorter implements AutoCloseable, ChunkHolder.LevelChangeListener {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<ProcessorHandle<?>, ChunkTaskPriorityQueue<? extends Function<ProcessorHandle<Unit>, ?>>> queues;
    private final Set<ProcessorHandle<?>> sleeping;
    private final ProcessorMailbox<StrictQueue.IntRunnable> mailbox;

    public ChunkTaskPriorityQueueSorter(List<ProcessorHandle<?>> param0, Executor param1, int param2) {
        this.queues = param0.stream()
            .collect(Collectors.toMap(Function.identity(), param1x -> new ChunkTaskPriorityQueue<>(param1x.name() + "_queue", param2)));
        this.sleeping = Sets.newHashSet(param0);
        this.mailbox = new ProcessorMailbox<>(new StrictQueue.FixedPriorityQueue(4), param1, "sorter");
    }

    public static ChunkTaskPriorityQueueSorter.Message<Runnable> message(Runnable param0, long param1, IntSupplier param2) {
        return new ChunkTaskPriorityQueueSorter.Message<>(param1x -> () -> {
                param0.run();
                param1x.tell(Unit.INSTANCE);
            }, param1, param2);
    }

    public static ChunkTaskPriorityQueueSorter.Message<Runnable> message(ChunkHolder param0, Runnable param1) {
        return message(param1, param0.getPos().toLong(), param0::getQueueLevel);
    }

    public static ChunkTaskPriorityQueueSorter.Release release(Runnable param0, long param1, boolean param2) {
        return new ChunkTaskPriorityQueueSorter.Release(param0, param1, param2);
    }

    public <T> ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<T>> getProcessor(ProcessorHandle<T> param0, boolean param1) {
        return this.mailbox
            .<ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<T>>>ask(
                param2 -> new StrictQueue.IntRunnable(
                        0,
                        () -> {
                            this.getQueue(param0);
                            param2.tell(
                                ProcessorHandle.of(
                                    "chunk priority sorter around " + param0.name(),
                                    param2x -> this.submit(param0, param2x.task, param2x.pos, param2x.level, param1)
                                )
                            );
                        }
                    )
            )
            .join();
    }

    public ProcessorHandle<ChunkTaskPriorityQueueSorter.Release> getReleaseProcessor(ProcessorHandle<Runnable> param0) {
        return this.mailbox
            .<ProcessorHandle<ChunkTaskPriorityQueueSorter.Release>>ask(
                param1 -> new StrictQueue.IntRunnable(
                        0,
                        () -> param1.tell(
                                ProcessorHandle.of(
                                    "chunk priority sorter around " + param0.name(),
                                    param1x -> this.release(param0, param1x.pos, param1x.task, param1x.clearQueue)
                                )
                            )
                    )
            )
            .join();
    }

    @Override
    public void onLevelChange(ChunkPos param0, IntSupplier param1, int param2, IntConsumer param3) {
        this.mailbox.tell(new StrictQueue.IntRunnable(0, () -> {
            int var0 = param1.getAsInt();
            this.queues.values().forEach(param3x -> param3x.resortChunkTasks(var0, param0, param2));
            param3.accept(param2);
        }));
    }

    private <T> void release(ProcessorHandle<T> param0, long param1, Runnable param2, boolean param3) {
        this.mailbox.tell(new StrictQueue.IntRunnable(1, () -> {
            ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> var0 = this.getQueue(param0);
            var0.release(param1, param3);
            if (this.sleeping.remove(param0)) {
                this.pollTask(var0, param0);
            }

            param2.run();
        }));
    }

    private <T> void submit(ProcessorHandle<T> param0, Function<ProcessorHandle<Unit>, T> param1, long param2, IntSupplier param3, boolean param4) {
        this.mailbox.tell(new StrictQueue.IntRunnable(2, () -> {
            ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> var0 = this.getQueue(param0);
            int var1x = param3.getAsInt();
            var0.submit(Optional.of(param1), param2, var1x);
            if (param4) {
                var0.submit(Optional.empty(), param2, var1x);
            }

            if (this.sleeping.remove(param0)) {
                this.pollTask(var0, param0);
            }

        }));
    }

    private <T> void pollTask(ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> param0, ProcessorHandle<T> param1) {
        this.mailbox.tell(new StrictQueue.IntRunnable(3, () -> {
            Stream<Either<Function<ProcessorHandle<Unit>, T>, Runnable>> var0 = param0.pop();
            if (var0 == null) {
                this.sleeping.add(param1);
            } else {
                Util.sequence(var0.<CompletableFuture>map(param1x -> param1x.map(param1::ask, param0x -> {
                        param0x.run();
                        return CompletableFuture.completedFuture(Unit.INSTANCE);
                    })).collect(Collectors.toList())).thenAccept(param2 -> this.pollTask(param0, param1));
            }

        }));
    }

    private <T> ChunkTaskPriorityQueue<Function<ProcessorHandle<Unit>, T>> getQueue(ProcessorHandle<T> param0) {
        ChunkTaskPriorityQueue<? extends Function<ProcessorHandle<Unit>, ?>> var0 = this.queues.get(param0);
        if (var0 == null) {
            throw new IllegalArgumentException("No queue for: " + param0);
        } else {
            return var0;
        }
    }

    @VisibleForTesting
    public String getDebugStatus() {
        return (String)this.queues
                .entrySet()
                .stream()
                .map(
                    param0 -> param0.getKey().name()
                            + "=["
                            + (String)param0.getValue()
                                .getAcquired()
                                .stream()
                                .map(param0x -> param0x + ":" + new ChunkPos(param0x))
                                .collect(Collectors.joining(","))
                            + "]"
                )
                .collect(Collectors.joining(","))
            + ", s="
            + this.sleeping.size();
    }

    @Override
    public void close() {
        this.queues.keySet().forEach(ProcessorHandle::close);
    }

    public static final class Message<T> {
        private final Function<ProcessorHandle<Unit>, T> task;
        private final long pos;
        private final IntSupplier level;

        private Message(Function<ProcessorHandle<Unit>, T> param0, long param1, IntSupplier param2) {
            this.task = param0;
            this.pos = param1;
            this.level = param2;
        }
    }

    public static final class Release {
        private final Runnable task;
        private final long pos;
        private final boolean clearQueue;

        private Release(Runnable param0, long param1, boolean param2) {
            this.task = param0;
            this.pos = param1;
            this.clearQueue = param2;
        }
    }
}
