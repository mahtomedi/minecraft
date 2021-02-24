package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundTabListPacket implements Packet<ClientGamePacketListener> {
    private final Component header;
    private final Component footer;

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

    @OnlyIn(Dist.CLIENT)
    public Component getHeader() {
        return this.header;
    }

    @OnlyIn(Dist.CLIENT)
    public Component getFooter() {
        return this.footer;
    }
}
