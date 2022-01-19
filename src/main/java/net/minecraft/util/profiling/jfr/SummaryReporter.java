package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.profiling.jfr.parse.JfrStatsParser;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class SummaryReporter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Runnable onDeregistration;

    protected SummaryReporter(Runnable param0) {
        this.onDeregistration = param0;
    }

    public void recordingStopped(@Nullable Path param0) {
        if (param0 != null) {
            this.onDeregistration.run();
            infoWithFallback(() -> "Dumped flight recorder profiling to " + param0);

            JfrStatsResult var0;
            try {
                var0 = JfrStatsParser.parse(param0);
            } catch (Throwable var5) {
                warnWithFallback(() -> "Failed to parse JFR recording", var5);
                return;
            }

            try {
                infoWithFallback(var0::asJson);
                Path var3 = param0.resolveSibling("jfr-report-" + StringUtils.substringBefore(param0.getFileName().toString(), ".jfr") + ".json");
                Files.writeString(var3, var0.asJson(), StandardOpenOption.CREATE);
                infoWithFallback(() -> "Dumped recording summary to " + var3);
            } catch (Throwable var41) {
                warnWithFallback(() -> "Failed to output JFR report", var41);
            }

        }
    }

    private static void infoWithFallback(Supplier<String> param0) {
        if (LogUtils.isLoggerActive()) {
            LOGGER.info(param0.get());
        } else {
            Bootstrap.realStdoutPrintln(param0.get());
        }

    }

    private static void warnWithFallback(Supplier<String> param0, Throwable param1) {
        if (LogUtils.isLoggerActive()) {
            LOGGER.warn(param0.get(), param1);
        } else {
            Bootstrap.realStdoutPrintln(param0.get());
            param1.printStackTrace(Bootstrap.STDOUT);
        }

    }
}
