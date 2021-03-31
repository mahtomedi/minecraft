package net.minecraft.util.thread;

import com.google.common.collect.Queues;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;

public interface StrictQueue<T, F> {
    @Nullable
    F pop();

    boolean push(T var1);

    boolean isEmpty();

    int size();

    public static final class FixedPriorityQueue implements StrictQueue<StrictQueue.IntRunnable, Runnable> {
        private final List<Queue<Runnable>> queueList;

        public FixedPriorityQueue(int param0) {
            this.queueList = IntStream.range(0, param0).mapToObj(param0x -> Queues.newConcurrentLinkedQueue()).collect(Collectors.toList());
        }

        @Nullable
        public Runnable pop() {
            for(Queue<Runnable> var0 : this.queueList) {
                Runnable var1 = var0.poll();
                if (var1 != null) {
                    return var1;
                }
            }

            return null;
        }

        public boolean push(StrictQueue.IntRunnable param0) {
            int var0 = param0.getPriority();
            this.queueList.get(var0).add(param0);
            return true;
        }

        @Override
        public boolean isEmpty() {
            return this.queueList.stream().allMatch(Collection::isEmpty);
        }

        @Override
        public int size() {
            int var0 = 0;

            for(Queue<Runnable> var1 : this.queueList) {
                var0 += var1.size();
            }

            return var0;
        }
    }

    public static final class IntRunnable implements Runnable {
        private final int priority;
        private final Runnable task;

        public IntRunnable(int param0, Runnable param1) {
            this.priority = param0;
            this.task = param1;
        }

        @Override
        public void run() {
            this.task.run();
        }

        public int getPriority() {
            return this.priority;
        }
    }

    public static final class QueueStrictQueue<T> implements StrictQueue<T, T> {
        private final Queue<T> queue;

        public QueueStrictQueue(Queue<T> param0) {
            this.queue = param0;
        }

        @Nullable
        @Override
        public T pop() {
            return this.queue.poll();
        }

        @Override
        public boolean push(T param0) {
            return this.queue.add(param0);
        }

        @Override
        public boolean isEmpty() {
            return this.queue.isEmpty();
        }

        @Override
        public int size() {
            return this.queue.size();
        }
    }
}
