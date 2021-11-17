package com.mojang.realmsclient.exception;

import java.lang.Thread.UncaughtExceptionHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsDefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {
    private final Logger logger;

    public RealmsDefaultUncaughtExceptionHandler(Logger param0) {
        this.logger = param0;
    }

    @Override
    public void uncaughtException(Thread param0, Throwable param1) {
        this.logger.error("Caught previously unhandled exception :");
        this.logger.error(param1);
    }
}
