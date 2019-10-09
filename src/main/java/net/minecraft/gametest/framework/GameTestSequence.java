package net.minecraft.gametest.framework;

import java.util.Iterator;
import java.util.List;

public class GameTestSequence {
    private final GameTestInfo parent;
    private final List<GameTestEvent> events;
    private long lastTick;

    public void tickAndContinue(long param0) {
        try {
            this.tick(param0);
        } catch (Exception var4) {
        }

    }

    public void tickAndFailIfNotComplete(long param0) {
        try {
            this.tick(param0);
        } catch (Exception var4) {
            this.parent.fail(var4);
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
}
