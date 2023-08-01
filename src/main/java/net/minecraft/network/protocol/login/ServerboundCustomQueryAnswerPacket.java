package net.minecraft.network.protocol.login;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import net.minecraft.network.protocol.login.custom.DiscardedQueryAnswerPayload;

public record ServerboundCustomQueryAnswerPacket(int transactionId, @Nullable CustomQueryAnswerPayload payload) implements Packet<ServerLoginPacketListener> {
    private static final int MAX_PAYLOAD_SIZE = 1048576;

    public static ServerboundCustomQueryAnswerPacket read(FriendlyByteBuf param0) {
        int var0 = param0.readVarInt();
        return new ServerboundCustomQueryAnswerPacket(var0, readPayload(var0, param0));
    }

    private static CustomQueryAnswerPayload readPayload(int param0, FriendlyByteBuf param1) {
        return readUnknownPayload(param1);
    }

    private static CustomQueryAnswerPayload readUnknownPayload(FriendlyByteBuf param0) {
        int var0 = param0.readableBytes();
        if (var0 >= 0 && var0 <= 1048576) {
            param0.skipBytes(var0);
            return DiscardedQueryAnswerPayload.INSTANCE;
        } else {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.transactionId);
        param0.writeNullable(this.payload, (param0x, param1) -> param1.write(param0x));
    }

    public void handle(ServerLoginPacketListener param0) {
        param0.handleCustomQueryPacket(this);
    }
}
