package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ServerboundUseItemPacket implements Packet<ServerGamePacketListener> {
    private InteractionHand hand;

    public ServerboundUseItemPacket() {
    }

    public ServerboundUseItemPacket(InteractionHand param0) {
        this.hand = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.hand = param0.readEnum(InteractionHand.class);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeEnum(this.hand);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleUseItem(this);
    }

    public InteractionHand getHand() {
        return this.hand;
    }
}
