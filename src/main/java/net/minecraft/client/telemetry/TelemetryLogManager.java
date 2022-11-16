package net.minecraft.client.telemetry;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.eventlog.EventLogDirectory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TelemetryLogManager implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String RAW_EXTENSION = ".json";
    private static final int EXPIRY_DAYS = 7;
    private final EventLogDirectory directory;
    @Nullable
    private CompletableFuture<Optional<TelemetryEventLog>> sessionLog;

    private TelemetryLogManager(EventLogDirectory param0) {
        this.directory = param0;
    }

    public static CompletableFuture<Optional<TelemetryLogManager>> open(Path param0) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                EventLogDirectory var0 = EventLogDirectory.open(param0, ".json");
                var0.listFiles().prune(LocalDate.now(), 7).compressAll();
                return Optional.of(new TelemetryLogManager(var0));
            } catch (Exception var2) {
                LOGGER.error("Failed to create telemetry log manager", (Throwable)var2);
                return Optional.empty();
            }
        }, Util.backgroundExecutor());
    }

    public CompletableFuture<Optional<TelemetryEventLogger>> openLogger() {
        if (this.sessionLog == null) {
            this.sessionLog = CompletableFuture.supplyAsync(() -> {
                try {
                    EventLogDirectory.RawFile var0 = this.directory.createNewFile(LocalDate.now());
                    FileChannel var1 = var0.openChannel();
                    return Optional.of(new TelemetryEventLog(var1, Util.backgroundExecutor()));
                } catch (IOException var3) {
                    LOGGER.error("Failed to open channel for telemetry event log", (Throwable)var3);
                    return Optional.empty();
                }
            }, Util.backgroundExecutor());
        }

        return this.sessionLog.thenApply(param0 -> param0.map(TelemetryEventLog::logger));
    }

    @Override
    public void close() {
        if (this.sessionLog != null) {
            this.sessionLog.thenAccept(param0 -> param0.ifPresent(TelemetryEventLog::close));
        }

    }
}
