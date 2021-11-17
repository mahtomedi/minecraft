package net.minecraft;

import java.lang.Thread.UncaughtExceptionHandler;
import org.apache.logging.log4j.Logger;

public class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {
    private final Logger logger;

    public DefaultUncaughtExceptionHandler(Logger param0) {
        this.logger = param0;
    }

    @Override
    public void uncaughtException(Thread param0, Throwable param1) {
        this.logger.error("Caught previously unhandled exception :", param1);
    }
}
