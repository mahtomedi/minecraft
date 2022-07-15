package net.minecraft.network.chat;

import java.util.Arrays;

public class LastSeenMessagesTracker {
    private final LastSeenMessages.Entry[] status;
    private int size;
    private LastSeenMessages result = LastSeenMessages.EMPTY;

    public LastSeenMessagesTracker(int param0) {
        this.status = new LastSeenMessages.Entry[param0];
    }

    public void push(LastSeenMessages.Entry param0) {
        LastSeenMessages.Entry var0 = param0;

        for(int var1 = 0; var1 < this.size; ++var1) {
            LastSeenMessages.Entry var2 = this.status[var1];
            this.status[var1] = var0;
            var0 = var2;
            if (var2.profileId().equals(param0.profileId())) {
                var0 = null;
                break;
            }
        }

        if (var0 != null && this.size < this.status.length) {
            this.status[this.size++] = var0;
        }

        this.result = new LastSeenMessages(Arrays.asList(Arrays.copyOf(this.status, this.size)));
    }

    public LastSeenMessages get() {
        return this.result;
    }
}
