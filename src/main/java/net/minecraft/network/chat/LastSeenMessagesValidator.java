package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Optional;
import javax.annotation.Nullable;

public class LastSeenMessagesValidator {
    private final int lastSeenCount;
    private final ObjectList<LastSeenTrackedEntry> trackedMessages = new ObjectArrayList();
    @Nullable
    private MessageSignature lastPendingMessage;

    public LastSeenMessagesValidator(int param0) {
        this.lastSeenCount = param0;

        for(int var0 = 0; var0 < param0; ++var0) {
            this.trackedMessages.add(null);
        }

    }

    public void addPending(MessageSignature param0) {
        if (!param0.equals(this.lastPendingMessage)) {
            this.trackedMessages.add(new LastSeenTrackedEntry(param0, true));
            this.lastPendingMessage = param0;
        }

    }

    public int trackedMessagesCount() {
        return this.trackedMessages.size();
    }

    public boolean applyOffset(int param0) {
        int var0 = this.trackedMessages.size() - this.lastSeenCount;
        if (param0 >= 0 && param0 <= var0) {
            this.trackedMessages.removeElements(0, param0);
            return true;
        } else {
            return false;
        }
    }

    public Optional<LastSeenMessages> applyUpdate(LastSeenMessages.Update param0) {
        if (!this.applyOffset(param0.offset())) {
            return Optional.empty();
        } else {
            ObjectList<MessageSignature> var0 = new ObjectArrayList(param0.acknowledged().cardinality());
            if (param0.acknowledged().length() > this.lastSeenCount) {
                return Optional.empty();
            } else {
                for(int var1 = 0; var1 < this.lastSeenCount; ++var1) {
                    boolean var2 = param0.acknowledged().get(var1);
                    LastSeenTrackedEntry var3 = (LastSeenTrackedEntry)this.trackedMessages.get(var1);
                    if (var2) {
                        if (var3 == null) {
                            return Optional.empty();
                        }

                        this.trackedMessages.set(var1, var3.acknowledge());
                        var0.add(var3.signature());
                    } else {
                        if (var3 != null && !var3.pending()) {
                            return Optional.empty();
                        }

                        this.trackedMessages.set(var1, null);
                    }
                }

                return Optional.of(new LastSeenMessages(var0));
            }
        }
    }
}
