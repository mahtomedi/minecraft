package net.minecraft.network.chat;

import javax.annotation.Nullable;

public class ChatPreviewThrottler {
    private boolean sentRequestThisTick;
    @Nullable
    private Runnable pendingRequest;

    public void tick() {
        Runnable var0 = this.pendingRequest;
        if (var0 != null) {
            var0.run();
            this.pendingRequest = null;
        }

        this.sentRequestThisTick = false;
    }

    public void execute(Runnable param0) {
        if (this.sentRequestThisTick) {
            this.pendingRequest = param0;
        } else {
            param0.run();
            this.sentRequestThisTick = true;
            this.pendingRequest = null;
        }

    }
}
