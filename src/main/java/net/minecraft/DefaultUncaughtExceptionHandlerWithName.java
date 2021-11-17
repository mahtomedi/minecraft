package net.minecraft;

import java.lang.Thread.UncaughtExceptionHandler;
import org.apache.logging.log4j.Logger;

public class DefaultUncaughtExceptionHandlerWithName implements UncaughtExceptionHandler {
    private final Logger logger;

    public DefaultUncaughtExceptionHandlerWithName(Logger param0) {
        this.logger = param0;
    }

    @Override
    public void uncaughtException(Thread param0, Throwable param1) {
        this.logger.error("Caught previously unhandled exception :");
        this.logger.error(param0.getName(), param1);
    }
}
