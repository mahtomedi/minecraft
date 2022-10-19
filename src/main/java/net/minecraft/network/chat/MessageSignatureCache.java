package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.VisibleForTesting;

public class MessageSignatureCache {
    private static final int DEFAULT_CAPACITY = 128;
    private final MessageSignature[] entries;

    public MessageSignatureCache(int param0) {
        this.entries = new MessageSignature[param0];
    }

    public static MessageSignatureCache createDefault() {
        return new MessageSignatureCache(128);
    }

    public MessageSignature.Packer packer() {
        return param0 -> {
            for(int var0 = 0; var0 < this.entries.length; ++var0) {
                if (param0.equals(this.entries[var0])) {
                    return var0;
                }
            }

            return -1;
        };
    }

    public MessageSignature.Unpacker unpacker() {
        return param0 -> this.entries[param0];
    }

    public void push(PlayerChatMessage param0) {
        List<MessageSignature> var0 = param0.signedBody().lastSeen().entries();
        ArrayDeque<MessageSignature> var1 = new ArrayDeque<>(var0.size() + 1);
        var1.addAll(var0);
        MessageSignature var2 = param0.signature();
        if (var2 != null) {
            var1.add(var2);
        }

        this.push(var1);
    }

    @VisibleForTesting
    void push(List<MessageSignature> param0) {
        this.push(new ArrayDeque<>(param0));
    }

    private void push(ArrayDeque<MessageSignature> param0) {
        Set<MessageSignature> var0 = new ObjectOpenHashSet<>(param0);

        for(int var1 = 0; !param0.isEmpty() && var1 < this.entries.length; ++var1) {
            MessageSignature var2 = this.entries[var1];
            this.entries[var1] = param0.removeLast();
            if (var2 != null && !var0.contains(var2)) {
                param0.addFirst(var2);
            }
        }

    }
}
