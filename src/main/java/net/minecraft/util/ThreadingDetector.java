package net.minecraft.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class ThreadingDetector {
    public static void checkAndLock(ReentrantLock param0, String param1) {
        if (param0.isLocked() && !param0.isHeldByCurrentThread()) {
            throw makeThreadingException(param1);
        } else {
            param0.lock();
        }
    }

    public static ReportedException makeThreadingException(String param0) {
        String var0 = Thread.getAllStackTraces()
            .keySet()
            .stream()
            .filter(Objects::nonNull)
            .map(
                param0x -> param0x.getName()
                        + ": \n\tat "
                        + (String)Arrays.stream(param0x.getStackTrace()).map(Object::toString).collect(Collectors.joining("\n\tat "))
            )
            .collect(Collectors.joining("\n"));
        CrashReport var1 = new CrashReport("Accessing " + param0 + " from multiple threads", new IllegalStateException());
        CrashReportCategory var2 = var1.addCategory("Thread dumps");
        var2.setDetail("Thread dumps", var0);
        return new ReportedException(var1);
    }
}
