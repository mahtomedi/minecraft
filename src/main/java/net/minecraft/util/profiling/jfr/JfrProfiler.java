package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import jdk.jfr.Configuration;
import jdk.jfr.Event;
import jdk.jfr.FlightRecorder;
import jdk.jfr.FlightRecorderListener;
import jdk.jfr.Recording;
import jdk.jfr.RecordingState;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.util.profiling.jfr.event.ChunkGenerationEvent;
import net.minecraft.util.profiling.jfr.event.NetworkSummaryEvent;
import net.minecraft.util.profiling.jfr.event.PacketReceivedEvent;
import net.minecraft.util.profiling.jfr.event.PacketSentEvent;
import net.minecraft.util.profiling.jfr.event.ServerTickTimeEvent;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class JfrProfiler implements JvmProfiler {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String ROOT_CATEGORY = "Minecraft";
    public static final String WORLD_GEN_CATEGORY = "World Generation";
    public static final String TICK_CATEGORY = "Ticking";
    public static final String NETWORK_CATEGORY = "Network";
    private static final List<Class<? extends Event>> CUSTOM_EVENTS = List.of(
        ChunkGenerationEvent.class,
        PacketReceivedEvent.class,
        PacketSentEvent.class,
        NetworkSummaryEvent.class,
        ServerTickTimeEvent.class,
        WorldLoadFinishedEvent.class
    );
    private static final String FLIGHT_RECORDER_CONFIG = "/flightrecorder-config.jfc";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd-HHmmss")
        .toFormatter()
        .withZone(ZoneId.systemDefault());
    private static final JfrProfiler INSTANCE = new JfrProfiler();
    @Nullable
    Recording recording;
    private float currentAverageTickTime;
    private final Map<String, NetworkSummaryEvent.SumAggregation> networkTrafficByAddress = new ConcurrentHashMap<>();

    private JfrProfiler() {
        CUSTOM_EVENTS.forEach(FlightRecorder::register);
        FlightRecorder.addPeriodicEvent(ServerTickTimeEvent.class, () -> new ServerTickTimeEvent(this.currentAverageTickTime).commit());
        FlightRecorder.addPeriodicEvent(NetworkSummaryEvent.class, () -> {
            Iterator<NetworkSummaryEvent.SumAggregation> var0 = this.networkTrafficByAddress.values().iterator();

            while(var0.hasNext()) {
                var0.next().commitEvent();
                var0.remove();
            }

        });
    }

    public static JfrProfiler getInstance() {
        return INSTANCE;
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
            this.networkTrafficByAddress.clear();
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

    private boolean start(Reader param0, Environment param1) {
        if (this.isRunning()) {
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
    public void onServerTick(float param0) {
        if (ServerTickTimeEvent.TYPE.isEnabled()) {
            this.currentAverageTickTime = param0;
        }

    }

    @Override
    public void onPacketReceived(int param0, int param1, SocketAddress param2, int param3) {
        if (PacketReceivedEvent.TYPE.isEnabled()) {
            new PacketReceivedEvent(param0, param1, param2, param3).commit();
        }

        if (NetworkSummaryEvent.TYPE.isEnabled()) {
            this.networkStatFor(param2).trackReceivedPacket(param3);
        }

    }

    @Override
    public void onPacketSent(int param0, int param1, SocketAddress param2, int param3) {
        if (PacketSentEvent.TYPE.isEnabled()) {
            new PacketSentEvent(param0, param1, param2, param3).commit();
        }

        if (NetworkSummaryEvent.TYPE.isEnabled()) {
            this.networkStatFor(param2).trackSentPacket(param3);
        }

    }

    private NetworkSummaryEvent.SumAggregation networkStatFor(SocketAddress param0) {
        return this.networkTrafficByAddress.computeIfAbsent(param0.toString(), NetworkSummaryEvent.SumAggregation::new);
    }

    @Nullable
    @Override
    public ProfiledDuration onWorldLoadedStarted() {
        if (!WorldLoadFinishedEvent.TYPE.isEnabled()) {
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
        if (!ChunkGenerationEvent.TYPE.isEnabled()) {
            return null;
        } else {
            ChunkGenerationEvent var0 = new ChunkGenerationEvent(param0, param1, param2);
            var0.begin();
            return var0::commit;
        }
    }
}
