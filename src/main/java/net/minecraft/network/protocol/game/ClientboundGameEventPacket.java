package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundGameEventPacket implements Packet<ClientGamePacketListener> {
    public static final String[] EVENT_LANGUAGE_ID = new String[]{"block.minecraft.bed.not_valid"};
    private int event;
    private float param;

    public ClientboundGameEventPacket() {
    }

    public ClientboundGameEventPacket(int param0, float param1) {
        this.event = param0;
        this.param = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.event = param0.readUnsignedByte();
        this.param = param0.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByte(this.event);
        param0.writeFloat(this.param);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleGameEvent(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getEvent() {
        return this.event;
    }

    @OnlyIn(Dist.CLIENT)
    public float getParam() {
        return this.param;
    }
}
