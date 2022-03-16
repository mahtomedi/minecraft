package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ServerboundUseItemPacket implements Packet<ServerGamePacketListener> {
    private final InteractionHand hand;
    private final int sequence;

    public ServerboundUseItemPacket(InteractionHand param0, int param1) {
        this.hand = param0;
        this.sequence = param1;
    }

    public ServerboundUseItemPacket(FriendlyByteBuf param0) {
        this.hand = param0.readEnum(InteractionHand.class);
        this.sequence = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.hand);
        param0.writeVarInt(this.sequence);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleUseItem(this);
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public int getSequence() {
        return this.sequence;
    }
}
