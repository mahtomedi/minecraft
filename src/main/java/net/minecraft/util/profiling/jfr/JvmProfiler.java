package net.minecraft.util.profiling.jfr;

import java.net.SocketAddress;
import java.nio.file.Path;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface JvmProfiler {
    JvmProfiler INSTANCE = (JvmProfiler)(Runtime.class.getModule().getLayer().findModule("jdk.jfr").isPresent()
        ? new JfrProfiler()
        : new JvmProfiler.NoOpProfiler());

    void initialize();

    boolean start(Environment var1);

    Path stop();

    boolean isRunning();

    boolean isAvailable();

    void onServerTick(float var1);

    void onPacketReceived(Supplier<String> var1, SocketAddress var2, int var3);

    void onPacketSent(Supplier<String> var1, SocketAddress var2, int var3);

    @Nullable
    ProfiledDuration onWorldLoadedStarted();

    @Nullable
    ProfiledDuration onChunkGenerate(ChunkPos var1, ResourceKey<Level> var2, String var3);

    public static class NoOpProfiler implements JvmProfiler {
        static final Logger LOGGER = LogManager.getLogger();
        static final ProfiledDuration noOpCommit = () -> {
        };

        @Override
        public void initialize() {
        }

        @Override
        public boolean start(Environment param0) {
            LOGGER.warn("Attempted to start Flight Recorder, but it's not supported on this JVM");
            return false;
        }

        @Override
        public Path stop() {
            throw new IllegalStateException("Attempted to stop Flight Recorder, but it's not supported on this JVM");
        }

        @Override
        public boolean isRunning() {
            return false;
        }

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public void onPacketReceived(Supplier<String> param0, SocketAddress param1, int param2) {
        }

        @Override
        public void onPacketSent(Supplier<String> param0, SocketAddress param1, int param2) {
        }

        @Override
        public void onServerTick(float param0) {
        }

        @Override
        public ProfiledDuration onWorldLoadedStarted() {
            return noOpCommit;
        }

        @Nullable
        @Override
        public ProfiledDuration onChunkGenerate(ChunkPos param0, ResourceKey<Level> param1, String param2) {
            return null;
        }
    }
}
