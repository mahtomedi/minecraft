package net.minecraft.client.telemetry.events;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class GameLoadTimesEvent {
    public static final GameLoadTimesEvent INSTANCE = new GameLoadTimesEvent(Ticker.systemTicker());
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Ticker timeSource;
    private final Map<TelemetryProperty<GameLoadTimesEvent.Measurement>, Stopwatch> measurements = new HashMap<>();
    private OptionalLong bootstrapTime = OptionalLong.empty();

    protected GameLoadTimesEvent(Ticker param0) {
        this.timeSource = param0;
    }

    public synchronized void beginStep(TelemetryProperty<GameLoadTimesEvent.Measurement> param0) {
        this.beginStep(param0, param0x -> Stopwatch.createStarted(this.timeSource));
    }

    public synchronized void beginStep(TelemetryProperty<GameLoadTimesEvent.Measurement> param0, Stopwatch param1) {
        this.beginStep(param0, param1x -> param1);
    }

    private synchronized void beginStep(
        TelemetryProperty<GameLoadTimesEvent.Measurement> param0, Function<TelemetryProperty<GameLoadTimesEvent.Measurement>, Stopwatch> param1
    ) {
        this.measurements.computeIfAbsent(param0, param1);
    }

    public synchronized void endStep(TelemetryProperty<GameLoadTimesEvent.Measurement> param0) {
        Stopwatch var0 = this.measurements.get(param0);
        if (var0 == null) {
            LOGGER.warn("Attempted to end step for {} before starting it", param0.id());
        } else {
            if (var0.isRunning()) {
                var0.stop();
            }

        }
    }

    public void send(TelemetryEventSender param0) {
        param0.send(
            TelemetryEventType.GAME_LOAD_TIMES,
            param0x -> {
                synchronized(this) {
                    this.measurements
                        .forEach(
                            (param1, param2) -> {
                                if (!param2.isRunning()) {
                                    long var0 = param2.elapsed(TimeUnit.MILLISECONDS);
                                    param0x.put(param1, new GameLoadTimesEvent.Measurement((int)var0));
                                } else {
                                    LOGGER.warn(
                                        "Measurement {} was discarded since it was still ongoing when the event {} was sent.",
                                        param1.id(),
                                        TelemetryEventType.GAME_LOAD_TIMES.id()
                                    );
                                }
            
                            }
                        );
                    this.bootstrapTime
                        .ifPresent(param1 -> param0x.put(TelemetryProperty.LOAD_TIME_BOOTSTRAP_MS, new GameLoadTimesEvent.Measurement((int)param1)));
                    this.measurements.clear();
                }
            }
        );
    }

    public synchronized void setBootstrapTime(long param0) {
        this.bootstrapTime = OptionalLong.of(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Measurement(int millis) {
        public static final Codec<GameLoadTimesEvent.Measurement> CODEC = Codec.INT.xmap(GameLoadTimesEvent.Measurement::new, param0 -> param0.millis);
    }
}
