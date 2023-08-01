package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import net.minecraft.network.protocol.login.custom.DiscardedQueryPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundCustomQueryPacket(int transactionId, CustomQueryPayload payload) implements Packet<ClientLoginPacketListener> {
    private static final int MAX_PAYLOAD_SIZE = 1048576;

    public ClientboundCustomQueryPacket(FriendlyByteBuf param0) {
        this(param0.readVarInt(), readPayload(param0.readResourceLocation(), param0));
    }

    private static DiscardedQueryPayload readPayload(ResourceLocation param0, FriendlyByteBuf param1) {
        return readUnknownPayload(param0, param1);
    }

    private static DiscardedQueryPayload readUnknownPayload(ResourceLocation param0, FriendlyByteBuf param1) {
        int var0 = param1.readableBytes();
        if (var0 >= 0 && var0 <= 1048576) {
            param1.skipBytes(var0);
            return new DiscardedQueryPayload(param0);
        } else {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.transactionId);
        param0.writeResourceLocation(this.payload.id());
        this.payload.write(param0);
    }

    public void handle(ClientLoginPacketListener param0) {
        param0.handleCustomQuery(this);
    }
}
