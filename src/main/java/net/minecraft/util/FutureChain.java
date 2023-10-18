package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.slf4j.Logger;

public class FutureChain implements AutoCloseable, TaskChainer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private CompletableFuture<?> head = CompletableFuture.completedFuture(null);
    private final Executor executor;
    private volatile boolean closed;

    public FutureChain(Executor param0) {
        this.executor = param0;
    }

    @Override
    public <T> void append(CompletableFuture<T> param0, Consumer<T> param1) {
        this.head = this.head.<T, Object>thenCombine(param0, (param0x, param1x) -> param1x).thenAcceptAsync(param1x -> {
            if (!this.closed) {
                param1.accept((T)param1x);
            }

        }, this.executor).exceptionally(param0x -> {
            if (param0x instanceof CompletionException var1x) {
                param0x = var1x.getCause();
            }

            if (param0x instanceof CancellationException var2x) {
                throw var2x;
            } else {
                LOGGER.error("Chain link failed, continuing to next one", param0x);
                return null;
            }
        });
    }

    @Override
    public void close() {
        this.closed = true;
    }
}
