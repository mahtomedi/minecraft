package net.minecraft.server;

import java.io.OutputStream;

public class DebugLoggedPrintStream extends LoggedPrintStream {
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
