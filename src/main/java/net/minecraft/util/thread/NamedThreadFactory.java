package net.minecraft.util.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NamedThreadFactory implements ThreadFactory {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public NamedThreadFactory(String param0) {
        SecurityManager var0 = System.getSecurityManager();
        this.group = var0 != null ? var0.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = param0 + "-";
    }

    @Override
    public Thread newThread(Runnable param0) {
        Thread var0 = new Thread(this.group, param0, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
        var0.setUncaughtExceptionHandler((param1, param2) -> {
            LOGGER.error("Caught exception in thread {} from {}", param1, param0);
            LOGGER.error("", param2);
        });
        if (var0.getPriority() != 5) {
            var0.setPriority(5);
        }

        return var0;
    }
}
