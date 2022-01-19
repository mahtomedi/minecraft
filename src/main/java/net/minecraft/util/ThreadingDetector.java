package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import org.slf4j.Logger;

public class ThreadingDetector {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String name;
    private final Semaphore lock = new Semaphore(1);
    private final Lock stackTraceLock = new ReentrantLock();
    @Nullable
    private volatile Thread threadThatFailedToAcquire;
    @Nullable
    private volatile ReportedException fullException;

    public ThreadingDetector(String param0) {
        this.name = param0;
    }

    public void checkAndLock() {
        boolean var0 = false;

        try {
            this.stackTraceLock.lock();
            if (!this.lock.tryAcquire()) {
                this.threadThatFailedToAcquire = Thread.currentThread();
                var0 = true;
                this.stackTraceLock.unlock();

                try {
                    this.lock.acquire();
                } catch (InterruptedException var6) {
                    Thread.currentThread().interrupt();
                }

                throw this.fullException;
            }
        } finally {
            if (!var0) {
                this.stackTraceLock.unlock();
            }

        }

    }

    public void checkAndUnlock() {
        try {
            this.stackTraceLock.lock();
            Thread var0 = this.threadThatFailedToAcquire;
            if (var0 != null) {
                ReportedException var1 = makeThreadingException(this.name, var0);
                this.fullException = var1;
                this.lock.release();
                throw var1;
            }

            this.lock.release();
        } finally {
            this.stackTraceLock.unlock();
        }

    }

    public static ReportedException makeThreadingException(String param0, @Nullable Thread param1) {
        String var0 = Stream.of(Thread.currentThread(), param1).filter(Objects::nonNull).map(ThreadingDetector::stackTrace).collect(Collectors.joining("\n"));
        String var1 = "Accessing " + param0 + " from multiple threads";
        CrashReport var2 = new CrashReport(var1, new IllegalStateException(var1));
        CrashReportCategory var3 = var2.addCategory("Thread dumps");
        var3.setDetail("Thread dumps", var0);
        LOGGER.error("Thread dumps: \n" + var0);
        return new ReportedException(var2);
    }

    private static String stackTrace(Thread param0x) {
        return param0x.getName() + ": \n\tat " + (String)Arrays.stream(param0x.getStackTrace()).map(Object::toString).collect(Collectors.joining("\n\tat "));
    }
}
