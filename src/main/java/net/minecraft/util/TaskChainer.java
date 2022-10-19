package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.Logger;

@FunctionalInterface
public interface TaskChainer {
    Logger LOGGER = LogUtils.getLogger();

    static TaskChainer immediate(Executor param0) {
        return param1 -> param1.submit(param0).exceptionally(param0x -> {
                LOGGER.error("Task failed", param0x);
                return null;
            });
    }

    void append(TaskChainer.DelayedTask var1);

    public interface DelayedTask {
        CompletableFuture<?> submit(Executor var1);
    }
}
