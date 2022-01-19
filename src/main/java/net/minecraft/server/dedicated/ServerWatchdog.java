package net.minecraft.server.dedicated;

import com.google.common.collect.Streams;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.Util;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;

public class ServerWatchdog implements Runnable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long MAX_SHUTDOWN_TIME = 10000L;
    private static final int SHUTDOWN_STATUS = 1;
    private final DedicatedServer server;
    private final long maxTickTime;

    public ServerWatchdog(DedicatedServer param0) {
        this.server = param0;
        this.maxTickTime = param0.getMaxTickLength();
    }

    @Override
    public void run() {
        while(this.server.isRunning()) {
            long var0 = this.server.getNextTickTime();
            long var1 = Util.getMillis();
            long var2 = var1 - var0;
            if (var2 > this.maxTickTime) {
                LOGGER.error(
                    LogUtils.FATAL_MARKER,
                    "A single server tick took {} seconds (should be max {})",
                    String.format(Locale.ROOT, "%.2f", (float)var2 / 1000.0F),
                    String.format(Locale.ROOT, "%.2f", 0.05F)
                );
                LOGGER.error(LogUtils.FATAL_MARKER, "Considering it to be crashed, server will forcibly shutdown.");
                ThreadMXBean var3 = ManagementFactory.getThreadMXBean();
                ThreadInfo[] var4 = var3.dumpAllThreads(true, true);
                StringBuilder var5 = new StringBuilder();
                Error var6 = new Error("Watchdog");

                for(ThreadInfo var7 : var4) {
                    if (var7.getThreadId() == this.server.getRunningThread().getId()) {
                        var6.setStackTrace(var7.getStackTrace());
                    }

                    var5.append(var7);
                    var5.append("\n");
                }

                CrashReport var8 = new CrashReport("Watching Server", var6);
                this.server.fillSystemReport(var8.getSystemReport());
                CrashReportCategory var9 = var8.addCategory("Thread Dump");
                var9.setDetail("Threads", var5);
                CrashReportCategory var10 = var8.addCategory("Performance stats");
                var10.setDetail("Random tick rate", () -> this.server.getWorldData().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).toString());
                var10.setDetail(
                    "Level stats",
                    () -> Streams.stream(this.server.getAllLevels())
                            .map(param0 -> param0.dimension() + ": " + param0.getWatchdogStats())
                            .collect(Collectors.joining(",\n"))
                );
                Bootstrap.realStdoutPrintln("Crash report:\n" + var8.getFriendlyReport());
                File var11 = new File(
                    new File(this.server.getServerDirectory(), "crash-reports"),
                    "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt"
                );
                if (var8.saveToFile(var11)) {
                    LOGGER.error("This crash report has been saved to: {}", var11.getAbsolutePath());
                } else {
                    LOGGER.error("We were unable to save this crash report to disk.");
                }

                this.exit();
            }

            try {
                Thread.sleep(var0 + this.maxTickTime - var1);
            } catch (InterruptedException var15) {
            }
        }

    }

    private void exit() {
        try {
            Timer var0 = new Timer();
            var0.schedule(new TimerTask() {
                @Override
                public void run() {
                    Runtime.getRuntime().halt(1);
                }
            }, 10000L);
            System.exit(1);
        } catch (Throwable var2) {
            Runtime.getRuntime().halt(1);
        }

    }
}
