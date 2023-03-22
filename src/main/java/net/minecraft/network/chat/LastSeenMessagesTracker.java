package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.BitSet;
import java.util.Objects;
import javax.annotation.Nullable;

public class LastSeenMessagesTracker {
    private final LastSeenTrackedEntry[] trackedMessages;
    private int tail;
    private int offset;
    @Nullable
    private MessageSignature lastTrackedMessage;

    public LastSeenMessagesTracker(int param0) {
        this.trackedMessages = new LastSeenTrackedEntry[param0];
    }

    public boolean addPending(MessageSignature param0, boolean param1) {
        if (Objects.equals(param0, this.lastTrackedMessage)) {
            return false;
        } else {
            this.lastTrackedMessage = param0;
            this.addEntry(param1 ? new LastSeenTrackedEntry(param0, true) : null);
            return true;
        }
    }

    private void addEntry(@Nullable LastSeenTrackedEntry param0) {
        int var0 = this.tail;
        this.tail = (var0 + 1) % this.trackedMessages.length;
        ++this.offset;
        this.trackedMessages[var0] = param0;
    }

    public void ignorePending(MessageSignature param0) {
        for(int var0 = 0; var0 < this.trackedMessages.length; ++var0) {
            LastSeenTrackedEntry var1 = this.trackedMessages[var0];
            if (var1 != null && var1.pending() && param0.equals(var1.signature())) {
                this.trackedMessages[var0] = null;
                break;
            }
        }

    }

    public int getAndClearOffset() {
        int var0 = this.offset;
        this.offset = 0;
        return var0;
    }

    public LastSeenMessagesTracker.Update generateAndApplyUpdate() {
        int var0 = this.getAndClearOffset();
        BitSet var1 = new BitSet(this.trackedMessages.length);
        ObjectList<MessageSignature> var2 = new ObjectArrayList<>(this.trackedMessages.length);

        for(int var3 = 0; var3 < this.trackedMessages.length; ++var3) {
            int var4 = (this.tail + var3) % this.trackedMessages.length;
            LastSeenTrackedEntry var5 = this.trackedMessages[var4];
            if (var5 != null) {
                var1.set(var3, true);
                var2.add(var5.signature());
                this.trackedMessages[var4] = var5.acknowledge();
            }
        }

        LastSeenMessages var6 = new LastSeenMessages(var2);
        LastSeenMessages.Update var7 = new LastSeenMessages.Update(var0, var1);
        return new LastSeenMessagesTracker.Update(var6, var7);
    }

    public int offset() {
        return this.offset;
    }

    public static record Update(LastSeenMessages lastSeen, LastSeenMessages.Update update) {
    }
}
