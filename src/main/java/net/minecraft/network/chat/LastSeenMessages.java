package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureUpdater;

public record LastSeenMessages(List<MessageSignature> entries) {
    public static LastSeenMessages EMPTY = new LastSeenMessages(List.of());
    public static final int LAST_SEEN_MESSAGES_MAX_LENGTH = 20;

    public void updateSignature(SignatureUpdater.Output param0) throws SignatureException {
        param0.update(Ints.toByteArray(this.entries.size()));

        for(MessageSignature var0 : this.entries) {
            param0.update(var0.bytes());
        }

    }

    public LastSeenMessages.Packed pack(MessageSignature.Packer param0) {
        return new LastSeenMessages.Packed(this.entries.stream().map(param1 -> param1.pack(param0)).toList());
    }

    public static record Packed(List<MessageSignature.Packed> entries) {
        public static final LastSeenMessages.Packed EMPTY = new LastSeenMessages.Packed(List.of());

        public Packed(FriendlyByteBuf param0) {
            this(param0.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 20), MessageSignature.Packed::read));
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeCollection(this.entries, MessageSignature.Packed::write);
        }

        public Optional<LastSeenMessages> unpack(MessageSignature.Unpacker param0) {
            List<MessageSignature> var0 = new ArrayList<>(this.entries.size());

            for(MessageSignature.Packed var1 : this.entries) {
                Optional<MessageSignature> var2 = var1.unpack(param0);
                if (var2.isEmpty()) {
                    return Optional.empty();
                }

                var0.add(var2.get());
            }

            return Optional.of(new LastSeenMessages(var0));
        }
    }

    public static record Update(int offset, BitSet acknowledged) {
        public Update(FriendlyByteBuf param0) {
            this(param0.readVarInt(), param0.readFixedBitSet(20));
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeVarInt(this.offset);
            param0.writeFixedBitSet(this.acknowledged, 20);
        }
    }
}
