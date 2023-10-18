package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.slf4j.Logger;

@FunctionalInterface
public interface TaskChainer {
    Logger LOGGER = LogUtils.getLogger();

    static TaskChainer immediate(final Executor param0) {
        return new TaskChainer() {
            @Override
            public <T> void append(CompletableFuture<T> param0x, Consumer<T> param1) {
                param0.thenAcceptAsync(param1, param0).exceptionally(param0xx -> {
                    LOGGER.error("Task failed", param0xx);
                    return null;
                });
            }
        };
    }

    default void append(Runnable param0) {
        this.append(CompletableFuture.completedFuture(null), param1 -> param0.run());
    }

    <T> void append(CompletableFuture<T> var1, Consumer<T> var2);
}
