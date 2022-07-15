package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.slf4j.Logger;

@FunctionalInterface
public interface TaskChainer {
    Logger LOGGER = LogUtils.getLogger();
    TaskChainer IMMEDIATE = param0 -> param0.get().exceptionally(param0x -> {
            LOGGER.error("Task failed", param0x);
            return null;
        });

    void append(TaskChainer.DelayedTask var1);

    public interface DelayedTask extends Supplier<CompletableFuture<?>> {
    }
}
