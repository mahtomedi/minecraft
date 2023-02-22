package com.mojang.realmsclient.gui.task;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.TimeSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class DataFetcher {
    static final Logger LOGGER = LogUtils.getLogger();
    final Executor executor;
    final TimeUnit resolution;
    final TimeSource timeSource;

    public DataFetcher(Executor param0, TimeUnit param1, TimeSource param2) {
        this.executor = param0;
        this.resolution = param1;
        this.timeSource = param2;
    }

    public <T> DataFetcher.Task<T> createTask(String param0, Callable<T> param1, Duration param2, RepeatedDelayStrategy param3) {
        long var0 = this.resolution.convert(param2);
        if (var0 == 0L) {
            throw new IllegalArgumentException("Period of " + param2 + " too short for selected resolution of " + this.resolution);
        } else {
            return new DataFetcher.Task<>(param0, param1, var0, param3);
        }
    }

    public DataFetcher.Subscription createSubscription() {
        return new DataFetcher.Subscription();
    }

    @OnlyIn(Dist.CLIENT)
    static record ComputationResult<T>(Either<T, Exception> value, long time) {
    }

    @OnlyIn(Dist.CLIENT)
    class SubscribedTask<T> {
        private final DataFetcher.Task<T> task;
        private final Consumer<T> output;
        private long lastCheckTime = -1L;

        SubscribedTask(DataFetcher.Task<T> param0, Consumer<T> param1) {
            this.task = param0;
            this.output = param1;
        }

        void update(long param0) {
            this.task.updateIfNeeded(param0);
            this.runCallbackIfNeeded();
        }

        void runCallbackIfNeeded() {
            DataFetcher.SuccessfulComputationResult<T> var0 = this.task.lastResult;
            if (var0 != null && this.lastCheckTime < var0.time) {
                this.output.accept(var0.value);
                this.lastCheckTime = var0.time;
            }

        }

        void runCallback() {
            DataFetcher.SuccessfulComputationResult<T> var0 = this.task.lastResult;
            if (var0 != null) {
                this.output.accept(var0.value);
                this.lastCheckTime = var0.time;
            }

        }

        void reset() {
            this.task.reset();
            this.lastCheckTime = -1L;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class Subscription {
        private final List<DataFetcher.SubscribedTask<?>> subscriptions = new ArrayList<>();

        public <T> void subscribe(DataFetcher.Task<T> param0, Consumer<T> param1) {
            DataFetcher.SubscribedTask<T> var0 = DataFetcher.this.new SubscribedTask<>(param0, param1);
            this.subscriptions.add(var0);
            var0.runCallbackIfNeeded();
        }

        public void forceUpdate() {
            for(DataFetcher.SubscribedTask<?> var0 : this.subscriptions) {
                var0.runCallback();
            }

        }

        public void tick() {
            for(DataFetcher.SubscribedTask<?> var0 : this.subscriptions) {
                var0.update(DataFetcher.this.timeSource.get(DataFetcher.this.resolution));
            }

        }

        public void reset() {
            for(DataFetcher.SubscribedTask<?> var0 : this.subscriptions) {
                var0.reset();
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    static record SuccessfulComputationResult<T>(T value, long time) {
    }

    @OnlyIn(Dist.CLIENT)
    public class Task<T> {
        private final String id;
        private final Callable<T> updater;
        private final long period;
        private final RepeatedDelayStrategy repeatStrategy;
        @Nullable
        private CompletableFuture<DataFetcher.ComputationResult<T>> pendingTask;
        @Nullable
        DataFetcher.SuccessfulComputationResult<T> lastResult;
        private long nextUpdate = -1L;

        Task(String param1, Callable<T> param2, long param3, RepeatedDelayStrategy param4) {
            this.id = param1;
            this.updater = param2;
            this.period = param3;
            this.repeatStrategy = param4;
        }

        void updateIfNeeded(long param0) {
            if (this.pendingTask != null) {
                DataFetcher.ComputationResult<T> var0 = (DataFetcher.ComputationResult)this.pendingTask.getNow((T)null);
                if (var0 == null) {
                    return;
                }

                this.pendingTask = null;
                long var1 = var0.time;
                var0.value().ifLeft(param1 -> {
                    this.lastResult = new DataFetcher.SuccessfulComputationResult<>(param1, var1);
                    this.nextUpdate = var1 + this.period * this.repeatStrategy.delayCyclesAfterSuccess();
                }).ifRight(param1 -> {
                    long var0x = this.repeatStrategy.delayCyclesAfterFailure();
                    DataFetcher.LOGGER.warn("Failed to process task {}, will repeat after {} cycles", this.id, var0x, param1);
                    this.nextUpdate = var1 + this.period * var0x;
                });
            }

            if (this.nextUpdate <= param0) {
                this.pendingTask = CompletableFuture.supplyAsync(() -> {
                    try {
                        T var0x = this.updater.call();
                        long var1x = DataFetcher.this.timeSource.get(DataFetcher.this.resolution);
                        return new DataFetcher.ComputationResult<>(Either.left(var0x), var1x);
                    } catch (Exception var4x) {
                        long var3x = DataFetcher.this.timeSource.get(DataFetcher.this.resolution);
                        return new DataFetcher.ComputationResult<>(Either.right(var4x), var3x);
                    }
                }, DataFetcher.this.executor);
            }

        }

        public void reset() {
            this.pendingTask = null;
            this.lastResult = null;
            this.nextUpdate = -1L;
        }
    }
}
