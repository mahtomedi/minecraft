package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundDisconnectPacket implements Packet<ClientGamePacketListener> {
    private Component reason;

    public ClientboundDisconnectPacket() {
    }

    public ClientboundDisconnectPacket(Component param0) {
        this.reason = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.reason = param0.readComponent();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeComponent(this.reason);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleDisconnect(this);
    }

    @OnlyIn(Dist.CLIENT)
    public Component getReason() {
        return this.reason;
    }
}
