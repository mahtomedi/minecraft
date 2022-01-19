package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.annotation.Nullable;
import org.slf4j.Logger;

public class LoggedPrintStream extends PrintStream {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final String name;

    public LoggedPrintStream(String param0, OutputStream param1) {
        super(param1);
        this.name = param0;
    }

    @Override
    public void println(@Nullable String param0) {
        this.logLine(param0);
    }

    @Override
    public void println(Object param0) {
        this.logLine(String.valueOf(param0));
    }

    protected void logLine(@Nullable String param0) {
        LOGGER.info("[{}]: {}", this.name, param0);
    }
}
