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
        this.data = param0.readNullable(param0x -> {
            int var0 = param0x.readableBytes();
            if (var0 >= 0 && var0 <= 1048576) {
                return new FriendlyByteBuf(param0x.readBytes(var0));
            } else {
                throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
            }
        });
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.transactionId);
        param0.writeNullable(this.data, (param0x, param1) -> param0x.writeBytes(param1.slice()));
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
