package com.mojang.realmsclient.gui.task;

import com.google.common.annotations.VisibleForTesting;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IntervalBasedStartupDelay implements RestartDelayCalculator {
    private final Duration interval;
    private final Supplier<Clock> clock;
    @Nullable
    private Instant lastStartedTimestamp;

    public IntervalBasedStartupDelay(Duration param0) {
        this.interval = param0;
        this.clock = Clock::systemUTC;
    }

    @VisibleForTesting
    protected IntervalBasedStartupDelay(Duration param0, Supplier<Clock> param1) {
        this.interval = param0;
        this.clock = param1;
    }

    @Override
    public void markExecutionStart() {
        this.lastStartedTimestamp = Instant.now(this.clock.get());
    }

    @Override
    public long getNextDelayMs() {
        return this.lastStartedTimestamp == null
            ? 0L
            : Math.max(0L, Duration.between(Instant.now(this.clock.get()), this.lastStartedTimestamp.plus(this.interval)).toMillis());
    }
}
