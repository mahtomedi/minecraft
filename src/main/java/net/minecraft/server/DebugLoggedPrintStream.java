package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.io.OutputStream;
import org.slf4j.Logger;

public class DebugLoggedPrintStream extends LoggedPrintStream {
    private static final Logger LOGGER = LogUtils.getLogger();

    public DebugLoggedPrintStream(String param0, OutputStream param1) {
        super(param0, param1);
    }

    @Override
    protected void logLine(String param0) {
        StackTraceElement[] var0 = Thread.currentThread().getStackTrace();
        StackTraceElement var1 = var0[Math.min(3, var0.length)];
        LOGGER.info("[{}]@.({}:{}): {}", this.name, var1.getFileName(), var1.getLineNumber(), param0);
    }
}
