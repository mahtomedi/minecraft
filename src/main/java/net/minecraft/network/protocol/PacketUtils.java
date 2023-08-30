package net.minecraft.network.protocol;

import com.mojang.logging.LogUtils;
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import org.slf4j.Logger;

public class PacketUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> param0, T param1, ServerLevel param2) throws RunningOnDifferentThreadException {
        ensureRunningOnSameThread(param0, param1, param2.getServer());
    }

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> param0, T param1, BlockableEventLoop<?> param2) throws RunningOnDifferentThreadException {
        if (!param2.isSameThread()) {
            param2.executeIfPossible(
                () -> {
                    if (param1.shouldHandleMessage(param0)) {
                        try {
                            param0.handle(param1);
                        } catch (Exception var4) {
                            if (var4 instanceof ReportedException var1x && var1x.getCause() instanceof OutOfMemoryError
                                || param1.shouldPropagateHandlingExceptions()) {
                                throw var4;
                            }
    
                            LOGGER.error("Failed to handle packet {}, suppressing error", param0, var4);
                        }
                    } else {
                        LOGGER.debug("Ignoring packet due to disconnection: {}", param0);
                    }
    
                }
            );
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }
}
