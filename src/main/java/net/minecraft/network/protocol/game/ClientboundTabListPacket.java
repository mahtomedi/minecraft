package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public class ClientboundTabListPacket implements Packet<ClientGamePacketListener> {
    private final Component header;
    private final Component footer;

    public ClientboundTabListPacket(Component param0, Component param1) {
        this.header = param0;
        this.footer = param1;
    }

    public ClientboundTabListPacket(FriendlyByteBuf param0) {
        this.header = param0.readComponent();
        this.footer = param0.readComponent();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.header);
        param0.writeComponent(this.footer);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleTabListCustomisation(this);
    }

    public Component getHeader() {
        return this.header;
    }

    public Component getFooter() {
        return this.footer;
    }
}
