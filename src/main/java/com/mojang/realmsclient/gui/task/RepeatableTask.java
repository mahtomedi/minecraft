package com.mojang.realmsclient.gui.task;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RepeatableTask implements Runnable {
    private final BooleanSupplier isActive;
    private final RestartDelayCalculator restartDelayCalculator;
    private final Duration interval;
    private final Runnable runnable;

    private RepeatableTask(Runnable param0, Duration param1, BooleanSupplier param2, RestartDelayCalculator param3) {
        this.runnable = param0;
        this.interval = param1;
        this.isActive = param2;
        this.restartDelayCalculator = param3;
    }

    @Override
    public void run() {
        if (this.isActive.getAsBoolean()) {
            this.restartDelayCalculator.markExecutionStart();
            this.runnable.run();
        }

    }

    public ScheduledFuture<?> schedule(ScheduledExecutorService param0) {
        return param0.scheduleAtFixedRate(this, this.restartDelayCalculator.getNextDelayMs(), this.interval.toMillis(), TimeUnit.MILLISECONDS);
    }

    public static RepeatableTask withRestartDelayAccountingForInterval(Runnable param0, Duration param1, BooleanSupplier param2) {
        return new RepeatableTask(param0, param1, param2, new IntervalBasedStartupDelay(param1));
    }

    public static RepeatableTask withImmediateRestart(Runnable param0, Duration param1, BooleanSupplier param2) {
        return new RepeatableTask(param0, param1, param2, new NoStartupDelay());
    }
}
