package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ClientboundOpenBookPacket implements Packet<ClientGamePacketListener> {
    private final InteractionHand hand;

    public ClientboundOpenBookPacket(InteractionHand param0) {
        this.hand = param0;
    }

    public ClientboundOpenBookPacket(FriendlyByteBuf param0) {
        this.hand = param0.readEnum(InteractionHand.class);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.hand);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleOpenBook(this);
    }

    public InteractionHand getHand() {
        return this.hand;
    }
}
