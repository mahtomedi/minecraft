package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetHealthPacket implements Packet<ClientGamePacketListener> {
    private float health;
    private int food;
    private float saturation;

    public ClientboundSetHealthPacket() {
    }

    public ClientboundSetHealthPacket(float param0, int param1, float param2) {
        this.health = param0;
        this.food = param1;
        this.saturation = param2;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.health = param0.readFloat();
        this.food = param0.readVarInt();
        this.saturation = param0.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeFloat(this.health);
        param0.writeVarInt(this.food);
        param0.writeFloat(this.saturation);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetHealth(this);
    }

    @OnlyIn(Dist.CLIENT)
    public float getHealth() {
        return this.health;
    }

    @OnlyIn(Dist.CLIENT)
    public int getFood() {
        return this.food;
    }

    @OnlyIn(Dist.CLIENT)
    public float getSaturation() {
        return this.saturation;
    }
}
