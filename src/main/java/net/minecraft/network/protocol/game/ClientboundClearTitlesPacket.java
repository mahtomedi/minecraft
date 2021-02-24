package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundClearTitlesPacket implements Packet<ClientGamePacketListener> {
    private final boolean resetTimes;

    public ClientboundClearTitlesPacket(boolean param0) {
        this.resetTimes = param0;
    }

    public ClientboundClearTitlesPacket(FriendlyByteBuf param0) {
        this.resetTimes = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBoolean(this.resetTimes);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleTitlesClear(this);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean shouldResetTimes() {
        return this.resetTimes;
    }
}
