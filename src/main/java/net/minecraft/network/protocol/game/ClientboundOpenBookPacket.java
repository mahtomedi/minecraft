package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundOpenBookPacket implements Packet<ClientGamePacketListener> {
    private InteractionHand hand;

    public ClientboundOpenBookPacket() {
    }

    public ClientboundOpenBookPacket(InteractionHand param0) {
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

    public void handle(ClientGamePacketListener param0) {
        param0.handleOpenBook(this);
    }

    @OnlyIn(Dist.CLIENT)
    public InteractionHand getHand() {
        return this.hand;
    }
}
