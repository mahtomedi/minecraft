package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetSubtitleTextPacket implements Packet<ClientGamePacketListener> {
    private final Component text;

    public ClientboundSetSubtitleTextPacket(Component param0) {
        this.text = param0;
    }

    public ClientboundSetSubtitleTextPacket(FriendlyByteBuf param0) {
        this.text = param0.readComponentTrusted();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.text);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.setSubtitleText(this);
    }

    public Component getText() {
        return this.text;
    }
}
