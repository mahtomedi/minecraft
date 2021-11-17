package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public class ThreadingDetector {
    public static void checkAndLock(Semaphore param0, @Nullable DebugBuffer<Pair<Thread, StackTraceElement[]>> param1, String param2) {
        boolean var0 = param0.tryAcquire();
        if (!var0) {
            throw makeThreadingException(param2, param1);
        }
    }

    public static ReportedException makeThreadingException(String param0, @Nullable DebugBuffer<Pair<Thread, StackTraceElement[]>> param1) {
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
        if (param1 != null) {
            StringBuilder var3 = new StringBuilder();

            for(Pair<Thread, StackTraceElement[]> var5 : param1.dump()) {
                var3.append("Thread ")
                    .append(var5.getFirst().getName())
                    .append(": \n\tat ")
                    .append(Arrays.stream(var5.getSecond()).map(Object::toString).collect(Collectors.joining("\n\tat ")))
                    .append("\n");
            }

            var2.setDetail("Last threads", var3.toString());
        }

        return new ReportedException(var1);
    }
}
