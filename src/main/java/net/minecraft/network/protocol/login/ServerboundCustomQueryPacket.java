package net.minecraft.network.protocol.login;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundCustomQueryPacket implements Packet<ServerLoginPacketListener> {
    private static final int MAX_PAYLOAD_SIZE = 1048576;
    private final int transactionId;
    @Nullable
    private final FriendlyByteBuf data;

    public ServerboundCustomQueryPacket(int param0, @Nullable FriendlyByteBuf param1) {
        this.transactionId = param0;
        this.data = param1;
    }

    public ServerboundCustomQueryPacket(FriendlyByteBuf param0) {
        this.transactionId = param0.readVarInt();
        if (param0.readBoolean()) {
            int var0 = param0.readableBytes();
            if (var0 < 0 || var0 > 1048576) {
                throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
            }

            this.data = new FriendlyByteBuf(param0.readBytes(var0));
        } else {
            this.data = null;
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.transactionId);
        if (this.data != null) {
            param0.writeBoolean(true);
            param0.writeBytes(this.data.copy());
        } else {
            param0.writeBoolean(false);
        }

    }

    public void handle(ServerLoginPacketListener param0) {
        param0.handleCustomQueryPacket(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    @Nullable
    public FriendlyByteBuf getData() {
        return this.data;
    }
}
