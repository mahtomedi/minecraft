package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import org.slf4j.Logger;

public class FutureChain implements TaskChainer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private CompletableFuture<?> head = CompletableFuture.completedFuture(null);
    private final Executor executor;

    public FutureChain(Executor param0) {
        this.executor = param0;
    }

    @Override
    public void append(TaskChainer.DelayedTask param0) {
        this.head = this.head.thenComposeAsync(param1 -> param0.get(), this.executor).exceptionally(param0x -> {
            if (param0x instanceof CompletionException var1x) {
                param0x = var1x.getCause();
            }

            if (param0x instanceof CancellationException var1) {
                throw var1;
            } else {
                LOGGER.error("Chain link failed, continuing to next one", param0x);
                return null;
            }
        });
    }
}
