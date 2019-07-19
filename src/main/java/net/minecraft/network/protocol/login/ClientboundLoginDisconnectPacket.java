package net.minecraft.network.protocol.login;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundLoginDisconnectPacket implements Packet<ClientLoginPacketListener> {
    private Component reason;

    public ClientboundLoginDisconnectPacket() {
    }

    public ClientboundLoginDisconnectPacket(Component param0) {
        this.reason = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.reason = Component.Serializer.fromJsonLenient(param0.readUtf(262144));
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeComponent(this.reason);
    }

    public void handle(ClientLoginPacketListener param0) {
        param0.handleDisconnect(this);
    }

    @OnlyIn(Dist.CLIENT)
    public Component getReason() {
        return this.reason;
    }
}
