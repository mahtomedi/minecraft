package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ServerboundSwingPacket implements Packet<ServerGamePacketListener> {
    private final InteractionHand hand;

    public ServerboundSwingPacket(InteractionHand param0) {
        this.hand = param0;
    }

    public ServerboundSwingPacket(FriendlyByteBuf param0) {
        this.hand = param0.readEnum(InteractionHand.class);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.hand);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleAnimate(this);
    }

    public InteractionHand getHand() {
        return this.hand;
    }
}
