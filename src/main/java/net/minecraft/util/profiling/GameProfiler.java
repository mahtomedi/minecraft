package net.minecraft.util.profiling;

import java.time.Duration;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GameProfiler implements ProfilerFiller {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final long MAXIMUM_TICK_TIME_NANOS = Duration.ofMillis(300L).toNanos();
    private final IntSupplier getTickTime;
    private final GameProfiler.ProfilerImpl continuous = new GameProfiler.ProfilerImpl();
    private final GameProfiler.ProfilerImpl perTick = new GameProfiler.ProfilerImpl();

    public GameProfiler(IntSupplier param0) {
        this.getTickTime = param0;
    }

    public GameProfiler.Profiler continuous() {
        return this.continuous;
    }

    @Override
    public void startTick() {
        this.continuous.collector.startTick();
        this.perTick.collector.startTick();
    }

    @Override
    public void endTick() {
        this.continuous.collector.endTick();
        this.perTick.collector.endTick();
    }

    @Override
    public void push(String param0) {
        this.continuous.collector.push(param0);
        this.perTick.collector.push(param0);
    }

    @Override
    public void push(Supplier<String> param0) {
        this.continuous.collector.push(param0);
        this.perTick.collector.push(param0);
    }

    @Override
    public void pop() {
        this.continuous.collector.pop();
        this.perTick.collector.pop();
    }

    @Override
    public void popPush(String param0) {
        this.continuous.collector.popPush(param0);
        this.perTick.collector.popPush(param0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void popPush(Supplier<String> param0) {
        this.continuous.collector.popPush(param0);
        this.perTick.collector.popPush(param0);
    }

    public interface Profiler {
        boolean isEnabled();

        ProfileResults disable();

        @OnlyIn(Dist.CLIENT)
        ProfileResults getResults();

        void enable();
    }

    class ProfilerImpl implements GameProfiler.Profiler {
        protected ProfileCollector collector = InactiveProfiler.INACTIVE;

        private ProfilerImpl() {
        }

        @Override
        public boolean isEnabled() {
            return this.collector != InactiveProfiler.INACTIVE;
        }

        @Override
        public ProfileResults disable() {
            ProfileResults var0 = this.collector.getResults();
            this.collector = InactiveProfiler.INACTIVE;
            return var0;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public ProfileResults getResults() {
            return this.collector.getResults();
        }

        @Override
        public void enable() {
            if (this.collector == InactiveProfiler.INACTIVE) {
                this.collector = new ActiveProfiler(Util.getNanos(), GameProfiler.this.getTickTime);
            }

        }
    }
}
