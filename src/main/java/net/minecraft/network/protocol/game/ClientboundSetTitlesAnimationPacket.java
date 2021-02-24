package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetTitlesAnimationPacket implements Packet<ClientGamePacketListener> {
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public ClientboundSetTitlesAnimationPacket(int param0, int param1, int param2) {
        this.fadeIn = param0;
        this.stay = param1;
        this.fadeOut = param2;
    }

    public ClientboundSetTitlesAnimationPacket(FriendlyByteBuf param0) {
        this.fadeIn = param0.readInt();
        this.stay = param0.readInt();
        this.fadeOut = param0.readInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeInt(this.fadeIn);
        param0.writeInt(this.stay);
        param0.writeInt(this.fadeOut);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.setTitlesAnimation(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getFadeIn() {
        return this.fadeIn;
    }

    @OnlyIn(Dist.CLIENT)
    public int getStay() {
        return this.stay;
    }

    @OnlyIn(Dist.CLIENT)
    public int getFadeOut() {
        return this.fadeOut;
    }
}
