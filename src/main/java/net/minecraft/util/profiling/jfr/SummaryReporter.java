package net.minecraft.util.profiling.jfr;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import javax.annotation.Nullable;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.profiling.jfr.parse.JfrStatsParser;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.util.Supplier;

public class SummaryReporter {
    private static final Logger LOGGER = LogManager.getLogger();
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
        if (log4jIsActive()) {
            LOGGER.info(param0);
        } else {
            Bootstrap.realStdoutPrintln(param0.get());
        }

    }

    private static void warnWithFallback(Supplier<String> param0, Throwable param1) {
        if (log4jIsActive()) {
            LOGGER.warn(param0, param1);
        } else {
            Bootstrap.realStdoutPrintln(param0.get());
            param1.printStackTrace(Bootstrap.STDOUT);
        }

    }

    private static boolean log4jIsActive() {
        LoggerContext var0 = LogManager.getContext();
        if (var0 instanceof LifeCycle var1) {
            return !var1.isStopped();
        } else {
            return true;
        }
    }
}
