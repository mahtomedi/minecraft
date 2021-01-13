package net.minecraft.network.protocol;

import net.minecraft.network.PacketListener;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.thread.BlockableEventLoop;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> param0, T param1, ServerLevel param2) throws RunningOnDifferentThreadException {
        ensureRunningOnSameThread(param0, param1, param2.getServer());
    }

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> param0, T param1, BlockableEventLoop<?> param2) throws RunningOnDifferentThreadException {
        if (!param2.isSameThread()) {
            param2.execute(() -> {
                if (param1.getConnection().isConnected()) {
                    param0.handle(param1);
                } else {
                    LOGGER.debug("Ignoring packet due to disconnection: " + param0);
                }

            });
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }
}
