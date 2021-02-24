package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundDisconnectPacket implements Packet<ClientGamePacketListener> {
    private final Component reason;

    public ClientboundDisconnectPacket(Component param0) {
        this.reason = param0;
    }

    public ClientboundDisconnectPacket(FriendlyByteBuf param0) {
        this.reason = param0.readComponent();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
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
