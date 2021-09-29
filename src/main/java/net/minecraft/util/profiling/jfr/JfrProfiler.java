package net.minecraft.util.profiling.jfr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import jdk.jfr.Configuration;
import jdk.jfr.Event;
import jdk.jfr.EventType;
import jdk.jfr.FlightRecorder;
import jdk.jfr.FlightRecorderListener;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.util.profiling.jfr.event.ChunkGenerationEvent;
import net.minecraft.util.profiling.jfr.event.PacketReceivedEvent;
import net.minecraft.util.profiling.jfr.event.PacketSentEvent;
import net.minecraft.util.profiling.jfr.event.ServerTickTimeEvent;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JfrProfiler implements JvmProfiler {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String ROOT_CATEGORY = "Minecraft";
    public static final String WORLD_GEN_CATEGORY = "World Generation";
    public static final String TICK_CATEGORY = "Ticking";
    public static final String NETWORK_CATEGORY = "Network";
    private static final List<Class<? extends Event>> CUSTOM_EVENTS = List.of(
        ChunkGenerationEvent.class, WorldLoadFinishedEvent.class, ServerTickTimeEvent.class, PacketReceivedEvent.class, PacketSentEvent.class
    );
    private static final String FLIGHT_RECORDER_CONFIG = "/flightrecorder-config.jfc";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd-HHmmss")
        .toFormatter()
        .withZone(ZoneId.systemDefault());
    @Nullable
    Recording recording;
    private long nextTickTimeReport;

    protected JfrProfiler() {
    }

    @Override
    public void initialize() {
        CUSTOM_EVENTS.forEach(FlightRecorder::register);
    }

    @Override
    public boolean start(Environment param0) {
        URL var0 = JfrProfiler.class.getResource("/flightrecorder-config.jfc");
        if (var0 == null) {
            LOGGER.warn("Could not find default flight recorder config at {}", "/flightrecorder-config.jfc");
            return false;
        } else {
            try {
                boolean var4;
                try (BufferedReader var1 = new BufferedReader(new InputStreamReader(var0.openStream()))) {
                    var4 = this.start(var1, param0);
                }

                return var4;
            } catch (IOException var8) {
                LOGGER.warn("Failed to start flight recorder using configuration at {}", var0, var8);
                return false;
            }
        }
    }

    @Override
    public Path stop() {
        if (this.recording == null) {
            throw new IllegalStateException("Not currently profiling");
        } else {
            Path var0 = this.recording.getDestination();
            this.recording.stop();
            return var0;
        }
    }

    @Override
    public boolean isRunning() {
        return this.recording != null;
    }

    @Override
    public boolean isAvailable() {
        return FlightRecorder.isAvailable();
    }

    @Override
    public void onServerTick(float param0) {
        if (EventType.getEventType(ServerTickTimeEvent.class).isEnabled()) {
            long var0 = Util.timeSource.getAsLong();
            if (this.nextTickTimeReport <= var0) {
                new ServerTickTimeEvent(param0).commit();
                this.nextTickTimeReport = var0 + TimeUnit.SECONDS.toNanos(1L);
            }
        }

    }

    private boolean start(Reader param0, Environment param1) {
        if (this.recording != null) {
            LOGGER.warn("Profiling already in progress");
            return false;
        } else {
            try {
                Configuration var0 = Configuration.create(param0);
                String var1 = DATE_TIME_FORMATTER.format(Instant.now());
                this.recording = Util.make(new Recording(var0), param2 -> {
                    CUSTOM_EVENTS.forEach(param2::enable);
                    param2.setDumpOnExit(true);
                    param2.setToDisk(true);
                    param2.setName("%s-%s-%s".formatted(param1.getDescription(), SharedConstants.getCurrentVersion().getName(), var1));
                });
                Path var2 = Paths.get("debug/%s-%s.jfr".formatted(param1.getDescription(), var1));
                if (!Files.exists(var2.getParent())) {
                    Files.createDirectories(var2.getParent());
                }

                this.recording.setDestination(var2);
                this.recording.start();
                this.setupSummaryListener();
            } catch (ParseException | IOException var6) {
                LOGGER.warn("Failed to start jfr profiling", (Throwable)var6);
                return false;
            }

            LOGGER.info(
                "Started flight recorder profiling id({}):name({}) - will dump to {} on exit or stop command",
                this.recording.getId(),
                this.recording.getName(),
                this.recording.getDestination()
            );
            return true;
        }
    }

    private void setupSummaryListener() {
        FlightRecorder.addListener(new FlightRecorderListener() {
            final SummaryReporter summaryReporter = new SummaryReporter(() -> JfrProfiler.this.recording = null);

            @Override
            public void recordingStateChanged(Recording param0) {
                if (param0 == JfrProfiler.this.recording && param0.getState() == RecordingState.STOPPED) {
                    this.summaryReporter.recordingStopped(param0.getDestination());
                    FlightRecorder.removeListener(this);
                }
            }
        });
    }

    @Override
    public void onPacketReceived(Supplier<String> param0, SocketAddress param1, int param2) {
        if (EventType.getEventType(PacketReceivedEvent.class).isEnabled()) {
            new PacketReceivedEvent(param0.get(), param1, param2).commit();
        }

    }

    @Override
    public void onPacketSent(Supplier<String> param0, SocketAddress param1, int param2) {
        if (EventType.getEventType(PacketSentEvent.class).isEnabled()) {
            new PacketSentEvent(param0.get(), param1, param2).commit();
        }

    }

    @Nullable
    @Override
    public ProfiledDuration onWorldLoadedStarted() {
        if (!EventType.getEventType(WorldLoadFinishedEvent.class).isEnabled()) {
            return null;
        } else {
            WorldLoadFinishedEvent var0 = new WorldLoadFinishedEvent();
            var0.begin();
            return var0::commit;
        }
    }

    @Nullable
    @Override
    public ProfiledDuration onChunkGenerate(ChunkPos param0, ResourceKey<Level> param1, String param2) {
        if (!EventType.getEventType(ChunkGenerationEvent.class).isEnabled()) {
            return null;
        } else {
            ChunkGenerationEvent var0 = new ChunkGenerationEvent(param0, param1, param2);
            var0.begin();
            return var0::commit;
        }
    }
}
