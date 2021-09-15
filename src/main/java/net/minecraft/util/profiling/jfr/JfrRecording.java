package net.minecraft.util.profiling.jfr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import javax.annotation.Nullable;
import jdk.jfr.Configuration;
import jdk.jfr.Event;
import jdk.jfr.FlightRecorder;
import jdk.jfr.FlightRecorderListener;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.profiling.jfr.event.network.PacketReceivedEvent;
import net.minecraft.util.profiling.jfr.event.network.PacketSentEvent;
import net.minecraft.util.profiling.jfr.event.ticking.ServerTickTimeEvent;
import net.minecraft.util.profiling.jfr.event.worldgen.ChunkGenerationEvent;
import net.minecraft.util.profiling.jfr.event.worldgen.WorldLoadFinishedEvent;
import net.minecraft.util.profiling.jfr.parse.JfrStatsParser;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.util.Supplier;

public class JfrRecording {
    static final Logger LOGGER = LogManager.getLogger();
    public static final String ROOT_CATEGORY = "Minecraft";
    public static final String WORLD_GEN_CATEGORY = "World Generation";
    public static final String TICK_CATEGORY = "Ticking";
    public static final String NETWORK_CATEGORY = "Network";
    public static final List<Class<? extends Event>> CUSTOM_EVENTS = List.of(
        ChunkGenerationEvent.class, WorldLoadFinishedEvent.class, ServerTickTimeEvent.class, PacketReceivedEvent.class, PacketSentEvent.class
    );
    private static final String FLIGHT_RECORDER_CONFIG = "/flightrecorder-config.jfc";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd-HHmm")
        .toFormatter()
        .withZone(ZoneId.systemDefault());
    @Nullable
    private static Recording recording;

    private JfrRecording() {
    }

    private static boolean start(Reader param0, JfrRecording.Environment param1) {
        if (!FlightRecorder.isAvailable()) {
            LOGGER.warn("Flight Recorder not available!");
            return false;
        } else if (recording != null) {
            LOGGER.warn("Profiling already in progress");
            return false;
        } else {
            try {
                Configuration var0 = Configuration.create(param0);
                String var1 = DATE_TIME_FORMATTER.format(Instant.now());
                recording = Util.make(new Recording(var0), param2 -> {
                    CUSTOM_EVENTS.forEach(param2::enable);
                    param2.setDumpOnExit(true);
                    param2.setToDisk(true);
                    param2.setName("%s-%s-%s".formatted(param1.getDescription(), SharedConstants.getCurrentVersion().getName(), var1));
                });
                Path var2 = Paths.get("debug/%s-%s.jfr".formatted(param1.getDescription(), var1));
                if (!Files.exists(var2.getParent())) {
                    Files.createDirectories(var2.getParent());
                }

                recording.setDestination(var2);
                recording.start();
                FlightRecorder.addListener(new JfrRecording.SummaryReporter(recording, () -> recording = null));
            } catch (ParseException | IOException var5) {
                LOGGER.warn("Failed to start jfr profiling", (Throwable)var5);
                return false;
            }

            LOGGER.info(
                "Started flight recorder profiling id({}):name({}) - will dump to {} on exit or stop command",
                recording.getId(),
                recording.getName(),
                recording.getDestination()
            );
            return true;
        }
    }

    public static boolean start(JfrRecording.Environment param0) {
        URL var0 = JfrRecording.class.getResource("/flightrecorder-config.jfc");
        if (var0 == null) {
            LOGGER.warn("Could not find default flight recorder config at {}", "/flightrecorder-config.jfc");
            return false;
        } else {
            try {
                boolean var3;
                try (BufferedReader var1 = new BufferedReader(new InputStreamReader(var0.openStream()))) {
                    var3 = start(var1, param0);
                }

                return var3;
            } catch (IOException var7) {
                LOGGER.warn("Failed to start flight recorder using configuration at {}", var0, var7);
                return false;
            }
        }
    }

    public static Path stop() {
        if (recording == null) {
            throw new IllegalStateException("Not currently profiling");
        } else {
            Path var0 = recording.getDestination();
            recording.stop();
            return var0;
        }
    }

    public static boolean isRunning() {
        return recording != null;
    }

    public static enum Environment {
        CLIENT("client"),
        SERVER("server");

        private final String description;

        private Environment(String param0) {
            this.description = param0;
        }

        public static JfrRecording.Environment from(MinecraftServer param0) {
            return param0.isDedicatedServer() ? SERVER : CLIENT;
        }

        String getDescription() {
            return this.description;
        }
    }

    static class SummaryReporter implements FlightRecorderListener {
        private final Recording recording;
        private final Runnable onDeregistration;

        SummaryReporter(Recording param0, Runnable param1) {
            this.recording = param0;
            this.onDeregistration = param1;
        }

        @Override
        public void recordingStateChanged(Recording param0) {
            if (param0 == this.recording && this.recording.getState() == RecordingState.STOPPED && param0.getDestination() != null) {
                FlightRecorder.removeListener(this);
                this.onDeregistration.run();
                Path var0 = param0.getDestination();
                infoWithFallback(() -> "Dumped flight recorder profiling to " + var0);

                JfrStatsResult var1;
                try {
                    var1 = JfrStatsParser.parse(var0);
                } catch (Throwable var6) {
                    warnWithFallback(() -> "Failed to parse JFR recording", var6);
                    return;
                }

                try {
                    infoWithFallback(var1::asJson);
                    Path var4 = var0.resolveSibling("jfr-report-" + StringUtils.substringBefore(var0.getFileName().toString(), ".jfr") + ".json");
                    Files.writeString(var4, var1.asJson(), StandardOpenOption.CREATE);
                    infoWithFallback(() -> "Dumped recording summary to " + var4);
                } catch (Throwable var51) {
                    warnWithFallback(() -> "Failed to output JFR report", var51);
                }

            }
        }

        private static void infoWithFallback(Supplier<String> param0) {
            if (log4jIsActive()) {
                JfrRecording.LOGGER.info(param0);
            } else {
                Bootstrap.realStdoutPrintln(param0.get());
            }

        }

        private static void warnWithFallback(Supplier<String> param0, Throwable param1) {
            if (log4jIsActive()) {
                JfrRecording.LOGGER.warn(param0, param1);
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
}
