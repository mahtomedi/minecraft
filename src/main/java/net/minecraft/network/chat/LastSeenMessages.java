package net.minecraft.network.chat;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public record LastSeenMessages(List<LastSeenMessages.Entry> entries) {
    public static LastSeenMessages EMPTY = new LastSeenMessages(List.of());
    public static final int LAST_SEEN_MESSAGES_MAX_LENGTH = 5;

    public LastSeenMessages(FriendlyByteBuf param0) {
        this(param0.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 5), LastSeenMessages.Entry::new));
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeCollection(this.entries, (param0x, param1) -> param1.write(param0x));
    }

    public void updateHash(DataOutput param0) throws IOException {
        for(LastSeenMessages.Entry var0 : this.entries) {
            UUID var1 = var0.profileId();
            MessageSignature var2 = var0.lastSignature();
            param0.writeByte(70);
            param0.writeLong(var1.getMostSignificantBits());
            param0.writeLong(var1.getLeastSignificantBits());
            param0.write(var2.bytes());
        }

    }

    public static record Entry(UUID profileId, MessageSignature lastSignature) {
        public Entry(FriendlyByteBuf param0) {
            this(param0.readUUID(), new MessageSignature(param0));
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeUUID(this.profileId);
            this.lastSignature.write(param0);
        }
    }

    public static record Update(LastSeenMessages lastSeen, Optional<LastSeenMessages.Entry> lastReceived) {
        public Update(FriendlyByteBuf param0) {
            this(new LastSeenMessages(param0), param0.readOptional(LastSeenMessages.Entry::new));
        }

        public void write(FriendlyByteBuf param0) {
            this.lastSeen.write(param0);
            param0.writeOptional(this.lastReceived, (param0x, param1) -> param1.write(param0x));
        }
    }
}
