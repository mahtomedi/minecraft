package net.minecraft.util.profiling;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import org.slf4j.Logger;

public class SingleTickProfiler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final LongSupplier realTime;
    private final long saveThreshold;
    private int tick;
    private final File location;
    private ProfileCollector profiler = InactiveProfiler.INSTANCE;

    public SingleTickProfiler(LongSupplier param0, String param1, long param2) {
        this.realTime = param0;
        this.location = new File("debug", param1);
        this.saveThreshold = param2;
    }

    public ProfilerFiller startTick() {
        this.profiler = new ActiveProfiler(this.realTime, () -> this.tick, false);
        ++this.tick;
        return this.profiler;
    }

    public void endTick() {
        if (this.profiler != InactiveProfiler.INSTANCE) {
            ProfileResults var0 = this.profiler.getResults();
            this.profiler = InactiveProfiler.INSTANCE;
            if (var0.getNanoDuration() >= this.saveThreshold) {
                File var1 = new File(this.location, "tick-results-" + Util.getFilenameFormattedDateTime() + ".txt");
                var0.saveResults(var1.toPath());
                LOGGER.info("Recorded long tick -- wrote info to: {}", var1.getAbsolutePath());
            }

        }
    }

    @Nullable
    public static SingleTickProfiler createTickProfiler(String param0) {
        return null;
    }

    public static ProfilerFiller decorateFiller(ProfilerFiller param0, @Nullable SingleTickProfiler param1) {
        return param1 != null ? ProfilerFiller.tee(param1.startTick(), param0) : param0;
    }
}
