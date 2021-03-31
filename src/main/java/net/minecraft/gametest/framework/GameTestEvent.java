package net.minecraft.gametest.framework;

import javax.annotation.Nullable;

class GameTestEvent {
    @Nullable
    public final Long expectedDelay;
    public final Runnable assertion;

    private GameTestEvent(@Nullable Long param0, Runnable param1) {
        this.expectedDelay = param0;
        this.assertion = param1;
    }

    static GameTestEvent create(Runnable param0) {
        return new GameTestEvent(null, param0);
    }

    static GameTestEvent create(long param0, Runnable param1) {
        return new GameTestEvent(param0, param1);
    }
}
