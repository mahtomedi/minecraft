package net.minecraft.network.protocol.login;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundCustomQueryPacket implements Packet<ClientLoginPacketListener> {
    private int transactionId;
    private ResourceLocation identifier;
    private FriendlyByteBuf data;

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.transactionId = param0.readVarInt();
        this.identifier = param0.readResourceLocation();
        int var0 = param0.readableBytes();
        if (var0 >= 0 && var0 <= 1048576) {
            this.data = new FriendlyByteBuf(param0.readBytes(var0));
        } else {
            throw new IOException("Payload may not be larger than 1048576 bytes");
        }
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.transactionId);
        param0.writeResourceLocation(this.identifier);
        param0.writeBytes(this.data.copy());
    }

    public void handle(ClientLoginPacketListener param0) {
        param0.handleCustomQuery(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getTransactionId() {
        return this.transactionId;
    }
}
