package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public class LastSeenMessagesValidator {
    private static final int NOT_FOUND = Integer.MIN_VALUE;
    private LastSeenMessages lastSeenMessages = LastSeenMessages.EMPTY;
    private final ObjectList<LastSeenMessages.Entry> pendingEntries = new ObjectArrayList<>();

    public void addPending(LastSeenMessages.Entry param0) {
        this.pendingEntries.add(param0);
    }

    public int pendingMessagesCount() {
        return this.pendingEntries.size();
    }

    private boolean hasDuplicateProfiles(LastSeenMessages param0) {
        Set<UUID> var0 = new HashSet<>(param0.entries().size());

        for(LastSeenMessages.Entry var1 : param0.entries()) {
            if (!var0.add(var1.profileId())) {
                return true;
            }
        }

        return false;
    }

    private int calculateIndices(List<LastSeenMessages.Entry> param0, int[] param1, @Nullable LastSeenMessages.Entry param2) {
        Arrays.fill(param1, Integer.MIN_VALUE);
        List<LastSeenMessages.Entry> var0 = this.lastSeenMessages.entries();
        int var1 = var0.size();

        for(int var2 = var1 - 1; var2 >= 0; --var2) {
            int var3 = param0.indexOf(var0.get(var2));
            if (var3 != -1) {
                param1[var3] = -var2 - 1;
            }
        }

        int var4 = Integer.MIN_VALUE;
        int var5 = this.pendingEntries.size();

        for(int var6 = 0; var6 < var5; ++var6) {
            LastSeenMessages.Entry var7 = this.pendingEntries.get(var6);
            int var8 = param0.indexOf(var7);
            if (var8 != -1) {
                param1[var8] = var6;
            }

            if (var7.equals(param2)) {
                var4 = var6;
            }
        }

        return var4;
    }

    public Set<LastSeenMessagesValidator.ErrorCondition> validateAndUpdate(LastSeenMessages.Update param0) {
        EnumSet<LastSeenMessagesValidator.ErrorCondition> var0 = EnumSet.noneOf(LastSeenMessagesValidator.ErrorCondition.class);
        LastSeenMessages var1 = param0.lastSeen();
        LastSeenMessages.Entry var2 = param0.lastReceived().orElse(null);
        List<LastSeenMessages.Entry> var3 = var1.entries();
        int var4 = this.lastSeenMessages.entries().size();
        int var5 = Integer.MIN_VALUE;
        int var6 = var3.size();
        if (var6 < var4) {
            var0.add(LastSeenMessagesValidator.ErrorCondition.REMOVED_MESSAGES);
        }

        int[] var7 = new int[var6];
        int var8 = this.calculateIndices(var3, var7, var2);

        for(int var9 = var6 - 1; var9 >= 0; --var9) {
            int var10 = var7[var9];
            if (var10 != Integer.MIN_VALUE) {
                if (var10 < var5) {
                    var0.add(LastSeenMessagesValidator.ErrorCondition.OUT_OF_ORDER);
                } else {
                    var5 = var10;
                }
            } else {
                var0.add(LastSeenMessagesValidator.ErrorCondition.UNKNOWN_MESSAGES);
            }
        }

        if (var2 != null) {
            if (var8 != Integer.MIN_VALUE && var8 >= var5) {
                var5 = var8;
            } else {
                var0.add(LastSeenMessagesValidator.ErrorCondition.UNKNOWN_MESSAGES);
            }
        }

        if (var5 >= 0) {
            this.pendingEntries.removeElements(0, var5 + 1);
        }

        if (this.hasDuplicateProfiles(var1)) {
            var0.add(LastSeenMessagesValidator.ErrorCondition.DUPLICATED_PROFILES);
        }

        this.lastSeenMessages = var1;
        return var0;
    }

    public static enum ErrorCondition {
        OUT_OF_ORDER("messages received out of order"),
        DUPLICATED_PROFILES("multiple entries for single profile"),
        UNKNOWN_MESSAGES("unknown message"),
        REMOVED_MESSAGES("previously present messages removed from context");

        private final String message;

        private ErrorCondition(String param0) {
            this.message = param0;
        }

        public String message() {
            return this.message;
        }
    }
}
