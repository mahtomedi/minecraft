package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetBorderSizePacket implements Packet<ClientGamePacketListener> {
    private final double size;

    public ClientboundSetBorderSizePacket(WorldBorder param0) {
        this.size = param0.getLerpTarget();
    }

    public ClientboundSetBorderSizePacket(FriendlyByteBuf param0) {
        this.size = param0.readDouble();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeDouble(this.size);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetBorderSize(this);
    }

    @OnlyIn(Dist.CLIENT)
    public double getSize() {
        return this.size;
    }
}
