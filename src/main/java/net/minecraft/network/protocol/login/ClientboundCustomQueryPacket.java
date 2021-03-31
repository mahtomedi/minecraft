package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;

public class ClientboundCustomQueryPacket implements Packet<ClientLoginPacketListener> {
    private static final int MAX_PAYLOAD_SIZE = 1048576;
    private final int transactionId;
    private final ResourceLocation identifier;
    private final FriendlyByteBuf data;

    public ClientboundCustomQueryPacket(int param0, ResourceLocation param1, FriendlyByteBuf param2) {
        this.transactionId = param0;
        this.identifier = param1;
        this.data = param2;
    }

    public ClientboundCustomQueryPacket(FriendlyByteBuf param0) {
        this.transactionId = param0.readVarInt();
        this.identifier = param0.readResourceLocation();
        int var0 = param0.readableBytes();
        if (var0 >= 0 && var0 <= 1048576) {
            this.data = new FriendlyByteBuf(param0.readBytes(var0));
        } else {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.transactionId);
        param0.writeResourceLocation(this.identifier);
        param0.writeBytes(this.data.copy());
    }

    public void handle(ClientLoginPacketListener param0) {
        param0.handleCustomQuery(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public ResourceLocation getIdentifier() {
        return this.identifier;
    }

    public FriendlyByteBuf getData() {
        return this.data;
    }
}
