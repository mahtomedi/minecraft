package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class GameTestSequence {
    final GameTestInfo parent;
    private final List<GameTestEvent> events = Lists.newArrayList();
    private long lastTick;

    GameTestSequence(GameTestInfo param0) {
        this.parent = param0;
        this.lastTick = param0.getTick();
    }

    public GameTestSequence thenWaitUntil(Runnable param0) {
        this.events.add(GameTestEvent.create(param0));
        return this;
    }

    public GameTestSequence thenWaitUntil(long param0, Runnable param1) {
        this.events.add(GameTestEvent.create(param0, param1));
        return this;
    }

    public GameTestSequence thenIdle(int param0) {
        return this.thenExecuteAfter(param0, () -> {
        });
    }

    public GameTestSequence thenExecute(Runnable param0) {
        this.events.add(GameTestEvent.create(() -> this.executeWithoutFail(param0)));
        return this;
    }

    public GameTestSequence thenExecuteAfter(int param0, Runnable param1) {
        this.events.add(GameTestEvent.create(() -> {
            if (this.parent.getTick() < this.lastTick + (long)param0) {
                throw new GameTestAssertException("Waiting");
            } else {
                this.executeWithoutFail(param1);
            }
        }));
        return this;
    }

    public GameTestSequence thenExecuteFor(int param0, Runnable param1) {
        this.events.add(GameTestEvent.create(() -> {
            if (this.parent.getTick() < this.lastTick + (long)param0) {
                this.executeWithoutFail(param1);
                throw new GameTestAssertException("Waiting");
            }
        }));
        return this;
    }

    public void thenSucceed() {
        this.events.add(GameTestEvent.create(this.parent::succeed));
    }

    public void thenFail(Supplier<Exception> param0) {
        this.events.add(GameTestEvent.create(() -> this.parent.fail(param0.get())));
    }

    public GameTestSequence.Condition thenTrigger() {
        GameTestSequence.Condition var0 = new GameTestSequence.Condition();
        this.events.add(GameTestEvent.create(() -> var0.trigger(this.parent.getTick())));
        return var0;
    }

    public void tickAndContinue(long param0) {
        try {
            this.tick(param0);
        } catch (GameTestAssertException var4) {
        }

    }

    public void tickAndFailIfNotComplete(long param0) {
        try {
            this.tick(param0);
        } catch (GameTestAssertException var4) {
            this.parent.fail(var4);
        }

    }

    private void executeWithoutFail(Runnable param0) {
        try {
            param0.run();
        } catch (GameTestAssertException var3) {
            this.parent.fail(var3);
        }

    }

    private void tick(long param0) {
        Iterator<GameTestEvent> var0 = this.events.iterator();

        while(var0.hasNext()) {
            GameTestEvent var1 = var0.next();
            var1.assertion.run();
            var0.remove();
            long var2 = param0 - this.lastTick;
            long var3 = this.lastTick;
            this.lastTick = param0;
            if (var1.expectedDelay != null && var1.expectedDelay != var2) {
                this.parent
                    .fail(new GameTestAssertException("Succeeded in invalid tick: expected " + (var3 + var1.expectedDelay) + ", but current tick is " + param0));
                break;
            }
        }

    }

    public class Condition {
        private static final long NOT_TRIGGERED = -1L;
        private long triggerTime = -1L;

        void trigger(long param0) {
            if (this.triggerTime != -1L) {
                throw new IllegalStateException("Condition already triggered at " + this.triggerTime);
            } else {
                this.triggerTime = param0;
            }
        }

        public void assertTriggeredThisTick() {
            long var0 = GameTestSequence.this.parent.getTick();
            if (this.triggerTime != var0) {
                if (this.triggerTime == -1L) {
                    throw new GameTestAssertException("Condition not triggered (t=" + var0 + ")");
                } else {
                    throw new GameTestAssertException("Condition triggered at " + this.triggerTime + ", (t=" + var0 + ")");
                }
            }
        }
    }
}
