package net.minecraft.client.telemetry;

import com.mojang.logging.LogUtils;
import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.Executor;
import net.minecraft.util.eventlog.JsonEventLog;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TelemetryEventLog implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final JsonEventLog<TelemetryEventInstance> log;
    private final ProcessorMailbox<Runnable> mailbox;

    public TelemetryEventLog(FileChannel param0, Executor param1) {
        this.log = new JsonEventLog<>(TelemetryEventInstance.CODEC, param0);
        this.mailbox = ProcessorMailbox.create(param1, "telemetry-event-log");
    }

    public TelemetryEventLogger logger() {
        return param0 -> this.mailbox.tell(() -> {
                try {
                    this.log.write(param0);
                } catch (IOException var3) {
                    LOGGER.error("Failed to write telemetry event to log", (Throwable)var3);
                }

            });
    }

    @Override
    public void close() {
        this.mailbox.tell(() -> IOUtils.closeQuietly((Closeable)this.log));
        this.mailbox.close();
    }
}
