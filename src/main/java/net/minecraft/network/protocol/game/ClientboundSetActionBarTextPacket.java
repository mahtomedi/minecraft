package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetActionBarTextPacket implements Packet<ClientGamePacketListener> {
    private final Component text;

    public ClientboundSetActionBarTextPacket(Component param0) {
        this.text = param0;
    }

    public ClientboundSetActionBarTextPacket(FriendlyByteBuf param0) {
        this.text = param0.readComponent();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeComponent(this.text);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.setActionBarText(this);
    }

    @OnlyIn(Dist.CLIENT)
    public Component getText() {
        return this.text;
    }
}
